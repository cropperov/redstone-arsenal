package com.example.redstonearsenal.item;

import com.example.redstonearsenal.entity.MissileEntity;
import com.example.redstonearsenal.entity.MissileType;
import com.example.redstonearsenal.registry.ModEntities;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.List;

/** Places a missile on any face of any block, snapped to the 16x16 pixel grid of that face. */
public class MissileItem extends Item {
    private final MissileType type;

    public MissileItem(MissileType type, Properties props) {
        super(props);
        this.type = type;
    }

    @Override
    public InteractionResult useOn(UseOnContext ctx) {
        Level level = ctx.getLevel();
        if (level.isClientSide) return InteractionResult.SUCCESS;

        Direction face = ctx.getClickedFace();
        Vec3 hit = ctx.getClickLocation();
        var bp = ctx.getClickedPos();

        double x = face.getAxis() == Direction.Axis.X ? hit.x : snap(hit.x, bp.getX());
        double y = face.getAxis() == Direction.Axis.Y ? hit.y : snap(hit.y, bp.getY());
        double z = face.getAxis() == Direction.Axis.Z ? hit.z : snap(hit.z, bp.getZ());

        MissileEntity m = new MissileEntity(ModEntities.MISSILE.get(), level);
        m.setMissileType(type);
        m.setDir(Vec3.atLowerCornerOf(face.getNormal()));
        m.setPos(x, y, z);
        level.addFreshEntity(m);
        level.playSound(null, x, y, z, SoundEvents.IRON_GOLEM_REPAIR, SoundSource.BLOCKS, 1.0F, 0.8F);

        Player player = ctx.getPlayer();
        if (player == null || !player.getAbilities().instabuild) ctx.getItemInHand().shrink(1);
        return InteractionResult.CONSUME;
    }

    /** Snap to the centre of the clicked pixel (1/16 block) inside the given block. */
    private static double snap(double v, int blockCoord) {
        int px = Mth.clamp((int) Math.floor((v - blockCoord) * 16.0), 0, 15);
        return blockCoord + (px + 0.5) / 16.0;
    }

    @Override
    public void appendHoverText(net.minecraft.world.item.ItemStack stack, TooltipContext ctx, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("tooltip.redstonearsenal.missile.place").withStyle(net.minecraft.ChatFormatting.GRAY));
        tooltip.add(Component.translatable("tooltip.redstonearsenal.missile.fire").withStyle(net.minecraft.ChatFormatting.DARK_RED));
        tooltip.add(Component.translatable("tooltip.redstonearsenal.missile." + type.textureName()).withStyle(net.minecraft.ChatFormatting.YELLOW));
    }
}
