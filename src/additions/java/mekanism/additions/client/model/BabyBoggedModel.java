package mekanism.additions.client.model;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.model.monster.skeleton.BoggedModel;
import net.minecraft.client.renderer.entity.state.SkeletonRenderState;
import net.minecraft.world.entity.HumanoidArm;

public class BabyBoggedModel extends BoggedModel {

    public BabyBoggedModel(ModelPart root) {
        super(root);
    }

    @Override
    public void translateToHand(SkeletonRenderState state, HumanoidArm arm, PoseStack poseStack) {
        //Note: Unlike super this does not require any custom offset before translating and rotating the arm
        root().translateAndRotate(poseStack);
        getArm(arm).translateAndRotate(poseStack);
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        BabySkeletonModel.createDefaultSkeletonMesh(root);

        root.getChild("head")
              .addOrReplaceChild("mushrooms", CubeListBuilder.create()
                          .texOffs(18, 0)
                          .addBox(-1.5F, -1.5F, 0, 3, 3, 0)
                          .texOffs(18, -3)
                          .addBox(0, -1.5F, -1.5F, 0, 3, 3),
                    PartPose.offsetAndRotation(1.5F, -7.5F - 1.75F, 1.5F, 0, -0.7854F, 0)
              );
        return LayerDefinition.create(mesh, 32, 32);
    }
}