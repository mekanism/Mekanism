package mekanism.additions.client.model;

import java.util.Set;
import mekanism.additions.common.MekanismAdditions;
import net.minecraft.client.model.BabyModelTransform;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.model.monster.creeper.CreeperModel;

public class ModelBabyCreeper extends CreeperModel {

    public static final ModelLayerLocation CREEPER_LAYER = new ModelLayerLocation(MekanismAdditions.rl("baby_creeper"), "main");
    public static final ModelLayerLocation ARMOR_LAYER = new ModelLayerLocation(MekanismAdditions.rl("baby_creeper"), "armor");

    //adapted from old AgeableListModel
    private static final BabyModelTransform BABY_MODEL_TRANSFORM = new BabyModelTransform(false, 5.0F, 2.0F, 2.0F, 2.0F, 24.0F, Set.of("head"));

    public static LayerDefinition createBodyLayer(CubeDeformation cubeDeformation) {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        root.addOrReplaceChild("head", CubeListBuilder.create()
                    .texOffs(0, 0)
                    .addBox(-4.0F, -8.0F, -4.0F, 8.0F, 8.0F, 8.0F, cubeDeformation),
              //Only real difference between this model and the vanilla creeper model is the "fix" for the head's rotation point
              PartPose.offset(0, 10, -2));
        root.addOrReplaceChild("body", CubeListBuilder.create()
                    .texOffs(16, 16)
                    .addBox(-4.0F, 0.0F, -2.0F, 8.0F, 12.0F, 4.0F, cubeDeformation),
              PartPose.offset(0.0F, 6.0F, 0.0F));
        CubeListBuilder cubelistbuilder = CubeListBuilder.create()
              .texOffs(0, 16)
              .addBox(-2.0F, 0.0F, -2.0F, 4.0F, 6.0F, 4.0F, cubeDeformation);
        root.addOrReplaceChild("right_hind_leg", cubelistbuilder, PartPose.offset(-2.0F, 18.0F, 4.0F));
        root.addOrReplaceChild("left_hind_leg", cubelistbuilder, PartPose.offset(2.0F, 18.0F, 4.0F));
        root.addOrReplaceChild("right_front_leg", cubelistbuilder, PartPose.offset(-2.0F, 18.0F, -4.0F));
        root.addOrReplaceChild("left_front_leg", cubelistbuilder, PartPose.offset(2.0F, 18.0F, -4.0F));
        return LayerDefinition.create(mesh, 64, 32)
              .apply(BABY_MODEL_TRANSFORM);//todo - 26.1: test this is the correct way to transform it
    }

    public ModelBabyCreeper(ModelPart root) {
        super(root);
    }

    //these now seem the same as the parent
    /*@Override
    public void setupAnim(EntityBabyCreeper creeper, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        this.head.yRot = netHeadYaw * Mth.DEG_TO_RAD;
        this.head.xRot = headPitch * Mth.DEG_TO_RAD;
        this.leftHindLeg.xRot = Mth.cos(limbSwing * 0.6662F) * 1.4F * limbSwingAmount;
        this.rightHindLeg.xRot = Mth.cos(limbSwing * 0.6662F + Mth.PI) * 1.4F * limbSwingAmount;
        this.leftFrontLeg.xRot = Mth.cos(limbSwing * 0.6662F + Mth.PI) * 1.4F * limbSwingAmount;
        this.rightFrontLeg.xRot = Mth.cos(limbSwing * 0.6662F) * 1.4F * limbSwingAmount;
    }*/
}