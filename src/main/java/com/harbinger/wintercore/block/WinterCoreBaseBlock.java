package com.harbinger.wintercore.block;

import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

public class WinterCoreBaseBlock extends Block {

    public static final net.minecraft.world.level.block.state.properties.BooleanProperty POWERED = net.minecraft.world.level.block.state.properties.BlockStateProperties.POWERED;

    public WinterCoreBaseBlock(Properties properties) {
        super(properties.lightLevel(state -> state.getValue(POWERED) ? 14 : 0));
        this.registerDefaultState(this.stateDefinition.any().setValue(POWERED, false));
    }

    @Override
    protected void createBlockStateDefinition(net.minecraft.world.level.block.state.StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(POWERED);
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        // 如果玩家主手拿的是凛冬核心，直接放行让原版方块放置逻辑介入，无需 Shift
        if (hand == InteractionHand.MAIN_HAND) {
            net.minecraft.world.item.ItemStack held = player.getMainHandItem();
            if (held.getItem() instanceof net.minecraft.world.item.BlockItem bi
                    && bi.getBlock() instanceof WinterCoreBlock) {
                return InteractionResult.PASS;
            }
        }

        if (!level.isClientSide) {
            // Find the WinterCoreBlockEntity above
            BlockEntity be = level.getBlockEntity(pos.above());
            if (be instanceof WinterCoreBlockEntity core) {
                net.minecraftforge.network.NetworkHooks.openScreen((net.minecraft.server.level.ServerPlayer) player, core, pos.above());
                return InteractionResult.CONSUME;
            }
        }
        return InteractionResult.SUCCESS;
    }
}
