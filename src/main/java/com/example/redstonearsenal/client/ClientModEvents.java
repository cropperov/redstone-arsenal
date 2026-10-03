package com.example.redstonearsenal.client;

import com.example.redstonearsenal.RedstoneArsenal;
import com.example.redstonearsenal.registry.ModEntities;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;

@EventBusSubscriber(modid = RedstoneArsenal.MODID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ClientModEvents {
    public static final ModelLayerLocation MISSILE_LAYER =
            new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(RedstoneArsenal.MODID, "missile"), "main");
    public static final ModelLayerLocation TANK_LAYER =
            new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(RedstoneArsenal.MODID, "tank"), "main");

    @SubscribeEvent
    public static void registerLayers(EntityRenderersEvent.RegisterLayerDefinitions event) {
        event.registerLayerDefinition(MISSILE_LAYER, MissileModel::createLayer);
        event.registerLayerDefinition(TANK_LAYER, TankModel::createLayer);
    }

    @SubscribeEvent
    public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(ModEntities.MISSILE.get(), MissileRenderer::new);
        event.registerEntityRenderer(ModEntities.TANK.get(), TankRenderer::new);
    }
}
