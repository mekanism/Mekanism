package mekanism.additions.client.model;

import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.model.monster.enderman.EndermanModel;
import net.minecraft.client.renderer.entity.state.EndermanRenderState;

public class BabyEndermanModel extends EndermanModel<EndermanRenderState> {

    public BabyEndermanModel(ModelPart part) {
        super(part);
    }

    @Override
    public void setupAnim(EndermanRenderState state) {
        super.setupAnim(state);
        if (state.isCreepy) {
            //Shrink how much the head opens
            float amt = 2;
            this.head.y += amt;
            this.hat.y -= amt;
        }
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        PartDefinition head = root.addOrReplaceChild("head", CubeListBuilder.create()
                    .addBox(-4, -8, -4, 6, 6, 6),
              PartPose.offset(1, 4, 1)
        );
        head.addOrReplaceChild("hat", CubeListBuilder.create()
                    .texOffs(24, 0)
                    .addBox(-4, -8, -4, 6, 6, 6, new CubeDeformation(-0.5F)),
              PartPose.ZERO
        );

        root.addOrReplaceChild("body", CubeListBuilder.create()
                    .texOffs(0, 12)
                    .addBox(-4, 0, -2, 6, 8, 4),
              PartPose.offset(1, 1, 0)
        );

        root.addOrReplaceChild("right_arm", CubeListBuilder.create()
                    .texOffs(20, 12)
                    .addBox(-1, 0, -1, 2, 15, 2),
              PartPose.offset(-4, 2, 0)
        );
        root.addOrReplaceChild("left_arm", CubeListBuilder.create()
                    .texOffs(28, 12)
                    .mirror()
                    .addBox(-1, 0, -1, 2, 15, 2),
              PartPose.offset(4, 2, 0F)
        );

        root.addOrReplaceChild("right_leg", CubeListBuilder.create()
                    .texOffs(36, 12)
                    .addBox(-1, 0, -1, 2, 15, 2),
              PartPose.offset(-2, 9, 0)
        );
        root.addOrReplaceChild("left_leg", CubeListBuilder.create()
                    .texOffs(44, 12)
                    .mirror()
                    .addBox(-1, 0, -1, 2, 15, 2),
              PartPose.offset(2, 9, 0)
        );
        return LayerDefinition.create(mesh, 64, 32);
    }
}