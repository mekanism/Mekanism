package mekanism.additions.client.model;

import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.model.monster.creeper.CreeperModel;

public class BabyCreeperModel extends CreeperModel {

    public BabyCreeperModel(ModelPart root) {
        super(root);
    }

    public static LayerDefinition createBodyLayer(CubeDeformation g) {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        root.addOrReplaceChild("head", CubeListBuilder.create()
                    .addBox(-3, -8, -3, 6, 6, 6, g),
              PartPose.offset(0, 16, 0)
        );

        root.addOrReplaceChild("body", CubeListBuilder.create()
                    .texOffs(0, 12)
                    .addBox(-4, 0, -2, 4, 6, 2, g),
              PartPose.offset(2, 14, 1)
        );
        root.addOrReplaceChild("right_hind_leg", CubeListBuilder.create()
                    .texOffs(16, 18)
                    .addBox(-2, 0, -2, 2, 4, 2, g),
              PartPose.offset(0, 20, 3)
        );
        root.addOrReplaceChild("left_hind_leg", CubeListBuilder.create()
                    .texOffs(16, 12)
                    .addBox(-2, 0, -2, 2, 4, 2, g),
              PartPose.offset(2, 20, 3)
        );
        root.addOrReplaceChild("right_front_leg", CubeListBuilder.create()
                    .texOffs(0, 20)
                    .addBox(-2, 0, -2, 2, 4, 2, g),
              PartPose.offset(0, 20, -1)
        );
        root.addOrReplaceChild("left_front_leg", CubeListBuilder.create()
                    .texOffs(8, 20)
                    .addBox(-2, 0, -2, 2, 4, 2, g),
              PartPose.offset(2, 20, -1)
        );
        return LayerDefinition.create(mesh, 32, 32);
    }
}