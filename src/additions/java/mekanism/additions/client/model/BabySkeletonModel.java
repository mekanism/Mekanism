package mekanism.additions.client.model;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.model.monster.skeleton.SkeletonModel;
import net.minecraft.client.renderer.entity.state.SkeletonRenderState;
import net.minecraft.world.entity.HumanoidArm;

public class BabySkeletonModel<STATE extends SkeletonRenderState> extends SkeletonModel<STATE> {

    public BabySkeletonModel(ModelPart root) {
        super(root);
    }

    @Override
    public void translateToHand(SkeletonRenderState state, HumanoidArm arm, PoseStack poseStack) {
        //Note: Unlike super this does not require any custom offset before translating and rotating the arm
        root().translateAndRotate(poseStack);
        getArm(arm).translateAndRotate(poseStack);
    }

    protected static void createDefaultSkeletonMesh(PartDefinition root) {
        PartDefinition head = root.addOrReplaceChild("head", CubeListBuilder.create()
                    .addBox(-3, -8, -3, 6, 6, 6),
              PartPose.offset(0, 17, 0)
        );
        //The vanilla models expect a hat, though they just use an empty part for baby zombies, so we can do the same here
        head.addOrReplaceChild("hat", CubeListBuilder.create(), PartPose.ZERO);

        root.addOrReplaceChild("body", CubeListBuilder.create()
                    .texOffs(8, 12)
                    .addBox(-2, 0, -1, 4, 5, 2),
              PartPose.offset(0, 15, 0)
        );

        root.addOrReplaceChild("right_arm", CubeListBuilder.create()
                    .texOffs(24, 12)
                    .addBox(0, -2, -0.5F, 1, 5, 1),
              PartPose.offset(-3, 17, 0)
        );
        root.addOrReplaceChild("left_arm", CubeListBuilder.create()
                    .texOffs(20, 12)
                    .addBox(-1, -2, -0.5F, 1, 5, 1),
              PartPose.offset(3, 17, 0)
        );

        root.addOrReplaceChild("right_leg", CubeListBuilder.create()
                    .texOffs(0, 12)
                    .addBox(-0.5F, 0, -0.5F, 1, 4, 1),
              PartPose.offset(-1, 20, 0)
        );
        root.addOrReplaceChild("left_leg", CubeListBuilder.create()
                    .texOffs(4, 12)
                    .addBox(-0.5F, 0, -0.5F, 1, 4, 1),
              PartPose.offset(1, 20, 0)
        );
    }

    public static LayerDefinition createSkeletonBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        createDefaultSkeletonMesh(mesh.getRoot());
        return LayerDefinition.create(mesh, 32, 32);
    }

    public static LayerDefinition createParchedBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        createDefaultSkeletonMesh(root);

        root.getChild("head")
              .addOrReplaceChild("wrap", CubeListBuilder.create()
                          .texOffs(24, 0)
                          .addBox(-3, -15, -3, 6, 6, 6, new CubeDeformation(0.25F)),
                    PartPose.offset(0, 8.75F - 1.75F, 0)
              );
        root.getChild("body")
              .addOrReplaceChild("wrap", CubeListBuilder.create()
                          .texOffs(28, 12)
                          .addBox(-2, -4, -1, 4, 2, 2, new CubeDeformation(0.025F)),
                    PartPose.offset(0, 6.5F + 2.5F, 0)
              );
        return LayerDefinition.create(mesh, 64, 32);
    }

    public static LayerDefinition createStrayBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        createDefaultSkeletonMesh(root);

        root.getChild("body")
              .addOrReplaceChild("skirt", CubeListBuilder.create()
                    .texOffs(18, 1)
                    .addBox(-2, -4, -1, 4, 3, 2, new CubeDeformation(0.025F)),
              PartPose.offset(0, 6.5F + 2.5F, 0)
        );
        return LayerDefinition.create(mesh, 32, 32);
    }
}