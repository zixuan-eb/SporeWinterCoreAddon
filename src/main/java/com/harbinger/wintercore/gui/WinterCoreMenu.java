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

        // Custom 5 slots for Battery and Upgrades (Range, Damage, Protection)
        for (int i = 0; i < 5; i++) {
            this.addSlot(new SlotItemHandler(blockEntity.itemHandler, i, 44 + i * 18, 20));
        }

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
            
            // If the clicked slot is our custom machine slot (0-4)
            if (idx < 5) {
                if (!this.moveItemStackTo(stack, 5, 41, true)) {
                    return ItemStack.EMPTY;
                }
                slot.onQuickCraft(stack, itemstack);
            } else {
                // Moving from player inventory to machine slot
                if (stack.getCapability(net.minecraftforge.common.capabilities.ForgeCapabilities.ENERGY).isPresent()) {
                    if (!this.moveItemStackTo(stack, 0, 1, false)) { // Slot 0 is reserved for battery
                        return ItemStack.EMPTY;
                    }
                } else if (stack.getItem() == WinterCoreBlocks.UPGRADE_RANGE.get() || 
                           stack.getItem() == WinterCoreBlocks.UPGRADE_DAMAGE.get() || 
                           stack.getItem() == WinterCoreBlocks.UPGRADE_PROTECTION.get()) {
                    if (!this.moveItemStackTo(stack, 1, 5, false)) { // Slots 1-4 for upgrades
                        return ItemStack.EMPTY;
                    }
                } else if (idx >= 5 && idx < 32) {
                    if (!this.moveItemStackTo(stack, 32, 41, false)) {
                        return ItemStack.EMPTY;
                    }
                } else if (idx >= 32 && idx < 41 && !this.moveItemStackTo(stack, 5, 32, false)) {
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
