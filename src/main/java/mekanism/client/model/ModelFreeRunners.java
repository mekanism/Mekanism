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

public class ModelFreeRunners extends MekanismJavaModel {

    public static final ModelLayerLocation FREE_RUNNER_LAYER = new ModelLayerLocation(Mekanism.rl("free_runners"), "main");
    private static final ResourceLocation FREE_RUNNER_TEXTURE = MekanismUtils.getResource(ResourceType.RENDER, "free_runners.png");

    protected static final ModelPartData SPRING_L = new ModelPartData("SpringL", CubeListBuilder.create()
          .texOffs(8, 0)
          .addBox(1.5F, 18F, 0F, 1, 6, 1),
          PartPose.rotation(0.1047198F, 0F, 0F));
    protected static final ModelPartData SPRING_R = new ModelPartData("SpringR", CubeListBuilder.create()
          .texOffs(8, 0)
          .addBox(-2.5F, 18F, 0F, 1, 6, 1),
          PartPose.rotation(0.1047198F, 0F, 0F));
    protected static final ModelPartData BRACE_L = new ModelPartData("BraceL", CubeListBuilder.create()
          .texOffs(12, 0)
          .addBox(0.2F, 18F, -0.8F, 4, 2, 3));
    protected static final ModelPartData BRACE_R = new ModelPartData("BraceR", CubeListBuilder.create()
          .texOffs(12, 0)
          .addBox(-4.2F, 18F, -0.8F, 4, 2, 3));
    protected static final ModelPartData SUPPORT_L = new ModelPartData("SupportL", CubeListBuilder.create()
          .addBox(1F, 16.5F, -4.2F, 2, 4, 2),
          PartPose.rotation(0.296706F, 0F, 0F));
    protected static final ModelPartData SUPPORT_R = new ModelPartData("SupportR", CubeListBuilder.create()
          .addBox(-3F, 16.5F, -4.2F, 2, 4, 2),
          PartPose.rotation(0.296706F, 0F, 0F));

    public static LayerDefinition createLayerDefinition() {
        return createLayerDefinition(64, 32, SPRING_L, SPRING_R, BRACE_L, BRACE_R, SUPPORT_L, SUPPORT_R);
    }

    private final RenderType RENDER_TYPE = renderType(FREE_RUNNER_TEXTURE);
    protected final List<ModelPart> leftParts;
    protected final List<ModelPart> rightParts;

    public ModelFreeRunners(EntityModelSet entityModelSet) {
        this(entityModelSet.bakeLayer(FREE_RUNNER_LAYER));
    }

    protected ModelFreeRunners(ModelPart root) {
        super(RenderType::entitySolid);
        leftParts = getRenderableParts(root, SPRING_L, BRACE_L, SUPPORT_L);
        rightParts = getRenderableParts(root, SPRING_R, BRACE_R, SUPPORT_R);
    }

    public void render(@Nonnull PoseStack matrix, @Nonnull MultiBufferSource renderer, int light, int overlayLight, boolean hasEffect) {
        renderToBuffer(matrix, getVertexConsumer(renderer, RENDER_TYPE, hasEffect), light, overlayLight, 1, 1, 1, 1);
    }

    @Override
    public void renderToBuffer(@Nonnull PoseStack poseStack, @Nonnull VertexConsumer vertexConsumer, int light, int overlayLight, float red, float green, float blue, float alpha) {
        renderLeg(poseStack, vertexConsumer, light, overlayLight, red, green, blue, alpha, true);
        renderLeg(poseStack, vertexConsumer, light, overlayLight, red, green, blue, alpha, false);
    }

    public void renderLeg(@Nonnull PoseStack poseStack, @Nonnull MultiBufferSource renderer, int light, int overlayLight, boolean hasEffect, boolean left) {
        renderLeg(poseStack, getVertexConsumer(renderer, RENDER_TYPE, hasEffect), light, overlayLight, 1, 1, 1, 1, left);
    }

    protected void renderLeg(@Nonnull PoseStack poseStack, @Nonnull VertexConsumer vertexConsumer, int light, int overlayLight, float red, float green, float blue,
          float alpha, boolean left) {
        if (left) {
            renderPartsToBuffer(leftParts, poseStack, vertexConsumer, light, overlayLight, red, green, blue, alpha);
        } else {
            renderPartsToBuffer(rightParts, poseStack, vertexConsumer, light, overlayLight, red, green, blue, alpha);
        }
    }
}