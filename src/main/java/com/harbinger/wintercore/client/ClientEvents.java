package com.harbinger.wintercore.client;

import com.harbinger.wintercore.WinterCoreAddon;
import com.harbinger.wintercore.block.WinterCoreBlockEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = WinterCoreAddon.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public class ClientEvents {

    @SubscribeEvent
    public static void onRenderLevel(RenderLevelStageEvent event) {
        if (event.getStage() == RenderLevelStageEvent.Stage.AFTER_TRANSLUCENT_BLOCKS) {
            PoseStack poseStack = event.getPoseStack();
            Minecraft mc = Minecraft.getInstance();
            if (mc.level == null || mc.player == null) return;

            Vec3 camPos = event.getCamera().getPosition();
            MultiBufferSource.BufferSource bufferSource = mc.renderBuffers().bufferSource();

            for (WinterCoreBlockEntity core : WinterCoreBlockEntity.CLIENT_CORES) {
                if (core.isRemoved() || !core.isPowered || !core.isFormed) continue;
                
                // Only render if relatively close, or frustum-cull manually. For now just distance check:
                if (core.getBlockPos().distToCenterSqr(camPos.x, camPos.y, camPos.z) > 65536) continue;

                poseStack.pushPose();
                poseStack.translate(core.getBlockPos().getX() - camPos.x,
                                    core.getBlockPos().getY() - camPos.y,
                                    core.getBlockPos().getZ() - camPos.z);

                // Re-use our renderer logic, we now inject it here directly so it is forced to draw AFTER ice blocks
                WinterCoreRenderer.renderBeams(core, event.getPartialTick(), poseStack, bufferSource, 
                                               net.minecraft.client.renderer.LightTexture.FULL_BRIGHT, 
                                               net.minecraft.client.renderer.texture.OverlayTexture.NO_OVERLAY);
                
                poseStack.popPose();
            }

            // Immediately flush to screen, passing translucent terrain's depth buffer!
            bufferSource.endBatch(WinterCoreRenderer.ADDITIVE_MAGIC_CIRCLE);
            bufferSource.endBatch(WinterCoreRenderer.ADDITIVE_MAGIC_BEAM);
            bufferSource.endBatch(WinterCoreRenderer.ADDITIVE_PULSE_WAVE);
            bufferSource.endBatch(WinterCoreRenderer.ADDITIVE_WINTER_STAR);
        }
    }
}
