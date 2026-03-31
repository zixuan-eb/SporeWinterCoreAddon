package com.harbinger.wintercore.block;

import com.harbinger.wintercore.data.WinterCoreData;
import com.harbinger.wintercore.init.WinterCoreBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.EntityTypeTags;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.registries.ForgeRegistries;

import com.harbinger.wintercore.config.WinterCoreConfig;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class WinterCoreBlockEntity extends BlockEntity {

    // Global registry of all active Winter Cores to quickly lookup blockspawn rules anywhere in the world.
    public static final Map<ResourceKey<Level>, Set<BlockPos>> ACTIVE_CORES = new ConcurrentHashMap<>();
    
    // Config dynamic properties
    private static Map<ResourceLocation, ResourceLocation> dynamicReplaceMap = null;
    private static long lastConfigRead = 0;
    private static final int[][] cornerCoords = {{-2, 0}, {0, -2}, {2, 0}, {0, 2}};

    public boolean isFormed = false;
    private int tickCounter = 0;
    private int scanY = -16;
    
    public WinterCoreBlockEntity(BlockPos pos, BlockState state) {
        super(WinterCoreBlocks.WINTER_CORE_BE.get(), pos, state);
    }

    @Override
    public void onLoad() {
        super.onLoad();
    }

    @Override
    protected void saveAdditional(net.minecraft.nbt.CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putBoolean("IsFormed", this.isFormed);
    }

    @Override
    public void load(net.minecraft.nbt.CompoundTag tag) {
        super.load(tag);
        this.isFormed = tag.getBoolean("IsFormed");
    }

    @Override
    public net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket getUpdatePacket() {
        return net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public net.minecraft.nbt.CompoundTag getUpdateTag() {
        return this.saveWithFullMetadata();
    }

    @Override
    public void setRemoved() {
        super.setRemoved();
        if (level != null && !level.isClientSide()) {
            unregisterCore();
        }
    }

    @Override
    public AABB getRenderBoundingBox() {
        return new AABB(worldPosition).inflate(100.0, 300.0, 100.0);
    }

    public static void clientTick(Level level, BlockPos pos, BlockState state, WinterCoreBlockEntity blockEntity) {
        if (!blockEntity.isFormed) return;
        
        // Spawn massive snowstorms if config allows
        if (WinterCoreConfig.COMMON.renderSnow.get()) {
            net.minecraft.client.player.LocalPlayer player = net.minecraft.client.Minecraft.getInstance().player;
            int radius = WinterCoreConfig.COMMON.effectRadius.get();
            if (player != null && player.distanceToSqr(pos.getX(), pos.getY(), pos.getZ()) <= radius * radius) {
                for (int i = 0; i < 60; i++) {
                    double px = player.getX() + level.random.nextGaussian() * 20;
                    double pz = player.getZ() + level.random.nextGaussian() * 20;
                    double py = player.getY() + 15 + level.random.nextFloat() * 15;
                    level.addParticle(net.minecraft.core.particles.ParticleTypes.SNOWFLAKE, px, py, pz, 0, -0.05, 0);
                }
            }
        }
    }

    private void registerCore() {
        if (!ACTIVE_CORES.containsKey(level.dimension()) || !ACTIVE_CORES.get(level.dimension()).contains(getBlockPos())) {
            ACTIVE_CORES.computeIfAbsent(level.dimension(), k -> new HashSet<>()).add(getBlockPos());
        }
    }

    private void unregisterCore() {
        if (ACTIVE_CORES.containsKey(level.dimension())) {
            ACTIVE_CORES.get(level.dimension()).remove(getBlockPos());
        }
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, WinterCoreBlockEntity blockEntity) {
        if (blockEntity.tickCounter++ % 20 != 0) return;
        
        if (blockEntity.checkMultiblock()) {
            if (!blockEntity.isFormed) return;
            blockEntity.processBlockConversion();
            blockEntity.processEntityDamage();
        }
    }

    private boolean checkMultiblock() {
        if (level == null) return false;
        
        int[][] baseCoords = {
            {1, 0}, {-1, 0}, {0, 1}, {0, -1},
            {2, 0}, {-2, 0}, {0, 2}, {0, -2}
        };
        boolean formed = level.getBlockState(worldPosition.below()).getBlock() == WinterCoreBlocks.WINTER_CORE_PEDESTAL.get() &&
                         level.getBlockState(worldPosition.below(2)).getBlock() == WinterCoreBlocks.WINTER_CORE_BASE.get();

        for (int[] c : baseCoords) {
            if (level.getBlockState(worldPosition.offset(c[0], -2, c[1])).getBlock() != WinterCoreBlocks.WINTER_CORE_BASE.get()) {
                formed = false;
                break;
            }
        }

        if (formed) {
            for (int i = 0; i < 4; i++) {
                int[] p = cornerCoords[i];
                BlockPos p1 = worldPosition.offset(p[0], -1, p[1]);
                BlockPos p2 = worldPosition.offset(p[0], 0, p[1]);

                BlockState s1 = level.getBlockState(p1);
                BlockState s2 = level.getBlockState(p2);

                if (s1.getBlock() != WinterCoreBlocks.WINTER_CORE_PILLAR.get() || 
                    s2.getBlock() != WinterCoreBlocks.WINTER_CORE_PILLAR.get()) {
                    formed = false;
                    break;
                }
            }
        }
        
        if (this.isFormed != formed) {
            this.isFormed = formed;
            if (formed) {
               registerCore();
               level.playSound(null, worldPosition, net.minecraft.sounds.SoundEvents.BEACON_ACTIVATE, net.minecraft.sounds.SoundSource.BLOCKS, 1.0f, 0.8f);
               
               for (int i = 0; i < 4; i++) {
                   int cornerIndex = i;
                   int[] p = cornerCoords[i];
                   BlockPos p1 = worldPosition.offset(p[0], -1, p[1]);
                   BlockPos p2 = worldPosition.offset(p[0], 0, p[1]);
                   
                   level.setBlock(p1, level.getBlockState(p1).setValue(WinterCorePillarBlock.FORMED, true).setValue(WinterCorePillarBlock.CORNER, cornerIndex).setValue(WinterCorePillarBlock.IS_TOP, false), 3);
                   level.setBlock(p2, level.getBlockState(p2).setValue(WinterCorePillarBlock.FORMED, true).setValue(WinterCorePillarBlock.CORNER, cornerIndex).setValue(WinterCorePillarBlock.IS_TOP, true), 3);
               }
               level.setBlock(worldPosition, getBlockState().setValue(WinterCoreBlock.FORMED, true), 3);

               // Grant Advancement "Pure Land" to the nearest player
               net.minecraft.world.entity.player.Player nearest = level.getNearestPlayer(worldPosition.getX() + 0.5, worldPosition.getY() + 0.5, worldPosition.getZ() + 0.5, 20.0, false);
               if (nearest instanceof net.minecraft.server.level.ServerPlayer serverPlayer && level.getServer() != null) {
                   net.minecraft.advancements.Advancement advancement = level.getServer().getAdvancements().getAdvancement(new ResourceLocation("wintercore:pure_land"));
                   if (advancement != null) {
                       net.minecraft.advancements.AdvancementProgress progress = serverPlayer.getAdvancements().getOrStartProgress(advancement);
                       for (String criterion : progress.getRemainingCriteria()) {
                           serverPlayer.getAdvancements().award(advancement, criterion);
                       }
                   }
               }

            } else {
               unregisterCore();
               level.playSound(null, worldPosition, net.minecraft.sounds.SoundEvents.BEACON_DEACTIVATE, net.minecraft.sounds.SoundSource.BLOCKS, 1.0f, 0.8f);
               
               this.revertMultiblock();
               level.setBlock(worldPosition, getBlockState().setValue(WinterCoreBlock.FORMED, false), 3);
            }
            this.setChanged();
        }
        return formed;
    }

    public void revertMultiblock() {
        if (level == null) return;
        for (int[] p : cornerCoords) {
            BlockPos p1 = worldPosition.offset(p[0], -1, p[1]);
            BlockPos p2 = worldPosition.offset(p[0], 0, p[1]);
            
            if (level.getBlockState(p1).getBlock() == WinterCoreBlocks.WINTER_CORE_PILLAR.get()) {
                level.setBlock(p1, level.getBlockState(p1).setValue(WinterCorePillarBlock.FORMED, false), 3);
            }
            if (level.getBlockState(p2).getBlock() == WinterCoreBlocks.WINTER_CORE_PILLAR.get()) {
                level.setBlock(p2, level.getBlockState(p2).setValue(WinterCorePillarBlock.FORMED, false), 3);
            }
        }
    }

    private void reloadReplacementMap() {
        dynamicReplaceMap = new HashMap<>();
        List<? extends String> cfg = WinterCoreConfig.COMMON.blockConversions.get();
        for (String pair : cfg) {
            String[] split = pair.split("\\|");
            if (split.length == 2) {
                dynamicReplaceMap.put(new ResourceLocation(split[0]), new ResourceLocation(split[1]));
            }
        }
        lastConfigRead = System.currentTimeMillis();
    }

    private void processBlockConversion() {
        if (dynamicReplaceMap == null || System.currentTimeMillis() - lastConfigRead > 5000) {
            reloadReplacementMap();
        }

        int radius = WinterCoreConfig.COMMON.effectRadius.get();
        int rSq = radius * radius;
        
        for (int dx = -radius; dx <= radius; dx++) {
            for (int dz = -radius; dz <= radius; dz++) {
                if (dx * dx + dz * dz <= rSq) {
                    BlockPos targetPos = worldPosition.offset(dx, scanY, dz);
                    if (level.isLoaded(targetPos)) {
                        BlockState targetState = level.getBlockState(targetPos);
                        ResourceLocation blockId = ForgeRegistries.BLOCKS.getKey(targetState.getBlock());
                        if (blockId != null && dynamicReplaceMap.containsKey(blockId)) {
                            Block targetBlock = ForgeRegistries.BLOCKS.getValue(dynamicReplaceMap.get(blockId));
                            if (targetBlock != net.minecraft.world.level.block.Blocks.AIR) {
                                level.setBlockAndUpdate(targetPos, targetBlock.defaultBlockState());
                            }
                        }
                    }
                }
            }
        }
        
        scanY++;
        if (scanY > 16) {
            scanY = -16;
        }
    }

    private void processEntityDamage() {
        if (level == null) return;
        
        int radius = WinterCoreConfig.COMMON.effectRadius.get();
        double multiplier = WinterCoreConfig.COMMON.damageMultiplier.get();
        AABB aabb = new AABB(worldPosition).inflate(radius);
        List<LivingEntity> entities = level.getEntitiesOfClass(LivingEntity.class, aabb);
        
        for (LivingEntity entity : entities) {
            double dx = entity.getX() - worldPosition.getX();
            double dz = entity.getZ() - worldPosition.getZ();
            if ((dx * dx + dz * dz) <= (radius * radius)) {
                
                // Add Weakness and Slowness to Hostile Monsters
                if (entity instanceof net.minecraft.world.entity.monster.Monster) {
                    entity.addEffect(new net.minecraft.world.effect.MobEffectInstance(net.minecraft.world.effect.MobEffects.MOVEMENT_SLOWDOWN, 100, 1, false, true));
                    entity.addEffect(new net.minecraft.world.effect.MobEffectInstance(net.minecraft.world.effect.MobEffects.WEAKNESS, 100, 1, false, true));
                }
                
                String entityId = ForgeRegistries.ENTITY_TYPES.getKey(entity.getType()).toString();
                if (entityId.contains("spore:") || entityId.contains("flesh_that_hates")) {
                    float baseDamage = 3.0F * (float)multiplier;
                    entity.hurt(level.damageSources().magic(), baseDamage);
                    
                    if (entity.getType().is(EntityTypeTags.FREEZE_HURTS_EXTRA_TYPES)) {
                        entity.setTicksFrozen(entity.getTicksFrozen() + 600);
                        entity.hurt(level.damageSources().freeze(), 10.0f * (float)multiplier); 
                    }
                    
                    // Visual frost on entity
                    if (level instanceof net.minecraft.server.level.ServerLevel serverLevel) {
                        serverLevel.sendParticles(net.minecraft.core.particles.ParticleTypes.SNOWFLAKE,
                                entity.getX(), entity.getY() + 1.0, entity.getZ(),
                                5, 0.2, 0.5, 0.2, 0.0);
                    }
                    
                    // Specific hardcoded entity deletion if they are small/particles like Scent or Tendril
                    String name = entity.getClass().getSimpleName();
                    if (name.equals("ScentEntity") || name.equals("InfectionTendril")) {
                        entity.discard();
                    }
                }
            }
        }
    }
}
