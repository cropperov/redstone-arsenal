package com.example.redstonearsenal.entity;

import com.example.redstonearsenal.registry.ModEntities;
import com.example.redstonearsenal.registry.ModItems;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;

/**
 * Rideable tank. WASD drives (A/D pivot on the spot), the turret follows where you look,
 * left click fires a shell.
 */
public class TankEntity extends Entity {
    private static final EntityDataAccessor<Float> DATA_TURRET_YAW = SynchedEntityData.defineId(TankEntity.class, EntityDataSerializers.FLOAT);

    public static final float MIN_PITCH = -35.0F;
    public static final float MAX_PITCH = 12.0F;

    private int cooldown;
    private float speed;

    public TankEntity(EntityType<? extends TankEntity> type, Level level) {
        super(type, level);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        builder.define(DATA_TURRET_YAW, 0.0F);
    }

    public float getTurretYaw() {
        return entityData.get(DATA_TURRET_YAW);
    }

    // ---------------------------------------------------------------- riding

    @Override
    public InteractionResult interact(Player player, InteractionHand hand) {
        if (player.isSecondaryUseActive()) return InteractionResult.PASS;
        if (level().isClientSide) return InteractionResult.SUCCESS;
        return player.startRiding(this) ? InteractionResult.CONSUME : InteractionResult.PASS;
    }

    @Override
    protected boolean canAddPassenger(Entity passenger) {
        return getPassengers().isEmpty();
    }

    @Nullable
    @Override
    public LivingEntity getControllingPassenger() {
        return getFirstPassenger() instanceof Player p ? p : null;
    }

    @Override
    public boolean isPickable() {
        return !isRemoved();
    }

    @Override
    public boolean canBeCollidedWith() {
        return true;
    }

    public float maxUpStep() {
        return 1.0F;
    }

    // ---------------------------------------------------------------- tick

    @Override
    public void tick() {
        super.tick();
        if (cooldown > 0) cooldown--;

        LivingEntity pilot = getControllingPassenger();
        if (!level().isClientSide && pilot != null) {
            entityData.set(DATA_TURRET_YAW, pilot.getYRot());
        }

        if (isControlledByLocalInstance()) {
            float fwd = 0, turn = 0;
            if (pilot != null) {
                fwd = pilot.zza;
                turn = pilot.xxa;
            }
            setYRot(getYRot() - turn * 3.5F);
            float target = fwd * (fwd > 0 ? 0.22F : 0.12F);
            speed += (target - speed) * 0.15F;
            Vec3 look = Vec3.directionFromRotation(0, getYRot());
            double vy = onGround() ? -0.05 : getDeltaMovement().y - 0.08;
            setDeltaMovement(look.x * speed, vy, look.z * speed);
            move(MoverType.SELF, getDeltaMovement());
        } else {
            setDeltaMovement(Vec3.ZERO);
        }
    }

    // ---------------------------------------------------------------- gun

    public void fire(Player pilot) {
        if (level().isClientSide || cooldown > 0) return;
        cooldown = 30;
        float pitch = Mth.clamp(pilot.getXRot(), MIN_PITCH, MAX_PITCH);
        Vec3 dir = Vec3.directionFromRotation(pitch, pilot.getYRot());
        Vec3 origin = position().add(0, 0.95, 0).add(dir.scale(1.7));

        MissileEntity shell = new MissileEntity(ModEntities.MISSILE.get(), level());
        shell.setMissileType(MissileType.SHELL);
        shell.setGravity(0.012);
        shell.setPos(origin);
        shell.setDir(dir);
        shell.setLaunched(true);
        shell.setOwner(pilot);
        shell.setDeltaMovement(dir.scale(2.4));
        level().addFreshEntity(shell);

        ServerLevel sl = (ServerLevel) level();
        sl.playSound(null, getX(), getY(), getZ(), SoundEvents.FIREWORK_ROCKET_LARGE_BLAST, SoundSource.NEUTRAL, 5.0F, 0.5F);
        sl.playSound(null, getX(), getY(), getZ(), SoundEvents.FIREWORK_ROCKET_BLAST, SoundSource.NEUTRAL, 5.0F, 0.4F);
        sl.sendParticles(ParticleTypes.POOF, origin.x, origin.y, origin.z, 12, 0.2, 0.2, 0.2, 0.05);
        sl.sendParticles(ParticleTypes.FLAME, origin.x, origin.y, origin.z, 8, 0.1, 0.1, 0.1, 0.08);
    }

    // ---------------------------------------------------------------- pickup

    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (!level().isClientSide && !isRemoved() && source.getEntity() instanceof Player p && !p.isPassenger()) {
            if (!p.getAbilities().instabuild) spawnAtLocation(new ItemStack(ModItems.TANK.get()));
            discard();
            return true;
        }
        return false;
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        entityData.set(DATA_TURRET_YAW, tag.getFloat("TurretYaw"));
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        tag.putFloat("TurretYaw", getTurretYaw());
    }
}
