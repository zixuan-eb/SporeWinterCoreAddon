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

import net.minecraft.client.renderer.RenderStateShard;

public class WinterCoreRenderer implements BlockEntityRenderer<WinterCoreBlockEntity> {

    private static final ResourceLocation MAGIC_CIRCLE = new ResourceLocation("wintercore", "textures/effect/magic_circle.png");
    private static final ResourceLocation MAGIC_BEAM = new ResourceLocation("wintercore", "textures/effect/magic_beam.png");
    private static final ResourceLocation PULSE_WAVE = new ResourceLocation("wintercore", "textures/effect/pulse_wave.png");
    public static class AdditiveRenderType extends RenderType {
        public AdditiveRenderType(String p_173178_, com.mojang.blaze3d.vertex.VertexFormat p_173179_, com.mojang.blaze3d.vertex.VertexFormat.Mode p_173180_, int p_173181_, boolean p_173182_, boolean p_173183_, Runnable p_173184_, Runnable p_173185_) {
            super(p_173178_, p_173179_, p_173180_, p_173181_, p_173182_, p_173183_, p_173184_, p_173185_);
        }

        public static RenderType getAdditive(ResourceLocation location) {
            RenderType.CompositeState state = RenderType.CompositeState.builder()
                    .setShaderState(RENDERTYPE_ENTITY_TRANSLUCENT_EMISSIVE_SHADER)
                    .setTextureState(new RenderStateShard.TextureStateShard(location, false, false))
                    .setTransparencyState(LIGHTNING_TRANSPARENCY)
                    .setCullState(NO_CULL)
                    .setWriteMaskState(COLOR_WRITE)
                    .createCompositeState(false);
            return RenderType.create("wintercore_additive",
                    com.mojang.blaze3d.vertex.DefaultVertexFormat.NEW_ENTITY,
                    com.mojang.blaze3d.vertex.VertexFormat.Mode.QUADS,
                    256, false, true, state);
        }
    }

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
        // 使用 AdditiveRenderType 避免写入深度缓冲（Depth Write），从而修复透过阵法看不见冰面/水面的透视 Bug！
        VertexConsumer circleBuilder = bufferSource.getBuffer(AdditiveRenderType.getAdditive(MAGIC_CIRCLE));
        
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

        // 5. Pulse Wave Rings —— 多层高空能量冲击波 (周期大幅加长)
        float cycleDuration = 60.0f; // 3秒长周期
        float pulseProgress = ((time % (long)cycleDuration) + partialTick) / cycleDuration;
        VertexConsumer pulseBuilder = bufferSource.getBuffer(AdditiveRenderType.getAdditive(PULSE_WAVE));

        // Layer A — 核心底部基座爆发：从核心扩散至全领域
        float aP = Math.min(1f, pulseProgress * 1.5f); // 2秒展开完毕，保留1秒余韵
        poseStack.pushPose();
        poseStack.translate(0.5D, -0.93f, 0.5D);
        poseStack.mulPose(Axis.YP.rotationDegrees(-angleY * 0.5f)); 
        poseStack.mulPose(Axis.XP.rotationDegrees(90f));
        float sizeA = aP * 70f; // 大范围扩散
        drawTexturedQuadWithAlpha(pulseBuilder, poseStack, sizeA, sizeA, maxLight, (int)(255 * (1f - aP)));
        poseStack.popPose();

        // Layer B — 中空扩散：升空并扩大
        float bP = pulseProgress; 
        poseStack.pushPose();
        poseStack.translate(0.5D, 40.0f, 0.5D); // 在40格高处爆发
        poseStack.mulPose(Axis.YP.rotationDegrees(angleY * 0.4f));
        poseStack.mulPose(Axis.XP.rotationDegrees(90f));
        float sizeB = bP * 120f; // 更广阔的覆盖
        drawTexturedQuadWithAlpha(pulseBuilder, poseStack, sizeB, sizeB, maxLight, (int)(200 * (1f - bP)));
        poseStack.popPose();

        // Layer C — 高空延迟追击波：在接近云层高度散开
        float cP = Math.max(0f, (pulseProgress - 0.2f) / 0.8f);
        poseStack.pushPose();
        poseStack.translate(0.5D, 90.0f, 0.5D); // 极高空
        poseStack.mulPose(Axis.YP.rotationDegrees(-angleY * 0.2f));
        poseStack.mulPose(Axis.XP.rotationDegrees(90f));
        float sizeC = cP * 160f; // 笼罩整个领域级别的天空波纹
        drawTexturedQuadWithAlpha(pulseBuilder, poseStack, sizeC, sizeC, maxLight, (int)(150 * (1f - cP)));
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
