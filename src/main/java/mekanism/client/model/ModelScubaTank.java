package mekanism.client.model;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import java.util.List;
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
import org.jetbrains.annotations.NotNull;

public class ModelScubaTank extends MekanismJavaModel {

    public static final ModelLayerLocation TANK_LAYER = new ModelLayerLocation(Mekanism.rl("scuba_tank"), "main");
    private static final ResourceLocation TANK_TEXTURE = MekanismUtils.getResource(ResourceType.RENDER, "scuba_set.png");

    private static final ModelPartData TANK_L = new ModelPartData("tankL", CubeListBuilder.create()
          .texOffs(23, 54)
          .addBox(-1F, 2F, 4F, 3, 7, 3),
          PartPose.rotation(-0.2443461F, 0.5235988F, 0F));
    private static final ModelPartData TANK_R = new ModelPartData("tankR", CubeListBuilder.create()
          .texOffs(23, 54)
          .addBox(-2F, 2F, 4F, 3, 7, 3),
          PartPose.rotation(-0.2443461F, -0.5235988F, 0F));
    private static final ModelPartData TANK_DOCK = new ModelPartData("tankDock", CubeListBuilder.create()
          .texOffs(0, 55)
          .addBox(-2F, 5F, 1F, 4, 4, 5));
    private static final ModelPartData CAP_L = new ModelPartData("capL", CubeListBuilder.create()
          .texOffs(23, 51)
          .addBox(-0.5F, 1F, 4.5F, 2, 1, 2),
          PartPose.rotation(-0.2443461F, 0.5235988F, 0F));
    private static final ModelPartData CAP_R = new ModelPartData("capR", CubeListBuilder.create()
          .texOffs(23, 51)
          .addBox(-1.5F, 1F, 4.5F, 2, 1, 2),
          PartPose.rotation(-0.2443461F, -0.5235988F, 0F));
    private static final ModelPartData TANK_BRIDGE = new ModelPartData("tankBridge", CubeListBuilder.create()
          .texOffs(0, 47)
          .addBox(-1F, 3F, -1.5F, 2, 5, 3),
          PartPose.rotation(0.5934119F, 0F, 0F));
    private static final ModelPartData TANK_PIPE_LOWER = new ModelPartData("tankPipeLower", CubeListBuilder.create()
          .texOffs(0, 37)
          .addBox(-0.5F, 2F, 3F, 1, 4, 1),
          PartPose.rotation(0.2094395F, 0F, 0F));
    private static final ModelPartData TANK_PIPE_UPPER = new ModelPartData("tankPipeUpper", CubeListBuilder.create()
          .texOffs(4, 38)
          .addBox(-0.5F, 1F, 1.5F, 1, 1, 3));
    private static final ModelPartData TANK_BACK_BRACE = new ModelPartData("tankBackBrace", CubeListBuilder.create()
          .texOffs(0, 42)
          .addBox(-3F, 2F, 0.5F, 6, 3, 2),
          PartPose.rotation(0.2443461F, 0F, 0F));

    public static LayerDefinition createLayerDefinition() {
        return createLayerDefinition(128, 64, TANK_L, TANK_R, TANK_DOCK, CAP_L, CAP_R, TANK_BRIDGE, TANK_PIPE_LOWER, TANK_PIPE_UPPER, TANK_BACK_BRACE);
    }

    private final RenderType RENDER_TYPE = renderType(TANK_TEXTURE);
    private final List<ModelPart> parts;

    public ModelScubaTank(EntityModelSet entityModelSet) {
        super(RenderType::entitySolid);
        ModelPart root = entityModelSet.bakeLayer(TANK_LAYER);
        parts = getRenderableParts(root, TANK_L, TANK_R, TANK_DOCK, CAP_L, CAP_R, TANK_BRIDGE, TANK_PIPE_LOWER, TANK_PIPE_UPPER, TANK_BACK_BRACE);
    }

    public void render(@NotNull PoseStack matrix, @NotNull MultiBufferSource renderer, int light, int overlayLight, boolean hasEffect) {
        renderToBuffer(matrix, getVertexConsumer(renderer, RENDER_TYPE, hasEffect), light, overlayLight, 0xFFFFFFFF);
    }

    @Override
    public void renderToBuffer(@NotNull PoseStack poseStack, @NotNull VertexConsumer vertexConsumer, int light, int overlayLight, int color) {
        renderPartsToBuffer(parts, poseStack, vertexConsumer, light, overlayLight, color);
    }
}