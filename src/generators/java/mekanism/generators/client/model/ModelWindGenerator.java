package mekanism.generators.client.model;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import java.util.List;
import mekanism.client.model.MekanismJavaModel;
import mekanism.client.model.ModelPartData;
import mekanism.generators.common.MekanismGenerators;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.NotNull;

public class ModelWindGenerator extends MekanismJavaModel {

    public static final ModelLayerLocation GENERATOR_LAYER = new ModelLayerLocation(MekanismGenerators.rl("wind_generator"), "main");
    private static final ResourceLocation GENERATOR_TEXTURE = MekanismGenerators.rl("render/wind_generator.png");

    private static final ModelPartData HEAD = new ModelPartData("head", CubeListBuilder.create()
          .texOffs(20, 0)
          .addBox(-3.5F, -3.5F, 0F, 7, 7, 9),
          PartPose.offset(0F, -48F, -4F));
    private static final ModelPartData PLATE_CONNECTOR_2 = new ModelPartData("plateConnector2", CubeListBuilder.create()
          .texOffs(42, 34)
          .addBox(0F, 0F, 0F, 6, 6, 10),
          PartPose.offset(-3F, 13F, -7F));
    private static final ModelPartData PLATE_CONNECTOR = new ModelPartData("plateConnector", CubeListBuilder.create()
          .texOffs(0, 75)
          .addBox(0F, 0F, 0F, 4, 2, 2),
          PartPose.offset(-2F, 19F, -5.5F));
    private static final ModelPartData PLATE = new ModelPartData("plate", CubeListBuilder.create()
          .texOffs(42, 25)
          .addBox(0F, 0F, 0F, 8, 8, 1),
          PartPose.offset(-4F, 12F, -8F));
    private static final ModelPartData BLADE_CAP = new ModelPartData("bladeCap", CubeListBuilder.create()
          .texOffs(22, 0)
          .addBox(-1F, -1F, -8F, 2, 2, 1),
          PartPose.offset(0F, -48F, 0F));
    private static final ModelPartData BLADE_CENTER = new ModelPartData("bladeCenter", CubeListBuilder.create()
          .texOffs(20, 25)
          .addBox(-2F, -2F, -7F, 4, 4, 3),
          PartPose.offset(0F, -48F, 0F));
    private static final ModelPartData BASE_RIM = new ModelPartData("baseRim", CubeListBuilder.create()
          .texOffs(26, 50)
          .addBox(0F, 0F, 0F, 12, 2, 12),
          PartPose.offset(-6F, 21F, -6F));
    private static final ModelPartData BASE = new ModelPartData("base", CubeListBuilder.create()
          .texOffs(10, 64)
          .addBox(0F, 0F, 0F, 16, 2, 16),
          PartPose.offset(-8F, 22F, -8F));
    private static final ModelPartData WIRE = new ModelPartData("wire", CubeListBuilder.create()
          .texOffs(74, 0)
          .addBox(-1F, 0F, -1.1F, 2, 65, 2),
          PartPose.offsetAndRotation(0F, -46F, -1.5F, -0.0349066F, 0F, 0F));
    private static final ModelPartData REAR_PLATE_1 = new ModelPartData("rearPlate1", CubeListBuilder.create()
          .texOffs(20, 16)
          .addBox(-2.5F, -6F, 0F, 5, 6, 3),
          PartPose.offsetAndRotation(0F, -44.5F, 4F, 0.122173F, 0F, 0F));
    private static final ModelPartData REAR_PLATE_2 = new ModelPartData("rearPlate2", CubeListBuilder.create()
          .texOffs(36, 16)
          .addBox(-1.5F, -5F, -1F, 3, 5, 2),
          PartPose.offsetAndRotation(0F, -45F, 7F, 0.2094395F, 0F, 0F));
    private static final ModelPartData BLADE_1A = new ModelPartData("blade1a", CubeListBuilder.create()
          .texOffs(20, 32)
          .addBox(-1F, -32F, 0F, 2, 32, 1),
          PartPose.offset(0F, -48F, -5.99F));
    private static final ModelPartData BLADE_2A = new ModelPartData("blade2a", CubeListBuilder.create()
          .texOffs(20, 32)
          .addBox(-1F, 0F, 0F, 2, 32, 1),
          PartPose.offsetAndRotation(0F, -48F, -6F, 0F, 0F, 1.047198F));
    private static final ModelPartData BLADE_3A = new ModelPartData("blade3a", CubeListBuilder.create()
          .texOffs(20, 32)
          .addBox(-1F, 0F, 0F, 2, 32, 1),
          PartPose.offsetAndRotation(0F, -48F, -6F, 0F, 0F, -1.047198F));
    private static final ModelPartData BLADE_1B = new ModelPartData("blade1b", CubeListBuilder.create()
          .texOffs(26, 32)
          .addBox(-2F, -28F, 0F, 2, 28, 1),
          PartPose.offsetAndRotation(0F, -48F, -6F, 0F, 0F, 0.0349066F));
    private static final ModelPartData BLADE_2B = new ModelPartData("blade2b", CubeListBuilder.create()
          .texOffs(26, 32)
          .addBox(0F, 0F, 0F, 2, 28, 1),
          PartPose.offsetAndRotation(0F, -48F, -6.01F, 0F, 0F, 1.082104F));
    private static final ModelPartData BLADE_3B = new ModelPartData("blade3b", CubeListBuilder.create()
          .texOffs(26, 32)
          .addBox(0F, 0F, 0F, 2, 28, 1),
          PartPose.offsetAndRotation(0F, -48F, -6.01F, 0F, 0F, -1.012291F));
    private static final ModelPartData POST_1A = new ModelPartData("post1a", CubeListBuilder.create()
          .addBox(-2.5F, 0F, -2.5F, 5, 68, 5),
          PartPose.offsetAndRotation(0F, -46F, 0F, -0.0349066F, 0F, 0.0349066F));
    private static final ModelPartData POST_1B = new ModelPartData("post1b", CubeListBuilder.create()
          .addBox(-2.5F, 0F, -2.5F, 5, 68, 5),
          PartPose.offsetAndRotation(0F, -46F, 0F, 0.0349066F, 0F, -0.0349066F));
    private static final ModelPartData POST_1C = new ModelPartData("post1c", CubeListBuilder.create()
          .addBox(-2.5F, 0F, -2.5F, 5, 68, 5),
          PartPose.offsetAndRotation(0F, -46F, 0F, 0.0347321F, 0F, 0.0347321F));
    private static final ModelPartData POST_1D = new ModelPartData("post1d", CubeListBuilder.create()
          .addBox(-2.5F, 0F, -2.5F, 5, 68, 5),
          PartPose.offsetAndRotation(0F, -46F, 0F, -0.0347321F, 0F, -0.0347321F));

    public static LayerDefinition createLayerDefinition() {
        return createLayerDefinition(128, 128, HEAD, PLATE_CONNECTOR_2, PLATE_CONNECTOR, PLATE, BLADE_CAP, BLADE_CENTER, BASE_RIM, BASE, WIRE,
              REAR_PLATE_1, REAR_PLATE_2, BLADE_1A, BLADE_2A, BLADE_3A, BLADE_1B, BLADE_2B, BLADE_3B, POST_1A, POST_1B, POST_1C, POST_1D);
    }

    private final RenderType RENDER_TYPE = renderType(GENERATOR_TEXTURE);
    private final List<ModelPart> parts;
    private final ModelPart blade1a;
    private final ModelPart blade1b;
    private final ModelPart blade2a;
    private final ModelPart blade2b;
    private final ModelPart blade3a;
    private final ModelPart blade3b;
    private final ModelPart bladeCap;
    private final ModelPart bladeCenter;

    public ModelWindGenerator(EntityModelSet entityModelSet) {
        super(RenderType::entitySolid);
        ModelPart root = entityModelSet.bakeLayer(GENERATOR_LAYER);
        parts = getRenderableParts(root, HEAD, PLATE_CONNECTOR_2, PLATE_CONNECTOR, PLATE, BASE_RIM, BASE, WIRE, REAR_PLATE_1, REAR_PLATE_2, POST_1A, POST_1B,
              POST_1C, POST_1D, BLADE_1A, BLADE_2A, BLADE_3A, BLADE_1B, BLADE_2B, BLADE_3B, BLADE_CAP, BLADE_CENTER);
        blade1a = BLADE_1A.getFromRoot(root);
        blade1b = BLADE_1B.getFromRoot(root);
        blade2a = BLADE_2A.getFromRoot(root);
        blade2b = BLADE_2B.getFromRoot(root);
        blade3a = BLADE_3A.getFromRoot(root);
        blade3b = BLADE_3B.getFromRoot(root);
        bladeCap = BLADE_CAP.getFromRoot(root);
        bladeCenter = BLADE_CENTER.getFromRoot(root);
    }

    public void render(@NotNull PoseStack matrix, @NotNull MultiBufferSource renderer, float angle, int light, int overlayLight, boolean hasEffect) {
        float baseRotation = getAbsoluteRotation(angle);
        setRotation(blade1a, 0F, 0F, baseRotation);
        setRotation(blade1b, 0F, 0F, 0.0349066F + baseRotation);

        float blade2Rotation = getAbsoluteRotation(angle - 60);
        setRotation(blade2a, 0F, 0F, blade2Rotation);
        setRotation(blade2b, 0F, 0F, 0.0349066F + blade2Rotation);

        float blade3Rotation = getAbsoluteRotation(angle + 60);
        setRotation(blade3a, 0F, 0F, blade3Rotation);
        setRotation(blade3b, 0F, 0F, 0.0349066F + blade3Rotation);

        setRotation(bladeCap, 0F, 0F, baseRotation);
        setRotation(bladeCenter, 0F, 0F, baseRotation);

        renderToBuffer(matrix, getVertexConsumer(renderer, RENDER_TYPE, hasEffect), light, overlayLight, 0xFFFFFFFF);
    }

    @Override
    public void renderToBuffer(@NotNull PoseStack poseStack, @NotNull VertexConsumer vertexConsumer, int light, int overlayLight, int color) {
        renderPartsToBuffer(parts, poseStack, vertexConsumer, light, overlayLight, color);
    }

    public void renderWireFrame(PoseStack matrix, VertexConsumer vertexBuilder, float angle) {
        float baseRotation = getAbsoluteRotation(angle);
        setRotation(blade1a, 0F, 0F, baseRotation);
        setRotation(blade1b, 0F, 0F, 0.0349066F + baseRotation);

        float blade2Rotation = getAbsoluteRotation(angle - 60);
        setRotation(blade2a, 0F, 0F, blade2Rotation);
        setRotation(blade2b, 0F, 0F, 0.0349066F + blade2Rotation);

        float blade3Rotation = getAbsoluteRotation(angle + 60);
        setRotation(blade3a, 0F, 0F, blade3Rotation);
        setRotation(blade3b, 0F, 0F, 0.0349066F + blade3Rotation);

        setRotation(bladeCap, 0F, 0F, baseRotation);
        setRotation(bladeCenter, 0F, 0F, baseRotation);
        renderPartsAsWireFrame(parts, matrix, vertexBuilder);
    }

    private float getAbsoluteRotation(float angle) {
        return (angle % 360) * Mth.DEG_TO_RAD;
    }
}