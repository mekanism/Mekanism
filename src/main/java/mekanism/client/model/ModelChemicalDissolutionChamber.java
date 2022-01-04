package mekanism.client.model;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import java.util.List;
import javax.annotation.Nonnull;
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
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;

public class ModelChemicalDissolutionChamber extends MekanismJavaModel {

    public static final ModelLayerLocation DISSOLUTION_LAYER = new ModelLayerLocation(Mekanism.rl("chemical_dissolution_chamber"), "main");
    private static final ResourceLocation DISSOLUTION_TEXTURE = MekanismUtils.getResource(ResourceType.RENDER, "chemical_dissolution_chamber.png");

    private static final ModelPartData SUPPORT_2 = new ModelPartData("support2", CubeListBuilder.create()
          .texOffs(4, 0)
          .addBox(0F, 0F, 0F, 1, 2, 1),
          PartPose.offset(6F, 9F, -7F));
    private static final ModelPartData VAT_5 = new ModelPartData("vat5", CubeListBuilder.create()
          .texOffs(0, 23)
          .addBox(0F, 0F, 0F, 3, 4, 3),
          PartPose.offset(-1.5F, 13F, -1.5F));
    private static final ModelPartData TOP_2 = new ModelPartData("top2", CubeListBuilder.create()
          .texOffs(0, 40)
          .addBox(0F, 0F, 0F, 16, 1, 15),
          PartPose.offset(-8F, 11F, -8F));
    private static final ModelPartData TOP = new ModelPartData("top", CubeListBuilder.create()
          .texOffs(0, 23)
          .addBox(0F, 0F, 0F, 16, 1, 16),
          PartPose.offset(-8F, 8F, -8F));
    private static final ModelPartData BASE = new ModelPartData("base", CubeListBuilder.create()
          .addBox(0F, 0F, 0F, 16, 7, 16),
          PartPose.offset(-8F, 17F, -8F));
    private static final ModelPartData VAT_2 = new ModelPartData("vat2", CubeListBuilder.create()
          .texOffs(0, 23)
          .addBox(0F, 0F, 0F, 3, 4, 3),
          PartPose.offset(-5F, 13F, -1.5F));
    private static final ModelPartData VAT_3 = new ModelPartData("vat3", CubeListBuilder.create()
          .texOffs(0, 23)
          .addBox(0F, 0F, 0F, 3, 4, 3),
          PartPose.offset(-5F, 13F, 2F));
    private static final ModelPartData VAT_6 = new ModelPartData("vat6", CubeListBuilder.create()
          .texOffs(0, 23)
          .addBox(0F, 0F, 0F, 3, 4, 3),
          PartPose.offset(-1.5F, 13F, 2F));
    private static final ModelPartData VAT_9 = new ModelPartData("vat9", CubeListBuilder.create()
          .texOffs(0, 23)
          .addBox(0F, 0F, 0F, 3, 4, 3),
          PartPose.offset(2F, 13F, 2F));
    private static final ModelPartData VAT_8 = new ModelPartData("vat8", CubeListBuilder.create()
          .texOffs(0, 23)
          .addBox(0F, 0F, 0F, 3, 4, 3),
          PartPose.offset(2F, 13F, -1.5F));
    private static final ModelPartData VAT_7 = new ModelPartData("vat7", CubeListBuilder.create()
          .texOffs(0, 23)
          .addBox(0F, 0F, 0F, 3, 4, 3),
          PartPose.offset(2F, 13F, -5F));
    private static final ModelPartData VAT_4 = new ModelPartData("vat4", CubeListBuilder.create()
          .texOffs(0, 23)
          .addBox(0F, 0F, 0F, 3, 4, 3),
          PartPose.offset(-1.5F, 13F, -5F));
    private static final ModelPartData BACK_EDGE_2 = new ModelPartData("backEdge2", CubeListBuilder.create()
          .texOffs(8, 0)
          .addBox(0F, 0F, 0F, 1, 8, 1),
          PartPose.offset(7F, 9F, 7F));
    private static final ModelPartData BACK = new ModelPartData("back", CubeListBuilder.create()
          .texOffs(48, 0)
          .addBox(0F, 0F, 0F, 14, 8, 2),
          PartPose.offset(-7F, 9F, 6F));
    private static final ModelPartData BACK_EDGE_1 = new ModelPartData("backEdge1", CubeListBuilder.create()
          .texOffs(8, 0)
          .addBox(0F, 0F, 0F, 1, 8, 1),
          PartPose.offset(-8F, 9F, 7F));
    private static final ModelPartData VENTS = new ModelPartData("vents", CubeListBuilder.create()
          .texOffs(70, 0)
          .addBox(0F, 0F, 0F, 8, 2, 10),
          PartPose.offset(-4F, 9F, -4F));
    private static final ModelPartData SUPPORT_1 = new ModelPartData("support1", CubeListBuilder.create()
          .texOffs(4, 0)
          .addBox(0F, 0F, 0F, 1, 2, 1),
          PartPose.offset(-7F, 9F, -7F));
    private static final ModelPartData VAT_1 = new ModelPartData("vat1", CubeListBuilder.create()
          .texOffs(0, 23)
          .addBox(0F, 0F, 0F, 3, 4, 3),
          PartPose.offset(-5F, 13F, -5F));
    private static final ModelPartData NOZZLE_8 = new ModelPartData("nozzle8", CubeListBuilder.create()
          .addBox(0F, 0F, 0F, 1, 1, 1),
          PartPose.offset(3F, 11.5F, -0.5F));
    private static final ModelPartData NOZZLE_5 = new ModelPartData("nozzle5", CubeListBuilder.create()
          .addBox(0F, 0F, 0F, 1, 1, 1),
          PartPose.offset(-0.5F, 11.5F, -0.5F));
    private static final ModelPartData NOZZLE_7 = new ModelPartData("nozzle7", CubeListBuilder.create()
          .addBox(0F, 0F, 0F, 1, 1, 1),
          PartPose.offset(3F, 11.5F, -4F));
    private static final ModelPartData NOZZLE_4 = new ModelPartData("nozzle4", CubeListBuilder.create()
          .addBox(0F, 0F, 0F, 1, 1, 1),
          PartPose.offset(-0.5F, 11.5F, -4F));
    private static final ModelPartData NOZZLE_9 = new ModelPartData("nozzle9", CubeListBuilder.create()
          .addBox(0F, 0F, 0F, 1, 1, 1),
          PartPose.offset(3F, 11.5F, 3F));
    private static final ModelPartData NOZZLE_6 = new ModelPartData("nozzle6", CubeListBuilder.create()
          .addBox(0F, 0F, 0F, 1, 1, 1),
          PartPose.offset(-0.5F, 11.5F, 3F));
    private static final ModelPartData NOZZLE_3 = new ModelPartData("nozzle3", CubeListBuilder.create()
          .addBox(0F, 0F, 0F, 1, 1, 1),
          PartPose.offset(-4F, 11.5F, 3F));
    private static final ModelPartData NOZZLE_2 = new ModelPartData("nozzle2", CubeListBuilder.create()
          .addBox(0F, 0F, 0F, 1, 1, 1),
          PartPose.offset(-4F, 11.5F, -0.5F));
    private static final ModelPartData NOZZLE_1 = new ModelPartData("nozzle1", CubeListBuilder.create()
          .addBox(0F, 0F, 0F, 1, 1, 1),
          PartPose.offset(-4F, 11.5F, -4F));
    private static final ModelPartData GLASS = new ModelPartData("glass", CubeListBuilder.create()
          .texOffs(64, 14)
          .addBox(0F, 0F, 0F, 14, 5, 13),
          PartPose.offset(-7F, 12F, -7F));
    private static final ModelPartData PORT_TOGGLE_1 = new ModelPartData("portToggle1", CubeListBuilder.create()
          .texOffs(106, 0)
          .addBox(0F, 0F, 0F, 1, 10, 10),
          PartPose.offset(-8.01F, 10.99F, -5F));
    private static final ModelPartData PORT_TOGGLE_2 = new ModelPartData("portToggle2", CubeListBuilder.create()
          .texOffs(64, 32)
          .addBox(0F, 0F, 0F, 1, 8, 8),
          PartPose.offset(7.01F, 12F, -4F));

    public static LayerDefinition createLayerDefinition() {
        return createLayerDefinition(128, 64, SUPPORT_2, VAT_5, TOP_2, TOP, BASE, VAT_2, VAT_3, VAT_6, VAT_9, VAT_8,
              VAT_7, VAT_4, BACK_EDGE_2, BACK, BACK_EDGE_1, VENTS, SUPPORT_1, VAT_1, NOZZLE_8, NOZZLE_5, NOZZLE_7, NOZZLE_4, NOZZLE_9, NOZZLE_6,
              NOZZLE_3, NOZZLE_2, NOZZLE_1, GLASS, PORT_TOGGLE_1, PORT_TOGGLE_2);
    }

    private final RenderType RENDER_TYPE = renderType(DISSOLUTION_TEXTURE);
    private final RenderType GLASS_RENDER_TYPE = MekanismRenderType.standard(DISSOLUTION_TEXTURE);
    private final List<ModelPart> parts;
    private final ModelPart glass;

    public ModelChemicalDissolutionChamber(EntityModelSet entityModelSet) {
        super(RenderType::entitySolid);
        ModelPart root = entityModelSet.bakeLayer(DISSOLUTION_LAYER);
        parts = getRenderableParts(root, SUPPORT_2, VAT_5, TOP_2, TOP, BASE, VAT_2, VAT_3, VAT_6, VAT_9, VAT_8, VAT_7, VAT_4, BACK_EDGE_2, BACK,
              BACK_EDGE_1, VENTS, SUPPORT_1, VAT_1, NOZZLE_8, NOZZLE_5, NOZZLE_7, NOZZLE_4, NOZZLE_9, NOZZLE_6, NOZZLE_3, NOZZLE_2, NOZZLE_1,
              PORT_TOGGLE_1, PORT_TOGGLE_2);
        glass = GLASS.getFromRoot(root);
    }

    public void render(@Nonnull PoseStack matrix, @Nonnull MultiBufferSource renderer, int light, int overlayLight, boolean hasEffect) {
        renderToBuffer(matrix, getVertexConsumer(renderer, RENDER_TYPE, hasEffect), light, overlayLight, 1, 1, 1, 1);
        //Render the glass on a more translucent layer
        //Note: The glass makes water, ice etc. behind it invisible. This is due to an engine limitation
        glass.render(matrix, getVertexConsumer(renderer, GLASS_RENDER_TYPE, hasEffect), light, overlayLight, 1, 1, 1, 1);
    }

    @Override
    public void renderToBuffer(@Nonnull PoseStack poseStack, @Nonnull VertexConsumer vertexConsumer, int light, int overlayLight, float red, float green, float blue, float alpha) {
        renderPartsToBuffer(parts, poseStack, vertexConsumer, light, overlayLight, red, green, blue, alpha);
    }
}