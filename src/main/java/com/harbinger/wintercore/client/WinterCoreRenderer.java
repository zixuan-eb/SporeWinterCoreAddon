package com.harbinger.wintercore.client;

import com.harbinger.wintercore.block.WinterCoreBlock;
import com.harbinger.wintercore.block.WinterCoreBlockEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.state.BlockState;

public class WinterCoreRenderer implements BlockEntityRenderer<WinterCoreBlockEntity> {

    private static final ResourceLocation MAGIC_CIRCLE = new ResourceLocation("wintercore", "textures/effect/magic_circle.png");
    private static final ResourceLocation MAGIC_BEAM = new ResourceLocation("wintercore", "textures/effect/magic_beam.png");
    private static final ResourceLocation PULSE_WAVE = new ResourceLocation("wintercore", "textures/effect/pulse_wave.png");

    public WinterCoreRenderer(BlockEntityRendererProvider.Context context) {}

    @Override
    public void render(WinterCoreBlockEntity blockEntity, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
        if (!blockEntity.isFormed || blockEntity.getLevel() == null) return;

        BlockState state = blockEntity.getBlockState();
        long time = blockEntity.getLevel().getGameTime();
        
        BlockState crystalState = state.setValue(WinterCoreBlock.FORMED, false);
        
        float floatOffset = 0.75f + (float)Math.sin((time + partialTick) * 0.05f) * 0.15f;
        int maxLight = 15728880;

        // 1. Crystal Core Mesh
        poseStack.pushPose();
        poseStack.translate(0.5D, floatOffset, 0.5D); 
        poseStack.mulPose(Axis.YP.rotationDegrees((time + partialTick) * 1.5f)); 
        poseStack.mulPose(Axis.XP.rotationDegrees(45f));
        poseStack.mulPose(Axis.ZP.rotationDegrees(45f));
        poseStack.translate(-0.5D, -0.5D, -0.5D);
        Minecraft.getInstance().getBlockRenderer().renderSingleBlock(crystalState, poseStack, bufferSource, maxLight, packedOverlay, net.minecraftforge.client.model.data.ModelData.EMPTY, null);
        poseStack.popPose();

        float angleY = (time + partialTick) * 3.0f; 

        // 2. Translucent Crystal Shell (Outer Holoram)
        poseStack.pushPose();
        poseStack.translate(0.5D, floatOffset, 0.5D); 
        poseStack.mulPose(Axis.YP.rotationDegrees(angleY));
        poseStack.mulPose(Axis.XP.rotationDegrees(45f));
        poseStack.mulPose(Axis.ZP.rotationDegrees(45f));
        
        float scale1 = 1.25f + 0.15f * (float)Math.sin((time + partialTick) * 0.1f);
        poseStack.scale(scale1, scale1, scale1);
        poseStack.translate(-0.5D, -0.5D, -0.5D);
        Minecraft.getInstance().getBlockRenderer().renderSingleBlock(crystalState, poseStack, bufferSource, maxLight, packedOverlay, net.minecraftforge.client.model.data.ModelData.EMPTY, RenderType.translucent());
        poseStack.popPose();

        // 3. Render Spinning Magic Circles (Runic Arrays)
        VertexConsumer circleBuilder = bufferSource.getBuffer(RenderType.entityTranslucent(MAGIC_CIRCLE));
        
        // Base wide circle (-0.95f is precisely right above the Winter Core Base surface at Y=-1.0)
        poseStack.pushPose();
        poseStack.translate(0.5D, -0.95f, 0.5D);
        poseStack.mulPose(Axis.YP.rotationDegrees(-angleY * 0.5f));
        poseStack.mulPose(Axis.XP.rotationDegrees(90f));
        float baseScale = 9.0f + 0.3f * (float)Math.sin((time + partialTick) * 0.08f);
        drawTexturedQuad(circleBuilder, poseStack, baseScale, baseScale, maxLight);
        poseStack.popPose();

        // Sky-high colossal domain magic circle
        poseStack.pushPose();
        poseStack.translate(0.5D, 120.0f, 0.5D);
        poseStack.mulPose(Axis.YP.rotationDegrees(angleY * 0.15f)); // Slow majestic spin
        poseStack.mulPose(Axis.XP.rotationDegrees(90f));
        float skyScale = 160.0f + 2.0f * (float)Math.sin((time + partialTick) * 0.05f);
        drawTexturedQuad(circleBuilder, poseStack, skyScale, skyScale, maxLight);
        poseStack.popPose();

        // 4. Volumetric intersecting Custom Laser Beams
        VertexConsumer beamBuilder = bufferSource.getBuffer(RenderType.entityTranslucentCull(MAGIC_BEAM));
        float beamHeight = 350f;

        // Big outer rotating laser core (12-way planar star = volumetric cylinder)
        poseStack.pushPose();
        poseStack.translate(0.5D, -1.8f, 0.5D);
        poseStack.mulPose(Axis.YP.rotationDegrees(angleY * 2.0f));
        float outerWidth = 1.6f + 0.1f * (float)Math.sin((time + partialTick) * 0.2f);
        drawVolumetricBeam(beamBuilder, poseStack, outerWidth, beamHeight, maxLight);
        poseStack.popPose();

        // High intensity inner shaft spinning opposite
        poseStack.pushPose();
        poseStack.translate(0.5D, -1.8f, 0.5D);
        poseStack.mulPose(Axis.YP.rotationDegrees(-angleY * 3.0f));
        float innerWidth = 0.6f;
        drawVolumetricBeam(beamBuilder, poseStack, innerWidth, beamHeight, maxLight);
        poseStack.popPose();

        // 5. Pulse Wave Rings —— 三层同心光环每秒从核心向外扩散淡出
        // 纯客户端时间驱动（gameTime % 20），无需服务端同步
        float pulseProgress = ((time % 20L) + partialTick) / 20.0f; // 0→1，每秒一次
        VertexConsumer pulseBuilder = bufferSource.getBuffer(RenderType.entityTranslucent(PULSE_WAVE));

        // Layer A — 内圈急速爆发：0→1 仅用 0.5s，小而亮，带额外旋转冲量
        float aP = Math.min(1f, pulseProgress * 2f);
        poseStack.pushPose();
        poseStack.translate(0.5D, -0.93f + aP * 0.15, 0.5D);
        poseStack.mulPose(Axis.YP.rotationDegrees(-angleY * 0.5f + aP * 45f)); // 旋转冲量
        poseStack.mulPose(Axis.XP.rotationDegrees(90f));
        drawTexturedQuadWithAlpha(pulseBuilder, poseStack, aP * 9.5f, aP * 9.5f, maxLight, (int)(220 * (1f - aP)));
        poseStack.popPose();

        // Layer B — 中环匀速扩散：完整 1s，反向缓转
        poseStack.pushPose();
        poseStack.translate(0.5D, -0.95f + pulseProgress * 0.25, 0.5D);
        poseStack.mulPose(Axis.YP.rotationDegrees(angleY * 0.25f));
        poseStack.mulPose(Axis.XP.rotationDegrees(90f));
        drawTexturedQuadWithAlpha(pulseBuilder, poseStack, pulseProgress * 15f, pulseProgress * 15f, maxLight, (int)(160 * (1f - pulseProgress)));
        poseStack.popPose();

        // Layer C — 外环延迟追踪：0.25s 后才开始，幅度最大，轻微反转
        float cP = Math.max(0f, (pulseProgress - 0.25f) / 0.75f);
        poseStack.pushPose();
        poseStack.translate(0.5D, -0.95f + cP * 0.35, 0.5D);
        poseStack.mulPose(Axis.YP.rotationDegrees(-angleY * 0.1f));
        poseStack.mulPose(Axis.XP.rotationDegrees(90f));
        drawTexturedQuadWithAlpha(pulseBuilder, poseStack, cP * 22f, cP * 22f, maxLight, (int)(110 * (1f - cP)));
        poseStack.popPose();
    }

    private void drawVolumetricBeam(VertexConsumer builder, PoseStack matrixStack, float width, float height, int light) {
        int planes = 6;
        for (int i = 0; i < planes; i++) {
            matrixStack.pushPose();
            matrixStack.mulPose(Axis.YP.rotationDegrees(i * (180f / planes)));
            drawVerticalBeamPlane(builder, matrixStack, width, height, light);
            matrixStack.popPose();
        }
    }

    /**
     * 绘制支持动态透明度的纹理四边形（用于脉冲光环淡出效果）
     */
    private void drawTexturedQuadWithAlpha(VertexConsumer builder, PoseStack matrixStack, float width, float height, int light, int alpha) {
        if (alpha <= 0) return;
        var pose = matrixStack.last().pose();
        var normal = matrixStack.last().normal();
        float hw = width / 2.0f;
        float hh = height / 2.0f;
        builder.vertex(pose, -hw, hh, 0).color(255, 255, 255, alpha).uv(0, 1).overlayCoords(net.minecraft.client.renderer.texture.OverlayTexture.NO_OVERLAY).uv2(light).normal(normal, 0, 0, 1).endVertex();
        builder.vertex(pose, hw, hh, 0).color(255, 255, 255, alpha).uv(1, 1).overlayCoords(net.minecraft.client.renderer.texture.OverlayTexture.NO_OVERLAY).uv2(light).normal(normal, 0, 0, 1).endVertex();
        builder.vertex(pose, hw, -hh, 0).color(255, 255, 255, alpha).uv(1, 0).overlayCoords(net.minecraft.client.renderer.texture.OverlayTexture.NO_OVERLAY).uv2(light).normal(normal, 0, 0, 1).endVertex();
        builder.vertex(pose, -hw, -hh, 0).color(255, 255, 255, alpha).uv(0, 0).overlayCoords(net.minecraft.client.renderer.texture.OverlayTexture.NO_OVERLAY).uv2(light).normal(normal, 0, 0, 1).endVertex();
    }

    private void drawTexturedQuad(VertexConsumer builder, PoseStack matrixStack, float width, float height, int light) {
        var pose = matrixStack.last().pose();
        var normal = matrixStack.last().normal();
        float hw = width / 2.0f;
        float hh = height / 2.0f;
        builder.vertex(pose, -hw, hh, 0).color(255, 255, 255, 255).uv(0, 1).overlayCoords(net.minecraft.client.renderer.texture.OverlayTexture.NO_OVERLAY).uv2(light).normal(normal, 0, 0, 1).endVertex();
        builder.vertex(pose, hw, hh, 0).color(255, 255, 255, 255).uv(1, 1).overlayCoords(net.minecraft.client.renderer.texture.OverlayTexture.NO_OVERLAY).uv2(light).normal(normal, 0, 0, 1).endVertex();
        builder.vertex(pose, hw, -hh, 0).color(255, 255, 255, 255).uv(1, 0).overlayCoords(net.minecraft.client.renderer.texture.OverlayTexture.NO_OVERLAY).uv2(light).normal(normal, 0, 0, 1).endVertex();
        builder.vertex(pose, -hw, -hh, 0).color(255, 255, 255, 255).uv(0, 0).overlayCoords(net.minecraft.client.renderer.texture.OverlayTexture.NO_OVERLAY).uv2(light).normal(normal, 0, 0, 1).endVertex();
    }

    private void drawVerticalBeamPlane(VertexConsumer builder, PoseStack matrixStack, float width, float height, int light) {
        var pose = matrixStack.last().pose();
        var normal = matrixStack.last().normal();
        float hw = width / 2.0f;
        builder.vertex(pose, -hw, 0, 0).color(255, 255, 255, 255).uv(0, 1).overlayCoords(net.minecraft.client.renderer.texture.OverlayTexture.NO_OVERLAY).uv2(light).normal(normal, 0, 1, 0).endVertex();
        builder.vertex(pose, hw, 0, 0).color(255, 255, 255, 255).uv(1, 1).overlayCoords(net.minecraft.client.renderer.texture.OverlayTexture.NO_OVERLAY).uv2(light).normal(normal, 0, 1, 0).endVertex();
        builder.vertex(pose, hw, height, 0).color(255, 255, 255, 255).uv(1, 0).overlayCoords(net.minecraft.client.renderer.texture.OverlayTexture.NO_OVERLAY).uv2(light).normal(normal, 0, 1, 0).endVertex();
        builder.vertex(pose, -hw, height, 0).color(255, 255, 255, 255).uv(0, 0).overlayCoords(net.minecraft.client.renderer.texture.OverlayTexture.NO_OVERLAY).uv2(light).normal(normal, 0, 1, 0).endVertex();
    }
    
    @Override
    public boolean shouldRenderOffScreen(WinterCoreBlockEntity blockEntity) {
        return true; 
    }
}
