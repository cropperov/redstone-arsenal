package com.example.redstonearsenal.network;

import com.example.redstonearsenal.RedstoneArsenal;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record FireTankPayload() implements CustomPacketPayload {
    public static final Type<FireTankPayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(RedstoneArsenal.MODID, "fire_tank"));
    public static final StreamCodec<RegistryFriendlyByteBuf, FireTankPayload> CODEC = StreamCodec.unit(new FireTankPayload());

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
