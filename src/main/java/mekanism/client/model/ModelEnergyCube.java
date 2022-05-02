package mekanism.client.model;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import javax.annotation.Nonnull;
import mekanism.api.NBTConstants;
import mekanism.api.RelativeSide;
import mekanism.api.text.EnumColor;
import mekanism.client.render.MekanismRenderType;
import mekanism.client.render.MekanismRenderer;
import mekanism.common.Mekanism;
import mekanism.common.lib.transmitter.TransmissionType;
import mekanism.common.tier.EnergyCubeTier;
import mekanism.common.tile.TileEntityEnergyCube;
import mekanism.common.tile.component.config.ConfigInfo;
import mekanism.common.tile.component.config.DataType;
import mekanism.common.tile.component.config.slot.ISlotInfo;
import mekanism.common.util.EnumUtils;
import mekanism.common.util.ItemDataUtils;
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
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

public class ModelEnergyCube extends MekanismJavaModel {

    public static final ModelLayerLocation CUBE_LAYER = new ModelLayerLocation(Mekanism.rl("energy_cube"), "main");
    private static final ResourceLocation CUBE_TEXTURE = MekanismUtils.getResource(ResourceType.RENDER, "energy_cube.png");
    private static final ResourceLocation OVERLAY_ON = MekanismUtils.getResource(ResourceType.RENDER, "energy_cube_overlay_on.png");
    private static final ResourceLocation OVERLAY_OFF = MekanismUtils.getResource(ResourceType.RENDER, "energy_cube_overlay_off.png");
    private static final ResourceLocation BASE_OVERLAY = MekanismUtils.getResource(ResourceType.RENDER, "energy_cube_overlay_base.png");
    private static final RenderType RENDER_TYPE_ON = MekanismRenderType.standard(OVERLAY_ON);
    private static final RenderType RENDER_TYPE_OFF = MekanismRenderType.standard(OVERLAY_OFF);
    private static final RenderType RENDER_TYPE_BASE = MekanismRenderType.standard(BASE_OVERLAY);
    private static final ModelPartData FRAME_12 = new ModelPartData("frame12", CubeListBuilder.create()
          .addBox(0F, 0F, 0F, 3, 10, 3),
          PartPose.offset(-8F, 11F, 5F));
    private static final ModelPartData FRAME_11 = new ModelPartData("frame11", CubeListBuilder.create()
          .addBox(0F, 0F, 0F, 3, 10, 3),
          PartPose.offset(5F, 11F, -8F));
    private static final ModelPartData FRAME_10 = new ModelPartData("frame10", CubeListBuilder.create()
          .texOffs(0, 13)
          .addBox(0F, 0F, 0F, 10, 3, 3),
          PartPose.offset(-5F, 21F, 5F));
    private static final ModelPartData FRAME_9 = new ModelPartData("frame9", CubeListBuilder.create()
          .texOffs(12, 0)
          .addBox(0F, 0F, 0F, 3, 3, 10),
          PartPose.offset(5F, 21F, -5F));
    private static final ModelPartData FRAME_8 = new ModelPartData("frame8", CubeListBuilder.create()
          .texOffs(0, 13)
          .addBox(0F, 0F, 0F, 10, 3, 3),
          PartPose.offset(-5F, 8F, 5F));
    private static final ModelPartData FRAME_7 = new ModelPartData("frame7", CubeListBuilder.create()
          .texOffs(0, 13)
          .addBox(0F, 0F, 0F, 10, 3, 3),
          PartPose.offset(-5F, 21F, -8F));
    private static final ModelPartData FRAME_6 = new ModelPartData("frame6", CubeListBuilder.create()
          .addBox(0F, 0F, 0F, 3, 10, 3),
          PartPose.offset(5F, 11F, 5F));
    private static final ModelPartData FRAME_5 = new ModelPartData("frame5", CubeListBuilder.create()
          .addBox(0F, 0F, 0F, 3, 10, 3),
          PartPose.offset(-8F, 11F, -8F));
    private static final ModelPartData FRAME_4 = new ModelPartData("frame4", CubeListBuilder.create()
          .texOffs(12, 0)
          .addBox(0F, 0F, 0F, 3, 3, 10),
          PartPose.offset(5F, 8F, -5F));
    private static final ModelPartData FRAME_3 = new ModelPartData("frame3", CubeListBuilder.create()
          .texOffs(12, 0)
          .addBox(0F, 0F, 0F, 3, 3, 10),
          PartPose.offset(-8F, 21F, -5F));
    private static final ModelPartData FRAME_2 = new ModelPartData("frame2", CubeListBuilder.create()
          .texOffs(12, 0)
          .addBox(0F, 0F, 0F, 3, 3, 10),
          PartPose.offset(-8F, 8F, -5F));
    private static final ModelPartData FRAME_1 = new ModelPartData("frame1", CubeListBuilder.create()
          .texOffs(0, 13)
          .addBox(0F, 0F, 0F, 10, 3, 3),
          PartPose.offset(-5F, 8F, -8F));
    private static final ModelPartData CORNER_8 = new ModelPartData("corner8", CubeListBuilder.create()
          .texOffs(26, 13)
          .addBox(0F, 0F, 0F, 3, 3, 3),
          PartPose.offset(5F, 21F, 5F));
    private static final ModelPartData CORNER_7 = new ModelPartData("corner7", CubeListBuilder.create()
          .texOffs(26, 13)
          .addBox(0F, 0F, 0F, 3, 3, 3),
          PartPose.offset(5F, 21F, -8F));
    private static final ModelPartData CORNER_6 = new ModelPartData("corner6", CubeListBuilder.create()
          .texOffs(26, 13)
          .addBox(0F, 0F, 0F, 3, 3, 3),
          PartPose.offset(-8F, 21F, 5F));
    private static final ModelPartData CORNER_5 = new ModelPartData("corner5", CubeListBuilder.create()
          .texOffs(26, 13)
          .addBox(0F, 0F, 0F, 3, 3, 3),
          PartPose.offset(-8F, 21F, -8F));
    private static final ModelPartData CORNER_4 = new ModelPartData("corner4", CubeListBuilder.create()
          .texOffs(26, 13)
          .addBox(0F, 0F, 0F, 3, 3, 3),
          PartPose.offset(5F, 8F, 5F));
    private static final ModelPartData CORNER_3 = new ModelPartData("corner3", CubeListBuilder.create()
          .texOffs(26, 13)
          .addBox(0F, 0F, 0F, 3, 3, 3),
          PartPose.offset(5F, 8F, -8F));
    private static final ModelPartData CORNER_2 = new ModelPartData("corner2", CubeListBuilder.create()
          .texOffs(26, 13)
          .addBox(0F, 0F, 0F, 3, 3, 3),
          PartPose.offset(-8F, 8F, 5F));
    private static final ModelPartData CORNER_1 = new ModelPartData("corner1", CubeListBuilder.create()
          .texOffs(26, 13)
          .addBox(0F, 0F, 0F, 3, 3, 3),
          PartPose.offset(-8F, 8F, -8F));
    private static final ModelPartData CONNECTOR_BACK_TOGGLE = new ModelPartData("connectorBackToggle", CubeListBuilder.create()
          .texOffs(38, 16)
          .addBox(0F, 0F, 0F, 10, 6, 1),
          PartPose.offset(-5F, 13F, 6F));
    private static final ModelPartData CONNECTOR_RIGHT_TOGGLE = new ModelPartData("connectorRightToggle", CubeListBuilder.create()
          .texOffs(38, 0)
          .addBox(0F, 0F, 0F, 1, 6, 10),
          PartPose.offset(6F, 13F, -5F));
    private static final ModelPartData CONNECTOR_BOTTOM_TOGGLE = new ModelPartData("connectorBottomToggle", CubeListBuilder.create()
          .texOffs(0, 19)
          .addBox(0F, 0F, 0F, 10, 1, 6),
          PartPose.offset(-5F, 22F, -3F));
    private static final ModelPartData CONNECTOR_LEFT_TOGGLE = new ModelPartData("connectorLeftToggle", CubeListBuilder.create()
          .texOffs(38, 0)
          .addBox(0F, 0F, 0F, 1, 6, 10),
          PartPose.offset(-7F, 13F, -5F));
    private static final ModelPartData CONNECTOR_FRONT_TOGGLE = new ModelPartData("connectorFrontToggle", CubeListBuilder.create()
          .texOffs(38, 16)
          .addBox(0F, 0F, 0F, 10, 6, 1),
          PartPose.offset(-5F, 13F, -7F));
    private static final ModelPartData CONNECTOR_TOP_TOGGLE = new ModelPartData("connectorTopToggle", CubeListBuilder.create()
          .texOffs(0, 19)
          .addBox(0F, 0F, 0F, 10, 1, 6),
          PartPose.offset(-5F, 9F, -3F));
    private static final ModelPartData PORT_BACK_TOGGLE = new ModelPartData("portBackToggle", CubeListBuilder.create()
          .texOffs(18, 35)
          .addBox(0F, 0F, 0F, 8, 8, 1),
          PartPose.offset(-4F, 12F, 7F));
    private static final ModelPartData PORT_BOTTOM_TOGGLE = new ModelPartData("portBottomToggle", CubeListBuilder.create()
          .texOffs(0, 26)
          .addBox(0F, 0F, 0F, 8, 1, 8),
          PartPose.offset(-4F, 23F, -4F));
    private static final ModelPartData PORT_FRONT_TOGGLE = new ModelPartData("portFrontToggle", CubeListBuilder.create()
          .texOffs(18, 35)
          .addBox(0F, 0F, 0F, 8, 8, 1),
          PartPose.offset(-4F, 12F, -8F));
    private static final ModelPartData PORT_LEFT_TOGGLE = new ModelPartData("portLeftToggle", CubeListBuilder.create()
          .texOffs(0, 35)
          .addBox(0F, 0F, 0F, 1, 8, 8),
          PartPose.offset(-8F, 12F, -4F));
    private static final ModelPartData PORT_RIGHT_TOGGLE = new ModelPartData("portRightToggle", CubeListBuilder.create()
          .texOffs(0, 35)
          .addBox(0F, 0F, 0F, 1, 8, 8),
          PartPose.offset(7F, 12F, -4F));
    private static final ModelPartData PORT_TOP_TOGGLE = new ModelPartData("portTopToggle", CubeListBuilder.create()
          .texOffs(0, 26)
          .addBox(0F, 0F, 0F, 8, 1, 8),
          PartPose.offset(-4F, 8F, -4F));
    private static final ModelPartData LED_TOP_1 = new ModelPartData("ledTop1", CubeListBuilder.create()
          .texOffs(0, 51)
          .addBox(0F, 0F, 0F, 1, 1, 1),
          PartPose.offset(-5.5F, 8.1F, -0.5F));
    private static final ModelPartData LED_TOP_2 = new ModelPartData("ledTop2", CubeListBuilder.create()
          .texOffs(0, 51)
          .addBox(0F, 0F, 0F, 1, 1, 1),
          PartPose.offset(4.5F, 8.1F, -0.5F));
    private static final ModelPartData LED_BACK_1 = new ModelPartData("ledBack1", CubeListBuilder.create()
          .texOffs(0, 51)
          .addBox(0F, 0F, 0F, 1, 1, 1),
          PartPose.offset(-5.5F, 15.5F, 6.9F));
    private static final ModelPartData LED_BACK_2 = new ModelPartData("ledBack2", CubeListBuilder.create()
          .texOffs(0, 51)
          .addBox(0F, 0F, 0F, 1, 1, 1),
          PartPose.offset(4.5F, 15.5F, 6.9F));
    private static final ModelPartData LED_BOTTOM_2 = new ModelPartData("ledBottom2", CubeListBuilder.create()
          .texOffs(0, 51)
          .addBox(0F, 0F, 0F, 1, 1, 1),
          PartPose.offset(4.5F, 22.9F, -0.5F));
    private static final ModelPartData LED_BOTTOM_1 = new ModelPartData("ledBottom1", CubeListBuilder.create()
          .texOffs(0, 51)
          .addBox(0F, 0F, 0F, 1, 1, 1),
          PartPose.offset(-5.5F, 22.9F, -0.5F));
    private static final ModelPartData LED_FRONT_1 = new ModelPartData("ledFront1", CubeListBuilder.create()
          .texOffs(0, 51)
          .addBox(0F, 0F, 0F, 1, 1, 1),
          PartPose.offset(-5.5F, 15.5F, -7.9F));
    private static final ModelPartData LED_FRONT_2 = new ModelPartData("ledFront2", CubeListBuilder.create()
          .texOffs(0, 51)
          .addBox(0F, 0F, 0F, 1, 1, 1),
          PartPose.offset(4.5F, 15.5F, -7.9F));
    private static final ModelPartData LED_RIGHT_2 = new ModelPartData("ledRight2", CubeListBuilder.create()
          .texOffs(0, 51)
          .addBox(0F, 0F, 0F, 1, 1, 1),
          PartPose.offset(6.9F, 15.5F, 4.5F));
    private static final ModelPartData LED_RIGHT_1 = new ModelPartData("ledRight1", CubeListBuilder.create()
          .texOffs(0, 51)
          .addBox(0F, 0F, 0F, 1, 1, 1),
          PartPose.offset(6.9F, 15.5F, -5.5F));
    private static final ModelPartData LED_LEFT_1 = new ModelPartData("ledLeft1", CubeListBuilder.create()
          .texOffs(0, 51)
          .addBox(0F, 0F, 0F, 1, 1, 1),
          PartPose.offset(-7.9F, 15.5F, 4.5F));
    private static final ModelPartData LED_LEFT_2 = new ModelPartData("ledLeft2", CubeListBuilder.create()
          .texOffs(0, 51)
          .addBox(0F, 0F, 0F, 1, 1, 1),
          PartPose.offset(-7.9F, 15.5F, -5.5F));

    public static LayerDefinition createLayerDefinition() {
        return createLayerDefinition(64, 64, FRAME_12, FRAME_11, FRAME_10, FRAME_9, FRAME_8, FRAME_7, FRAME_6, FRAME_5, FRAME_4, FRAME_3,
              FRAME_2, FRAME_1, CORNER_8, CORNER_7, CORNER_6, CORNER_5, CORNER_4, CORNER_3, CORNER_2, CORNER_1, CONNECTOR_BACK_TOGGLE, CONNECTOR_RIGHT_TOGGLE,
              CONNECTOR_BOTTOM_TOGGLE, CONNECTOR_LEFT_TOGGLE, CONNECTOR_FRONT_TOGGLE, CONNECTOR_TOP_TOGGLE, PORT_BACK_TOGGLE, PORT_BOTTOM_TOGGLE, PORT_FRONT_TOGGLE,
              PORT_LEFT_TOGGLE, PORT_RIGHT_TOGGLE, PORT_TOP_TOGGLE, LED_TOP_1, LED_TOP_2, LED_BACK_1, LED_BACK_2, LED_BOTTOM_2, LED_BOTTOM_1, LED_FRONT_1,
              LED_FRONT_2, LED_RIGHT_2, LED_RIGHT_1, LED_LEFT_1, LED_LEFT_2);
    }


    private final RenderType RENDER_TYPE = renderType(CUBE_TEXTURE);

    private final List<ModelPart> frame;
    private final List<ModelPart> corners;
    private final List<ModelPart> leds1;
    private final List<ModelPart> leds2;
    private final List<ModelPart> ports;
    private final List<ModelPart> connectors;

    public ModelEnergyCube(EntityModelSet entityModelSet) {
        super(RenderType::entitySolid);
        ModelPart root = entityModelSet.bakeLayer(CUBE_LAYER);
        frame = getRenderableParts(root, FRAME_12, FRAME_11, FRAME_10, FRAME_9, FRAME_8, FRAME_7, FRAME_6, FRAME_5, FRAME_4, FRAME_3, FRAME_2, FRAME_1);
        corners = getRenderableParts(root, CORNER_8, CORNER_7, CORNER_6, CORNER_5, CORNER_4, CORNER_3, CORNER_2, CORNER_1);
        leds1 = getRenderableParts(root, LED_FRONT_1, LED_LEFT_1, LED_RIGHT_1, LED_BACK_1, LED_TOP_1, LED_BOTTOM_1);
        leds2 = getRenderableParts(root, LED_FRONT_2, LED_LEFT_2, LED_RIGHT_2, LED_BACK_2, LED_TOP_2, LED_BOTTOM_2);
        ports = getRenderableParts(root, PORT_FRONT_TOGGLE, PORT_LEFT_TOGGLE, PORT_RIGHT_TOGGLE, PORT_BACK_TOGGLE, PORT_TOP_TOGGLE, PORT_BOTTOM_TOGGLE);
        connectors = getRenderableParts(root, CONNECTOR_FRONT_TOGGLE, CONNECTOR_LEFT_TOGGLE, CONNECTOR_RIGHT_TOGGLE, CONNECTOR_BACK_TOGGLE,
              CONNECTOR_TOP_TOGGLE, CONNECTOR_BOTTOM_TOGGLE);
    }

    public void render(@Nonnull PoseStack matrix, @Nonnull MultiBufferSource renderer, int light, int overlayLight, EnergyCubeTier tier, boolean renderMain, boolean hasEffect) {
        if (renderMain) {
            renderToBuffer(matrix, getVertexConsumer(renderer, RENDER_TYPE, hasEffect), light, overlayLight, 1, 1, 1, 1);
        }
        EnumColor color = tier.getBaseTier().getColor();
        renderCorners(matrix, getVertexConsumer(renderer, RENDER_TYPE_BASE, hasEffect), MekanismRenderer.FULL_LIGHT, overlayLight, color.getColor(0),
              color.getColor(1), color.getColor(2), 1);
    }

    @Override
    public void renderToBuffer(@Nonnull PoseStack poseStack, @Nonnull VertexConsumer vertexConsumer, int light, int overlayLight, float red, float green, float blue, float alpha) {
        renderPartsToBuffer(frame, poseStack, vertexConsumer, light, overlayLight, red, green, blue, alpha);
        renderPartsToBuffer(corners, poseStack, vertexConsumer, light, overlayLight, red, green, blue, alpha);
    }

    private void renderCorners(@Nonnull PoseStack poseStack, @Nonnull VertexConsumer vertexConsumer, int light, int overlayLight, float red, float green, float blue,
          float alpha) {
        poseStack.pushPose();
        poseStack.scale(1.001F, 1.005F, 1.001F);
        poseStack.translate(0, -0.0061, 0);
        renderPartsToBuffer(corners, poseStack, vertexConsumer, light, overlayLight, red, green, blue, alpha);
        poseStack.popPose();
    }

    public void renderSidesBatched(@Nonnull TileEntityEnergyCube tile, @Nonnull PoseStack matrix, @Nonnull MultiBufferSource renderer, int light, int overlayLight) {
        Set<RelativeSide> enabledSides = EnumSet.noneOf(RelativeSide.class);
        Set<RelativeSide> outputSides = EnumSet.noneOf(RelativeSide.class);
        ConfigInfo config = tile.getConfig().getConfig(TransmissionType.ENERGY);
        if (config != null) {
            for (RelativeSide side : EnumUtils.SIDES) {
                ISlotInfo slotInfo = config.getSlotInfo(side);
                if (slotInfo != null) {
                    if (slotInfo.canInput()) {
                        enabledSides.add(side);
                    } else if (slotInfo.canOutput()) {
                        enabledSides.add(side);
                        outputSides.add(side);
                    }
                }
            }
        }
        renderSidesBatched(matrix, renderer, light, overlayLight, enabledSides, outputSides, false);
    }

    public void renderSidesBatched(@Nonnull ItemStack stack, EnergyCubeTier tier, @Nonnull PoseStack matrix, @Nonnull MultiBufferSource renderer, int light,
          int overlayLight, boolean hasEffect) {
        Set<RelativeSide> enabledSides;
        Set<RelativeSide> outputSides;
        CompoundTag configData = ItemDataUtils.getDataMapIfPresent(stack);
        if (configData != null && configData.contains(NBTConstants.COMPONENT_CONFIG, Tag.TAG_COMPOUND)) {
            enabledSides = EnumSet.noneOf(RelativeSide.class);
            outputSides = EnumSet.noneOf(RelativeSide.class);
            CompoundTag sideConfig = configData.getCompound(NBTConstants.COMPONENT_CONFIG).getCompound(NBTConstants.CONFIG + TransmissionType.ENERGY.ordinal());
            //TODO: Maybe improve on this, but for now this is a decent way of making it not have disabled sides show
            for (RelativeSide side : EnumUtils.SIDES) {
                DataType dataType = DataType.byIndexStatic(sideConfig.getInt(NBTConstants.SIDE + side.ordinal()));
                if (dataType == DataType.INPUT) {
                    enabledSides.add(side);
                } else if (dataType == DataType.OUTPUT) {
                    enabledSides.add(side);
                    outputSides.add(side);
                }
            }
        } else {
            enabledSides = EnumSet.allOf(RelativeSide.class);
            if (tier == EnergyCubeTier.CREATIVE) {
                outputSides = EnumSet.allOf(RelativeSide.class);
            } else {
                outputSides = Collections.singleton(RelativeSide.FRONT);
            }
        }
        renderSidesBatched(matrix, renderer, light, overlayLight, enabledSides, outputSides, hasEffect);
    }

    /**
     * Batched version of to render sides of the energy cube that render all sides per render type before switching to the next render type. This is because the way
     * Minecraft draws custom render types, is it flushes and instantly draws as soon as it gets a new type if it doesn't know how to handle the type.
     */
    private void renderSidesBatched(@Nonnull PoseStack matrix, @Nonnull MultiBufferSource renderer, int light, int overlayLight, Set<RelativeSide> enabledSides,
          Set<RelativeSide> outputSides, boolean hasEffect) {
        if (!enabledSides.isEmpty()) {
            VertexConsumer buffer = getVertexConsumer(renderer, RENDER_TYPE, hasEffect);
            for (RelativeSide enabledSide : enabledSides) {
                int sideOrdinal = enabledSide.ordinal();
                connectors.get(sideOrdinal).render(matrix, buffer, light, overlayLight, 1, 1, 1, 1);
                ports.get(sideOrdinal).render(matrix, buffer, light, overlayLight, 1, 1, 1, 1);
            }
            if (!outputSides.isEmpty()) {
                buffer = getVertexConsumer(renderer, RENDER_TYPE_BASE, hasEffect);
                for (RelativeSide outputSide : outputSides) {
                    ports.get(outputSide.ordinal()).render(matrix, buffer, MekanismRenderer.FULL_LIGHT, overlayLight, 1, 1, 1, 1);
                }
                renderLEDS(outputSides, getVertexConsumer(renderer, RENDER_TYPE_ON, hasEffect), matrix, MekanismRenderer.FULL_LIGHT, overlayLight);
            }
        }
        if (outputSides.size() < EnumUtils.SIDES.length) {
            Set<RelativeSide> remainingSides = EnumSet.allOf(RelativeSide.class);
            remainingSides.removeAll(outputSides);
            renderLEDS(remainingSides, getVertexConsumer(renderer, RENDER_TYPE_OFF, hasEffect), matrix, light, overlayLight);
        }
    }

    private void renderLEDS(Set<RelativeSide> sides, VertexConsumer ledBuffer, PoseStack matrix, int light, int overlayLight) {
        for (RelativeSide side : sides) {
            int sideOrdinal = side.ordinal();
            leds1.get(sideOrdinal).render(matrix, ledBuffer, light, overlayLight, 1, 1, 1, 1);
            leds2.get(sideOrdinal).render(matrix, ledBuffer, light, overlayLight, 1, 1, 1, 1);
        }
    }

    public static class ModelEnergyCore extends MekanismJavaModel {

        public static final ModelLayerLocation CORE_LAYER = new ModelLayerLocation(Mekanism.rl("energy_core"), "main");
        private static final ResourceLocation CORE_TEXTURE = MekanismUtils.getResource(ResourceType.RENDER, "energy_core.png");

        private static final ModelPartData CUBE = new ModelPartData("cube", CubeListBuilder.create()
              .texOffs(0, 0)
              .addBox(-8, -8, -8, 16, 16, 16));

        public static LayerDefinition createLayerDefinition() {
            return createLayerDefinition(32, 32, CUBE);
        }

        private final RenderType RENDER_TYPE = renderType(CORE_TEXTURE);
        private final ModelPart cube;

        public ModelEnergyCore(EntityModelSet entityModelSet) {
            super(MekanismRenderType::standard);
            ModelPart root = entityModelSet.bakeLayer(CORE_LAYER);
            cube = CUBE.getFromRoot(root);
        }

        public VertexConsumer getBuffer(@Nonnull MultiBufferSource renderer) {
            return renderer.getBuffer(RENDER_TYPE);
        }

        public void render(@Nonnull PoseStack matrix, @Nonnull MultiBufferSource renderer, int light, int overlayLight, EnumColor color, float energyPercentage) {
            renderToBuffer(matrix, getBuffer(renderer), light, overlayLight, color.getColor(0), color.getColor(1), color.getColor(2),
                  energyPercentage);
        }

        public void render(@Nonnull PoseStack matrix, @Nonnull VertexConsumer buffer, int light, int overlayLight, EnumColor color, float energyPercentage) {
            cube.render(matrix, buffer, light, overlayLight, color.getColor(0), color.getColor(1), color.getColor(2), energyPercentage);
        }

        @Override
        public void renderToBuffer(@Nonnull PoseStack matrix, @Nonnull VertexConsumer vertexBuilder, int light, int overlayLight, float red, float green, float blue,
              float alpha) {
            cube.render(matrix, vertexBuilder, light, overlayLight, red, green, blue, alpha);
        }
    }
}