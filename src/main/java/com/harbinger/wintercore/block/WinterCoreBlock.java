package com.harbinger.wintercore.block;

import com.harbinger.wintercore.init.WinterCoreBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public class WinterCoreBlock extends BaseEntityBlock {

    public static final net.minecraft.world.level.block.state.properties.BooleanProperty FORMED = net.minecraft.world.level.block.state.properties.BooleanProperty.create("formed");

    public WinterCoreBlock() {
        super(net.minecraft.world.level.block.state.BlockBehaviour.Properties.of()
                .strength(50.0F, 1200.0F)
                .sound(net.minecraft.world.level.block.SoundType.GLASS)
                .lightLevel(state -> 15)
                .requiresCorrectToolForDrops()
                .noOcclusion());
        this.registerDefaultState(this.stateDefinition.any().setValue(FORMED, false));
    }

    @Override
    protected void createBlockStateDefinition(net.minecraft.world.level.block.state.StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FORMED);
    }

    @Override
    public net.minecraft.world.level.block.RenderShape getRenderShape(BlockState state) {
        return state.getValue(FORMED) ? net.minecraft.world.level.block.RenderShape.INVISIBLE : net.minecraft.world.level.block.RenderShape.MODEL;
    }

    @Override
    public net.minecraft.world.phys.shapes.VoxelShape getShape(BlockState state, net.minecraft.world.level.BlockGetter level, net.minecraft.core.BlockPos pos, net.minecraft.world.phys.shapes.CollisionContext context) {
        return net.minecraft.world.level.block.Block.box(2.0D, 2.0D, 2.0D, 14.0D, 14.0D, 14.0D);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new WinterCoreBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> blockEntityType) {
        if (level.isClientSide) {
            return createTickerHelper(blockEntityType, WinterCoreBlocks.WINTER_CORE_BE.get(),
                    WinterCoreBlockEntity::clientTick);
        }
        return createTickerHelper(blockEntityType, WinterCoreBlocks.WINTER_CORE_BE.get(),
                WinterCoreBlockEntity::serverTick);
    }



    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving) {
        if (!state.is(newState.getBlock())) {
            BlockEntity blockEntity = level.getBlockEntity(pos);
            if (blockEntity instanceof WinterCoreBlockEntity core) {
                if (core.isFormed) {
                    core.revertMultiblock();
                }
            }
            super.onRemove(state, level, pos, newState, isMoving);
        }
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, net.minecraft.util.RandomSource random) {
        BlockEntity blockEntity = level.getBlockEntity(pos);
        if (blockEntity instanceof WinterCoreBlockEntity core && core.isFormed) {
            // Ambient soft hum
            if (random.nextInt(80) == 0) {
                level.playLocalSound((double)pos.getX() + 0.5D, (double)pos.getY() + 0.5D, (double)pos.getZ() + 0.5D,
                        net.minecraft.sounds.SoundEvents.BEACON_AMBIENT, net.minecraft.sounds.SoundSource.BLOCKS, 0.4F, random.nextFloat() * 0.2F + 0.9F, false);
            }
            // Occasional frost crackle
            if (random.nextInt(120) == 0) {
                level.playLocalSound((double)pos.getX() + 0.5D, (double)pos.getY() + 0.5D, (double)pos.getZ() + 0.5D,
                        net.minecraft.sounds.SoundEvents.GLASS_BREAK, net.minecraft.sounds.SoundSource.BLOCKS, 0.15F, random.nextFloat() * 0.4F + 1.2F, false);
            }

            // Swirling frost and magic particles
            for (int i = 0; i < 4; i++) {
                double x0 = (double)pos.getX() + random.nextDouble() * 1.5D - 0.25D;
                double y0 = (double)pos.getY() + random.nextDouble() * 2.0D;
                double z0 = (double)pos.getZ() + random.nextDouble() * 1.5D - 0.25D;
                level.addParticle(net.minecraft.core.particles.ParticleTypes.SNOWFLAKE, x0, y0, z0,
                        (random.nextDouble() - 0.5D) * 0.1D,
                        random.nextDouble() * 0.1D + 0.05D,
                        (random.nextDouble() - 0.5D) * 0.1D);

                if (random.nextBoolean()) {
                    level.addParticle(net.minecraft.core.particles.ParticleTypes.ENCHANT, x0, y0 - 1.0D, z0,
                            (random.nextDouble() - 0.5D) * 0.5D,
                            0.5D,
                            (random.nextDouble() - 0.5D) * 0.5D);
                }
            }
        }
    }
}
