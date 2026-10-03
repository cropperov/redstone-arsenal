package com.example.redstonearsenal.registry;

import com.example.redstonearsenal.RedstoneArsenal;
import com.example.redstonearsenal.block.OrbitalCannonBlockEntity;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModBlockEntities {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
            DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, RedstoneArsenal.MODID);

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<OrbitalCannonBlockEntity>> ORBITAL_CANNON =
            BLOCK_ENTITIES.register("orbital_cannon", () ->
                    BlockEntityType.Builder.of(OrbitalCannonBlockEntity::new, ModBlocks.ORBITAL_CANNON.get()).build(null));
}
