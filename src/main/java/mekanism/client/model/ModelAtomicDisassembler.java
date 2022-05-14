package mekanism.client.model;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import java.util.List;
import javax.annotation.Nonnull;
import mekanism.client.render.MekanismRenderType;
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

public class ModelAtomicDisassembler extends MekanismJavaModel {

    public static final ModelLayerLocation DISASSEMBLER_LAYER = new ModelLayerLocation(Mekanism.rl("atomic_disassembler"), "main");
    private static final ResourceLocation DISASSEMBLER_TEXTURE = MekanismUtils.getResource(ResourceType.RENDER, "atomic_disassembler.png");

    private static final ModelPartData HANDLE = new ModelPartData("handle", CubeListBuilder.create()
          .texOffs(0, 10)
          .addBox(0, -1, -3, 1, 16, 1));
    private static final ModelPartData HANDLE_TOP = new ModelPartData("handleTop", CubeListBuilder.create()
          .texOffs(34, 9)
          .addBox(-0.5F, -3.5F, -3.5F, 2, 5, 2));
    private static final ModelPartData BLADE_BACK = new ModelPartData("bladeBack", CubeListBuilder.create()
          .texOffs(42, 0)
          .addBox(0, -4, -4, 1, 2, 10));
    private static final ModelPartData HEAD = new ModelPartData("head", CubeListBuilder.create()
          .texOffs(24, 0)
          .addBox(-5, -5.7F, -5.5F, 3, 3, 6),
          PartPose.rotation(0, 0, 0.7853982F));
    private static final ModelPartData NECK = new ModelPartData("neck", CubeListBuilder.create()
          .addBox(-0.5F, -6, -7, 2, 2, 8));
    private static final ModelPartData BLADE_FRONT_UPPER = new ModelPartData("bladeFrontUpper", CubeListBuilder.create()
          .texOffs(60, 0)
          .addBox(0, -0.5333334F, -9.6F, 1, 3, 1),
          PartPose.rotation(-0.7853982F, 0, 0));
    private static final ModelPartData BLADE_FRONT_LOWER = new ModelPartData("bladeFrontLower", CubeListBuilder.create()
          .texOffs(58, 0)
          .addBox(0, -9.58F, -4, 1, 5, 2),
          PartPose.rotation(0.7853982F, 0, 0));
    private static final ModelPartData NECK_ANGLED = new ModelPartData("neckAngled", CubeListBuilder.create()
          .texOffs(12, 0)
          .addBox(-0.5F, -8.2F, -2.5F, 2, 1, 1),
          PartPose.rotation(0.7853982F, 0, 0));
    private static final ModelPartData BLADE_FRONT_CONNECTOR = new ModelPartData("bladeFrontConnector", CubeListBuilder.create()
          .texOffs(56, 0)
          .addBox(0, -2.44F, -6.07F, 1, 2, 3));
    private static final ModelPartData BLADE_HOLDER_BACK = new ModelPartData("bladeHolderBack", CubeListBuilder.create()
          .texOffs(42, 14)
          .addBox(-0.5F, -4.5F, 3.5F, 2, 1, 1));
    private static final ModelPartData BLADE_HOLDER_MAIN = new ModelPartData("bladeHolderMain", CubeListBuilder.create()
          .texOffs(30, 16)
          .addBox(-0.5F, -3.5F, -1.5F, 2, 1, 4));
    private static final ModelPartData BLADE_HOLDER_FRONT = new ModelPartData("bladeHolderFront", CubeListBuilder.create()
          .texOffs(42, 12)
          .addBox(-0.5F, -4.5F, 1.5F, 2, 1, 1));
    private static final ModelPartData REAR_BAR = new ModelPartData("rearBar", CubeListBuilder.create()
          .texOffs(4, 10)
          .addBox(0, -5.3F, 0, 1, 1, 7));
    private static final ModelPartData BLADE_BACK_SMALL = new ModelPartData("bladeBackSmall", CubeListBuilder.create()
          .texOffs(60, 0)
          .addBox(0, -4, 6, 1, 1, 1));
    private static final ModelPartData HANDLE_BASE = new ModelPartData("handleBase", CubeListBuilder.create()
          .texOffs(26, 9)
          .addBox(-0.5F, 15, -3.5F, 2, 4, 2));
    private static final ModelPartData HANDLE_TOP_BACK = new ModelPartData("handleTopBack", CubeListBuilder.create()
          .texOffs(37, 0)
          .addBox(0, -2, -2, 1, 4, 1));

    public static LayerDefinition createLayerDefinition() {
        return createLayerDefinition(64, 32, HANDLE, HANDLE_TOP, BLADE_BACK, HEAD, NECK, BLADE_FRONT_UPPER, BLADE_FRONT_LOWER,
              NECK_ANGLED, BLADE_FRONT_CONNECTOR, BLADE_HOLDER_BACK, BLADE_HOLDER_MAIN, BLADE_HOLDER_FRONT, REAR_BAR, BLADE_BACK_SMALL, HANDLE_BASE,
              HANDLE_TOP_BACK);
    }

    private final RenderType BLADE_RENDER_TYPE = MekanismRenderType.BLADE.apply(DISASSEMBLER_TEXTURE);
    private final RenderType RENDER_TYPE = renderType(DISASSEMBLER_TEXTURE);
    private final List<ModelPart> parts;
    private final List<ModelPart> bladeParts;

    public ModelAtomicDisassembler(EntityModelSet entityModelSet) {
        super(RenderType::entitySolid);
        ModelPart root = entityModelSet.bakeLayer(DISASSEMBLER_LAYER);
        parts = getRenderableParts(root, HANDLE, HANDLE_TOP, HEAD, NECK, REAR_BAR, NECK_ANGLED, BLADE_HOLDER_BACK, BLADE_HOLDER_MAIN,
              BLADE_HOLDER_FRONT, HANDLE_BASE, HANDLE_TOP_BACK);
        bladeParts = getRenderableParts(root, BLADE_FRONT_CONNECTOR, BLADE_BACK, BLADE_FRONT_UPPER, BLADE_FRONT_LOWER, BLADE_BACK_SMALL);
    }

    public void render(@Nonnull PoseStack matrix, @Nonnull MultiBufferSource renderer, int light, int overlayLight, boolean hasEffect) {
        renderToBuffer(matrix, getVertexConsumer(renderer, RENDER_TYPE, hasEffect), light, overlayLight, 1, 1, 1, 1);
        renderPartsToBuffer(bladeParts, matrix, getVertexConsumer(renderer, BLADE_RENDER_TYPE, hasEffect), MekanismRenderer.FULL_LIGHT, overlayLight, 1, 1, 1, 0.75F);
    }

    @Override
    public void renderToBuffer(@Nonnull PoseStack poseStack, @Nonnull VertexConsumer vertexConsumer, int light, int overlayLight, float red, float green, float blue, float alpha) {
        renderPartsToBuffer(parts, poseStack, vertexConsumer, light, overlayLight, red, green, blue, alpha);
    }
}