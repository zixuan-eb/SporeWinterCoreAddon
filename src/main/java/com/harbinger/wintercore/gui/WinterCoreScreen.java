package com.harbinger.wintercore.gui;

import com.harbinger.wintercore.WinterCoreAddon;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class WinterCoreScreen extends AbstractContainerScreen<WinterCoreMenu> {

    private static final ResourceLocation TEXTURE = new ResourceLocation("minecraft", "textures/gui/container/hopper.png");

    public WinterCoreScreen(WinterCoreMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        this.imageWidth = 176;
        this.imageHeight = 133;
        this.inventoryLabelY = this.imageHeight - 94; // Align "Inventory" text just above slots
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        RenderSystem.setShader(net.minecraft.client.renderer.GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.setShaderTexture(0, TEXTURE);
        
        int relX = (this.width - this.imageWidth) / 2;
        int relY = (this.height - this.imageHeight) / 2;

        guiGraphics.blit(TEXTURE, relX, relY, 0, 0, this.imageWidth, this.imageHeight);

        // 遮盖掉漏斗的左右4个多余插槽，只保留中间那一个（我们的电池槽）
        // 漏斗的背景色差不多是 #C6C6C6 (RGB 198, 198, 198) 
        guiGraphics.fill(relX + 43, relY + 19, relX + 79, relY + 37, 0xFFC6C6C6); // 遮盖左侧2槽
        guiGraphics.fill(relX + 97, relY + 19, relX + 133, relY + 37, 0xFFC6C6C6); // 遮盖右侧2槽

        // 给核心的电池槽画一个蓝绿色的科幻外框高亮边
        guiGraphics.renderOutline(relX + 79, relY + 19, 18, 18, 0xFF00FFFF);

        // FE Energy Bar 渲染
        int stored = this.menu.blockEntity.energyStorage.getEnergyStored();
        int max = this.menu.blockEntity.energyStorage.getMaxEnergyStored();
        int maxBarHeight = 30; // 我们可以在左侧画一个高30的电池刻度条
        int feHeight = Math.round((float) maxBarHeight * stored / max);
        
        // 绘制电池槽底层边框（暗灰色）
        guiGraphics.fill(relX + 20, relY + 15, relX + 28, relY + 15 + maxBarHeight, 0xFF555555);
        if (feHeight > 0) {
            // 画出青蓝色的充能进度
            guiGraphics.fill(relX + 20, relY + 15 + maxBarHeight - feHeight, relX + 28, relY + 15 + maxBarHeight, 0xFF00FFFF);
        }
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(guiGraphics);
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        this.renderTooltip(guiGraphics, mouseX, mouseY);

        int relX = (this.width - this.imageWidth) / 2;
        int relY = (this.height - this.imageHeight) / 2;

        // Hover energy tip (鼠标放在电池刻度条上显示文本)
        if (mouseX >= relX + 20 && mouseX <= relX + 28 && mouseY >= relY + 15 && mouseY <= relY + 15 + 30) {
            int stored = this.menu.blockEntity.energyStorage.getEnergyStored();
            int max = this.menu.blockEntity.energyStorage.getMaxEnergyStored();
            guiGraphics.renderTooltip(this.font, Component.literal(stored + " / " + max + " FE"), mouseX, mouseY);
        }
    }
}
