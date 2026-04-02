package com.harbinger.wintercore.gui;

import com.harbinger.wintercore.block.WinterCoreBlockEntity;
import com.harbinger.wintercore.init.WinterCoreBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.SlotItemHandler;

public class WinterCoreMenu extends AbstractContainerMenu {

    public final WinterCoreBlockEntity blockEntity;

    // Client-side constructor
    public WinterCoreMenu(int windowId, Inventory playerInventory, BlockPos pos) {
        this(windowId, playerInventory, (WinterCoreBlockEntity) playerInventory.player.level().getBlockEntity(pos));
    }

    // Common constructor
    public WinterCoreMenu(int windowId, Inventory playerInventory, WinterCoreBlockEntity blockEntity) {
        super(WinterCoreBlocks.WINTER_CORE_MENU.get(), windowId);
        this.blockEntity = blockEntity;

        // Custom single slot for Winter Energy Cell
        this.addSlot(new SlotItemHandler(blockEntity.itemHandler, 0, 80, 20));

        // Player Inventory
        int startX = 8;
        int startY = 51;
        for (int row = 0; row < 3; ++row) {
            for (int col = 0; col < 9; ++col) {
                this.addSlot(new Slot(playerInventory, col + row * 9 + 9, startX + col * 18, startY + row * 18));
            }
        }
        // Player Hotbar
        for (int col = 0; col < 9; ++col) {
            this.addSlot(new Slot(playerInventory, col, startX + col * 18, startY + 58));
        }
    }

    @Override
    public ItemStack quickMoveStack(Player player, int idx) {
        ItemStack itemstack = ItemStack.EMPTY;
        Slot slot = this.slots.get(idx);
        if (slot != null && slot.hasItem()) {
            ItemStack stack = slot.getItem();
            itemstack = stack.copy();
            
            // If the clicked slot is our custom machine slot
            if (idx == 0) {
                if (!this.moveItemStackTo(stack, 1, 37, true)) {
                    return ItemStack.EMPTY;
                }
                slot.onQuickCraft(stack, itemstack);
            } else {
                // Moving from player inventory to machine slot
                if (stack.getItem() == WinterCoreBlocks.WINTER_ENERGY_CELL.get()) {
                    if (!this.moveItemStackTo(stack, 0, 1, false)) {
                        return ItemStack.EMPTY;
                    }
                } else if (idx >= 1 && idx < 28) {
                    if (!this.moveItemStackTo(stack, 28, 37, false)) {
                        return ItemStack.EMPTY;
                    }
                } else if (idx >= 28 && idx < 37 && !this.moveItemStackTo(stack, 1, 28, false)) {
                    return ItemStack.EMPTY;
                }
            }

            if (stack.isEmpty()) {
                slot.set(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }

            if (stack.getCount() == itemstack.getCount()) {
                return ItemStack.EMPTY;
            }

            slot.onTake(player, stack);
        }
        return itemstack;
    }

    @Override
    public boolean stillValid(Player player) {
        return stillValid(net.minecraft.world.inventory.ContainerLevelAccess.create(blockEntity.getLevel(), blockEntity.getBlockPos()), player, WinterCoreBlocks.WINTER_CORE.get());
    }
}
