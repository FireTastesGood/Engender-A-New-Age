package net.firetastesgood.ageofminecraft.fusion;

import net.firetastesgood.ageofminecraft.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.network.NetworkHooks;

public class FusionCrafterBlock extends BaseEntityBlock {

    public FusionCrafterBlock(Properties props) {
        super(props);
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new FusionCrafterBlockEntity(pos, state);
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos,
                                 Player player, InteractionHand hand, BlockHitResult hit) {
        if (!level.isClientSide) {
            BlockEntity be = level.getBlockEntity(pos);
            if (be instanceof FusionCrafterBlockEntity fc && player instanceof ServerPlayer sp) {
                NetworkHooks.openScreen(sp, fc, pos);
            }
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    @Override
    public void onRemove(BlockState oldState, Level level, BlockPos pos,
                         BlockState newState, boolean isMoving) {
        if (oldState.getBlock() != newState.getBlock()) {
            BlockEntity be = level.getBlockEntity(pos);
            if (be instanceof FusionCrafterBlockEntity crafter) {
                crafter.dropContentsAndRefundEnergy();
            }
        }
        super.onRemove(oldState, level, pos, newState, isMoving);
    }

    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return !level.isClientSide
                ? createTickerHelper(type, ModBlockEntities.FUSION_CRAFTER.get(),
                FusionCrafterBlockEntity::serverTick)
                : null;
    }
}