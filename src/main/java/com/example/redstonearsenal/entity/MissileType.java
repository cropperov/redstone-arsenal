package com.example.redstonearsenal.entity;

import com.example.redstonearsenal.registry.ModItems;
import net.minecraft.world.level.ItemLike;

public enum MissileType {
    EXPLOSIVE(7.0F),
    CLUSTER(3.5F),
    TERRAFORMER(0.0F),
    SHELL(2.8F);

    public final float power;

    MissileType(float power) {
        this.power = power;
    }

    public static MissileType byId(int id) {
        MissileType[] v = values();
        return v[Math.floorMod(id, v.length)];
    }

    /** Item that gives this missile back when picked up (null for tank shells / bomblets). */
    public ItemLike item() {
        return switch (this) {
            case EXPLOSIVE -> ModItems.MISSILE_EXPLOSIVE.get();
            case CLUSTER -> ModItems.MISSILE_CLUSTER.get();
            case TERRAFORMER -> ModItems.MISSILE_TERRAFORMER.get();
            case SHELL -> null;
        };
    }

    public String textureName() {
        return name().toLowerCase(java.util.Locale.ROOT);
    }
}
