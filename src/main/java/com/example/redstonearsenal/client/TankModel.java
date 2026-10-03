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
import net.minecraft.util.Mth;

/** Model-space y=24 is the ground. UV layout matches tools/gen_textures.py (128x128). */
public class TankModel extends Model {
    private final ModelPart root;
    private final ModelPart turret;
    private final ModelPart barrel;

    public TankModel(ModelPart root) {
        super(RenderType::entityCutoutNoCull);
        this.root = root;
        this.turret = root.getChild("turret");
        this.barrel = turret.getChild("barrel");
    }

    public static LayerDefinition createLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition p = mesh.getRoot();
        p.addOrReplaceChild("track_l", CubeListBuilder.create().texOffs(0, 0).addBox(-14, 16, -16, 6, 8, 32), PartPose.ZERO);
        p.addOrReplaceChild("track_r", CubeListBuilder.create().texOffs(0, 0).addBox(8, 16, -16, 6, 8, 32), PartPose.ZERO);
        p.addOrReplaceChild("hull", CubeListBuilder.create().texOffs(0, 40).addBox(-8, 12, -15, 16, 10, 30), PartPose.ZERO);

        PartDefinition turret = p.addOrReplaceChild("turret",
                CubeListBuilder.create().texOffs(0, 80).addBox(-6, -6, -7, 12, 6, 14),
                PartPose.offset(0, 12, 0));
        turret.addOrReplaceChild("hatch", CubeListBuilder.create().texOffs(0, 100).addBox(-2.5F, -8, 0, 5, 2, 5), PartPose.ZERO);
        turret.addOrReplaceChild("barrel",
                CubeListBuilder.create().texOffs(60, 80).addBox(-1.5F, -1.5F, -16, 3, 3, 16),
                PartPose.offset(0, -3, -7));
        return LayerDefinition.create(mesh, 128, 128);
    }

    /** @param relativeYawDeg turret yaw relative to the hull, @param pitchDeg gun pitch (positive = down) */
    public void setup(float relativeYawDeg, float pitchDeg) {
        turret.yRot = relativeYawDeg * Mth.DEG_TO_RAD;
        barrel.xRot = pitchDeg * Mth.DEG_TO_RAD;
    }

    @Override
    public void renderToBuffer(PoseStack ps, VertexConsumer vc, int light, int overlay, int color) {
        root.render(ps, vc, light, overlay, color);
    }
}
