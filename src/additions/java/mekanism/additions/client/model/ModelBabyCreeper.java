package mekanism.additions.client.model;

import com.google.common.collect.ImmutableList;
import javax.annotation.Nonnull;
import mekanism.additions.common.MekanismAdditions;
import mekanism.additions.common.entity.baby.EntityBabyCreeper;
import net.minecraft.client.model.AgeableListModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.util.Mth;

public class ModelBabyCreeper extends AgeableListModel<EntityBabyCreeper> {

    public static final ModelLayerLocation CREEPER_LAYER = new ModelLayerLocation(MekanismAdditions.rl("baby_creeper"), "main");
    public static final ModelLayerLocation ARMOR_LAYER = new ModelLayerLocation(MekanismAdditions.rl("baby_creeper"), "armor");

    public static LayerDefinition createBodyLayer(CubeDeformation cubeDeformation) {
        MeshDefinition meshDefinition = new MeshDefinition();
        PartDefinition partDefinition = meshDefinition.getRoot();
        partDefinition.addOrReplaceChild("head", CubeListBuilder.create()
                    .texOffs(0, 0)
                    .addBox(-4.0F, -8.0F, -4.0F, 8.0F, 8.0F, 8.0F, cubeDeformation),
              //Only real difference between this model and the vanilla creeper model is the "fix" for the head's rotation point
              // the other difference is extending ageable model instead
              PartPose.offset(0, 10, -2));
        partDefinition.addOrReplaceChild("body", CubeListBuilder.create()
                    .texOffs(16, 16)
                    .addBox(-4.0F, 0.0F, -2.0F, 8.0F, 12.0F, 4.0F, cubeDeformation),
              PartPose.offset(0.0F, 6.0F, 0.0F));
        CubeListBuilder cubelistbuilder = CubeListBuilder.create()
              .texOffs(0, 16)
              .addBox(-2.0F, 0.0F, -2.0F, 4.0F, 6.0F, 4.0F, cubeDeformation);
        partDefinition.addOrReplaceChild("right_hind_leg", cubelistbuilder, PartPose.offset(-2.0F, 18.0F, 4.0F));
        partDefinition.addOrReplaceChild("left_hind_leg", cubelistbuilder, PartPose.offset(2.0F, 18.0F, 4.0F));
        partDefinition.addOrReplaceChild("right_front_leg", cubelistbuilder, PartPose.offset(-2.0F, 18.0F, -4.0F));
        partDefinition.addOrReplaceChild("left_front_leg", cubelistbuilder, PartPose.offset(2.0F, 18.0F, -4.0F));
        return LayerDefinition.create(meshDefinition, 64, 32);
    }

    private final ModelPart head;
    private final ModelPart body;
    private final ModelPart leftHindLeg;
    private final ModelPart rightHindLeg;
    private final ModelPart leftFrontLeg;
    private final ModelPart rightFrontLeg;

    public ModelBabyCreeper(ModelPart root) {//TODO - 1.18: Test this
        /*this.head = new ModelPart(this, 0, 0);
        this.head.addBox(-4, -8, -4, 8, 8, 8, size);
        //Only real difference between this model and the vanilla creeper model is the "fix" for the head's rotation point
        // the other difference is extending ageable model instead
        this.head.setPos(0, 10, -2);
        this.body = new ModelPart(this, 16, 16);
        this.body.addBox(-4, 0, -2, 8, 12, 4, size);
        this.body.setPos(0, 6, 0);
        this.leftHindLeg = new ModelPart(this, 0, 16);
        this.leftHindLeg.addBox(-2, 0, -2, 4, 6, 4, size);
        this.leftHindLeg.setPos(-2, 18, 4);
        this.rightHindLeg = new ModelPart(this, 0, 16);
        this.rightHindLeg.addBox(-2, 0, -2, 4, 6, 4, size);
        this.rightHindLeg.setPos(2, 18, 4);
        this.leftFrontLeg = new ModelPart(this, 0, 16);
        this.leftFrontLeg.addBox(-2, 0, -2, 4, 6, 4, size);
        this.leftFrontLeg.setPos(-2, 18, -4);
        this.rightFrontLeg = new ModelPart(this, 0, 16);
        this.rightFrontLeg.addBox(-2, 0, -2, 4, 6, 4, size);
        this.rightFrontLeg.setPos(2, 18, -4);*/
        this.head = root.getChild("head");
        this.body = root.getChild("body");
        this.leftHindLeg = root.getChild("right_hind_leg");
        this.rightHindLeg = root.getChild("left_hind_leg");
        this.leftFrontLeg = root.getChild("right_front_leg");
        this.rightFrontLeg = root.getChild("left_front_leg");
    }

    @Nonnull
    @Override
    protected Iterable<ModelPart> headParts() {
        return ImmutableList.of(this.head);
    }

    @Nonnull
    @Override
    protected Iterable<ModelPart> bodyParts() {
        return ImmutableList.of(this.body, this.leftHindLeg, this.rightHindLeg, this.leftFrontLeg, this.rightFrontLeg);
    }

    @Override
    public void setupAnim(@Nonnull EntityBabyCreeper creeper, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        this.head.yRot = netHeadYaw * ((float) Math.PI / 180F);
        this.head.xRot = headPitch * ((float) Math.PI / 180F);
        this.leftHindLeg.xRot = Mth.cos(limbSwing * 0.6662F) * 1.4F * limbSwingAmount;
        this.rightHindLeg.xRot = Mth.cos(limbSwing * 0.6662F + (float) Math.PI) * 1.4F * limbSwingAmount;
        this.leftFrontLeg.xRot = Mth.cos(limbSwing * 0.6662F + (float) Math.PI) * 1.4F * limbSwingAmount;
        this.rightFrontLeg.xRot = Mth.cos(limbSwing * 0.6662F) * 1.4F * limbSwingAmount;
    }
}