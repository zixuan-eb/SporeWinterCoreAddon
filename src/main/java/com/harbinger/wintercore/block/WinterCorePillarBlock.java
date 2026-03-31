package com.harbinger.wintercore.block;

import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;

public class WinterCorePillarBlock extends RotatedPillarBlock {
    public static final BooleanProperty FORMED = BooleanProperty.create("formed");
    public static final IntegerProperty CORNER = IntegerProperty.create("corner", 0, 3);
    public static final BooleanProperty IS_TOP = BooleanProperty.create("is_top");

    public WinterCorePillarBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.defaultBlockState().setValue(FORMED, false).setValue(CORNER, 0).setValue(IS_TOP, false));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<net.minecraft.world.level.block.Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(FORMED, CORNER, IS_TOP);
    }
}
