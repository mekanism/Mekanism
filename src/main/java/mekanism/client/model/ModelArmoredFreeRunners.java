package mekanism.client.model;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import java.util.List;
import javax.annotation.Nonnull;
import mekanism.client.render.MekanismRenderer;
import mekanism.common.Mekanism;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;

public class ModelArmoredFreeRunners extends ModelFreeRunners {

    public static final ModelLayerLocation ARMORED_FREE_RUNNER_LAYER = new ModelLayerLocation(Mekanism.rl("armored_free_runners"), "main");

    private static final ModelPartData PLATE_L = new ModelPartData("PlateL", CubeListBuilder.create()
          .mirror()
          .texOffs(0, 11)
          .addBox(0.5F, 21, -3, 3, 2, 1)
          .texOffs(0, 7)
          .addBox(0.5F, 17, -3, 3, 1, 1)
    );
    private static final ModelPartData PLATE_R = new ModelPartData("PlateR", CubeListBuilder.create()
          .texOffs(0, 11)
          .addBox(-3.5F, 21, -3, 3, 2, 1)
          .texOffs(0, 7)
          .addBox(-3.5F, 17, -3, 3, 1, 1)
    );
    private static final ModelPartData TOP_PLATE_L = new ModelPartData("TopPlateL", CubeListBuilder.create()
          .mirror()
          .texOffs(12, 7)
          .addBox(0, 0, -0.25F, 2, 2, 1),
          PartPose.offsetAndRotation(1, 16, -2, -0.7854F, 0, 0));
    private static final ModelPartData TOP_PLATE_R = new ModelPartData("TopPlateR", CubeListBuilder.create()
          .texOffs(12, 7)
          .addBox(-2, 0, -0.25F, 2, 2, 1),
          PartPose.offsetAndRotation(-1, 16, -2, -0.7854F, 0, 0));
    private static final ModelPartData CONNECTION_L = new ModelPartData("ConnectionL", CubeListBuilder.create()
          .mirror()
          .texOffs(8, 7)
          .addBox(2.5F, 18, -3, 1, 3, 1)
          .texOffs(8, 7)
          .addBox(0.5F, 18, -3, 1, 3, 1));
    private static final ModelPartData CONNECTION_R = new ModelPartData("ConnectionR", CubeListBuilder.create()
          .texOffs(8, 7)
          .addBox(-1.5F, 18, -3, 1, 3, 1)
          .texOffs(8, 7)
          .addBox(-3.5F, 18, -3, 1, 3, 1));
    private static final ModelPartData ARMORED_BRACE_L = new ModelPartData("ArmoredBraceL", CubeListBuilder.create()
          .texOffs(10, 12)
          .addBox(0.2F, 17, -2.3F, 4, 1, 1)
          .texOffs(8, 10)
          .addBox(0.2F, 21, -2.3F, 4, 1, 3));
    private static final ModelPartData ARMORED_BRACE_R = new ModelPartData("ArmoredBraceR", CubeListBuilder.create()
          .mirror()
          .texOffs(10, 12)
          .addBox(-4.2F, 17, -2.3F, 4, 1, 1)
          .texOffs(8, 10)
          .addBox(-4.2F, 21, -2.3F, 4, 1, 3));
    private static final ModelPartData BATTERY_L = new ModelPartData("BatteryL", CubeListBuilder.create()
          .texOffs(22, 11)
          .addBox(1.5F, 18, -3, 1, 2, 1));
    private static final ModelPartData BATTERY_R = new ModelPartData("BatteryR", CubeListBuilder.create()
          .texOffs(22, 11)
          .addBox(-2.5F, 18, -3, 1, 2, 1));

    public static LayerDefinition createLayerDefinition() {
        return createLayerDefinition(64, 32, SPRING_L, SPRING_R, BRACE_L, BRACE_R, SUPPORT_L, SUPPORT_R, PLATE_L, PLATE_R, TOP_PLATE_L, TOP_PLATE_R,
              CONNECTION_L, CONNECTION_R, ARMORED_BRACE_L, ARMORED_BRACE_R, BATTERY_L, BATTERY_R);
    }

    private final List<ModelPart> litLeftParts;
    private final List<ModelPart> litRightParts;

    public ModelArmoredFreeRunners(EntityModelSet entityModelSet) {
        this(entityModelSet.bakeLayer(ARMORED_FREE_RUNNER_LAYER));
    }

    private ModelArmoredFreeRunners(ModelPart root) {
        super(root);
        leftParts.addAll(getRenderableParts(root, PLATE_L, TOP_PLATE_L, CONNECTION_L, ARMORED_BRACE_L));
        rightParts.addAll(getRenderableParts(root, PLATE_R, TOP_PLATE_R, CONNECTION_R, ARMORED_BRACE_R));
        litLeftParts = getRenderableParts(root, BATTERY_L);
        litRightParts = getRenderableParts(root, BATTERY_R);
    }

    @Override
    protected void renderLeg(@Nonnull PoseStack poseStack, @Nonnull VertexConsumer vertexConsumer, int light, int overlayLight, float red, float green, float blue,
          float alpha, boolean left) {
        super.renderLeg(poseStack, vertexConsumer, light, overlayLight, red, green, blue, alpha, left);
        if (left) {
            renderPartsToBuffer(litLeftParts, poseStack, vertexConsumer, MekanismRenderer.FULL_LIGHT, overlayLight, red, green, blue, alpha);
        } else {
            renderPartsToBuffer(litRightParts, poseStack, vertexConsumer, MekanismRenderer.FULL_LIGHT, overlayLight, red, green, blue, alpha);
        }
    }
}