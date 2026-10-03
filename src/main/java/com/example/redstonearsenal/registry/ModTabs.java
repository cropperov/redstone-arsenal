package com.example.redstonearsenal.registry;

import com.example.redstonearsenal.RedstoneArsenal;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModTabs {
    public static final DeferredRegister<CreativeModeTab> TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, RedstoneArsenal.MODID);

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> MAIN = TABS.register("main",
            () -> CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup.redstonearsenal"))
                    .icon(() -> new ItemStack(ModItems.MISSILE_EXPLOSIVE.get()))
                    .displayItems((params, out) -> {
                        out.accept(ModItems.MISSILE_EXPLOSIVE.get());
                        out.accept(ModItems.MISSILE_CLUSTER.get());
                        out.accept(ModItems.MISSILE_TERRAFORMER.get());
                        out.accept(ModItems.TANK.get());
                        out.accept(ModItems.ORBITAL_CANNON.get());
                        out.accept(ModItems.STRIKE_DESIGNATOR.get());
                    }).build());
}
