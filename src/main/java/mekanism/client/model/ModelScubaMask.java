package mekanism.client.model;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import java.util.List;
import mekanism.client.render.MekanismRenderType;
import mekanism.common.Mekanism;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.ResourceType;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

public class ModelScubaMask extends MekanismJavaModel {

    public static final ModelLayerLocation MASK_LAYER = new ModelLayerLocation(Mekanism.rl("scuba_mask"), "main");
    private static final ResourceLocation MASK_TEXTURE = MekanismUtils.getResource(ResourceType.RENDER, "scuba_set.png");

    private static final ModelPartData HELMET_FEED = new ModelPartData("helmetFeed", CubeListBuilder.create()
          .texOffs(88, 43)
          .addBox(-2F, -2F, 2F, 4, 3, 4));
    private static final ModelPartData TUBE_BACK = new ModelPartData("tubeBack", CubeListBuilder.create()
          .texOffs(106, 50)
          .addBox(-4.5F, -1F, 4.5F, 9, 1, 1));
    private static final ModelPartData TUBE_L = new ModelPartData("tubeL", CubeListBuilder.create()
          .texOffs(106, 54)
          .addBox(4.5F, -1F, -4.5F, 1, 1, 9));
    private static final ModelPartData TUBE_R = new ModelPartData("tubeR", CubeListBuilder.create()
          .texOffs(106, 54)
          .addBox(-5.5F, -1F, -4.5F, 1, 1, 9));
    private static final ModelPartData TUBE_FRONT = new ModelPartData("tubeFront", CubeListBuilder.create()
          .texOffs(106, 50)
          .addBox(-4.5F, -1F, -5.5F, 9, 1, 1));
    private static final ModelPartData MOUTH_INTAKE = new ModelPartData("mouthIntake", CubeListBuilder.create()
          .texOffs(118, 42)
          .addBox(-1.5F, -0.7F, -6F, 3, 2, 3),
          PartPose.offsetAndRotation(0F, -2F, 0F, 0.2094395F, 0F, 0F));
    private static final ModelPartData FIN_UPPER_R = new ModelPartData("finUpperR", CubeListBuilder.create()
          .texOffs(78, 50)
          .addBox(-6F, -7.5F, -3.3F, 1, 2, 12),
          PartPose.rotation(0.0698132F, 0F, 0F));
    private static final ModelPartData FIN_UPPER_L = new ModelPartData("finUpperL", CubeListBuilder.create()
          .texOffs(78, 50)
          .addBox(5F, -7.5F, -3.3F, 1, 2, 12),
          PartPose.rotation(0.0698132F, 0F, 0F));
    private static final ModelPartData FIN_MID_R = new ModelPartData("finMidR", CubeListBuilder.create()
          .texOffs(72, 34)
          .addBox(-7.5F, -6F, -1F, 2, 2, 5));
    private static final ModelPartData FIN_MID_L = new ModelPartData("finMidL", CubeListBuilder.create()
          .texOffs(72, 34)
          .addBox(5.5F, -6F, -1F, 2, 2, 5));
    private static final ModelPartData FIN_BACK = new ModelPartData("finBack", CubeListBuilder.create()
          .texOffs(80, 0)
          .addBox(-1F, -9.6F, 2.5F, 2, 10, 3));
    private static final ModelPartData TOP_PLATE = new ModelPartData("topPlate", CubeListBuilder.create()
          .texOffs(104, 34)
          .addBox(-3F, -10F, -2F, 6, 2, 6),
          PartPose.rotation(0.1396263F, 0F, 0F));
    private static final ModelPartData FILTER_L = new ModelPartData("filterL", CubeListBuilder.create()
          .texOffs(108, 42)
          .addBox(3.4F, -1.8F, -5F, 2, 3, 3),
          PartPose.rotation(0F, 0.3839724F, 0.5061455F));
    private static final ModelPartData FILTER_R = new ModelPartData("filterR", CubeListBuilder.create()
          .texOffs(108, 42)
          .addBox(-5.4F, -1.8F, -5F, 2, 3, 3),
          PartPose.rotation(0F, -0.3839724F, -0.5061455F));
    private static final ModelPartData FILTER_PIPE_LOWER = new ModelPartData("filterPipeLower", CubeListBuilder.create()
          .texOffs(92, 41)
          .addBox(-3F, 1F, -5F, 5, 1, 1));
    private static final ModelPartData FILTER_PIPE_UPPER = new ModelPartData("filterPipeUpper", CubeListBuilder.create()
          .texOffs(104, 42)
          .addBox(-0.5F, 0F, -5F, 1, 1, 1));
    private static final ModelPartData GLASS_TOP = new ModelPartData("glassTop", CubeListBuilder.create()
          .addBox(-4F, -9F, -4F, 8, 1, 8));
    private static final ModelPartData GLASS_FRONT = new ModelPartData("glassFront", CubeListBuilder.create()
          .addBox(-4F, -8F, -5F, 8, 7, 1));
    private static final ModelPartData GLASS_R = new ModelPartData("glassR", CubeListBuilder.create()
          .addBox(-5F, -8F, -4F, 1, 7, 8));
    private static final ModelPartData GLASS_L = new ModelPartData("glassL", CubeListBuilder.create()
          .addBox(4F, -8F, -4F, 1, 7, 8));
    private static final ModelPartData GLASS_BACK_R = new ModelPartData("glassBackR", CubeListBuilder.create()
          .addBox(-4F, -8F, 4F, 3, 7, 1));
    private static final ModelPartData GLASS_BACK_L = new ModelPartData("glassBackL", CubeListBuilder.create()
          .addBox(1F, -8F, 4F, 3, 7, 1));
    private static final ModelPartData PIPE_CORNER_F_L = new ModelPartData("pipeCornerFL", CubeListBuilder.create()
          .texOffs(109, 50)
          .addBox(3.5F, -1F, -4.5F, 1, 1, 1));
    private static final ModelPartData PIPE_CORNER_F_R = new ModelPartData("pipeCornerFR", CubeListBuilder.create()
          .texOffs(109, 50)
          .addBox(-4.5F, -1F, -4.5F, 1, 1, 1));
    private static final ModelPartData PIPE_CORNER_B_R = new ModelPartData("pipeCornerBR", CubeListBuilder.create()
          .texOffs(109, 50)
          .addBox(-4.5F, -1F, 3.5F, 1, 1, 1));
    private static final ModelPartData PIPE_CORNER_B_L = new ModelPartData("pipeCornerBL", CubeListBuilder.create()
          .texOffs(109, 50)
          .addBox(3.5F, -1F, 4.5F, 1, 1, 1),
          PartPose.offset(0F, 0F, -1F));
    private static final ModelPartData LIGHT_L = new ModelPartData("lightL", CubeListBuilder.create()
          .texOffs(89, 37)
          .addBox(5.5F, -6F, -2F, 2, 2, 1));
    private static final ModelPartData LIGHT_R = new ModelPartData("lightR", CubeListBuilder.create()
          .texOffs(89, 37)
          .addBox(-7.5F, -6F, -2F, 2, 2, 1));

    public static LayerDefinition createLayerDefinition() {
        return createLayerDefinition(128, 64, HELMET_FEED, TUBE_BACK, TUBE_L, TUBE_R, TUBE_FRONT, MOUTH_INTAKE, FIN_UPPER_R, FIN_UPPER_L,
              FIN_MID_R, FIN_MID_L, FIN_BACK, TOP_PLATE, FILTER_L, FILTER_R, FILTER_PIPE_LOWER, FILTER_PIPE_UPPER, GLASS_TOP, GLASS_FRONT, GLASS_R, GLASS_L,
              GLASS_BACK_R, GLASS_BACK_L, PIPE_CORNER_F_L, PIPE_CORNER_F_R, PIPE_CORNER_B_R, PIPE_CORNER_B_L, LIGHT_L, LIGHT_R);
    }

    private final RenderType GLASS_RENDER_TYPE = MekanismRenderType.STANDARD.apply(MASK_TEXTURE);
    private final RenderType RENDER_TYPE = renderType(MASK_TEXTURE);
    private final List<ModelPart> parts;
    private final List<ModelPart> litParts;
    private final List<ModelPart> glass;

    public ModelScubaMask(EntityModelSet entityModelSet) {
        super(RenderType::entitySolid);
        ModelPart root = entityModelSet.bakeLayer(MASK_LAYER);
        parts = getRenderableParts(root, HELMET_FEED, TUBE_BACK, TUBE_L, TUBE_R, TUBE_FRONT, MOUTH_INTAKE, FIN_UPPER_R, FIN_UPPER_L,
              FIN_MID_R, FIN_MID_L, FIN_BACK, TOP_PLATE, FILTER_L, FILTER_R, FILTER_PIPE_LOWER, FILTER_PIPE_UPPER, PIPE_CORNER_F_L,
              PIPE_CORNER_F_R, PIPE_CORNER_B_R, PIPE_CORNER_B_L);
        litParts = getRenderableParts(root, LIGHT_L, LIGHT_R);
        glass = getRenderableParts(root, GLASS_TOP, GLASS_FRONT, GLASS_R, GLASS_L, GLASS_BACK_R, GLASS_BACK_L);
    }

    public void render(@NotNull PoseStack matrix, @NotNull MultiBufferSource renderer, int light, int overlayLight, boolean hasEffect) {
        renderToBuffer(matrix, getVertexConsumer(renderer, RENDER_TYPE, hasEffect), light, overlayLight, 0xFFFFFFFF);
        renderPartsToBuffer(glass, matrix, getVertexConsumer(renderer, GLASS_RENDER_TYPE, hasEffect), LightTexture.FULL_BRIGHT, overlayLight, 0x4CFFFFFF);
    }

    @Override
    public void renderToBuffer(@NotNull PoseStack poseStack, @NotNull VertexConsumer vertexConsumer, int light, int overlayLight, int color) {
        renderPartsToBuffer(parts, poseStack, vertexConsumer, light, overlayLight, color);
        renderPartsToBuffer(litParts, poseStack, vertexConsumer, LightTexture.FULL_BRIGHT, overlayLight, color);
    }
}