package com.example.redstonearsenal.block;

import com.example.redstonearsenal.item.StrikeDesignatorItem;
import com.example.redstonearsenal.registry.ModBlockEntities;
import com.mojang.serialization.MapCodec;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.BlockHitResult;

import javax.annotation.Nullable;
import java.util.Optional;

public class OrbitalCannonBlock extends BaseEntityBlock {
    public static final MapCodec<OrbitalCannonBlock> CODEC = simpleCodec(OrbitalCannonBlock::new);
    public static final BooleanProperty POWERED = BlockStateProperties.POWERED;

    public OrbitalCannonBlock(Properties props) {
        super(props);
        registerDefaultState(stateDefinition.any().setValue(POWERED, false));
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(POWERED);
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new OrbitalCannonBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return level.isClientSide ? null
                : createTickerHelper(type, ModBlockEntities.ORBITAL_CANNON.get(), OrbitalCannonBlockEntity::serverTick);
    }

    @Override
    protected void neighborChanged(BlockState state, Level level, BlockPos pos, Block block, BlockPos fromPos, boolean moving) {
        if (level.isClientSide) return;
        boolean powered = level.hasNeighborSignal(pos);
        if (powered != state.getValue(POWERED)) {
            level.setBlock(pos, state.setValue(POWERED, powered), 3);
            if (powered && level.getBlockEntity(pos) instanceof OrbitalCannonBlockEntity be) {
                if (!be.trigger()) {
                    Player near = level.getNearestPlayer(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, 10, false);
                    if (near != null && be.getTarget() == null) {
                        near.displayClientMessage(Component.translatable("message.redstonearsenal.no_target").withStyle(ChatFormatting.RED), true);
                    }
                }
            }
        }
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos,
                                              Player player, InteractionHand hand, BlockHitResult hit) {
        if (stack.getItem() instanceof StrikeDesignatorItem) {
            Optional<BlockPos> target = StrikeDesignatorItem.getTarget(stack);
            if (target.isEmpty()) {
                if (!level.isClientSide) {
                    player.displayClientMessage(Component.translatable("message.redstonearsenal.designator_empty").withStyle(ChatFormatting.RED), true);
                }
            } else if (!level.isClientSide && level.getBlockEntity(pos) instanceof OrbitalCannonBlockEntity be) {
                be.setTarget(target.get());
                BlockPos t = target.get();
                player.displayClientMessage(Component.translatable("message.redstonearsenal.linked", t.getX(), t.getY(), t.getZ()).withStyle(ChatFormatting.GREEN), true);
            }
            return ItemInteractionResult.sidedSuccess(level.isClientSide);
        }
        return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        if (!level.isClientSide && level.getBlockEntity(pos) instanceof OrbitalCannonBlockEntity be) {
            BlockPos t = be.getTarget();
            player.displayClientMessage(t == null
                    ? Component.translatable("message.redstonearsenal.no_target").withStyle(ChatFormatting.RED)
                    : Component.translatable("message.redstonearsenal.status", t.getX(), t.getY(), t.getZ()).withStyle(ChatFormatting.AQUA), true);
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }
}
