package com.harbinger.wintercore.block;

import com.harbinger.wintercore.init.WinterCoreBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class WinterCoreFoundationBlockEntity extends BlockEntity {

    public WinterCoreFoundationBlockEntity(BlockPos pos, BlockState state) {
        super(WinterCoreBlocks.WINTER_CORE_FOUNDATION_BE.get(), pos, state);
    }

    @Override
    public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        if (this.getBlockState().hasProperty(WinterCoreFoundationBlock.FORMED) && this.getBlockState().getValue(WinterCoreFoundationBlock.FORMED)) {
            if (this.level != null) {
                BlockEntity core = this.level.getBlockEntity(this.worldPosition.above(2));
                if (core instanceof WinterCoreBlockEntity) {
                    return core.getCapability(cap, side);
                }
            }
        }
        return super.getCapability(cap, side);
    }
}
