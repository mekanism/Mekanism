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

public class ModelQuantumEntangloporter extends MekanismJavaModel {

    public static final ModelLayerLocation ENTANGLOPORTER_LAYER = new ModelLayerLocation(Mekanism.rl("quantum_entangloporter"), "main");
    private static final ResourceLocation ENTANGLOPORTER_TEXTURE = MekanismUtils.getResource(ResourceType.RENDER, "quantum_entangloporter.png");
    private static final ResourceLocation OVERLAY = MekanismUtils.getResource(ResourceType.RENDER, "quantum_entangloporter_overlay.png");

    private static final ModelPartData PORT_TOP = new ModelPartData("portTop", CubeListBuilder.create()
          .texOffs(36, 0)
          .addBox(0F, 0F, 0F, 8, 1, 8),
          PartPose.offset(-4F, 8F, -4F));
    private static final ModelPartData PORT_BOTTOM = new ModelPartData("portBottom", CubeListBuilder.create()
          .texOffs(36, 9)
          .addBox(0F, 0F, 0F, 8, 1, 8),
          PartPose.offset(-4F, 23F, -4F));
    private static final ModelPartData PORT_LEFT = new ModelPartData("portLeft", CubeListBuilder.create()
          .addBox(0F, 0F, 0F, 1, 8, 8),
          PartPose.offset(-8F, 12F, -4F));
    private static final ModelPartData PORT_RIGHT = new ModelPartData("portRight", CubeListBuilder.create()
          .mirror()
          .addBox(0F, 0F, 0F, 1, 8, 8),
          PartPose.offset(7F, 12F, -4F));
    private static final ModelPartData PORT_BACK = new ModelPartData("portBack", CubeListBuilder.create()
          .texOffs(18, 9)
          .addBox(0F, 0F, 0F, 8, 8, 1),
          PartPose.offset(-4F, 12F, 7F));
    private static final ModelPartData PORT_FRONT = new ModelPartData("portFront", CubeListBuilder.create()
          .texOffs(18, 0)
          .addBox(0F, 0F, 0F, 8, 8, 1),
          PartPose.offset(-4F, 12F, -8F));
    private static final ModelPartData ENERGY_CUBE_CORE = new ModelPartData("energyCubeCore", CubeListBuilder.create()
          .texOffs(0, 41)
          .addBox(-2F, -2F, -2F, 4, 4, 4),
          PartPose.offsetAndRotation(0F, 16F, 0F, 0.7132579F, 0.403365F, 0.645384F));
    private static final ModelPartData FRAME_EDGE_1 = new ModelPartData("frameEdge1", CubeListBuilder.create()
          .texOffs(0, 16)
          .addBox(0F, 0F, 0F, 1, 10, 1),
          PartPose.offset(-7.5F, 11F, -7.5F));
    private static final ModelPartData FRAME_EDGE_2 = new ModelPartData("frameEdge2", CubeListBuilder.create()
          .texOffs(0, 16)
          .addBox(0F, 0F, 0F, 1, 10, 1),
          PartPose.offset(6.5F, 11F, -7.5F));
    private static final ModelPartData FRAME_EDGE_3 = new ModelPartData("frameEdge3", CubeListBuilder.create()
          .texOffs(0, 16)
          .addBox(0F, 0F, 0F, 1, 10, 1),
          PartPose.offset(-7.5F, 11F, 6.5F));
    private static final ModelPartData FRAME_EDGE_4 = new ModelPartData("frameEdge4", CubeListBuilder.create()
          .texOffs(0, 16)
          .addBox(0F, 0F, 0F, 1, 10, 1),
          PartPose.offset(6.5F, 11F, 6.5F));
    private static final ModelPartData FRAME_EDGE_5 = new ModelPartData("frameEdge5", CubeListBuilder.create()
          .texOffs(4, 27)
          .addBox(0F, 0F, 0F, 10, 1, 1),
          PartPose.offset(-5F, 22.5F, -7.5F));
    private static final ModelPartData FRAME_EDGE_6 = new ModelPartData("frameEdge6", CubeListBuilder.create()
          .texOffs(4, 16)
          .addBox(0F, 0F, 0F, 1, 1, 10),
          PartPose.offset(-7.5F, 22.5F, -5F));
    private static final ModelPartData FRAME_EDGE_7 = new ModelPartData("frameEdge7", CubeListBuilder.create()
          .texOffs(4, 16)
          .addBox(0F, 0F, 0F, 1, 1, 10),
          PartPose.offset(6.5F, 22.5F, -5F));
    private static final ModelPartData FRAME_EDGE_8 = new ModelPartData("frameEdge8", CubeListBuilder.create()
          .texOffs(4, 27)
          .addBox(0F, 0F, 0F, 10, 1, 1),
          PartPose.offset(-5F, 22.5F, 6.5F));
    private static final ModelPartData FRAME_EDGE_9 = new ModelPartData("frameEdge9", CubeListBuilder.create()
          .texOffs(4, 27)
          .addBox(0F, 0F, 0F, 10, 1, 1),
          PartPose.offset(-5F, 8.5F, -7.5F));
    private static final ModelPartData FRAME_EDGE_10 = new ModelPartData("frameEdge10", CubeListBuilder.create()
          .texOffs(4, 16)
          .addBox(0F, 0F, 0F, 1, 1, 10),
          PartPose.offset(-7.5F, 8.5F, -5F));
    private static final ModelPartData FRAME_EDGE_11 = new ModelPartData("frameEdge11", CubeListBuilder.create()
          .texOffs(4, 16)
          .addBox(0F, 0F, 0F, 1, 1, 10),
          PartPose.offset(6.5F, 8.5F, -5F));
    private static final ModelPartData FRAME_EDGE_12 = new ModelPartData("frameEdge12", CubeListBuilder.create()
          .texOffs(4, 27)
          .addBox(0F, 0F, 0F, 10, 1, 1),
          PartPose.offset(-5F, 8.5F, 6.5F));
    private static final ModelPartData FRAME_1 = new ModelPartData("frame1", CubeListBuilder.create()
          .texOffs(0, 29)
          .addBox(0F, 0F, 0F, 2, 10, 2),
          PartPose.offset(-7F, 11F, -7F));
    private static final ModelPartData FRAME_2 = new ModelPartData("frame2", CubeListBuilder.create()
          .texOffs(0, 29)
          .mirror()
          .addBox(0F, 0F, 0F, 2, 10, 2),
          PartPose.offset(5F, 11F, -7F));
    private static final ModelPartData FRAME_3 = new ModelPartData("frame3", CubeListBuilder.create()
          .texOffs(8, 29)
          .addBox(0F, 0F, 0F, 2, 10, 2),
          PartPose.offset(-7F, 11F, 5F));
    private static final ModelPartData FRAME_4 = new ModelPartData("frame4", CubeListBuilder.create()
          .texOffs(8, 29)
          .mirror()
          .addBox(0F, 0F, 0F, 2, 10, 2),
          PartPose.offset(5F, 11F, 5F));
    private static final ModelPartData FRAME_5 = new ModelPartData("frame5", CubeListBuilder.create()
          .texOffs(16, 45)
          .addBox(0F, 0F, 0F, 10, 2, 2),
          PartPose.offset(-5F, 21F, -7F));
    private static final ModelPartData FRAME_6 = new ModelPartData("frame6", CubeListBuilder.create()
          .texOffs(40, 29)
          .addBox(0F, 0F, 0F, 2, 2, 10),
          PartPose.offset(-7F, 21F, -5F));
    private static final ModelPartData FRAME_7 = new ModelPartData("frame7", CubeListBuilder.create()
          .texOffs(40, 29)
          .mirror()
          .addBox(0F, 0F, 0F, 2, 2, 10),
          PartPose.offset(5F, 21F, -5F));
    private static final ModelPartData FRAME_8 = new ModelPartData("frame8", CubeListBuilder.create()
          .texOffs(16, 49)
          .addBox(0F, 0F, 0F, 10, 2, 2),
          PartPose.offset(-5F, 21F, 5F));
    private static final ModelPartData FRAME_9 = new ModelPartData("frame9", CubeListBuilder.create()
          .texOffs(16, 41)
          .addBox(0F, 0F, 0F, 10, 2, 2),
          PartPose.offset(-5F, 9F, -7F));
    private static final ModelPartData FRAME_10 = new ModelPartData("frame10", CubeListBuilder.create()
          .texOffs(16, 29)
          .addBox(0F, 0F, 0F, 2, 2, 10),
          PartPose.offset(-7F, 9F, -5F));
    private static final ModelPartData FRAME_11 = new ModelPartData("frame11", CubeListBuilder.create()
          .texOffs(16, 29)
          .mirror()
          .addBox(0F, 0F, 0F, 2, 2, 10),
          PartPose.offset(5F, 9F, -5F));
    private static final ModelPartData FRAME_12 = new ModelPartData("frame12", CubeListBuilder.create()
          .texOffs(16, 53)
          .addBox(0F, 0F, 0F, 10, 2, 2),
          PartPose.offset(-5F, 9F, 5F));
    private static final ModelPartData CORNER_1 = new ModelPartData("corner1", CubeListBuilder.create()
          .texOffs(0, 49)
          .addBox(0F, 0F, 0F, 3, 3, 3),
          PartPose.offset(-8F, 8F, -8F));
    private static final ModelPartData CORNER_2 = new ModelPartData("corner2", CubeListBuilder.create()
          .texOffs(0, 49)
          .addBox(0F, 0F, 0F, 3, 3, 3),
          PartPose.offset(5F, 8F, -8F));
    private static final ModelPartData CORNER_3 = new ModelPartData("corner3", CubeListBuilder.create()
          .texOffs(0, 49)
          .addBox(0F, 0F, 0F, 3, 3, 3),
          PartPose.offset(-8F, 8F, 5F));
    private static final ModelPartData CORNER_4 = new ModelPartData("corner4", CubeListBuilder.create()
          .texOffs(0, 49)
          .addBox(0F, 0F, 0F, 3, 3, 3),
          PartPose.offset(5F, 8F, 5F));
    private static final ModelPartData CORNER_5 = new ModelPartData("corner5", CubeListBuilder.create()
          .texOffs(0, 49)
          .addBox(0F, 0F, 0F, 3, 3, 3),
          PartPose.offset(-8F, 21F, -8F));
    private static final ModelPartData CORNER_6 = new ModelPartData("corner6", CubeListBuilder.create()
          .texOffs(0, 49)
          .addBox(0F, 0F, 0F, 3, 3, 3),
          PartPose.offset(5F, 21F, -8F));
    private static final ModelPartData CORNER_7 = new ModelPartData("corner7", CubeListBuilder.create()
          .texOffs(0, 49)
          .addBox(0F, 0F, 0F, 3, 3, 3),
          PartPose.offset(-8F, 21F, 5F));
    private static final ModelPartData CORNER_8 = new ModelPartData("corner8", CubeListBuilder.create()
          .texOffs(0, 49)
          .addBox(0F, 0F, 0F, 3, 3, 3),
          PartPose.offset(5F, 21F, 5F));

    public static LayerDefinition createLayerDefinition() {
        return createLayerDefinition(128, 64, PORT_TOP, PORT_BOTTOM, PORT_LEFT, PORT_RIGHT, PORT_BACK, PORT_FRONT, ENERGY_CUBE_CORE,
              FRAME_EDGE_1, FRAME_EDGE_2, FRAME_EDGE_3, FRAME_EDGE_4, FRAME_EDGE_5, FRAME_EDGE_6, FRAME_EDGE_7, FRAME_EDGE_8, FRAME_EDGE_9, FRAME_EDGE_10,
              FRAME_EDGE_11, FRAME_EDGE_12, FRAME_1, FRAME_2, FRAME_3, FRAME_4, FRAME_5, FRAME_6, FRAME_7, FRAME_8, FRAME_9, FRAME_10, FRAME_11, FRAME_12,
              CORNER_1, CORNER_2, CORNER_3, CORNER_4, CORNER_5, CORNER_6, CORNER_7, CORNER_8);
    }

    private final RenderType RENDER_TYPE_OVERLAY = MekanismRenderType.standard(OVERLAY);
    private final RenderType RENDER_TYPE = renderType(ENTANGLOPORTER_TEXTURE);
    private final List<ModelPart> parts;

    public ModelQuantumEntangloporter(EntityModelSet entityModelSet) {
        super(RenderType::entitySolid);
        ModelPart root = entityModelSet.bakeLayer(ENTANGLOPORTER_LAYER);
        parts = getRenderableParts(root, PORT_TOP, PORT_BOTTOM, PORT_LEFT, PORT_RIGHT, PORT_BACK, PORT_FRONT, ENERGY_CUBE_CORE, FRAME_EDGE_1, FRAME_EDGE_2,
              FRAME_EDGE_3, FRAME_EDGE_4, FRAME_EDGE_5, FRAME_EDGE_6, FRAME_EDGE_7, FRAME_EDGE_8, FRAME_EDGE_9, FRAME_EDGE_10, FRAME_EDGE_11, FRAME_EDGE_12,
              FRAME_1, FRAME_2, FRAME_3, FRAME_4, FRAME_5, FRAME_6, FRAME_7, FRAME_8, FRAME_9, FRAME_10, FRAME_11, FRAME_12, CORNER_1, CORNER_2, CORNER_3,
              CORNER_4, CORNER_5, CORNER_6, CORNER_7, CORNER_8);

    }

    public void render(@Nonnull PoseStack matrix, @Nonnull MultiBufferSource renderer, int light, int overlayLight, boolean renderMain, boolean hasEffect) {
        if (renderMain) {
            renderToBuffer(matrix, getVertexConsumer(renderer, RENDER_TYPE, hasEffect), light, overlayLight, 1, 1, 1, 1);
        }
        matrix.pushPose();
        matrix.scale(1.001F, 1.001F, 1.001F);
        matrix.translate(0, -0.0011, 0);
        renderToBuffer(matrix, getVertexConsumer(renderer, RENDER_TYPE_OVERLAY, hasEffect), MekanismRenderer.FULL_LIGHT, overlayLight, 1, 1, 1, 1);
        matrix.popPose();
    }

    @Override
    public void renderToBuffer(@Nonnull PoseStack poseStack, @Nonnull VertexConsumer vertexConsumer, int light, int overlayLight, float red, float green, float blue,
          float alpha) {
        renderPartsToBuffer(parts, poseStack, vertexConsumer, light, overlayLight, red, green, blue, alpha);
    }
}