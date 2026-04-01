package com.harbinger.wintercore.client;

import com.harbinger.wintercore.WinterCoreAddon;
import com.harbinger.wintercore.client.ponder.WinterCorePonderPlugin;
import com.harbinger.wintercore.init.WinterCoreBlocks;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

@Mod.EventBusSubscriber(modid = WinterCoreAddon.MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ClientSetup {

    @SubscribeEvent
    public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerBlockEntityRenderer(WinterCoreBlocks.WINTER_CORE_BE.get(), WinterCoreRenderer::new);
    }

    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        // 仅当 Create 已加载时注册思索（Ponder）场景，避免硬依赖
        event.enqueueWork(() -> {
            if (net.minecraftforge.fml.ModList.get().isLoaded("create")) {
                WinterCorePonderPlugin.register();
            }
        });
    }
}
