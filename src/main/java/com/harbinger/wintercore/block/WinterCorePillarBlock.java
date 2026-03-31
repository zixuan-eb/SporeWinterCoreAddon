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
    public static final BooleanProperty UP = BooleanProperty.create("up");
    public static final BooleanProperty DOWN = BooleanProperty.create("down");

    public WinterCorePillarBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.defaultBlockState().setValue(FORMED, false).setValue(CORNER, 0).setValue(IS_TOP, false).setValue(UP, false).setValue(DOWN, false));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<net.minecraft.world.level.block.Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(FORMED, CORNER, IS_TOP, UP, DOWN);
    }

    @Override
    public BlockState getStateForPlacement(net.minecraft.world.item.context.BlockPlaceContext context) {
        BlockState state = super.getStateForPlacement(context);
        if (state == null) state = this.defaultBlockState();
        net.minecraft.world.level.Level level = context.getLevel();
        net.minecraft.core.BlockPos pos = context.getClickedPos();
        return state
                .setValue(UP, level.getBlockState(pos.above()).getBlock() == this)
                .setValue(DOWN, level.getBlockState(pos.below()).getBlock() == this);
    }

    @Override
    public BlockState updateShape(BlockState state, net.minecraft.core.Direction direction, BlockState neighborState, net.minecraft.world.level.LevelAccessor level, net.minecraft.core.BlockPos currentPos, net.minecraft.core.BlockPos neighborPos) {
        if (direction == net.minecraft.core.Direction.UP) {
            return state.setValue(UP, neighborState.getBlock() == this);
        }
        if (direction == net.minecraft.core.Direction.DOWN) {
            return state.setValue(DOWN, neighborState.getBlock() == this);
        }
        return super.updateShape(state, direction, neighborState, level, currentPos, neighborPos);
    }

    @Override
    public net.minecraft.world.phys.shapes.VoxelShape getShape(BlockState state, net.minecraft.world.level.BlockGetter level, net.minecraft.core.BlockPos pos, net.minecraft.world.phys.shapes.CollisionContext context) {
        if (state.getValue(FORMED)) return net.minecraft.world.phys.shapes.Shapes.block();
        net.minecraft.core.Direction.Axis axis = state.getValue(net.minecraft.world.level.block.state.properties.BlockStateProperties.AXIS);
        if (axis == net.minecraft.core.Direction.Axis.X) return net.minecraft.world.level.block.Block.box(0, 2, 2, 16, 14, 14);
        if (axis == net.minecraft.core.Direction.Axis.Z) return net.minecraft.world.level.block.Block.box(2, 2, 0, 14, 14, 16);
        return net.minecraft.world.level.block.Block.box(2, 0, 2, 14, 16, 14);
    }
}
