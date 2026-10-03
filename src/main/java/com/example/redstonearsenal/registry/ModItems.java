package com.example.redstonearsenal.registry;

import com.example.redstonearsenal.RedstoneArsenal;
import com.example.redstonearsenal.entity.MissileType;
import com.example.redstonearsenal.item.*;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModItems {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(RedstoneArsenal.MODID);

    public static final DeferredItem<Item> MISSILE_EXPLOSIVE = ITEMS.register("missile_explosive",
            () -> new MissileItem(MissileType.EXPLOSIVE, new Item.Properties().stacksTo(16)));
    public static final DeferredItem<Item> MISSILE_CLUSTER = ITEMS.register("missile_cluster",
            () -> new MissileItem(MissileType.CLUSTER, new Item.Properties().stacksTo(16)));
    public static final DeferredItem<Item> MISSILE_TERRAFORMER = ITEMS.register("missile_terraformer",
            () -> new MissileItem(MissileType.TERRAFORMER, new Item.Properties().stacksTo(16)));
    public static final DeferredItem<Item> TANK = ITEMS.register("tank",
            () -> new TankItem(new Item.Properties().stacksTo(1)));
    public static final DeferredItem<Item> STRIKE_DESIGNATOR = ITEMS.register("strike_designator",
            () -> new StrikeDesignatorItem(new Item.Properties().stacksTo(1)));
    public static final DeferredItem<BlockItem> ORBITAL_CANNON = ITEMS.registerSimpleBlockItem(ModBlocks.ORBITAL_CANNON);
}
