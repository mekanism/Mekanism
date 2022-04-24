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

public class ModelSolarNeutronActivator extends MekanismJavaModel {

    public static final ModelLayerLocation ACTIVATOR_LAYER = new ModelLayerLocation(Mekanism.rl("solar_neutron_activator"), "main");
    private static final ResourceLocation ACTIVATOR_TEXTURE = MekanismUtils.getResource(ResourceType.RENDER, "solar_neutron_activator.png");

    private static final ModelPartData POLE = new ModelPartData("pole", CubeListBuilder.create()
          .texOffs(116, 0)
          .addBox(0F, 0F, 0F, 4, 15, 2),
          PartPose.offset(-2F, -5F, 6F));
    private static final ModelPartData PANEL_3 = new ModelPartData("panel3", CubeListBuilder.create()
          .texOffs(84, 32)
          .addBox(-6F, 0F, -16F, 6, 1, 16),
          PartPose.offsetAndRotation(-2.75F, -4.95F, 8F, -0.1082104F, 0.0279253F, 0.2617994F));
    private static final ModelPartData PORT = new ModelPartData("port", CubeListBuilder.create()
          .texOffs(0, 45)
          .addBox(0F, 0F, 0F, 8, 8, 1),
          PartPose.offset(-4F, 12F, -8.01F));
    private static final ModelPartData PANEL_1 = new ModelPartData("panel1", CubeListBuilder.create()
          .texOffs(84, 32)
          .mirror()
          .addBox(0F, 0F, -16F, 6, 1, 16),
          PartPose.offsetAndRotation(2.75F, -4.95F, 8F, -0.1082104F, -0.0279253F, -0.2617994F));
    private static final ModelPartData PANEL_2 = new ModelPartData("panel2", CubeListBuilder.create()
          .texOffs(84, 15)
          .addBox(0F, 0F, -16F, 6, 1, 16),
          PartPose.offsetAndRotation(-3F, -5F, 8F, -0.1047198F, 0F, 0F));
    private static final ModelPartData PANEL_BASE = new ModelPartData("panelBase", CubeListBuilder.create()
          .texOffs(28, 45)
          .addBox(0F, 1F, -16F, 6, 1, 14),
          PartPose.offsetAndRotation(-3F, -5F, 9F, -0.1047198F, 0F, 0F));
    private static final ModelPartData PANEL_BRACE_LEFT_2 = new ModelPartData("panelBraceLeft2", CubeListBuilder.create()
          .texOffs(64, 15)
          .addBox(-4F, 0.5F, -5F, 5, 1, 2),
          PartPose.offsetAndRotation(-3F, -5F, 9F, -0.1047198F, 0F, 0.2505517F));
    private static final ModelPartData PANEL_BRACE_RIGHT_2 = new ModelPartData("panelBraceRight2", CubeListBuilder.create()
          .texOffs(64, 15)
          .addBox(-1F, 0.5F, -5F, 5, 1, 2),
          PartPose.offsetAndRotation(3F, -5F, 9F, -0.1047198F, 0F, -0.2555938F));
    private static final ModelPartData PANEL_BRACE_LEFT_1 = new ModelPartData("panelBraceLeft1", CubeListBuilder.create()
          .texOffs(64, 15)
          .addBox(-4F, 0.5F, -15F, 5, 1, 2),
          PartPose.offsetAndRotation(-3F, -5F, 9F, -0.1047198F, 0F, 0.2505517F));
    private static final ModelPartData PANEL_BRACE_RIGHT_1 = new ModelPartData("panelBraceRight1", CubeListBuilder.create()
          .texOffs(64, 15)
          .addBox(-1F, 0.5F, -15F, 5, 1, 2),
          PartPose.offsetAndRotation(3F, -5F, 9F, -0.1047198F, 0F, -0.2555938F));
    private static final ModelPartData PANEL_BRACE = new ModelPartData("panelBrace", CubeListBuilder.create()
          .texOffs(56, 18)
          .addBox(0F, 1.2F, -10F, 2, 2, 9),
          PartPose.offsetAndRotation(-1F, -5F, 8F, -0.1047198F, 0F, 0F));
    private static final ModelPartData BRIDGE = new ModelPartData("bridge", CubeListBuilder.create()
          .texOffs(65, 1)
          .addBox(0F, 0F, 0F, 12, 1, 13),
          PartPose.offset(-6F, 19F, -6F));
    private static final ModelPartData PLATFORM = new ModelPartData("platform", CubeListBuilder.create()
          .texOffs(18, 45)
          .addBox(-2.5F, 1F, -2.5F, 6, 3, 6),
          PartPose.offsetAndRotation(-0.5F, 8F, -2.5F, -0.1047198F, 0F, 0F));
    private static final ModelPartData HOLE_2 = new ModelPartData("hole2", CubeListBuilder.create()
          .texOffs(0, 6)
          .addBox(1F, 0F, 0F, 1, 2, 1),
          PartPose.offsetAndRotation(-0.5F, 8F, -2.5F, -0.1047198F, 0F, 0F));
    private static final ModelPartData HOLE_4 = new ModelPartData("hole4", CubeListBuilder.create()
          .texOffs(0, 3)
          .addBox(-1F, 0F, 1F, 3, 2, 1),
          PartPose.offsetAndRotation(-0.5F, 8F, -2.5F, -0.1047198F, 0F, 0F));
    private static final ModelPartData HOLE_1 = new ModelPartData("hole1", CubeListBuilder.create()
          .texOffs(0, 3)
          .addBox(-1F, 0F, -1F, 3, 2, 1),
          PartPose.offsetAndRotation(-0.5F, 8F, -2.5F, -0.1047198F, 0F, 0F));
    private static final ModelPartData HOLE_3 = new ModelPartData("hole3", CubeListBuilder.create()
          .texOffs(0, 6)
          .addBox(-1F, 0F, 0F, 1, 2, 1),
          PartPose.offsetAndRotation(-0.5F, 8F, -2.5F, -0.1047198F, 0F, 0F));
    private static final ModelPartData BRACE_2 = new ModelPartData("brace2", CubeListBuilder.create()
          .texOffs(0, 11)
          .addBox(0F, 0F, 0F, 1, 1, 2),
          PartPose.offsetAndRotation(1F, 9.5F, -7.1F, 0.1745329F, 0F, 0F));
    private static final ModelPartData TUBE_2C = new ModelPartData("tube2c", CubeListBuilder.create()
          .texOffs(0, 9)
          .addBox(0F, 0F, 0F, 1, 1, 1),
          PartPose.offset(2F, 9F, 4F));
    private static final ModelPartData TUBE_1B = new ModelPartData("tube1b", CubeListBuilder.create()
          .texOffs(0, 14)
          .addBox(0F, 0F, 0F, 6, 1, 1),
          PartPose.offset(-3F, 8F, 2F));
    private static final ModelPartData TUBE_1C = new ModelPartData("tube1c", CubeListBuilder.create()
          .texOffs(0, 9)
          .addBox(0F, 0F, 0F, 1, 1, 1),
          PartPose.offset(2F, 9F, 2F));
    private static final ModelPartData TUBE_2B = new ModelPartData("tube2b", CubeListBuilder.create()
          .texOffs(0, 14)
          .addBox(0F, 0F, 0F, 6, 1, 1),
          PartPose.offset(-3F, 8F, 4F));
    private static final ModelPartData TUBE_2A = new ModelPartData("tube2a", CubeListBuilder.create()
          .texOffs(0, 9)
          .addBox(0F, 0F, 0F, 1, 1, 1),
          PartPose.offset(-3F, 9F, 4F));
    private static final ModelPartData TUBE_1A = new ModelPartData("tube1a", CubeListBuilder.create()
          .texOffs(0, 9)
          .addBox(0F, 0F, 0F, 1, 1, 1),
          PartPose.offset(-3F, 9F, 2F));
    private static final ModelPartData CONDUIT = new ModelPartData("conduit", CubeListBuilder.create()
          .texOffs(48, 0)
          .addBox(0F, 0F, 0F, 2, 1, 7),
          PartPose.offset(-1F, 9.5F, -1F));
    private static final ModelPartData BRACE_1 = new ModelPartData("brace1", CubeListBuilder.create()
          .texOffs(0, 11)
          .addBox(0F, 0F, 0F, 1, 1, 2),
          PartPose.offsetAndRotation(-2F, 9.5F, -7.1F, 0.1745329F, 0F, 0F));
    private static final ModelPartData TANK = new ModelPartData("tank", CubeListBuilder.create()
          .addBox(0F, 0F, 0F, 16, 9, 16),
          PartPose.offset(-8F, 10F, -8F));
    private static final ModelPartData LASER = new ModelPartData("laser", CubeListBuilder.create()
          .texOffs(4, 0)
          .addBox(0.5F, 2.1F, -9F, 1, 2, 1),
          PartPose.offsetAndRotation(-1F, -5F, 8F, -0.1117011F, 0F, 0F));
    private static final ModelPartData BASE = new ModelPartData("base", CubeListBuilder.create()
          .texOffs(0, 25)
          .addBox(0F, 0F, 0F, 16, 4, 16),
          PartPose.offset(-8F, 20F, -8F));
    private static final ModelPartData SUPPORT_1 = new ModelPartData("support1", CubeListBuilder.create()
          .addBox(0F, 0F, 0F, 1, 1, 1),
          PartPose.offset(6.5F, 19F, -7.5F));
    private static final ModelPartData SUPPORT_2 = new ModelPartData("support2", CubeListBuilder.create()
          .addBox(0F, 0F, 0F, 1, 1, 1),
          PartPose.offset(6.5F, 19F, 6.5F));
    private static final ModelPartData SUPPORT_3 = new ModelPartData("support3", CubeListBuilder.create()
          .addBox(0F, 0F, 0F, 1, 1, 1),
          PartPose.offset(6.5F, 19F, -5.5F));
    private static final ModelPartData SUPPORT_4 = new ModelPartData("support4", CubeListBuilder.create()
          .addBox(0F, 0F, 0F, 1, 1, 1),
          PartPose.offset(6.5F, 19F, -3.5F));
    private static final ModelPartData SUPPORT_5 = new ModelPartData("support5", CubeListBuilder.create()
          .addBox(0F, 0F, 0F, 1, 1, 1),
          PartPose.offset(6.5F, 19F, -1.5F));
    private static final ModelPartData SUPPORT_6 = new ModelPartData("support6", CubeListBuilder.create()
          .addBox(0F, 0F, 0F, 1, 1, 1),
          PartPose.offset(6.5F, 19F, 0.5F));
    private static final ModelPartData SUPPORT_7 = new ModelPartData("support7", CubeListBuilder.create()
          .addBox(0F, 0F, 0F, 1, 1, 1),
          PartPose.offset(6.5F, 19F, 2.5F));
    private static final ModelPartData SUPPORT_8 = new ModelPartData("support8", CubeListBuilder.create()
          .addBox(0F, 0F, 0F, 1, 1, 1),
          PartPose.offset(6.5F, 19F, 4.5F));
    private static final ModelPartData SUPPORT_9 = new ModelPartData("support9", CubeListBuilder.create()
          .addBox(0F, 0F, 0F, 1, 1, 1),
          PartPose.offset(-7.5F, 19F, 6.5F));
    private static final ModelPartData SUPPORT_10 = new ModelPartData("support10", CubeListBuilder.create()
          .addBox(0F, 0F, 0F, 1, 1, 1),
          PartPose.offset(-7.5F, 19F, 4.5F));
    private static final ModelPartData SUPPORT_11 = new ModelPartData("support11", CubeListBuilder.create()
          .addBox(0F, 0F, 0F, 1, 1, 1),
          PartPose.offset(-7.5F, 19F, 2.5F));
    private static final ModelPartData SUPPORT_12 = new ModelPartData("support12", CubeListBuilder.create()
          .addBox(0F, 0F, 0F, 1, 1, 1),
          PartPose.offset(-7.5F, 19F, 0.5F));
    private static final ModelPartData SUPPORT_13 = new ModelPartData("support13", CubeListBuilder.create()
          .addBox(0F, 0F, 0F, 1, 1, 1),
          PartPose.offset(-7.5F, 19F, -1.5F));
    private static final ModelPartData SUPPORT_14 = new ModelPartData("support14", CubeListBuilder.create()
          .addBox(0F, 0F, 0F, 1, 1, 1),
          PartPose.offset(-7.5F, 19F, -3.5F));
    private static final ModelPartData SUPPORT_15 = new ModelPartData("support15", CubeListBuilder.create()
          .addBox(0F, 0F, 0F, 1, 1, 1),
          PartPose.offset(-7.5F, 19F, -5.5F));
    private static final ModelPartData SUPPORT_16 = new ModelPartData("support16", CubeListBuilder.create()
          .addBox(0F, 0F, 0F, 1, 1, 1),
          PartPose.offset(-7.5F, 19F, -7.5F));
    private static final ModelPartData PORT_CONNECTOR = new ModelPartData("portConnector", CubeListBuilder.create()
          .texOffs(0, 14)
          .addBox(0F, 0F, 0F, 6, 1, 1),
          PartPose.offset(-3F, 19F, -7.01F));
    private static final ModelPartData LASER_BEAM_TOGGLE = new ModelPartData("laserBeamToggle", CubeListBuilder.create()
          .texOffs(12, 0)
          .addBox(0.5F, 4.1F, -9F, 1, 11, 1),
          PartPose.offsetAndRotation(-1F, -5F, 8F, -0.1117011F, 0F, 0F));

    public static LayerDefinition createLayerDefinition() {
        return createLayerDefinition(128, 64, POLE, PANEL_3, PORT, PANEL_1, PANEL_2, PANEL_BASE, PANEL_BRACE_LEFT_2, PANEL_BRACE_RIGHT_2,
              PANEL_BRACE_LEFT_1, PANEL_BRACE_RIGHT_1, PANEL_BRACE, BRIDGE, PLATFORM, HOLE_2, HOLE_4, HOLE_1, HOLE_3, BRACE_2, TUBE_2C, TUBE_1B, TUBE_1C, TUBE_2B,
              TUBE_2A, TUBE_1A, CONDUIT, BRACE_1, TANK, LASER, BASE, SUPPORT_1, SUPPORT_2, SUPPORT_3, SUPPORT_4, SUPPORT_5, SUPPORT_6, SUPPORT_7, SUPPORT_8, SUPPORT_9,
              SUPPORT_10, SUPPORT_11, SUPPORT_12, SUPPORT_13, SUPPORT_14, SUPPORT_15, SUPPORT_16, PORT_CONNECTOR, LASER_BEAM_TOGGLE);
    }

    private final RenderType RENDER_TYPE = renderType(ACTIVATOR_TEXTURE);
    private final List<ModelPart> parts;
    private final ModelPart laserBeamToggle;

    public ModelSolarNeutronActivator(EntityModelSet entityModelSet) {
        super(RenderType::entityCutout);
        ModelPart root = entityModelSet.bakeLayer(ACTIVATOR_LAYER);
        parts = getRenderableParts(root, POLE, PANEL_3, PORT, PANEL_1, PANEL_2, PANEL_BASE, PANEL_BRACE_LEFT_2, PANEL_BRACE_RIGHT_2, PANEL_BRACE_LEFT_1,
              PANEL_BRACE_RIGHT_1, PANEL_BRACE, BRIDGE, PLATFORM, HOLE_2, HOLE_4, HOLE_1, HOLE_3, BRACE_2, TUBE_2C, TUBE_1B, TUBE_1C, TUBE_2B, TUBE_2A,
              TUBE_1A, CONDUIT, BRACE_1, TANK, LASER, BASE, SUPPORT_1, SUPPORT_2, SUPPORT_3, SUPPORT_4, SUPPORT_5, SUPPORT_6, SUPPORT_7, SUPPORT_8,
              SUPPORT_9, SUPPORT_10, SUPPORT_11, SUPPORT_12, SUPPORT_13, SUPPORT_14, SUPPORT_15, SUPPORT_16, PORT_CONNECTOR);
        laserBeamToggle = LASER_BEAM_TOGGLE.getFromRoot(root);
    }

    public void render(@Nonnull PoseStack matrix, @Nonnull MultiBufferSource renderer, int light, int overlayLight, boolean hasEffect) {
        renderToBuffer(matrix, getVertexConsumer(renderer, RENDER_TYPE, hasEffect), light, overlayLight, 1, 1, 1, 1);
    }

    @Override
    public void renderToBuffer(@Nonnull PoseStack matrix, @Nonnull VertexConsumer vertexBuilder, int light, int overlayLight, float red, float green, float blue, float alpha) {
        renderPartsToBuffer(parts, matrix, vertexBuilder, light, overlayLight, red, green, blue, alpha);
        //TODO: Figure out if there is there supposed to be a "laser" here?
        laserBeamToggle.render(matrix, vertexBuilder, light, overlayLight, red, green, blue, alpha);
    }

    public void renderWireFrame(PoseStack matrix, VertexConsumer vertexBuilder, float red, float green, float blue, float alpha) {
        renderPartsAsWireFrame(parts, matrix, vertexBuilder, red, green, blue, alpha);
    }
}