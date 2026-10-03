package com.example.redstonearsenal.client;

import com.example.redstonearsenal.RedstoneArsenal;
import com.example.redstonearsenal.entity.TankEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;

public class TankRenderer extends EntityRenderer<TankEntity> {
    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(RedstoneArsenal.MODID, "textures/entity/tank.png");
    private final TankModel model;

    public TankRenderer(EntityRendererProvider.Context ctx) {
        super(ctx);
        this.model = new TankModel(ctx.bakeLayer(ClientModEvents.TANK_LAYER));
        this.shadowRadius = 1.1F;
    }

    @Override
    public void render(TankEntity tank, float yaw, float partial, PoseStack ps, MultiBufferSource buf, int light) {
        ps.pushPose();
        ps.mulPose(Axis.YP.rotationDegrees(180.0F - yaw));
        ps.scale(-1.0F, -1.0F, 1.0F);
        ps.translate(0.0F, -1.5F, 0.0F);

        LivingEntity pilot = tank.getControllingPassenger();
        float turretYaw = pilot != null ? Mth.rotLerp(partial, pilot.yRotO, pilot.getYRot()) : tank.getTurretYaw();
        float pitch = pilot != null ? Mth.lerp(partial, pilot.xRotO, pilot.getXRot()) : 0.0F;
        model.setup(Mth.wrapDegrees(turretYaw - yaw), Mth.clamp(pitch, TankEntity.MIN_PITCH, TankEntity.MAX_PITCH));

        VertexConsumer vc = buf.getBuffer(model.renderType(TEXTURE));
        model.renderToBuffer(ps, vc, light, OverlayTexture.NO_OVERLAY, -1);
        ps.popPose();
        super.render(tank, yaw, partial, ps, buf, light);
    }

    @Override
    public ResourceLocation getTextureLocation(TankEntity tank) {
        return TEXTURE;
    }
}
