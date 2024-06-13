package mekanism.client.model;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import mekanism.api.text.EnumColor;
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

    public void render(@NotNull PoseStack matrix, @NotNull MultiBufferSource renderer, int overlayLight, float x, float y, float z, EnumColor color) {
        matrix.pushPose();
        matrix.translate(x, y, z);
        renderToBuffer(matrix, renderer.getBuffer(RENDER_TYPE), LightTexture.FULL_BRIGHT, overlayLight, color.getPackedColor());
        matrix.popPose();
    }

    @Override
    public void renderToBuffer(@NotNull PoseStack matrix, @NotNull VertexConsumer vertexBuilder, int light, int overlayLight, int color) {
        box.render(matrix, vertexBuilder, light, overlayLight, 0xFFFFFFFF);
    }
}