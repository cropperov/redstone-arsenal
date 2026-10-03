package com.example.redstonearsenal.block;

import com.example.redstonearsenal.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

import javax.annotation.Nullable;

public class OrbitalCannonBlockEntity extends BlockEntity {
    /** Ticks spent charging before the beam arrives. */
    public static final int CHARGE_TICKS = 100;
    /** Ticks the beam stays on the ground. */
    public static final int FIRE_TICKS = 140;
    /** Explosion power of the opening and closing blasts. Lower these if your PC cries. */
    public static final float OPENING_POWER = 12.0F;
    public static final float FINAL_POWER = 16.0F;
    public static final float PULSE_POWER = 4.5F;
    public static final double PULSE_RADIUS = 16.0;

    @Nullable
    private BlockPos target;
    private int timer = -1;

    public OrbitalCannonBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.ORBITAL_CANNON.get(), pos, state);
    }

    @Nullable
    public BlockPos getTarget() {
        return target;
    }

    public void setTarget(BlockPos pos) {
        this.target = pos;
        setChanged();
    }

    /** @return false if it could not start (no target or already firing) */
    public boolean trigger() {
        if (target == null || timer >= 0 || level == null) return false;
        timer = 0;
        setChanged();
        level.playSound(null, worldPosition, SoundEvents.BEACON_ACTIVATE, SoundSource.BLOCKS, 3.0F, 0.6F);
        return true;
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, OrbitalCannonBlockEntity be) {
        if (be.timer < 0 || be.target == null || !(level instanceof ServerLevel sl)) return;
        if (!sl.isLoaded(be.target)) {
            be.timer = -1;
            return;
        }
        be.timer++;
        Vec3 t = Vec3.atBottomCenterOf(be.target).add(0, 1, 0);
        RandomSource r = sl.random;

        if (be.timer <= CHARGE_TICKS) {
            double a = be.timer * 0.45;
            double h = 1.0 + be.timer * 0.04;
            sl.sendParticles(ParticleTypes.END_ROD, pos.getX() + 0.5 + Math.cos(a) * 0.9, pos.getY() + h, pos.getZ() + 0.5 + Math.sin(a) * 0.9, 1, 0, 0, 0, 0);
            sl.sendParticles(ParticleTypes.END_ROD, pos.getX() + 0.5 - Math.cos(a) * 0.9, pos.getY() + h, pos.getZ() + 0.5 - Math.sin(a) * 0.9, 1, 0, 0, 0, 0);

            if (be.timer % 2 == 0) {
                double ringR = 14.0 * (1.0 - be.timer / (double) CHARGE_TICKS) + 1.5;
                DustParticleOptions red = new DustParticleOptions(new Vector3f(1.0F, 0.1F, 0.1F), 2.0F);
                for (int i = 0; i < 24; i++) {
                    double ang = i * (Math.PI * 2 / 24) + be.timer * 0.1;
                    sl.sendParticles(red, t.x + Math.cos(ang) * ringR, t.y + 0.2, t.z + Math.sin(ang) * ringR, 1, 0, 0, 0, 0);
                }
            }
            if (be.timer % 20 == 0) {
                sl.playSound(null, pos, SoundEvents.BEACON_AMBIENT, SoundSource.BLOCKS, 3.0F, 0.5F + be.timer / 100.0F);
                sl.playSound(null, BlockPos.containing(t), SoundEvents.BEACON_AMBIENT, SoundSource.BLOCKS, 4.0F, 0.5F + be.timer / 100.0F);
            }
        } else if (be.timer <= CHARGE_TICKS + FIRE_TICKS) {
            int f = be.timer - CHARGE_TICKS;

            for (double y = 0; y < 160; y += 3.0) {
                sl.sendParticles(ParticleTypes.END_ROD, t.x, t.y + y, t.z, 2, 0.6, 1.0, 0.6, 0.0);
            }
            sl.sendParticles(ParticleTypes.FLAME, t.x, t.y + 0.5, t.z, 12, 3.0, 0.5, 3.0, 0.1);
            sl.sendParticles(ParticleTypes.LARGE_SMOKE, t.x, t.y + 1, t.z, 8, 3.0, 1.0, 3.0, 0.05);

            if (f == 1) {
                sl.playSound(null, BlockPos.containing(t), SoundEvents.LIGHTNING_BOLT_THUNDER, SoundSource.WEATHER, 12.0F, 0.5F);
                sl.explode(null, t.x, t.y, t.z, OPENING_POWER, Level.ExplosionInteraction.TNT);
            } else if (f % 4 == 0 && f < FIRE_TICKS) {
                double ang = r.nextDouble() * Math.PI * 2;
                double dist = r.nextDouble() * PULSE_RADIUS;
                sl.explode(null, t.x + Math.cos(ang) * dist, t.y, t.z + Math.sin(ang) * dist, PULSE_POWER, Level.ExplosionInteraction.TNT);
            }
            if (f == FIRE_TICKS) {
                sl.playSound(null, BlockPos.containing(t), SoundEvents.LIGHTNING_BOLT_THUNDER, SoundSource.WEATHER, 12.0F, 0.4F);
                sl.explode(null, t.x, t.y, t.z, FINAL_POWER, Level.ExplosionInteraction.TNT);
            }
        } else {
            be.timer = -1;
        }
        be.setChanged();
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putInt("Timer", timer);
        if (target != null) tag.putLong("Target", target.asLong());
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        timer = tag.getInt("Timer");
        target = tag.contains("Target") ? BlockPos.of(tag.getLong("Target")) : null;
    }
}
