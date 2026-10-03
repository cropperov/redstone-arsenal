package com.example.redstonearsenal.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.renderer.RenderType;

/** Missile pointing "up" (+Y) once the renderer's flip is applied. UV layout matches tools/gen_textures.py */
public class MissileModel extends Model {
    private final ModelPart root;

    public MissileModel(ModelPart root) {
        super(RenderType::entityCutoutNoCull);
        this.root = root;
    }

    public static LayerDefinition createLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition p = mesh.getRoot();
        p.addOrReplaceChild("body", CubeListBuilder.create().texOffs(0, 0).addBox(-2, -20, -2, 4, 20, 4), PartPose.ZERO);
        p.addOrReplaceChild("nose", CubeListBuilder.create().texOffs(16, 0).addBox(-1.5F, -26, -1.5F, 3, 6, 3), PartPose.ZERO);
        p.addOrReplaceChild("fin_a", CubeListBuilder.create().texOffs(32, 0).addBox(-6, -5, -0.5F, 12, 5, 1), PartPose.ZERO);
        p.addOrReplaceChild("fin_b", CubeListBuilder.create().texOffs(32, 8).addBox(-0.5F, -5, -6, 1, 5, 12), PartPose.ZERO);
        return LayerDefinition.create(mesh, 64, 32);
    }

    @Override
    public void renderToBuffer(PoseStack ps, VertexConsumer vc, int light, int overlay, int color) {
        root.render(ps, vc, light, overlay, color);
    }
}
