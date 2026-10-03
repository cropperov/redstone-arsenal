package com.example.redstonearsenal.network;

import com.example.redstonearsenal.RedstoneArsenal;
import com.example.redstonearsenal.entity.TankEntity;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;

@EventBusSubscriber(modid = RedstoneArsenal.MODID, bus = EventBusSubscriber.Bus.MOD)
public class ModNetwork {
    @SubscribeEvent
    public static void register(RegisterPayloadHandlersEvent event) {
        event.registrar("1").playToServer(FireTankPayload.TYPE, FireTankPayload.CODEC,
                (payload, ctx) -> ctx.enqueueWork(() -> {
                    if (ctx.player().getVehicle() instanceof TankEntity tank) {
                        tank.fire(ctx.player());
                    }
                }));
    }
}
