package com.hollowguest.client;

import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.util.Mth;

/**
 * Blocky model, 3.25 blocks tall. Skin is a 64x80 texture (see tools/gen_texture.py).
 * Units are pixels: 16 pixels = 1 block, y = 24 is the ground.
 */
public class HollowGuestModel extends EntityModel<HollowGuestRenderState> {
    private final ModelPart head;
    private final ModelPart rightArm;
    private final ModelPart leftArm;
    private final ModelPart rightLeg;
    private final ModelPart leftLeg;

    public HollowGuestModel(ModelPart root) {
        super(root);
        this.head = root.getChild("head");
        this.rightArm = root.getChild("right_arm");
        this.leftArm = root.getChild("left_arm");
        this.rightLeg = root.getChild("right_leg");
        this.leftLeg = root.getChild("left_leg");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();

        root.addOrReplaceChild("head",
            CubeListBuilder.create().texOffs(0, 0).addBox(-4.0F, -8.0F, -4.0F, 8.0F, 8.0F, 8.0F),
            PartPose.offset(0.0F, -20.0F, 0.0F));
        root.addOrReplaceChild("body",
            CubeListBuilder.create().texOffs(0, 16).addBox(-4.0F, 0.0F, -2.0F, 8.0F, 16.0F, 4.0F),
            PartPose.offset(0.0F, -20.0F, 0.0F));
        root.addOrReplaceChild("right_arm",
            CubeListBuilder.create().texOffs(32, 0).addBox(-1.5F, 0.0F, -1.5F, 3.0F, 30.0F, 3.0F),
            PartPose.offset(-5.5F, -18.0F, 0.0F));
        root.addOrReplaceChild("left_arm",
            CubeListBuilder.create().texOffs(44, 0).addBox(-1.5F, 0.0F, -1.5F, 3.0F, 30.0F, 3.0F),
            PartPose.offset(5.5F, -18.0F, 0.0F));
        root.addOrReplaceChild("right_leg",
            CubeListBuilder.create().texOffs(0, 40).addBox(-1.5F, 0.0F, -1.5F, 3.0F, 28.0F, 3.0F),
            PartPose.offset(-2.5F, -4.0F, 0.0F));
        root.addOrReplaceChild("left_leg",
            CubeListBuilder.create().texOffs(12, 40).addBox(-1.5F, 0.0F, -1.5F, 3.0F, 28.0F, 3.0F),
            PartPose.offset(2.5F, -4.0F, 0.0F));

        return LayerDefinition.create(mesh, 64, 80);
    }

    @Override
    public void setupAnim(HollowGuestRenderState state) {
        super.setupAnim(state);
        this.head.yRot = state.yRot * Mth.DEG_TO_RAD;
        this.head.xRot = state.xRot * Mth.DEG_TO_RAD;

        float swing = Mth.cos(state.walkAnimationPos * 0.6662F) * state.walkAnimationSpeed;
        this.rightLeg.xRot = swing * 0.8F;
        this.leftLeg.xRot = -swing * 0.8F;
        this.rightArm.xRot = -swing * 0.5F;
        this.leftArm.xRot = swing * 0.5F;

        float sway = Mth.sin(state.ageInTicks * 0.05F) * 0.04F;
        this.rightArm.zRot = 0.04F + sway;
        this.leftArm.zRot = -0.04F - sway;

        if (state.noticed) {
            // Head tilts sideways once it's hunting you.
            this.head.zRot = 0.25F;
        }
        if (state.attacking) {
            this.rightArm.xRot = -1.4F;
            this.leftArm.xRot = -1.4F;
        }
    }
}
