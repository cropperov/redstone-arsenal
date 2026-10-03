package com.example.redstonearsenal.item;

import com.example.redstonearsenal.block.OrbitalCannonBlock;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.context.UseOnContext;

import java.util.List;
import java.util.Optional;

/** Right-click a block to mark it as the strike target, then right-click an Orbital Cannon to link it. */
public class StrikeDesignatorItem extends Item {
    public StrikeDesignatorItem(Properties props) {
        super(props);
    }

    @Override
    public InteractionResult useOn(UseOnContext ctx) {
        var level = ctx.getLevel();
        BlockPos pos = ctx.getClickedPos();
        if (level.getBlockState(pos).getBlock() instanceof OrbitalCannonBlock) return InteractionResult.PASS;
        if (level.isClientSide) return InteractionResult.SUCCESS;

        CustomData.update(DataComponents.CUSTOM_DATA, ctx.getItemInHand(), tag -> {
            tag.putInt("tx", pos.getX());
            tag.putInt("ty", pos.getY());
            tag.putInt("tz", pos.getZ());
        });
        if (ctx.getPlayer() != null) {
            ctx.getPlayer().displayClientMessage(Component.translatable("message.redstonearsenal.target_set",
                    pos.getX(), pos.getY(), pos.getZ()).withStyle(ChatFormatting.RED), true);
        }
        return InteractionResult.CONSUME;
    }

    public static Optional<BlockPos> getTarget(ItemStack stack) {
        CompoundTag tag = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
        if (!tag.contains("tx")) return Optional.empty();
        return Optional.of(new BlockPos(tag.getInt("tx"), tag.getInt("ty"), tag.getInt("tz")));
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        return getTarget(stack).isPresent();
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext ctx, List<Component> tooltip, TooltipFlag flag) {
        Optional<BlockPos> t = getTarget(stack);
        if (t.isPresent()) {
            BlockPos p = t.get();
            tooltip.add(Component.translatable("tooltip.redstonearsenal.designator.target", p.getX(), p.getY(), p.getZ()).withStyle(ChatFormatting.RED));
        } else {
            tooltip.add(Component.translatable("tooltip.redstonearsenal.designator.none").withStyle(ChatFormatting.GRAY));
        }
        tooltip.add(Component.translatable("tooltip.redstonearsenal.designator.help").withStyle(ChatFormatting.DARK_GRAY));
    }
}
