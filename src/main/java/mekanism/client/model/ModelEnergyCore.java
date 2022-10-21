package mekanism.client.model;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import mekanism.api.text.EnumColor;
import mekanism.client.render.MekanismRenderType;
import mekanism.common.Mekanism;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.ResourceType;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

public class ModelEnergyCore extends MekanismJavaModel {

    public static final ModelLayerLocation CORE_LAYER = new ModelLayerLocation(Mekanism.rl("energy_core"), "main");
    private static final ResourceLocation CORE_TEXTURE = MekanismUtils.getResource(ResourceType.RENDER, "energy_core.png");

    private static final ModelPartData CUBE = new ModelPartData("cube", CubeListBuilder.create()
          .texOffs(0, 0)
          .addBox(-8, -8, -8, 16, 16, 16));

    public static LayerDefinition createLayerDefinition() {
        return createLayerDefinition(32, 32, CUBE);
    }

    public static final RenderType BATCHED_RENDER_TYPE = MekanismRenderType.STANDARD_TRANSLUCENT_TARGET.apply(CORE_TEXTURE);
    public final RenderType RENDER_TYPE = renderType(CORE_TEXTURE);
    private final ModelPart cube;

    public ModelEnergyCore(EntityModelSet entityModelSet) {
        super(MekanismRenderType.STANDARD);
        ModelPart root = entityModelSet.bakeLayer(CORE_LAYER);
        cube = CUBE.getFromRoot(root);
    }

    public void render(@NotNull PoseStack matrix, @NotNull MultiBufferSource renderer, int light, int overlayLight, EnumColor color, float energyPercentage) {
        render(matrix, renderer.getBuffer(RENDER_TYPE), light, overlayLight, color, energyPercentage);
    }

    public void render(@NotNull PoseStack matrix, @NotNull VertexConsumer buffer, int light, int overlayLight, EnumColor color, float energyPercentage) {
        renderToBuffer(matrix, buffer, light, overlayLight, color.getColor(0), color.getColor(1), color.getColor(2), energyPercentage);
    }

    @Override
    public void renderToBuffer(@NotNull PoseStack matrix, @NotNull VertexConsumer vertexBuilder, int light, int overlayLight, float red, float green, float blue,
          float alpha) {
        cube.render(matrix, vertexBuilder, light, overlayLight, red, green, blue, alpha);
    }
}