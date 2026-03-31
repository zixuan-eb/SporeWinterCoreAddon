package com.harbinger.wintercore.client;

import com.harbinger.wintercore.block.WinterCoreBlock;
import com.harbinger.wintercore.block.WinterCoreBlockEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.world.level.block.state.BlockState;

public class WinterCoreRenderer implements BlockEntityRenderer<WinterCoreBlockEntity> {

    public WinterCoreRenderer(BlockEntityRendererProvider.Context context) {}

    @Override
    public void render(WinterCoreBlockEntity blockEntity, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
        if (!blockEntity.isFormed || blockEntity.getLevel() == null) return;

        BlockState state = blockEntity.getBlockState();
        long time = blockEntity.getLevel().getGameTime();
        
        // 我们利用未成形的本体模型作为渲染网格
        BlockState crystalState = state.setValue(WinterCoreBlock.FORMED, false);
        
        // 1. 底层实体物理悬浮展示（尖尖朝下的钻石漂浮心态）
        poseStack.pushPose();
        poseStack.translate(0.5D, 0.5D + Math.sin((time + partialTick) * 0.05) * 0.15D, 0.5D); 
        poseStack.mulPose(Axis.YP.rotationDegrees((time + partialTick) * 1.5f)); 
        poseStack.mulPose(Axis.XP.rotationDegrees(45f));
        poseStack.mulPose(Axis.ZP.rotationDegrees(45f));
        poseStack.translate(-0.5D, -0.5D, -0.5D);
        
        Minecraft.getInstance().getBlockRenderer().renderSingleBlock(crystalState, poseStack, bufferSource, 15728880, packedOverlay, net.minecraftforge.client.model.data.ModelData.EMPTY, null);
        poseStack.popPose();

        float angleY = (time + partialTick) * 3.0f; 

        // 2. 第一层绚丽光效：全息呼吸结界
        poseStack.pushPose();
        poseStack.translate(0.5D, 0.5D + Math.sin((time + partialTick) * 0.05) * 0.15D, 0.5D); // 跟随核心漂浮
        poseStack.mulPose(Axis.YP.rotationDegrees(angleY));
        poseStack.mulPose(Axis.XP.rotationDegrees(45f));
        poseStack.mulPose(Axis.ZP.rotationDegrees(45f));
        
        float scale1 = 1.15f + 0.08f * (float)Math.sin((time + partialTick) * 0.1f);
        poseStack.scale(scale1, scale1, scale1);
        poseStack.translate(-0.5D, -0.5D, -0.5D);
        
        Minecraft.getInstance().getBlockRenderer().renderSingleBlock(crystalState, poseStack, bufferSource, 15728880, packedOverlay, net.minecraftforge.client.model.data.ModelData.EMPTY, RenderType.translucent());
        poseStack.popPose();

        // 3. 第二层高阶光效：逆向超立方体切割屏障 
        poseStack.pushPose();
        poseStack.translate(0.5D, 0.5D + Math.sin((time + partialTick) * 0.05) * 0.15D, 0.5D);
        poseStack.mulPose(Axis.YP.rotationDegrees(-angleY * 2.0f)); 
        poseStack.mulPose(Axis.XP.rotationDegrees(45f)); 
        poseStack.mulPose(Axis.ZP.rotationDegrees(30f)); 
        
        float scale2 = 1.35f + 0.05f * (float)Math.cos((time + partialTick) * 0.15f);
        poseStack.scale(scale2, scale2, scale2);
        poseStack.translate(-0.5D, -0.5D, -0.5D);
        
        Minecraft.getInstance().getBlockRenderer().renderSingleBlock(crystalState, poseStack, bufferSource, 15728880, packedOverlay, net.minecraftforge.client.model.data.ModelData.EMPTY, RenderType.translucent());
        poseStack.popPose();
    }
    
    @Override
    public boolean shouldRenderOffScreen(WinterCoreBlockEntity blockEntity) {
        return true; 
    }
}
