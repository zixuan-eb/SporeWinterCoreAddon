package com.harbinger.wintercore.client;

import com.harbinger.wintercore.WinterCoreAddon;
import com.harbinger.wintercore.init.WinterCoreBlocks;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = WinterCoreAddon.MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ClientSetup {

    @SubscribeEvent
    public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerBlockEntityRenderer(WinterCoreBlocks.WINTER_CORE_BE.get(), WinterCoreRenderer::new);
    }
}
