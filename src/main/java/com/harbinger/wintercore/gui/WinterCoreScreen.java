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

        // 遮盖多余的左侧2个槽和右侧2个槽，保留中间槽 (X=79)
        guiGraphics.fill(relX + 43, relY + 19, relX + 79, relY + 37, 0xFFC6C6C6); 
        guiGraphics.fill(relX + 97, relY + 19, relX + 133, relY + 37, 0xFFC6C6C6); 

        // 核心电池槽的蓝绿色发光外框
        guiGraphics.renderOutline(relX + 79, relY + 19, 18, 18, 0xFF00FFFF);

        // FE Energy Bar 渲染（移动到右侧）
        int stored = this.menu.blockEntity.energyStorage.getEnergyStored();
        int max = this.menu.blockEntity.energyStorage.getMaxEnergyStored();
        int maxBarHeight = 30; // 高度30
        int feHeight = Math.round((float) maxBarHeight * stored / max);
        
        // 绘制电池进度条底槽（暗灰色），位于最右侧空白区
        int barX = 152;
        int barY = 17;
        guiGraphics.fill(relX + barX, relY + barY, relX + barX + 8, relY + barY + maxBarHeight, 0xFF555555);
        if (feHeight > 0) {
            // 充能进度
            guiGraphics.fill(relX + barX, relY + barY + maxBarHeight - feHeight, relX + barX + 8, relY + barY + maxBarHeight, 0xFF00FFFF);
        }
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(guiGraphics);
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        this.renderTooltip(guiGraphics, mouseX, mouseY);

        int relX = (this.width - this.imageWidth) / 2;
        int relY = (this.height - this.imageHeight) / 2;

        // Hover energy tip (鼠标悬停在右侧电量条上显示文本)
        int barX = 152;
        int barY = 17;
        int maxBarHeight = 30;
        if (mouseX >= relX + barX && mouseX <= relX + barX + 8 && mouseY >= relY + barY && mouseY <= relY + barY + maxBarHeight) {
            int stored = this.menu.blockEntity.energyStorage.getEnergyStored();
            int max = this.menu.blockEntity.energyStorage.getMaxEnergyStored();
            guiGraphics.renderTooltip(this.font, Component.literal(stored + " / " + max + " FE"), mouseX, mouseY);
        }
    }
}
