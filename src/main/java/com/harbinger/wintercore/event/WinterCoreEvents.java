package com.harbinger.wintercore.event;

import com.harbinger.wintercore.WinterCoreAddon;
import com.harbinger.wintercore.block.WinterCoreBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.entity.living.MobSpawnEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;

@Mod.EventBusSubscriber(modid = WinterCoreAddon.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class WinterCoreEvents {

    @SubscribeEvent
    public static void onCheckSpawn(MobSpawnEvent.PositionCheck event) {
        if (event.getEntity() != null) {
            ResourceLocation entityLoc = ForgeRegistries.ENTITY_TYPES.getKey(event.getEntity().getType());
            if (entityLoc != null && entityLoc.getNamespace().equals("spore")) {
                if (isNearWinterCore((Level) event.getLevel(), event.getEntity().blockPosition())) {
                    // Deny the spawn request completely
                    event.setResult(Event.Result.DENY);
                }
            }
        }
    }

    @SubscribeEvent
    public static void onFinalizeSpawn(MobSpawnEvent.FinalizeSpawn event) {
        if (event.getEntity() != null) {
            ResourceLocation entityLoc = ForgeRegistries.ENTITY_TYPES.getKey(event.getEntity().getType());
            // Double layer of security against spawners or scripts bypassing PositionCheck
            if (entityLoc != null && entityLoc.getNamespace().equals("spore")) {
                if (isNearWinterCore((Level) event.getLevel(), event.getEntity().blockPosition())) {
                    event.setSpawnCancelled(true);
                }
            }
        }
    }

    private static boolean isNearWinterCore(Level level, BlockPos pos) {
        if (level == null || level.isClientSide()) return false;

        ResourceKey<Level> dim = level.dimension();
        if (WinterCoreBlockEntity.ACTIVE_CORES.containsKey(dim)) {
            for (BlockPos corePos : WinterCoreBlockEntity.ACTIVE_CORES.get(dim)) {
                // Ignore Y-axis by checking distSqr on X and Z only
                double dx = corePos.getX() - pos.getX();
                double dz = corePos.getZ() - pos.getZ();
                if ((dx * dx + dz * dz) <= (WinterCoreBlockEntity.EFFECT_RADIUS * WinterCoreBlockEntity.EFFECT_RADIUS)) {
                    return true;
                }
            }
        }
        return false;
    }
}
