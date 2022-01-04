package mekanism.generators.client.model;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Vector3f;
import java.util.List;
import javax.annotation.Nonnull;
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

public class ModelTurbine extends MekanismJavaModel {

    public static final ModelLayerLocation TURBINE_LAYER = new ModelLayerLocation(MekanismGenerators.rl("turbine"), "main");
    private static final ResourceLocation TURBINE_TEXTURE = MekanismGenerators.rl("render/turbine.png");
    private static final float BLADE_ROTATE = 0.418879F;

    private static final ModelPartData EXTENSION_SOUTH = new ModelPartData("extensionSouth", CubeListBuilder.create()
          .addBox(-1.0F, 0.0F, 1.0F, 2, 1, 3),
          PartPose.offsetAndRotation(0.0F, 20.0F, 0.0F, 0.0F, 0.0F, -BLADE_ROTATE));
    private static final ModelPartData EXTENSION_WEST = new ModelPartData("extensionWest", CubeListBuilder.create()
          .texOffs(0, 4)
          .addBox(-4.0F, 0.0F, -1.0F, 3, 1, 2),
          PartPose.offsetAndRotation(0.0F, 20.0F, 0.0F, BLADE_ROTATE, 0.0F, 0.0F));
    private static final ModelPartData BLADE_EAST = new ModelPartData("bladeEast", CubeListBuilder.create()
          .texOffs(10, 5)
          .addBox(4.0F, 0.0F, -1.5F, 4, 1, 3),
          PartPose.offsetAndRotation(0.0F, 20.0F, 0.0F, -BLADE_ROTATE, 0.0F, 0.0F));
    private static final ModelPartData BLADE_NORTH = new ModelPartData("bladeNorth", CubeListBuilder.create()
          .texOffs(10, 0)
          .addBox(-1.5F, 0.0F, -8.0F, 3, 1, 4),
          PartPose.offsetAndRotation(0.0F, 20.0F, 0.0F, 0.0F, 0.0F, BLADE_ROTATE));
    private static final ModelPartData EXTENSION_EAST = new ModelPartData("extensionEast", CubeListBuilder.create()
          .texOffs(0, 4)
          .addBox(1.0F, 0.0F, -1.0F, 3, 1, 2),
          PartPose.offsetAndRotation(0.0F, 20.0F, 0.0F, -BLADE_ROTATE, 0.0F, 0.0F));
    private static final ModelPartData BLADE_SOUTH = new ModelPartData("bladeSouth", CubeListBuilder.create()
          .texOffs(10, 0)
          .addBox(-1.5F, 0.0F, 4.0F, 3, 1, 4),
          PartPose.offsetAndRotation(0.0F, 20.0F, 0.0F, 0.0F, 0.0F, -BLADE_ROTATE));
    private static final ModelPartData EXTENSION_NORTH = new ModelPartData("extensionNorth", CubeListBuilder.create()
          .addBox(-1.0F, 0.0F, -4.0F, 2, 1, 3),
          PartPose.offsetAndRotation(0.0F, 20.0F, 0.0F, 0.0F, 0.0F, BLADE_ROTATE));
    private static final ModelPartData BLADE_WEST = new ModelPartData("bladeWest", CubeListBuilder.create()
          .texOffs(10, 5)
          .addBox(-8.0F, 0.0F, -1.5F, 4, 1, 3),
          PartPose.offsetAndRotation(0.0F, 20.0F, 0.0F, BLADE_ROTATE, 0.0F, 0.0F));

    public static LayerDefinition createLayerDefinition() {
        return createLayerDefinition(64, 64, EXTENSION_SOUTH, EXTENSION_WEST, BLADE_EAST, BLADE_NORTH, EXTENSION_EAST, BLADE_SOUTH,
              EXTENSION_NORTH, BLADE_WEST);
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

    public VertexConsumer getBuffer(@Nonnull MultiBufferSource renderer) {
        return renderer.getBuffer(RENDER_TYPE);
    }

    public void render(@Nonnull PoseStack matrix, VertexConsumer buffer, int light, int overlayLight, int index) {
        matrix.pushPose();
        matrix.mulPose(Vector3f.YP.rotationDegrees(index * 5));
        renderToBuffer(matrix, buffer, light, overlayLight, 1, 1, 1, 1);
        float scale = index * 0.5F;
        float widthDiv = 16;
        renderBlade(matrix, buffer, light, overlayLight, bladeWest, scale, scale / widthDiv, -0.25, 0);
        renderBlade(matrix, buffer, light, overlayLight, bladeEast, scale, scale / widthDiv, 0.25, 0);
        renderBlade(matrix, buffer, light, overlayLight, bladeNorth, scale / widthDiv, scale, 0, -0.25);
        renderBlade(matrix, buffer, light, overlayLight, bladeSouth, scale / widthDiv, scale, 0, 0.25);
        matrix.popPose();
    }

    @Override
    public void renderToBuffer(@Nonnull PoseStack poseStack, @Nonnull VertexConsumer vertexConsumer, int light, int overlayLight, float red, float green, float blue,
          float alpha) {
        renderPartsToBuffer(parts, poseStack, vertexConsumer, light, overlayLight, red, green, blue, alpha);
    }

    private void renderBlade(@Nonnull PoseStack matrix, @Nonnull VertexConsumer vertexBuilder, int light, int overlayLight, ModelPart blade, float scaleX,
          float scaleZ, double transX, double transZ) {
        matrix.pushPose();
        matrix.translate(transX, 0, transZ);
        matrix.scale(1.0F + scaleX, 1.0F, 1.0F + scaleZ);
        matrix.translate(-transX, 0, -transZ);
        blade.render(matrix, vertexBuilder, light, overlayLight, 1, 1, 1, 1);
        matrix.popPose();
    }
}