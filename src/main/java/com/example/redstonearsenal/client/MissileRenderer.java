package com.example.redstonearsenal.client;

import com.example.redstonearsenal.RedstoneArsenal;
import com.example.redstonearsenal.entity.MissileEntity;
import com.example.redstonearsenal.entity.MissileType;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;
import org.joml.Quaternionf;
import org.joml.Vector3f;

public class MissileRenderer extends EntityRenderer<MissileEntity> {
    private final MissileModel model;

    public MissileRenderer(EntityRendererProvider.Context ctx) {
        super(ctx);
        this.model = new MissileModel(ctx.bakeLayer(ClientModEvents.MISSILE_LAYER));
        this.shadowRadius = 0.0F;
    }

    @Override
    public void render(MissileEntity e, float yaw, float partial, PoseStack ps, MultiBufferSource buf, int light) {
        ps.pushPose();
        Vec3 d = e.getDir();
        ps.mulPose(new Quaternionf().rotationTo(new Vector3f(0, 1, 0), new Vector3f((float) d.x, (float) d.y, (float) d.z)));
        if (e.getMissileType() == MissileType.SHELL) ps.scale(0.35F, 0.35F, 0.35F);
        ps.scale(-1.0F, -1.0F, 1.0F);
        VertexConsumer vc = buf.getBuffer(model.renderType(getTextureLocation(e)));
        model.renderToBuffer(ps, vc, light, OverlayTexture.NO_OVERLAY, -1);
        ps.popPose();
        super.render(e, yaw, partial, ps, buf, light);
    }

    @Override
    public ResourceLocation getTextureLocation(MissileEntity e) {
        return ResourceLocation.fromNamespaceAndPath(RedstoneArsenal.MODID,
                "textures/entity/missile_" + e.getMissileType().textureName() + ".png");
    }
}
