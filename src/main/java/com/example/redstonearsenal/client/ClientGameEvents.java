package com.example.redstonearsenal.client;

import com.example.redstonearsenal.RedstoneArsenal;
import com.example.redstonearsenal.entity.TankEntity;
import com.example.redstonearsenal.network.FireTankPayload;
import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.InputEvent;
import net.neoforged.neoforge.network.PacketDistributor;

/** Left click while driving a tank fires the cannon instead of swinging. */
@EventBusSubscriber(modid = RedstoneArsenal.MODID, value = Dist.CLIENT)
public class ClientGameEvents {
    @SubscribeEvent
    public static void onAttackKey(InputEvent.InteractionKeyMappingTriggered event) {
        if (!event.isAttack()) return;
        Minecraft mc = Minecraft.getInstance();
        if (mc.player != null && mc.player.getVehicle() instanceof TankEntity) {
            PacketDistributor.sendToServer(new FireTankPayload());
            event.setSwingHand(false);
            event.setCanceled(true);
        }
    }
}
