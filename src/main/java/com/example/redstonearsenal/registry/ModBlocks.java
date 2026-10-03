package com.example.redstonearsenal.registry;

import com.example.redstonearsenal.RedstoneArsenal;
import com.example.redstonearsenal.block.OrbitalCannonBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModBlocks {
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(RedstoneArsenal.MODID);

    public static final DeferredBlock<OrbitalCannonBlock> ORBITAL_CANNON = BLOCKS.register("orbital_cannon",
            () -> new OrbitalCannonBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.METAL)
                    .strength(5.0F, 1200.0F)
                    .sound(SoundType.NETHERITE_BLOCK)
                    .requiresCorrectToolForDrops()
                    .lightLevel(s -> s.getValue(OrbitalCannonBlock.POWERED) ? 12 : 0)));
}
