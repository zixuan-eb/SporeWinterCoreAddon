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
    private static final ResourceLocation WINTER_STAR = new ResourceLocation("wintercore", "textures/effect/winter_star.png");
    private static final ResourceLocation HEX_SHIELD = new ResourceLocation("wintercore", "textures/effect/hex_shield.png");

    public static class AdditiveRenderType extends RenderType {
        public AdditiveRenderType(String name, com.mojang.blaze3d.vertex.VertexFormat format, com.mojang.blaze3d.vertex.VertexFormat.Mode mode, int size, boolean affectsCrumbling, boolean sortOnUpload, Runnable setup, Runnable clear) {
            super(name, format, mode, size, affectsCrumbling, sortOnUpload, setup, clear);
        }

        public static RenderType getAdditive(ResourceLocation location) {
            RenderType.CompositeState state = RenderType.CompositeState.builder()
                    .setShaderState(RENDERTYPE_ENTITY_TRANSLUCENT_EMISSIVE_SHADER)
                    .setTextureState(new RenderStateShard.TextureStateShard(location, false, false))
                    .setTransparencyState(LIGHTNING_TRANSPARENCY)
                    .setCullState(NO_CULL)
                    .setWriteMaskState(COLOR_WRITE)
                    .setDepthTestState(NO_DEPTH_TEST)
                    .createCompositeState(false);
            return RenderType.create("wintercore_additive",
                    com.mojang.blaze3d.vertex.DefaultVertexFormat.NEW_ENTITY,
                    com.mojang.blaze3d.vertex.VertexFormat.Mode.QUADS,
                    256, false, true, state);
        }
    }

    public static final RenderType ADDITIVE_MAGIC_CIRCLE = AdditiveRenderType.getAdditive(MAGIC_CIRCLE);
    public static final RenderType ADDITIVE_MAGIC_BEAM = AdditiveRenderType.getAdditive(MAGIC_BEAM);
    public static final RenderType ADDITIVE_PULSE_WAVE = AdditiveRenderType.getAdditive(PULSE_WAVE);
    public static final RenderType ADDITIVE_WINTER_STAR = AdditiveRenderType.getAdditive(WINTER_STAR);
    public static final RenderType ADDITIVE_HEX_SHIELD = AdditiveRenderType.getAdditive(HEX_SHIELD);

    public WinterCoreRenderer(BlockEntityRendererProvider.Context context) {}

    @Override
    public void render(WinterCoreBlockEntity blockEntity, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
        if (!blockEntity.isFormed || blockEntity.getLevel() == null) return;
        
        BlockState state = blockEntity.getBlockState();
        long time = blockEntity.getLevel().getGameTime();
        
        BlockState crystalState = state.setValue(WinterCoreBlock.FORMED, false);
        
        float floatOffset = 0.75f + (float)Math.sin((time + partialTick) * 0.05f) * 0.15f;
        int maxLight = 15728880;

        // 1. Crystal Core Mesh (无论有没有电，成形了就渲染其悬浮状态及外壳的微弱发光)
        poseStack.pushPose();
        poseStack.translate(0.5D, floatOffset, 0.5D); 
        poseStack.mulPose(Axis.YP.rotationDegrees((time + partialTick) * 1.5f)); 
        poseStack.mulPose(Axis.XP.rotationDegrees(45f));
        poseStack.mulPose(Axis.ZP.rotationDegrees(45f));
        poseStack.translate(-0.5D, -0.5D, -0.5D);
        Minecraft.getInstance().getBlockRenderer().renderSingleBlock(crystalState, poseStack, bufferSource, maxLight, packedOverlay, net.minecraftforge.client.model.data.ModelData.EMPTY, null);
        poseStack.popPose();

        // 没电的话不渲染能量光束和风暴粒子特效
        if (!blockEntity.isPowered) return;
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

        // Effects are now drawn via ClientEvents using renderBeams() to ensure depth sorting.
    }

    public static void renderBeams(WinterCoreBlockEntity blockEntity, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource, int maxLight, int overlay) {
        long time = blockEntity.getLevel().getGameTime();
        float angleY = (time + partialTick) * 4f;

        // 删除最早获取 circleBuilder 的逻辑，放到实际需要渲染的位置获取
        // ---- Orbital Comets with Trails ----
        float coreFloatOffset = 0.75f + (float)Math.sin((time + partialTick) * 0.05f) * 0.15f;
        org.joml.Quaternionf cameraRot = net.minecraft.client.Minecraft.getInstance().gameRenderer.getMainCamera().rotation();
        VertexConsumer cometBuilder = bufferSource.getBuffer(ADDITIVE_WINTER_STAR);

        // Orbit definitions: { radius, tiltX, tiltZ, speed, baseScale }
        float[][] orbits = {
            { 1.8f, 70f, 20f, 8f, 0.6f },
            { 2.5f, -45f, 60f, -6f, 0.8f },
            { 3.2f, 25f, -80f, 4f, 0.9f },
            { 1.2f, 90f, 0f, -10f, 0.5f }
        };

        int trailLength = 25; // 彗星拖尾段数

        for (float[] orbit : orbits) {
            float r = orbit[0];
            float tiltX = orbit[1] * (float)Math.PI / 180f;
            float tiltZ = orbit[2] * (float)Math.PI / 180f;
            float speed = orbit[3];
            float baseScale = orbit[4];

            for (int i = 0; i < trailLength; i++) {
                // 通过步进模拟过去的时空节点，生成拖尾
                float angleDegrees = ((time + partialTick) - i * 0.8f) * speed;
                float angleRads = angleDegrees * (float)Math.PI / 180f;

                // 在标准 XZ 轨道面上生成坐标
                org.joml.Vector3f posVec = new org.joml.Vector3f(r * (float)Math.cos(angleRads), 0f, r * (float)Math.sin(angleRads));
                // 叠加双轴轨道倾角
                posVec.rotateX(tiltX);
                posVec.rotateZ(tiltZ);

                float progress = (float)i / (float)(trailLength - 1); // 0.0=彗星头部, 1.0=拖尾末端
                float alphaR = 1.0f - progress;
                int alpha = (int)(255 * alphaR * alphaR); // 平滑渐变的衰减算法

                float scale = baseScale * (1.0f - progress * 0.8f); // 拖尾越来越细

                poseStack.pushPose();
                poseStack.translate(0.5D + posVec.x(), coreFloatOffset + posVec.y(), 0.5D + posVec.z());
                poseStack.mulPose(cameraRot); // 始终朝向玩家摄像机，保证光效饱满
                poseStack.mulPose(Axis.ZP.rotationDegrees((time + partialTick) * 3f + i * 20f));
                drawTexturedQuadWithAlpha(cometBuilder, poseStack, scale, scale, maxLight, alpha);
                poseStack.popPose();
            }
        }
        // ------------------------------------------------
        
        // ---- Ground Magic Array (画地为牢的威严感) ----
        VertexConsumer circleBuilder = bufferSource.getBuffer(ADDITIVE_MAGIC_CIRCLE);
        
        // 底层巨大慢速反转顺时针法阵
        poseStack.pushPose();
        poseStack.translate(0.5D, -0.95f, 0.5D);
        poseStack.mulPose(Axis.YP.rotationDegrees(-angleY * 0.3f));
        poseStack.mulPose(Axis.XP.rotationDegrees(90f));
        float groundScale = 14.0f + 0.3f * (float)Math.sin((time + partialTick) * 0.05f); // 半径很大，圈定整个核心区
        drawTexturedQuadWithAlpha(circleBuilder, poseStack, groundScale, groundScale, maxLight, 220);
        poseStack.popPose();

        // 中层较快逆时针嵌套小法阵
        poseStack.pushPose();
        poseStack.translate(0.5D, -0.94f, 0.5D);
        poseStack.mulPose(Axis.YP.rotationDegrees(angleY * 0.8f));
        poseStack.mulPose(Axis.XP.rotationDegrees(90f));
        float innerGroundScale = 6.0f;
        drawTexturedQuadWithAlpha(circleBuilder, poseStack, innerGroundScale, innerGroundScale, maxLight, 255);
        poseStack.popPose();
        // ----------------------------------------------------------------

        // ---- Giant Expanding World Border Barrier ----
        float waveRadius = blockEntity.getWaveRadius();
        if (waveRadius > 1.0f) {
            poseStack.pushPose();
            // 壁障深入地底且高耸入云，展现包裹感
            poseStack.translate(0.5D, -120.0f, 0.5D); 
            poseStack.mulPose(Axis.YP.rotationDegrees(-angleY * 0.05f)); 
            
            VertexConsumer boundaryBuilder = bufferSource.getBuffer(ADDITIVE_HEX_SHIELD);
            int borderSegments = Math.max(32, (int)(waveRadius * 1.2f)); 
            
            // 提升透明度和亮度，让罩子存在感更强
            int borderAlpha = 110 + (int)(40 * Math.sin((time + partialTick) * 0.05f)); 
            
            // 使用无缝平铺贴图模式，将六边形矩阵铺满巨型光墙
            drawAuraRing(boundaryBuilder, poseStack, waveRadius, 300.0f, maxLight, time + partialTick, borderSegments, borderAlpha, 0.02f, true);
            poseStack.popPose();
        }
        // ----------------------------------------------------------------

        // ---- Sky Giant Magic Circle ----
        poseStack.pushPose();
        poseStack.translate(0.5D, 120.0f, 0.5D);
        poseStack.mulPose(Axis.YP.rotationDegrees(angleY * 0.15f)); 
        poseStack.mulPose(Axis.XP.rotationDegrees(90f));
        float skyScale = 160.0f + 2.0f * (float)Math.sin((time + partialTick) * 0.05f);
        drawTexturedQuad(bufferSource.getBuffer(ADDITIVE_MAGIC_CIRCLE), poseStack, skyScale, skyScale, maxLight);
        poseStack.popPose();

        VertexConsumer beamBuilder = bufferSource.getBuffer(ADDITIVE_MAGIC_BEAM);
        float beamHeight = 350f;

        poseStack.pushPose();
        poseStack.translate(0.5D, coreFloatOffset, 0.5D);
        poseStack.mulPose(Axis.YP.rotationDegrees(angleY * 2.0f));
        float outerWidth = 1.6f + 0.1f * (float)Math.sin((time + partialTick) * 0.2f);
        drawTrueVolumetricBeam(beamBuilder, poseStack, outerWidth, beamHeight, maxLight);
        poseStack.popPose();

        poseStack.pushPose();
        poseStack.translate(0.5D, coreFloatOffset, 0.5D);
        poseStack.mulPose(Axis.YP.rotationDegrees(-angleY * 3.0f));
        float innerWidth = 0.6f;
        drawTrueVolumetricBeam(beamBuilder, poseStack, innerWidth, beamHeight, maxLight);
        poseStack.popPose();

        float cycleDuration = 60.0f; 
        float pulseProgress = ((time % (long)cycleDuration) + partialTick) / cycleDuration;
        VertexConsumer pulseBuilder = bufferSource.getBuffer(ADDITIVE_PULSE_WAVE);

        float aP = Math.min(1f, pulseProgress * 1.5f); 
        poseStack.pushPose();
        poseStack.translate(0.5D, -0.93f, 0.5D);
        poseStack.mulPose(Axis.YP.rotationDegrees(-angleY * 0.5f)); 
        poseStack.mulPose(Axis.XP.rotationDegrees(90f));
        float sizeA = aP * 70f; 
        drawTexturedQuadWithAlpha(pulseBuilder, poseStack, sizeA, sizeA, maxLight, (int)(255 * (1f - aP)));
        poseStack.popPose();

        float bP = pulseProgress; 
        poseStack.pushPose();
        poseStack.translate(0.5D, 40.0f, 0.5D); 
        poseStack.mulPose(Axis.YP.rotationDegrees(angleY * 0.4f));
        poseStack.mulPose(Axis.XP.rotationDegrees(90f));
        float sizeB = bP * 120f; 
        drawTexturedQuadWithAlpha(pulseBuilder, poseStack, sizeB, sizeB, maxLight, (int)(200 * (1f - bP)));
        poseStack.popPose();

        float cP = Math.max(0f, (pulseProgress - 0.2f) / 0.8f);
        poseStack.pushPose();
        poseStack.translate(0.5D, 90.0f, 0.5D);
        poseStack.mulPose(Axis.YP.rotationDegrees(-angleY * 0.2f));
        poseStack.mulPose(Axis.XP.rotationDegrees(90f));
        float sizeC = cP * 160f; 
        drawTexturedQuadWithAlpha(pulseBuilder, poseStack, sizeC, sizeC, maxLight, (int)(150 * (1f - cP)));
        poseStack.popPose();

        VertexConsumer starBuilder = bufferSource.getBuffer(ADDITIVE_WINTER_STAR);
        int starCount = 80;
        
        for (int i = 0; i < starCount; i++) {
            long seed = i * 314159265L ^ blockEntity.getBlockPos().asLong();
            java.util.Random rand = new java.util.Random(seed);
            
            float startDist = 10f + rand.nextFloat() * 25f; // 起始距离核心 10~35 格
            float speed = 0.1f + rand.nextFloat() * 0.4f;   // 汇聚速度
            float currentDist = startDist - (((time + partialTick) * speed) % startDist);

            if (currentDist < 0.2f) continue; // 吸入核心内部后不渲染

            float theta = rand.nextFloat() * (float)Math.PI * 2f; 
            float phi = (rand.nextFloat() - 0.5f) * (float)Math.PI; // 球面坐标
            
            // 增加龙卷风般的缓慢公转旋涡感
            float spinAngle = (time + partialTick) * (rand.nextFloat() * 0.05f + 0.02f) * (rand.nextBoolean() ? 1 : -1);
            theta += spinAngle;

            double dx = currentDist * Math.cos(phi) * Math.cos(theta);
            double dy = currentDist * Math.sin(phi);
            double dz = currentDist * Math.cos(phi) * Math.sin(theta);
            
            // 计算粒子不透明度，在远处淡入，在极近处淡出，中间保持最亮
            float starAlphaF = 1.0f;
            if (currentDist > startDist - 2f) starAlphaF = (startDist - currentDist) / 2f;
            if (currentDist < 3f) starAlphaF = currentDist / 3f;
            int starAlpha = (int)(255 * starAlphaF);

            poseStack.pushPose();
            poseStack.translate(0.5D + dx, 1.5D + dy, 0.5D + dz);
            poseStack.mulPose(cameraRot); // 始终朝向摄像机 (Billboard)
            
            // 给星星一个自身的旋转角，更鲜活
            poseStack.mulPose(Axis.ZP.rotationDegrees((time + partialTick) * (rand.nextFloat() * 5f + 2f) * (rand.nextBoolean() ? 1:-1)));
            
            float starScale = 0.4f + rand.nextFloat() * 0.8f;
            drawTexturedQuadWithAlpha(starBuilder, poseStack, starScale, starScale, maxLight, starAlpha);
            poseStack.popPose();
        }
    }

    private static void drawTrueVolumetricBeam(VertexConsumer builder, PoseStack matrixStack, float width, float height, int light) {
        var pose = matrixStack.last().pose();
        var normal = matrixStack.last().normal();
        float radius = width / 2.0f;
        int segments = 8; // 8边形圆柱体，完美解决纸片感（也可以是 12 边形）
        
        for (int i = 0; i < segments; i++) {
            float angle1 = i * (float) Math.PI * 2.0f / segments;
            float angle2 = (i + 1) * (float) Math.PI * 2.0f / segments;

            float x1 = (float) Math.cos(angle1) * radius;
            float z1 = (float) Math.sin(angle1) * radius;
            
            float x2 = (float) Math.cos(angle2) * radius;
            float z2 = (float) Math.sin(angle2) * radius;
            
            // 将完整的贴图光束均匀映射在每一个多边形切面上
            // （而不是切分贴图，否则由于原光束贴图两边带透明通道边缘，会导致看似一根偏心的光线在绕柱公转）
            float u1 = 0.0f;
            float u2 = 1.0f;
            
            // 四边形的顶点绘制：底左、底右、顶右、顶左
            builder.vertex(pose, x1, 0, z1).color(255, 255, 255, 255).uv(u1, 1).overlayCoords(net.minecraft.client.renderer.texture.OverlayTexture.NO_OVERLAY).uv2(light).normal(normal, 0, 1, 0).endVertex();
            builder.vertex(pose, x2, 0, z2).color(255, 255, 255, 255).uv(u2, 1).overlayCoords(net.minecraft.client.renderer.texture.OverlayTexture.NO_OVERLAY).uv2(light).normal(normal, 0, 1, 0).endVertex();
            builder.vertex(pose, x2, height, z2).color(255, 255, 255, 255).uv(u2, 0).overlayCoords(net.minecraft.client.renderer.texture.OverlayTexture.NO_OVERLAY).uv2(light).normal(normal, 0, 1, 0).endVertex();
            builder.vertex(pose, x1, height, z1).color(255, 255, 255, 255).uv(u1, 0).overlayCoords(net.minecraft.client.renderer.texture.OverlayTexture.NO_OVERLAY).uv2(light).normal(normal, 0, 1, 0).endVertex();
        }
    }

    private static void drawAuraRing(VertexConsumer builder, PoseStack matrixStack, float radius, float height, int light, float time, int segments, int maxAlpha, float flickerAmp, boolean tileTexture) {
        var pose = matrixStack.last().pose();
        var normal = matrixStack.last().normal();
        
        for (int i = 0; i < segments; i++) {
            float angle1 = i * (float) Math.PI * 2.0f / segments;
            float angle2 = (i + 1) * (float) Math.PI * 2.0f / segments;

            float x1 = (float) Math.cos(angle1) * radius;
            float z1 = (float) Math.sin(angle1) * radius;
            
            float x2 = (float) Math.cos(angle2) * radius;
            float z2 = (float) Math.sin(angle2) * radius;
            
            // 无缝水平贴图铺设（每一段占1个UV宽）
            float u1 = tileTexture ? (float)i : 0.0f;
            float u2 = tileTexture ? (float)(i + 1) : 1.0f;
            
            // Generate flickering flame heights based on sine wave harmonics
            float flicker1 = 1.0f + flickerAmp * (float)Math.sin(time * 0.4f + i * 1.5f);
            float flicker2 = 1.0f + flickerAmp * (float)Math.sin(time * 0.4f + (i + 1) * 1.5f);
            
            float h1 = height * flicker1;
            float h2 = height * flicker2;

            // 保持贴图等比例映射（宽度:高度的实际世界占比来控制 V 坐标）
            float segmentWidth = (float)(2.0 * Math.PI * radius / segments);
            
            // 控制贴图在垂直方向的重复率。增加这个值能让六边形变扁（显得更宽更厚实），而不是拉条瘦长
            float verticalTileDensity = tileTexture ? 0.9f : 1.0f; 
            
            // Texture offset (让蜂巢或光幕沿着高度缓缓流淌上升)
            float vOffset = tileTexture ? -(time * 0.08f) : 0.0f;

            float vBottom = tileTexture ? 0.0f : 1.0f;
            float vTop1 = tileTexture ? (h1 / segmentWidth) * verticalTileDensity : 0.0f;
            float vTop2 = tileTexture ? (h2 / segmentWidth) * verticalTileDensity : 0.0f;

            // Fade completely at the top of the flames
            int alphaBottom = maxAlpha;
            int alphaTop = 0;

            builder.vertex(pose, x1, 0, z1).color(180, 220, 255, alphaBottom).uv(u1, vBottom + vOffset).overlayCoords(net.minecraft.client.renderer.texture.OverlayTexture.NO_OVERLAY).uv2(light).normal(normal, 0, 1, 0).endVertex();
            builder.vertex(pose, x2, 0, z2).color(180, 220, 255, alphaBottom).uv(u2, vBottom + vOffset).overlayCoords(net.minecraft.client.renderer.texture.OverlayTexture.NO_OVERLAY).uv2(light).normal(normal, 0, 1, 0).endVertex();
            builder.vertex(pose, x2, h2, z2).color(180, 220, 255, alphaTop).uv(u2, vTop2 + vOffset).overlayCoords(net.minecraft.client.renderer.texture.OverlayTexture.NO_OVERLAY).uv2(light).normal(normal, 0, 1, 0).endVertex();
            builder.vertex(pose, x1, h1, z1).color(180, 220, 255, alphaTop).uv(u1, vTop1 + vOffset).overlayCoords(net.minecraft.client.renderer.texture.OverlayTexture.NO_OVERLAY).uv2(light).normal(normal, 0, 1, 0).endVertex();
        }
    }

    /**
     * 绘制支持动态透明度的纹理四边形（用于脉冲光环淡出效果）
     */
    private static void drawTexturedQuadWithAlpha(VertexConsumer builder, PoseStack matrixStack, float width, float height, int light, int alpha) {
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

    private static void drawTexturedQuad(VertexConsumer builder, PoseStack matrixStack, float width, float height, int light) {
        var pose = matrixStack.last().pose();
        var normal = matrixStack.last().normal();
        float hw = width / 2.0f;
        float hh = height / 2.0f;
        builder.vertex(pose, -hw, hh, 0).color(255, 255, 255, 255).uv(0, 1).overlayCoords(net.minecraft.client.renderer.texture.OverlayTexture.NO_OVERLAY).uv2(light).normal(normal, 0, 0, 1).endVertex();
        builder.vertex(pose, hw, hh, 0).color(255, 255, 255, 255).uv(1, 1).overlayCoords(net.minecraft.client.renderer.texture.OverlayTexture.NO_OVERLAY).uv2(light).normal(normal, 0, 0, 1).endVertex();
        builder.vertex(pose, hw, -hh, 0).color(255, 255, 255, 255).uv(1, 0).overlayCoords(net.minecraft.client.renderer.texture.OverlayTexture.NO_OVERLAY).uv2(light).normal(normal, 0, 0, 1).endVertex();
        builder.vertex(pose, -hw, -hh, 0).color(255, 255, 255, 255).uv(0, 0).overlayCoords(net.minecraft.client.renderer.texture.OverlayTexture.NO_OVERLAY).uv2(light).normal(normal, 0, 0, 1).endVertex();
    }
    
    @Override
    public boolean shouldRenderOffScreen(WinterCoreBlockEntity blockEntity) {
        return true; 
    }
}
