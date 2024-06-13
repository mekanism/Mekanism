package mekanism.generators.client.model;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
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
import org.jetbrains.annotations.NotNull;

public class ModelTurbine extends MekanismJavaModel {

    public static final ModelLayerLocation TURBINE_LAYER = new ModelLayerLocation(MekanismGenerators.rl("turbine"), "main");
    private static final ResourceLocation TURBINE_TEXTURE = MekanismGenerators.rl("render/turbine.png");
    private static final float BLADE_ROTATE = 0.418879F;

    private static final ModelPartData EXTENSION_NORTH = new ModelPartData("extensionNorth", CubeListBuilder.create()
          .texOffs(0, 9)
          .addBox(-1, 0, -4, 2, 1, 3),
          PartPose.offsetAndRotation(0, 20, 0, 0, 0, BLADE_ROTATE));
    private static final ModelPartData EXTENSION_EAST = new ModelPartData("extensionEast", CubeListBuilder.create()
          .texOffs(0, 13)
          .addBox(1, 0, -1, 3, 1, 2),
          PartPose.offsetAndRotation(0, 20, 0, -BLADE_ROTATE, 0, 0));
    private static final ModelPartData EXTENSION_SOUTH = new ModelPartData("extensionSouth", CubeListBuilder.create()
          .texOffs(0, 9)
          .addBox(-1, 0, 1, 2, 1, 3),
          PartPose.offsetAndRotation(0, 20, 0, 0, 0, -BLADE_ROTATE));
    private static final ModelPartData EXTENSION_WEST = new ModelPartData("extensionWest", CubeListBuilder.create()
          .texOffs(0, 13)
          .addBox(-4, 0, -1, 3, 1, 2),
          PartPose.offsetAndRotation(0, 20, 0, BLADE_ROTATE, 0, 0));
    private static final ModelPartData BLADE_NORTH = new ModelPartData("bladeNorth", CubeListBuilder.create()
          .addBox(-1.5F, 0, -8, 3, 1, 4),
          PartPose.offsetAndRotation(0, 20, 0, 0, 0, BLADE_ROTATE));
    private static final ModelPartData BLADE_EAST = new ModelPartData("bladeEast", CubeListBuilder.create()
          .texOffs(0, 5)
          .addBox(4, 0, -1.5F, 4, 1, 3),
          PartPose.offsetAndRotation(0, 20, 0, -BLADE_ROTATE, 0, 0));
    private static final ModelPartData BLADE_SOUTH = new ModelPartData("bladeSouth", CubeListBuilder.create()
          .addBox(-1.5F, 0, 4, 3, 1, 4),
          PartPose.offsetAndRotation(0, 20, 0, 0, 0, -BLADE_ROTATE));
    private static final ModelPartData BLADE_WEST = new ModelPartData("bladeWest", CubeListBuilder.create()
          .texOffs(0, 5)
          .addBox(-8, 0, -1.5F, 4, 1, 3),
          PartPose.offsetAndRotation(0, 20, 0, BLADE_ROTATE, 0, 0));

    public static LayerDefinition createLayerDefinition() {
        return createLayerDefinition(16, 16, EXTENSION_NORTH, EXTENSION_EAST, EXTENSION_SOUTH, EXTENSION_WEST, BLADE_NORTH, BLADE_EAST, BLADE_SOUTH,
              BLADE_WEST);
    }

    private final RenderType RENDER_TYPE = renderType(TURBINE_TEXTURE);
    private final List<ModelPart> parts;
    private final ModelPart bladeWest;
    private final ModelPart bladeEast;
    private final ModelPart bladeNorth;
    private final ModelPart bladeSouth;

    public ModelTurbine(EntityModelSet entityModelSet) {
        super(RenderType::entitySolid);
        ModelPart root = entityModelSet.bakeLayer(TURBINE_LAYER);
        parts = getRenderableParts(root, EXTENSION_SOUTH, EXTENSION_WEST, EXTENSION_EAST, EXTENSION_NORTH);
        bladeWest = BLADE_WEST.getFromRoot(root);
        bladeEast = BLADE_EAST.getFromRoot(root);
        bladeNorth = BLADE_NORTH.getFromRoot(root);
        bladeSouth = BLADE_SOUTH.getFromRoot(root);
    }

    public VertexConsumer getBuffer(@NotNull MultiBufferSource renderer) {
        return renderer.getBuffer(RENDER_TYPE);
    }

    public void render(@NotNull PoseStack matrix, VertexConsumer buffer, int light, int overlayLight, int index) {
        matrix.pushPose();
        matrix.mulPose(Axis.YP.rotationDegrees(index * 5));
        renderToBuffer(matrix, buffer, light, overlayLight, 0xFFFFFFFF);
        float scale = index * 0.5F;
        float adjustedScale = scale / 16;
        renderBlade(matrix, buffer, light, overlayLight, bladeWest, scale, adjustedScale, -0.25, 0);
        renderBlade(matrix, buffer, light, overlayLight, bladeEast, scale, adjustedScale, 0.25, 0);
        renderBlade(matrix, buffer, light, overlayLight, bladeNorth, adjustedScale, scale, 0, -0.25);
        renderBlade(matrix, buffer, light, overlayLight, bladeSouth, adjustedScale, scale, 0, 0.25);
        matrix.popPose();
    }

    @Override
    public void renderToBuffer(@NotNull PoseStack poseStack, @NotNull VertexConsumer vertexConsumer, int light, int overlayLight, int color) {
        renderPartsToBuffer(parts, poseStack, vertexConsumer, light, overlayLight, color);
    }

    private void renderBlade(@NotNull PoseStack matrix, @NotNull VertexConsumer vertexBuilder, int light, int overlayLight, ModelPart blade, float scaleX,
          float scaleZ, double transX, double transZ) {
        matrix.pushPose();
        matrix.translate(transX, 0, transZ);
        matrix.scale(1 + scaleX, 1, 1 + scaleZ);
        matrix.translate(-transX, 0, -transZ);
        blade.render(matrix, vertexBuilder, light, overlayLight, 0xFFFFFFFF);
        matrix.popPose();
    }
}