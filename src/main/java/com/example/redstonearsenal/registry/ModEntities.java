package com.example.redstonearsenal.registry;

import com.example.redstonearsenal.RedstoneArsenal;
import com.example.redstonearsenal.entity.MissileEntity;
import com.example.redstonearsenal.entity.TankEntity;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModEntities {
    public static final DeferredRegister<EntityType<?>> ENTITIES =
            DeferredRegister.create(Registries.ENTITY_TYPE, RedstoneArsenal.MODID);

    public static final DeferredHolder<EntityType<?>, EntityType<MissileEntity>> MISSILE = ENTITIES.register("missile",
            () -> EntityType.Builder.<MissileEntity>of(MissileEntity::new, MobCategory.MISC)
                    .sized(0.5F, 0.5F).clientTrackingRange(10).updateInterval(1).fireImmune()
                    .build("missile"));

    public static final DeferredHolder<EntityType<?>, EntityType<TankEntity>> TANK = ENTITIES.register("tank",
            () -> EntityType.Builder.<TankEntity>of(TankEntity::new, MobCategory.MISC)
                    .sized(1.8F, 1.3F).clientTrackingRange(10).updateInterval(1)
                    .passengerAttachments(1.05F)
                    .build("tank"));
}
