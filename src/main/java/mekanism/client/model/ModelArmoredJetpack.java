package mekanism.client.model;

import mekanism.common.Mekanism;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;

public class ModelArmoredJetpack extends ModelJetpack {

    public static final ModelLayerLocation ARMORED_JETPACK_LAYER = new ModelLayerLocation(Mekanism.rl("armored_jetpack"), "main");
    public static final ModelLayerLocation ARMORED_JETPACK_BABY_LAYER = new ModelLayerLocation(Mekanism.rl("armored_jetpack_baby"), "main");

    public static LayerDefinition createLayerDefinition() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        createDefaultMesh(root, -1.9F);

        PartDefinition mainParts = root.getChild("main");
        PartPose armoredOffset = PartPose.offset(0, 0, -0.0625F);

        PartDefinition armoredParts = mainParts.addOrReplaceChild("armored", CubeListBuilder.create(), armoredOffset);

        armoredParts.addOrReplaceChild("chestplate", CubeListBuilder.create()
                    .texOffs(104, 22)
                    .addBox(-4, 1.333333F, -3, 8, 4, 3),
              PartPose.rotation(-0.3665191F, 0, 0)
        );
        armoredParts.addOrReplaceChild("middlePlate", CubeListBuilder.create()
                    .texOffs(93, 20)
                    .addBox(-1.5F, 3, -6.2F, 3, 5, 3)
                    //Top guards
                    .texOffs(87, 31)
                    .addBox(0.95F, 3, -5, 3, 4, 2)//Left
                    .addBox(-3.95F, 3, -5, 3, 4, 2)//Right
              ,
              PartPose.rotation(0.2094395F, 0, 0)
        );
        armoredParts.addOrReplaceChild("bottom_guards", CubeListBuilder.create()
                    .texOffs(84, 30)
                    //Left
                    .addBox(1.5F, 5.5F, -6.5F, 2, 2, 2)
                    //Right
                    .addBox(-3.5F, 5.5F, -6.5F, 2, 2, 2),
              PartPose.rotation(0.4712389F, 0, 0)
        );

        PartDefinition litParts = root.getChild("lit");
        litParts.addOrReplaceChild("lights_armored", CubeListBuilder.create()
                    .texOffs(81, 0)
                    .addBox(-3, 4, -4.5F, 1, 3, 1)
                    .addBox(2, 4, -4.5F, 1, 3, 1),
              armoredOffset
        );

        return LayerDefinition.create(mesh, 128, 64);
    }

    public ModelArmoredJetpack(EntityModelSet entityModelSet) {
        this(entityModelSet.bakeLayer(ARMORED_JETPACK_LAYER));
    }

    public ModelArmoredJetpack(ModelPart root) {
        super(root);
    }
}