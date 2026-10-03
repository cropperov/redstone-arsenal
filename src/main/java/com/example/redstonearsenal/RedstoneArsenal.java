package com.example.redstonearsenal;

import com.example.redstonearsenal.registry.*;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;

@Mod(RedstoneArsenal.MODID)
public class RedstoneArsenal {
    public static final String MODID = "redstonearsenal";

    public RedstoneArsenal(IEventBus modBus) {
        ModBlocks.BLOCKS.register(modBus);
        ModItems.ITEMS.register(modBus);
        ModEntities.ENTITIES.register(modBus);
        ModBlockEntities.BLOCK_ENTITIES.register(modBus);
        ModTabs.TABS.register(modBus);
    }
}
