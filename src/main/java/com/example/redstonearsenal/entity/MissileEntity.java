package com.example.redstonearsenal.entity;

import com.example.redstonearsenal.registry.ModEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import java.util.UUID;

/**
 * One entity for every projectile in the mod: placed missiles waiting for a redstone signal,
 * flying missiles, cluster bomblets and tank shells.
 */
public class MissileEntity extends Entity {
    private static final EntityDataAccessor<Integer> DATA_TYPE = SynchedEntityData.defineId(MissileEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Boolean> DATA_LAUNCHED = SynchedEntityData.defineId(MissileEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Float> DATA_DX = SynchedEntityData.defineId(MissileEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> DATA_DY = SynchedEntityData.defineId(MissileEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> DATA_DZ = SynchedEntityData.defineId(MissileEntity.class, EntityDataSerializers.FLOAT);

    private int age;
    private double gravity;
    private UUID ownerId;

    public MissileEntity(EntityType<? extends MissileEntity> type, Level level) {
        super(type, level);
        this.noPhysics = true;
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        builder.define(DATA_TYPE, MissileType.EXPLOSIVE.ordinal());
        builder.define(DATA_LAUNCHED, false);
        builder.define(DATA_DX, 0.0F);
        builder.define(DATA_DY, 1.0F);
        builder.define(DATA_DZ, 0.0F);
    }

    // ---------------------------------------------------------------- accessors

    public MissileType getMissileType() {
        return MissileType.byId(entityData.get(DATA_TYPE));
    }

    public void setMissileType(MissileType t) {
        entityData.set(DATA_TYPE, t.ordinal());
    }

    public boolean isLaunched() {
        return entityData.get(DATA_LAUNCHED);
    }

    public void setLaunched(boolean b) {
        entityData.set(DATA_LAUNCHED, b);
    }

    public Vec3 getDir() {
        return new Vec3(entityData.get(DATA_DX), entityData.get(DATA_DY), entityData.get(DATA_DZ));
    }

    public void setDir(Vec3 v) {
        if (v.lengthSqr() < 1.0E-8) v = new Vec3(0, 1, 0);
        Vec3 n = v.normalize();
        entityData.set(DATA_DX, (float) n.x);
        entityData.set(DATA_DY, (float) n.y);
        entityData.set(DATA_DZ, (float) n.z);
        setBoundingBox(makeBoundingBox());
    }

    public void setGravity(double g) {
        this.gravity = g;
    }

    public void setOwner(Entity e) {
        this.ownerId = e == null ? null : e.getUUID();
    }

    @Override
    public void onSyncedDataUpdated(EntityDataAccessor<?> key) {
        super.onSyncedDataUpdated(key);
        if (key == DATA_DX || key == DATA_DY || key == DATA_DZ) {
            setBoundingBox(makeBoundingBox());
        }
    }

    /** Hitbox sits along the missile body instead of at its base. */
    @Override
    protected AABB makeBoundingBox() {
        if (entityData == null) return super.makeBoundingBox();
        Vec3 d = getDir();
        boolean shell = getMissileType() == MissileType.SHELL;
        double half = shell ? 0.2 : 0.3;
        double len = shell ? 0.3 : 0.8;
        Vec3 c = position().add(d.scale(len));
        return new AABB(c.x - half, c.y - half, c.z - half, c.x + half, c.y + half, c.z + half);
    }

    @Override
    public boolean isPickable() {
        return !isRemoved() && !isLaunched();
    }

    // ---------------------------------------------------------------- tick

    @Override
    public void tick() {
        super.tick();
        if (level().isClientSide) {
            clientTick();
            return;
        }
        ServerLevel sl = (ServerLevel) level();

        if (!isLaunched()) {
            if (tickCount % 2 == 0) {
                Vec3 d = getDir();
                BlockPos support = BlockPos.containing(position().subtract(d.scale(0.05)));
                if (sl.getBlockState(support).isAir()) {
                    dropAsItem();
                } else if (isPowered(sl, support)) {
                    launch(sl);
                }
            }
            return;
        }
        flight(sl);
    }

    private boolean isPowered(Level level, BlockPos support) {
        return level.hasNeighborSignal(support)
                || level.getDirectSignalTo(support) > 0
                || level.hasNeighborSignal(blockPosition());
    }

    private void launch(ServerLevel sl) {
        setLaunched(true);
        age = 0;
        setDeltaMovement(getDir().scale(0.3));
        sl.playSound(null, getX(), getY(), getZ(), SoundEvents.FIREWORK_ROCKET_LAUNCH, SoundSource.NEUTRAL, 4.0F, 0.6F);
        sl.playSound(null, getX(), getY(), getZ(), SoundEvents.FIREWORK_ROCKET_LARGE_BLAST, SoundSource.NEUTRAL, 2.0F, 0.5F);
        sl.sendParticles(ParticleTypes.LARGE_SMOKE, getX(), getY(), getZ(), 25, 0.3, 0.3, 0.3, 0.05);
    }

    private void flight(ServerLevel sl) {
        age++;
        MissileType type = getMissileType();
        Vec3 mot;
        if (gravity > 0) {
            mot = getDeltaMovement().add(0, -gravity, 0);
            if (mot.lengthSqr() > 1.0E-6) setDir(mot);
        } else {
            double speed = Math.min(2.0, 0.3 + age * 0.07);
            mot = getDir().scale(speed);
        }
        setDeltaMovement(mot);

        double tipLen = type == MissileType.SHELL ? 0.3 : 1.6;
        Vec3 from = position().add(getDir().scale(tipLen));
        Vec3 to = from.add(mot);

        BlockHitResult bhr = sl.clip(new ClipContext(from, to, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, this));
        boolean hit = bhr.getType() != HitResult.Type.MISS;
        Vec3 end = hit ? bhr.getLocation() : to;

        EntityHitResult ehr = ProjectileUtil.getEntityHitResult(sl, this, from, end,
                new AABB(from, end).inflate(0.6), this::canHit);
        if (ehr != null) {
            hit = true;
            end = ehr.getLocation();
        }

        if (hit) {
            detonate(sl, end.subtract(mot.normalize().scale(0.2)), mot);
            return;
        }
        setPos(position().add(mot));
        if (age > 400 || getY() < sl.getMinBuildHeight() - 32) {
            detonate(sl, position(), mot);
        }
    }

    private boolean canHit(Entity e) {
        if (!e.isPickable() || e.isSpectator() || e instanceof MissileEntity) return false;
        if (ownerId != null && age < 60 && level() instanceof ServerLevel sl) {
            Entity owner = sl.getEntity(ownerId);
            if (owner != null && (e == owner || e == owner.getVehicle())) return false;
        }
        return true;
    }

    private void clientTick() {
        if (!isLaunched()) return;
        setPos(position().add(getDeltaMovement()));
        boolean shell = getMissileType() == MissileType.SHELL;
        Vec3 tail = position().subtract(getDir().scale(shell ? 0.1 : 0.2));
        if (!shell) {
            level().addParticle(ParticleTypes.FLAME, tail.x, tail.y, tail.z, 0, 0, 0);
            level().addParticle(ParticleTypes.LARGE_SMOKE, tail.x, tail.y, tail.z, 0, 0.02, 0);
        } else {
            level().addParticle(ParticleTypes.SMOKE, tail.x, tail.y, tail.z, 0, 0, 0);
        }
    }

    // ---------------------------------------------------------------- explosions

    private void detonate(ServerLevel sl, Vec3 p, Vec3 mot) {
        MissileType type = getMissileType();
        switch (type) {
            case EXPLOSIVE, SHELL -> sl.explode(this, p.x, p.y, p.z, type.power, Level.ExplosionInteraction.TNT);
            case CLUSTER -> {
                sl.explode(this, p.x, p.y, p.z, type.power, Level.ExplosionInteraction.TNT);
                spawnBomblets(sl, p);
            }
            case TERRAFORMER -> terraform(sl, BlockPos.containing(p));
        }
        discard();
    }

    private void spawnBomblets(ServerLevel sl, Vec3 p) {
        RandomSource r = sl.random;
        for (int i = 0; i < 14; i++) {
            MissileEntity b = new MissileEntity(ModEntities.MISSILE.get(), sl);
            b.setMissileType(MissileType.SHELL);
            Vec3 v = new Vec3((r.nextDouble() - 0.5) * 1.2, 0.5 + r.nextDouble() * 0.7, (r.nextDouble() - 0.5) * 1.2);
            b.setGravity(0.05);
            b.setPos(p.x, p.y + 0.5, p.z);
            b.setDir(v);
            b.setLaunched(true);
            b.setDeltaMovement(v);
            b.age = 8;
            b.ownerId = this.ownerId;
            sl.addFreshEntity(b);
        }
    }

    /** Flattens the area to the impact height and covers it with grass and flowers. */
    private void terraform(ServerLevel sl, BlockPos center) {
        final int radius = 12;
        final int baseY = center.getY();
        RandomSource r = sl.random;
        BlockState grass = Blocks.GRASS_BLOCK.defaultBlockState();
        BlockState dirt = Blocks.DIRT.defaultBlockState();
        BlockState air = Blocks.AIR.defaultBlockState();

        for (int dx = -radius; dx <= radius; dx++) {
            for (int dz = -radius; dz <= radius; dz++) {
                double dist = Math.sqrt(dx * dx + dz * dz);
                if (dist > radius) continue;
                if (dist > radius - 2 && r.nextFloat() < 0.4F) continue; // ragged edge
                int x = center.getX() + dx;
                int z = center.getZ() + dz;

                for (int y = baseY + 14; y >= baseY; y--) {
                    BlockPos p = new BlockPos(x, y, z);
                    BlockState s = sl.getBlockState(p);
                    if (!s.isAir() && s.getDestroySpeed(sl, p) >= 0) sl.setBlock(p, air, 3);
                }
                for (int y = baseY - 1; y >= baseY - 8; y--) {
                    BlockPos p = new BlockPos(x, y, z);
                    BlockState s = sl.getBlockState(p);
                    if (s.getDestroySpeed(sl, p) < 0) break;
                    boolean fillable = s.isAir() || !s.getFluidState().isEmpty() || s.canBeReplaced();
                    if (y == baseY - 1) {
                        sl.setBlock(p, grass, 3);
                    } else if (fillable) {
                        sl.setBlock(p, dirt, 3);
                    } else {
                        break;
                    }
                }
                BlockPos top = new BlockPos(x, baseY, z);
                float roll = r.nextFloat();
                if (roll < 0.20F) sl.setBlock(top, Blocks.SHORT_GRASS.defaultBlockState(), 3);
                else if (roll < 0.23F) sl.setBlock(top, Blocks.DANDELION.defaultBlockState(), 3);
                else if (roll < 0.26F) sl.setBlock(top, Blocks.POPPY.defaultBlockState(), 3);
                else if (roll < 0.275F) sl.setBlock(top, Blocks.CORNFLOWER.defaultBlockState(), 3);
            }
        }
        sl.sendParticles(ParticleTypes.HAPPY_VILLAGER, center.getX() + 0.5, center.getY() + 1, center.getZ() + 0.5,
                200, radius * 0.5, 1.5, radius * 0.5, 0.0);
        sl.playSound(null, center, SoundEvents.FIREWORK_ROCKET_TWINKLE, SoundSource.NEUTRAL, 4.0F, 0.7F);
        sl.playSound(null, center, SoundEvents.BONE_MEAL_USE, SoundSource.NEUTRAL, 4.0F, 0.6F);
    }

    // ---------------------------------------------------------------- pickup

    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (!level().isClientSide && !isLaunched() && !isRemoved() && source.getEntity() instanceof Player p) {
            if (!p.getAbilities().instabuild) dropAsItem();
            else discard();
            return true;
        }
        return false;
    }

    private void dropAsItem() {
        var item = getMissileType().item();
        if (item != null) spawnAtLocation(new ItemStack(item));
        discard();
    }

    // ---------------------------------------------------------------- save

    @Override
    protected void readAdditionalSaveData(CompoundTag t) {
        setMissileType(MissileType.byId(t.getInt("Type")));
        setLaunched(t.getBoolean("Launched"));
        setDir(new Vec3(t.getFloat("Dx"), t.getFloat("Dy"), t.getFloat("Dz")));
        gravity = t.getDouble("Gravity");
        age = t.getInt("Age");
        if (t.hasUUID("Owner")) ownerId = t.getUUID("Owner");
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag t) {
        t.putInt("Type", getMissileType().ordinal());
        t.putBoolean("Launched", isLaunched());
        Vec3 d = getDir();
        t.putFloat("Dx", (float) d.x);
        t.putFloat("Dy", (float) d.y);
        t.putFloat("Dz", (float) d.z);
        t.putDouble("Gravity", gravity);
        t.putInt("Age", age);
        if (ownerId != null) t.putUUID("Owner", ownerId);
    }
}
