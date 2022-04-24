package mekanism.client.model;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import javax.annotation.Nonnull;
import mekanism.api.text.EnumColor;
import mekanism.client.render.MekanismRenderer;
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

public class ModelTransporterBox extends MekanismJavaModel {

    public static final ModelLayerLocation BOX_LAYER = new ModelLayerLocation(Mekanism.rl("transporter_box"), "main");
    private static final ResourceLocation BOX_TEXTURE = MekanismUtils.getResource(ResourceType.RENDER, "transporter_box.png");

    private static final ModelPartData BOX = new ModelPartData("box", CubeListBuilder.create()
          .addBox(0F, 0F, 0F, 7, 7, 7),
          PartPose.offset(-3.5F, 0, -3.5F));

    public static LayerDefinition createLayerDefinition() {
        return createLayerDefinition(64, 64, BOX);
    }

    private final RenderType RENDER_TYPE = renderType(BOX_TEXTURE);
    private final ModelPart box;

    public ModelTransporterBox(EntityModelSet entityModelSet) {
        super(RenderType::entityCutoutNoCull);
        ModelPart root = entityModelSet.bakeLayer(BOX_LAYER);
        box = BOX.getFromRoot(root);
    }

    public void render(@Nonnull PoseStack matrix, @Nonnull MultiBufferSource renderer, int light, int overlayLight, float x, float y, float z, EnumColor color) {
        matrix.pushPose();
        matrix.translate(x, y, z);
        renderToBuffer(matrix, renderer.getBuffer(RENDER_TYPE), MekanismRenderer.FULL_LIGHT, overlayLight, color.getColor(0), color.getColor(1), color.getColor(2), 1);
        matrix.popPose();
    }

    @Override
    public void renderToBuffer(@Nonnull PoseStack matrix, @Nonnull VertexConsumer vertexBuilder, int light, int overlayLight, float red, float green, float blue, float alpha) {
        box.render(matrix, vertexBuilder, light, overlayLight, red, green, blue, alpha);
    }
}