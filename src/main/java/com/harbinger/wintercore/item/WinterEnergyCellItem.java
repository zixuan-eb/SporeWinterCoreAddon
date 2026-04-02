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

public class WinterEnergyCellItem extends Item {

    public WinterEnergyCellItem(Properties properties) {
        super(properties.stacksTo(1));
    }

    @Override
    public boolean isBarVisible(ItemStack stack) {
        return stack.getCapability(ForgeCapabilities.ENERGY).isPresent();
    }

    @Override
    public int getBarWidth(ItemStack stack) {
        return stack.getCapability(ForgeCapabilities.ENERGY)
                .map(energy -> Math.round(13.0F * energy.getEnergyStored() / energy.getMaxEnergyStored()))
                .orElse(0); 
    }

    @Override
    public int getBarColor(ItemStack stack) {
        return 0x00FFFF; // Cyan color for Winter Core Tech
    }

    @Nullable
    @Override
    public ICapabilityProvider initCapabilities(ItemStack stack, @Nullable CompoundTag nbt) {
        return new ICapabilityProvider() {
            private final EnergyStorage storage = new EnergyStorage(2000000, 50000, 50000) {
                @Override
                public int getEnergyStored() {
                    return stack.hasTag() ? stack.getTag().getInt("Energy") : 0;
                }

                public int setEnergy(int energy) {
                    stack.getOrCreateTag().putInt("Energy", energy);
                    return energy;
                }

                @Override
                public int receiveEnergy(int maxReceive, boolean simulate) {
                    int energy = getEnergyStored();
                    int energyReceived = Math.min(capacity - energy, Math.min(this.maxReceive, maxReceive));
                    if (!simulate && energyReceived > 0) setEnergy(energy + energyReceived);
                    return energyReceived;
                }

                @Override
                public int extractEnergy(int maxExtract, boolean simulate) {
                    int energy = getEnergyStored();
                    int energyExtracted = Math.min(energy, Math.min(this.maxExtract, maxExtract));
                    if (!simulate && energyExtracted > 0) setEnergy(energy - energyExtracted);
                    return energyExtracted;
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
