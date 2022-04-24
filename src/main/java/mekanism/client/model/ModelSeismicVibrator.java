package mekanism.client.model;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import java.util.List;
import javax.annotation.Nonnull;
import mekanism.common.Mekanism;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.ResourceType;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;

public class ModelSeismicVibrator extends MekanismJavaModel {

    public static final ModelLayerLocation VIBRATOR_LAYER = new ModelLayerLocation(Mekanism.rl("seismic_vibrator"), "main");
    private static final ResourceLocation VIBRATOR_TEXTURE = MekanismUtils.getResource(ResourceType.RENDER, "seismic_vibrator.png");

    private static final ModelPartData PLATE_3 = new ModelPartData("plate3", CubeListBuilder.create()
          .texOffs(36, 42)
          .addBox(0F, 0F, 0F, 8, 2, 8),
          PartPose.offset(-4F, 22F, -4F));
    private static final ModelPartData BASE_BACK = new ModelPartData("baseBack", CubeListBuilder.create()
          .texOffs(0, 26)
          .addBox(0F, 0F, 0F, 16, 5, 3),
          PartPose.offset(-8F, 19F, 5F));
    private static final ModelPartData MOTOR = new ModelPartData("motor", CubeListBuilder.create()
          .texOffs(76, 13)
          .addBox(0F, 0F, 0F, 6, 4, 10),
          PartPose.offset(-3F, -5F, -3F));
    private static final ModelPartData PORT = new ModelPartData("port", CubeListBuilder.create()
          .texOffs(38, 33)
          .addBox(0F, 0F, 0F, 8, 8, 1),
          PartPose.offset(-4F, 12F, 7.01F));
    private static final ModelPartData POLE_4 = new ModelPartData("pole4", CubeListBuilder.create()
          .texOffs(0, 34)
          .addBox(0F, 0F, 0F, 1, 25, 1),
          PartPose.offset(6.5F, -6F, 6.5F));
    private static final ModelPartData SHAFT_2 = new ModelPartData("shaft2", CubeListBuilder.create()
          .texOffs(16, 34)
          .addBox(0F, 0F, 0F, 3, 11, 3),
          PartPose.offset(-1.5F, -5F, -1.5F));
    private static final ModelPartData SHAFT_1 = new ModelPartData("shaft1", CubeListBuilder.create()
          .texOffs(8, 34)
          .addBox(0F, 0F, 0F, 2, 15, 2),
          PartPose.offset(-1F, 6F, -1F));
    private static final ModelPartData ARM_3 = new ModelPartData("arm3", CubeListBuilder.create()
          .texOffs(0, 6)
          .addBox(0F, 0F, 0F, 2, 2, 4),
          PartPose.offsetAndRotation(-1F, 7F, 3F, -0.3665191F, 0F, 0F));
    private static final ModelPartData PLATE_2 = new ModelPartData("plate2", CubeListBuilder.create()
          .texOffs(48, 0)
          .addBox(0F, 0F, 0F, 4, 2, 4),
          PartPose.offset(-2F, 21F, -2F));
    private static final ModelPartData ARM_2 = new ModelPartData("arm2", CubeListBuilder.create()
          .texOffs(48, 6)
          .addBox(0F, 0F, 0F, 4, 2, 4),
          PartPose.offset(-2F, 7F, -2F));
    private static final ModelPartData ARM_1 = new ModelPartData("arm1", CubeListBuilder.create()
          .texOffs(56, 33)
          .addBox(0F, 0F, 0F, 3, 2, 4),
          PartPose.offset(-1.5F, 7F, 2F));
    private static final ModelPartData TOP = new ModelPartData("top", CubeListBuilder.create()
          .addBox(0F, 0F, 0F, 16, 2, 16),
          PartPose.offset(-8F, -8F, -8F));
    private static final ModelPartData FRAME_BACK_5 = new ModelPartData("frameBack5", CubeListBuilder.create()
          .texOffs(4, 34)
          .addBox(-1F, 0F, 0F, 1, 19, 1),
          PartPose.offsetAndRotation(7.5F, 7F, 6.49F, 0F, 0F, 0.837758F));
    private static final ModelPartData POLE_3 = new ModelPartData("pole3", CubeListBuilder.create()
          .texOffs(0, 34)
          .addBox(0F, 0F, 0F, 1, 25, 1),
          PartPose.offset(6.5F, -6F, -7.5F));
    private static final ModelPartData FRAME_RIGHT_5 = new ModelPartData("frameRight5", CubeListBuilder.create()
          .texOffs(4, 34)
          .addBox(0F, 0F, 0F, 1, 19, 1),
          PartPose.offsetAndRotation(6.485F, 7F, -7.5F, 0.837758F, 0F, 0F));
    private static final ModelPartData BASE_RIGHT = new ModelPartData("baseRight", CubeListBuilder.create()
          .texOffs(38, 18)
          .mirror()
          .addBox(0F, 0F, 0F, 3, 5, 10),
          PartPose.offset(5F, 19F, -5F));
    private static final ModelPartData BASE_FRONT = new ModelPartData("baseFront", CubeListBuilder.create()
          .texOffs(0, 18)
          .addBox(0F, 0F, 0F, 16, 5, 3),
          PartPose.offset(-8F, 19F, -8F));
    private static final ModelPartData BASE_LEFT = new ModelPartData("baseLeft", CubeListBuilder.create()
          .texOffs(38, 18)
          .addBox(0F, 0F, 0F, 3, 5, 10),
          PartPose.offset(-8F, 19F, -5F));
    private static final ModelPartData FRAME_RIGHT_3 = new ModelPartData("frameRight3", CubeListBuilder.create()
          .texOffs(64, 27)
          .addBox(0F, 0F, 0F, 1, 1, 13),
          PartPose.offset(6.5F, 6F, -6.5F));
    private static final ModelPartData POLE_1 = new ModelPartData("pole1", CubeListBuilder.create()
          .texOffs(0, 34)
          .addBox(0F, 0F, 0F, 1, 25, 1),
          PartPose.offset(-7.5F, -6F, -7.5F));
    private static final ModelPartData FRAME_RIGHT_4 = new ModelPartData("frameRight4", CubeListBuilder.create()
          .texOffs(4, 34)
          .addBox(0F, 0F, -1F, 1, 19, 1),
          PartPose.offsetAndRotation(6.49F, 7F, 7.5F, -0.837758F, 0F, 0F));
    private static final ModelPartData FRAME_RIGHT_1 = new ModelPartData("frameRight1", CubeListBuilder.create()
          .texOffs(4, 34)
          .addBox(0F, 0F, 0F, 1, 19, 1),
          PartPose.offsetAndRotation(6.485F, -6F, -7.5F, 0.837758F, 0F, 0F));
    private static final ModelPartData FRAME_RIGHT_2 = new ModelPartData("frameRight2", CubeListBuilder.create()
          .texOffs(4, 34)
          .addBox(0F, 0F, -1F, 1, 19, 1),
          PartPose.offsetAndRotation(6.49F, -6F, 7.5F, -0.837758F, 0F, 0F));
    private static final ModelPartData FRAME_LEFT_5 = new ModelPartData("frameLeft5", CubeListBuilder.create()
          .texOffs(4, 34)
          .addBox(0F, 0F, 0F, 1, 19, 1),
          PartPose.offsetAndRotation(-7.485F, 7F, -7.5F, 0.837758F, 0F, 0F));
    private static final ModelPartData FRAME_LEFT_4 = new ModelPartData("frameLeft4", CubeListBuilder.create()
          .texOffs(4, 34)
          .addBox(0F, 0F, -1F, 1, 19, 1),
          PartPose.offsetAndRotation(-7.49F, 7F, 7.5F, -0.837758F, 0F, 0F));
    private static final ModelPartData FRAME_BACK_3 = new ModelPartData("frameBack3", CubeListBuilder.create()
          .texOffs(36, 52)
          .addBox(0F, 0F, 0F, 13, 1, 1),
          PartPose.offset(-6.5F, 6F, 6.5F));
    private static final ModelPartData FRAME_LEFT_2 = new ModelPartData("frameLeft2", CubeListBuilder.create()
          .texOffs(4, 34)
          .addBox(0F, 0F, 0F, 1, 19, 1),
          PartPose.offsetAndRotation(-7.485F, -6F, -7.5F, 0.837758F, 0F, 0F));
    private static final ModelPartData FRAME_LEFT_1 = new ModelPartData("frameLeft1", CubeListBuilder.create()
          .texOffs(4, 34)
          .addBox(0F, 0F, -1F, 1, 19, 1),
          PartPose.offsetAndRotation(-7.49F, -6F, 7.5F, -0.837758F, 0F, 0F));
    private static final ModelPartData POLE_2 = new ModelPartData("pole2", CubeListBuilder.create()
          .texOffs(0, 34)
          .addBox(0F, 0F, 0F, 1, 25, 1),
          PartPose.offset(-7.5F, -6F, 6.5F));
    private static final ModelPartData FRAME_BACK_1 = new ModelPartData("frameBack1", CubeListBuilder.create()
          .texOffs(4, 34)
          .addBox(-1F, 0F, 0F, 1, 19, 1),
          PartPose.offsetAndRotation(7.5F, -6F, 6.49F, 0F, 0F, 0.837758F));
    private static final ModelPartData FRAME_BACK_2 = new ModelPartData("frameBack2", CubeListBuilder.create()
          .texOffs(4, 34)
          .addBox(0F, 0F, 0F, 1, 19, 1),
          PartPose.offsetAndRotation(-7.5F, -6F, 6.49F, 0F, 0F, -0.837758F));
    private static final ModelPartData FRAME_BACK_4 = new ModelPartData("frameBack4", CubeListBuilder.create()
          .texOffs(4, 34)
          .addBox(0F, 0F, 0F, 1, 19, 1),
          PartPose.offsetAndRotation(-7.5F, 7F, 6.49F, 0F, 0F, -0.837758F));
    private static final ModelPartData FRAME_LEFT_3 = new ModelPartData("frameLeft3", CubeListBuilder.create()
          .texOffs(64, 27)
          .addBox(0F, 0F, 0F, 1, 1, 13),
          PartPose.offset(-7.5F, 6F, -6.5F));
    private static final ModelPartData CONDUIT = new ModelPartData("conduit", CubeListBuilder.create()
          .texOffs(64, 0)
          .addBox(0F, 0F, 0F, 4, 25, 2),
          PartPose.offset(-2F, -6F, 6F));
    private static final ModelPartData PLATE_1 = new ModelPartData("plate1", CubeListBuilder.create()
          .texOffs(76, 0)
          .addBox(0F, 0F, 0F, 10, 1, 12),
          PartPose.offset(-5F, -6F, -5F));
    private static final ModelPartData RIVET_10 = new ModelPartData("rivet10", CubeListBuilder.create()
          .addBox(0F, 0F, 0F, 1, 1, 1),
          PartPose.offset(3.5F, -5.5F, 3.5F));
    private static final ModelPartData RIVET_5 = new ModelPartData("rivet5", CubeListBuilder.create()
          .addBox(0F, 0F, 0F, 1, 1, 1),
          PartPose.offset(-4.5F, -5.5F, 3.5F));
    private static final ModelPartData RIVET_1 = new ModelPartData("rivet1", CubeListBuilder.create()
          .addBox(0F, 0F, 0F, 1, 1, 1),
          PartPose.offset(-4.5F, -5.5F, -4.5F));
    private static final ModelPartData RIVET_6 = new ModelPartData("rivet6", CubeListBuilder.create()
          .addBox(0F, 0F, 0F, 1, 1, 1),
          PartPose.offset(3.5F, -5.5F, -4.5F));
    private static final ModelPartData RIVET_2 = new ModelPartData("rivet2", CubeListBuilder.create()
          .addBox(0F, 0F, 0F, 1, 1, 1),
          PartPose.offset(-4.5F, -5.5F, -2.5F));
    private static final ModelPartData RIVET_7 = new ModelPartData("rivet7", CubeListBuilder.create()
          .addBox(0F, 0F, 0F, 1, 1, 1),
          PartPose.offset(3.5F, -5.5F, -2.5F));
    private static final ModelPartData RIVET_3 = new ModelPartData("rivet3", CubeListBuilder.create()
          .addBox(0F, 0F, 0F, 1, 1, 1),
          PartPose.offset(-4.5F, -5.5F, -0.5F));
    private static final ModelPartData RIVET_8 = new ModelPartData("rivet8", CubeListBuilder.create()
          .addBox(0F, 0F, 0F, 1, 1, 1),
          PartPose.offset(3.5F, -5.5F, -0.5F));
    private static final ModelPartData RIVET_4 = new ModelPartData("rivet4", CubeListBuilder.create()
          .addBox(0F, 0F, 0F, 1, 1, 1),
          PartPose.offset(-4.5F, -5.5F, 1.5F));
    private static final ModelPartData RIVET_9 = new ModelPartData("rivet9", CubeListBuilder.create()
          .addBox(0F, 0F, 0F, 1, 1, 1),
          PartPose.offset(3.5F, -5.5F, 1.5F));

    public static LayerDefinition createLayerDefinition() {
        return createLayerDefinition(128, 64, PLATE_3, BASE_BACK, MOTOR, PORT, POLE_4, SHAFT_2, SHAFT_1, ARM_3, PLATE_2, ARM_2, ARM_1,
              TOP, FRAME_BACK_5, POLE_3, FRAME_RIGHT_5, BASE_RIGHT, BASE_FRONT, BASE_LEFT, FRAME_RIGHT_3, POLE_1, FRAME_RIGHT_4, FRAME_RIGHT_1, FRAME_RIGHT_2,
              FRAME_LEFT_5, FRAME_LEFT_4, FRAME_BACK_3, FRAME_LEFT_2, FRAME_LEFT_1, POLE_2, FRAME_BACK_1, FRAME_BACK_2, FRAME_BACK_4, FRAME_LEFT_3, CONDUIT,
              PLATE_1, RIVET_10, RIVET_5, RIVET_1, RIVET_6, RIVET_2, RIVET_7, RIVET_3, RIVET_8, RIVET_4, RIVET_9);
    }

    private final RenderType RENDER_TYPE = renderType(VIBRATOR_TEXTURE);
    private final List<ModelPart> parts;
    private final ModelPart shaft1;
    private final ModelPart plate2;
    private final ModelPart plate3;

    public ModelSeismicVibrator(EntityModelSet entityModelSet) {
        super(RenderType::entitySolid);
        ModelPart root = entityModelSet.bakeLayer(VIBRATOR_LAYER);
        parts = getRenderableParts(root, PLATE_3, BASE_BACK, MOTOR, PORT, POLE_4, SHAFT_2, SHAFT_1, ARM_3, PLATE_2, ARM_2, ARM_1,
              TOP, FRAME_BACK_5, POLE_3, FRAME_RIGHT_5, BASE_RIGHT, BASE_FRONT, BASE_LEFT, FRAME_RIGHT_3, POLE_1, FRAME_RIGHT_4, FRAME_RIGHT_1, FRAME_RIGHT_2,
              FRAME_LEFT_5, FRAME_LEFT_4, FRAME_BACK_3, FRAME_LEFT_2, FRAME_LEFT_1, POLE_2, FRAME_BACK_1, FRAME_BACK_2, FRAME_BACK_4, FRAME_LEFT_3, CONDUIT,
              PLATE_1, RIVET_10, RIVET_5, RIVET_1, RIVET_6, RIVET_2, RIVET_7, RIVET_3, RIVET_8, RIVET_4, RIVET_9);
        shaft1 = SHAFT_1.getFromRoot(root);
        plate2 = PLATE_2.getFromRoot(root);
        plate3 = PLATE_3.getFromRoot(root);
    }

    public void render(@Nonnull PoseStack poseStack, @Nonnull MultiBufferSource renderer, int light, int overlayLight, float piston, boolean hasEffect) {
        shaft1.y = 6 - (piston * 12);
        plate2.y = 21 - (piston * 12);
        plate3.y = 22 - (piston * 12);
        renderToBuffer(poseStack, getVertexConsumer(renderer, RENDER_TYPE, hasEffect), light, overlayLight, 1, 1, 1, 1);
    }

    @Override
    public void renderToBuffer(@Nonnull PoseStack poseStack, @Nonnull VertexConsumer vertexConsumer, int light, int overlayLight, float red, float green, float blue, float alpha) {
        renderPartsToBuffer(parts, poseStack, vertexConsumer, light, overlayLight, red, green, blue, alpha);
    }

    public void renderWireFrame(PoseStack poseStack, VertexConsumer vertexConsumer, float piston, float red, float green, float blue, float alpha) {
        shaft1.y = 6 - (piston * 12);
        plate2.y = 21 - (piston * 12);
        plate3.y = 22 - (piston * 12);
        renderPartsAsWireFrame(parts, poseStack, vertexConsumer, red, green, blue, alpha);
    }
}