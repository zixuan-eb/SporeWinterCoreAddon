package com.harbinger.wintercore.item;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.EnergyStorage;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class WinterInfiniteEnergyCellItem extends Item {

    public WinterInfiniteEnergyCellItem(Properties properties) {
        super(properties.stacksTo(1));
    }

    @Override
    public boolean isBarVisible(ItemStack stack) {
        return true;
    }

    @Override
    public int getBarWidth(ItemStack stack) {
        return 13; // Always full
    }

    @Override
    public int getBarColor(ItemStack stack) {
        return 0xFF00FF; // Magenta color for creative infinite energy
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        return true; // Make it glow like an enchanted item
    }

    @Nullable
    @Override
    public ICapabilityProvider initCapabilities(ItemStack stack, @Nullable CompoundTag nbt) {
        return new ICapabilityProvider() {
            private final EnergyStorage storage = new EnergyStorage(2000000, 2000000, 2000000) {
                @Override
                public int getEnergyStored() {
                    return 2000000; // Always return full energy
                }

                public int setEnergy(int energy) {
                    return 2000000; // Do nothing
                }

                @Override
                public int receiveEnergy(int maxReceive, boolean simulate) {
                    return 0; // Already full, cannot receive more
                }

                @Override
                public int extractEnergy(int maxExtract, boolean simulate) {
                    return maxExtract; // Provide infinite energy, extract is always successful without depleting
                }
            };
            private final LazyOptional<IEnergyStorage> energyOptional = LazyOptional.of(() -> storage);

            @Override
            public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
                if (cap == ForgeCapabilities.ENERGY) return energyOptional.cast();
                return LazyOptional.empty();
            }
        };
    }
}
