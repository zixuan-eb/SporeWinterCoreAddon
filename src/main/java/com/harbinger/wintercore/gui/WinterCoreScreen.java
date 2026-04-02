package com.harbinger.wintercore.gui;

import com.harbinger.wintercore.WinterCoreAddon;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class WinterCoreScreen extends AbstractContainerScreen<WinterCoreMenu> {

    private static final ResourceLocation TEXTURE = new ResourceLocation(WinterCoreAddon.MODID, "textures/gui/winter_core_screen.png");

    public WinterCoreScreen(WinterCoreMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        this.imageWidth = 176;
        this.imageHeight = 166;
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        RenderSystem.setShader(net.minecraft.client.renderer.GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.setShaderTexture(0, TEXTURE);
        
        int relX = (this.width - this.imageWidth) / 2;
        int relY = (this.height - this.imageHeight) / 2;

        guiGraphics.blit(TEXTURE, relX, relY, 0, 0, this.imageWidth, this.imageHeight);

        // FE Energy Bar rendering
        int stored = this.menu.blockEntity.energyStorage.getEnergyStored();
        int max = this.menu.blockEntity.energyStorage.getMaxEnergyStored();
        int feHeight = Math.round(50.0F * stored / max); // 50 is the max height of our bar
        if (feHeight > 0) {
            // Suppose we have an active bar texture placed in the same png at 176, 0
            // We just draw a cyan rect for now
            guiGraphics.fill(relX + 20, relY + 70 - feHeight, relX + 30, relY + 70, 0xFF00FFFF);
        }
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(guiGraphics);
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        this.renderTooltip(guiGraphics, mouseX, mouseY);

        int relX = (this.width - this.imageWidth) / 2;
        int relY = (this.height - this.imageHeight) / 2;

        // Hover energy tip
        if (mouseX >= relX + 20 && mouseX <= relX + 30 && mouseY >= relY + 20 && mouseY <= relY + 70) {
            int stored = this.menu.blockEntity.energyStorage.getEnergyStored();
            int max = this.menu.blockEntity.energyStorage.getMaxEnergyStored();
            guiGraphics.renderTooltip(this.font, Component.literal(stored + " / " + max + " FE"), mouseX, mouseY);
        }
    }
}
