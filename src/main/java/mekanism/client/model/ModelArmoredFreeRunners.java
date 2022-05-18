package mekanism.client.model;

import mekanism.common.Mekanism;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;

public class ModelArmoredFreeRunners extends ModelFreeRunners {

    public static final ModelLayerLocation ARMORED_FREE_RUNNER_LAYER = new ModelLayerLocation(Mekanism.rl("armored_free_runners"), "main");

    private static final ModelPartData FRONT_PLATE_L = new ModelPartData("FrontPlateL", CubeListBuilder.create()
          .mirror()
          .texOffs(1, 22)
          .addBox(0.5F, -10, -3, 3, 5, 1)
          .texOffs(1, 29)
          .addBox(0.5F, -3, -3, 3, 2, 1),
          PartPose.offset(0, 24, 0),
          //Children
          new ModelPartData("UpperPlate", CubeListBuilder.create()
                .texOffs(14, 29)
                .mirror()
                .addBox(0, 0, -0.25F, 2, 2, 1),
                PartPose.offsetAndRotation(1, -11, -2, -0.7854F, 0, 0)
          )
    );
    private static final ModelPartData FRONT_PLATE_R = new ModelPartData("FrontPlateR", CubeListBuilder.create()
          .texOffs(1, 22)
          .addBox(-3.5F, -10, -3, 3, 5, 1)
          .texOffs(1, 29)
          .addBox(-3.5F, -3, -3, 3, 2, 1),
          PartPose.offset(0, 24, 0),
          //Children
          new ModelPartData("UpperPlate", CubeListBuilder.create()
                .texOffs(14, 29)
                .addBox(-2, 0, -0.25F, 2, 2, 1),
                PartPose.offsetAndRotation(-1, -11, -2, -0.7854F, 0, 0)
          )
    );
    private static final ModelPartData CONNECTION_L = new ModelPartData("ConnectionL", CubeListBuilder.create()
          .mirror()
          .texOffs(10, 29)
          .addBox(2.5F, -5, -3, 1, 2, 1)
          .addBox(0.5F, -5, -3, 1, 2, 1),
          PartPose.offset(0, 24, 0));
    private static final ModelPartData CONNECTION_R = new ModelPartData("ConnectionR", CubeListBuilder.create()
          .texOffs(10, 29)
          .addBox(-1.5F, -5, -3, 1, 2, 1)
          .addBox(-3.5F, -5, -3, 1, 2, 1),
          PartPose.offset(0, 24, 0));
    private static final ModelPartData ARMORED_BRACE_L = new ModelPartData("ArmoredBraceL", CubeListBuilder.create()
          .texOffs(12, 0)
          .addBox(0.2F, 15, -2.3F, 4, 2, 3));
    private static final ModelPartData ARMORED_BRACE_R = new ModelPartData("ArmoredBraceR", CubeListBuilder.create()
          .texOffs(12, 0)
          .mirror()
          .addBox(-4.2F, 15, -2.3F, 4, 2, 3));

    public static LayerDefinition createLayerDefinition() {
        return createLayerDefinition(64, 32, SPRING_L, SPRING_R, BRACE_L, BRACE_R, SUPPORT_L, SUPPORT_R, FRONT_PLATE_L, FRONT_PLATE_R,
              CONNECTION_L, CONNECTION_R, ARMORED_BRACE_L, ARMORED_BRACE_R);
    }

    public ModelArmoredFreeRunners(EntityModelSet entityModelSet) {
        this(entityModelSet.bakeLayer(ARMORED_FREE_RUNNER_LAYER));
    }

    private ModelArmoredFreeRunners(ModelPart root) {
        super(root);
        leftParts.addAll(getRenderableParts(root, FRONT_PLATE_L, CONNECTION_L, ARMORED_BRACE_L));
        rightParts.addAll(getRenderableParts(root, FRONT_PLATE_R, CONNECTION_R, ARMORED_BRACE_R));
    }
}