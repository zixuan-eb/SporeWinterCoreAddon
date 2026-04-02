import re

with open('src/main/java/com/harbinger/wintercore/client/WinterCoreRenderer.java', 'r', encoding='utf-8') as f:
    content = f.read()

# Replace block from "        // 3. Render Spinning Magic Circles" up to the end of the star loop
start_str = "        // 3. Render Spinning Magic Circles"
end_str = "        }\n    }\n\n    private void drawTrueVolumetricBeam"

new_render_method_content = """        // Effects are now drawn via ClientEvents using renderBeams() to ensure depth sorting.
    }

    public static void renderBeams(WinterCoreBlockEntity blockEntity, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource, int maxLight, int overlay) {
        long time = blockEntity.getLevel().getGameTime();
        float angleY = (time + partialTick) * 4f;

        VertexConsumer circleBuilder = bufferSource.getBuffer(ADDITIVE_MAGIC_CIRCLE);
        
        poseStack.pushPose();
        poseStack.translate(0.5D, -0.95f, 0.5D);
        poseStack.mulPose(com.mojang.math.Axis.YP.rotationDegrees(-angleY * 0.5f));
        poseStack.mulPose(com.mojang.math.Axis.XP.rotationDegrees(90f));
        float baseScale = 9.0f + 0.3f * (float)Math.sin((time + partialTick) * 0.08f);
        drawTexturedQuad(circleBuilder, poseStack, baseScale, baseScale, maxLight);
        poseStack.popPose();

        poseStack.pushPose();
        poseStack.translate(0.5D, 120.0f, 0.5D);
        poseStack.mulPose(com.mojang.math.Axis.YP.rotationDegrees(angleY * 0.15f)); 
        poseStack.mulPose(com.mojang.math.Axis.XP.rotationDegrees(90f));
        float skyScale = 160.0f + 2.0f * (float)Math.sin((time + partialTick) * 0.05f);
        drawTexturedQuad(circleBuilder, poseStack, skyScale, skyScale, maxLight);
        poseStack.popPose();

        VertexConsumer beamBuilder = bufferSource.getBuffer(ADDITIVE_MAGIC_BEAM);
        float beamHeight = 350f;

        poseStack.pushPose();
        poseStack.translate(0.5D, -1.8f, 0.5D);
        poseStack.mulPose(com.mojang.math.Axis.YP.rotationDegrees(angleY * 2.0f));
        float outerWidth = 1.6f + 0.1f * (float)Math.sin((time + partialTick) * 0.2f);
        drawTrueVolumetricBeam(beamBuilder, poseStack, outerWidth, beamHeight, maxLight);
        poseStack.popPose();

        poseStack.pushPose();
        poseStack.translate(0.5D, -1.8f, 0.5D);
        poseStack.mulPose(com.mojang.math.Axis.YP.rotationDegrees(-angleY * 3.0f));
        float innerWidth = 0.6f;
        drawTrueVolumetricBeam(beamBuilder, poseStack, innerWidth, beamHeight, maxLight);
        poseStack.popPose();

        float cycleDuration = 60.0f; 
        float pulseProgress = ((time % (long)cycleDuration) + partialTick) / cycleDuration;
        VertexConsumer pulseBuilder = bufferSource.getBuffer(ADDITIVE_PULSE_WAVE);

        float aP = Math.min(1f, pulseProgress * 1.5f); 
        poseStack.pushPose();
        poseStack.translate(0.5D, -0.93f, 0.5D);
        poseStack.mulPose(com.mojang.math.Axis.YP.rotationDegrees(-angleY * 0.5f)); 
        poseStack.mulPose(com.mojang.math.Axis.XP.rotationDegrees(90f));
        float sizeA = aP * 70f; 
        drawTexturedQuadWithAlpha(pulseBuilder, poseStack, sizeA, sizeA, maxLight, (int)(255 * (1f - aP)));
        poseStack.popPose();

        float bP = pulseProgress; 
        poseStack.pushPose();
        poseStack.translate(0.5D, 40.0f, 0.5D); 
        poseStack.mulPose(com.mojang.math.Axis.YP.rotationDegrees(angleY * 0.4f));
        poseStack.mulPose(com.mojang.math.Axis.XP.rotationDegrees(90f));
        float sizeB = bP * 120f; 
        drawTexturedQuadWithAlpha(pulseBuilder, poseStack, sizeB, sizeB, maxLight, (int)(200 * (1f - bP)));
        poseStack.popPose();

        float cP = Math.max(0f, (pulseProgress - 0.2f) / 0.8f);
        poseStack.pushPose();
        poseStack.translate(0.5D, 90.0f, 0.5D);
        poseStack.mulPose(com.mojang.math.Axis.YP.rotationDegrees(-angleY * 0.2f));
        poseStack.mulPose(com.mojang.math.Axis.XP.rotationDegrees(90f));
        float sizeC = cP * 160f; 
        drawTexturedQuadWithAlpha(pulseBuilder, poseStack, sizeC, sizeC, maxLight, (int)(150 * (1f - cP)));
        poseStack.popPose();

        org.joml.Quaternionf cameraRot = net.minecraft.client.Minecraft.getInstance().gameRenderer.getMainCamera().rotation();
        VertexConsumer starBuilder = bufferSource.getBuffer(ADDITIVE_WINTER_STAR);
        int starCount = 80;
        
        for (int i = 0; i < starCount; i++) {
            java.util.Random rand = new java.util.Random(blockEntity.getBlockPos().hashCode() * 31L + i * 17L);
            
            float startDist = 30f + rand.nextFloat() * 20f; 
            float duration = 100f + rand.nextFloat() * 60f; 
            
            float rawProgress = ((time + i * 77L) % (long)duration + partialTick) / duration;
            float p = rawProgress * rawProgress * rawProgress; 
            
            float currentDist = startDist * (1.0f - p);
            
            float theta = rand.nextFloat() * (float)Math.PI * 2f + (time + partialTick) * 0.01f;
            float phi = rand.nextFloat() * (float)Math.PI - (float)Math.PI / 2f; 
            
            float dx = currentDist * (float)(Math.cos(phi) * Math.cos(theta));
            float dy = currentDist * (float)(Math.sin(phi));
            float dz = currentDist * (float)(Math.cos(phi) * Math.sin(theta));
            
            float starAlphaF = 1.0f;
            if (currentDist > startDist - 2f) starAlphaF = (startDist - currentDist) / 2f;
            if (currentDist < 3f) starAlphaF = currentDist / 3f;
            int starAlpha = (int)(255 * starAlphaF);

            poseStack.pushPose();
            poseStack.translate(0.5D + dx, 1.5D + dy, 0.5D + dz);
            poseStack.mulPose(cameraRot); 
            
            poseStack.mulPose(com.mojang.math.Axis.ZP.rotationDegrees((time + partialTick) * (rand.nextFloat() * 5f + 2f) * (rand.nextBoolean() ? 1:-1)));
            
            float starScale = 0.4f + rand.nextFloat() * 0.8f;
            drawTexturedQuadWithAlpha(starBuilder, poseStack, starScale, starScale, maxLight, starAlpha);
            poseStack.popPose();
        }
    }

    private static void drawTrueVolumetricBeam"""

s_idx = content.find(start_str)
e_idx = content.find(end_str)

if s_idx != -1 and e_idx != -1:
    content = content[:s_idx] + new_render_method_content + content[e_idx + len(end_str):]
else:
    print("Could not find blocks to replace!")

# Let's also restore ADDITIVE RenderType definitions at the top!
head_target = '    private static final ResourceLocation WINTER_STAR = new ResourceLocation("wintercore", "textures/effect/winter_star.png");'
add_rt = """

    public static final RenderType ADDITIVE_MAGIC_CIRCLE = getAdditive(MAGIC_CIRCLE);
    public static final RenderType ADDITIVE_MAGIC_BEAM = getAdditive(MAGIC_BEAM);
    public static final RenderType ADDITIVE_PULSE_WAVE = getAdditive(PULSE_WAVE);
    public static final RenderType ADDITIVE_WINTER_STAR = getAdditive(WINTER_STAR);

    public static RenderType getAdditive(ResourceLocation location) {
        RenderType.CompositeState state = RenderType.CompositeState.builder()
                .setShaderState(RenderStateShard.RENDERTYPE_ENTITY_TRANSLUCENT_EMISSIVE_SHADER)
                .setTextureState(new RenderStateShard.TextureStateShard(location, false, false))
                .setTransparencyState(RenderStateShard.LIGHTNING_TRANSPARENCY)
                .setCullState(RenderStateShard.NO_CULL)
                .setWriteMaskState(RenderStateShard.COLOR_WRITE)
                .setDepthTestState(RenderStateShard.NO_DEPTH_TEST)
                .createCompositeState(false);
        return RenderType.create("wintercore_additive",
                com.mojang.blaze3d.vertex.DefaultVertexFormat.NEW_ENTITY,
                com.mojang.blaze3d.vertex.VertexFormat.Mode.QUADS,
                256, false, true, state);
    }
"""
content = content.replace(head_target, head_target + add_rt)

# We need to change drawTrueVolumetricBeam to be STATIC
content = content.replace("private void drawTrueVolumetricBeam", "private static void drawTrueVolumetricBeam")
content = content.replace("private void drawTexturedQuadWithAlpha", "private static void drawTexturedQuadWithAlpha")
content = content.replace("private void drawTexturedQuad", "private static void drawTexturedQuad")

with open('src/main/java/com/harbinger/wintercore/client/WinterCoreRenderer.java', 'w', encoding='utf-8') as f:
    f.write(content)
print("Done!")
