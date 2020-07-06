package mekanism.client.model;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import java.util.EnumSet;
import java.util.Set;
import javax.annotation.Nonnull;
import mekanism.api.NBTConstants;
import mekanism.api.RelativeSide;
import mekanism.api.text.EnumColor;
import mekanism.client.render.MekanismRenderType;
import mekanism.client.render.MekanismRenderer;
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
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.model.ModelRenderer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.util.Constants.NBT;

public class ModelEnergyCube extends MekanismJavaModel {

    private static final ResourceLocation CUBE_TEXTURE = MekanismUtils.getResource(ResourceType.RENDER, "energy_cube.png");
    private static final ResourceLocation OVERLAY_ON = MekanismUtils.getResource(ResourceType.RENDER, "energy_cube_overlay_on.png");
    private static final ResourceLocation OVERLAY_OFF = MekanismUtils.getResource(ResourceType.RENDER, "energy_cube_overlay_off.png");
    private static final ResourceLocation BASE_OVERLAY = MekanismUtils.getResource(ResourceType.RENDER, "energy_cube_overlay_base.png");
    private static final RenderType RENDER_TYPE_ON = MekanismRenderType.mekStandard(OVERLAY_ON);
    private static final RenderType RENDER_TYPE_OFF = MekanismRenderType.mekStandard(OVERLAY_OFF);
    private static final RenderType RENDER_TYPE_BASE = MekanismRenderType.mekStandard(BASE_OVERLAY);

    private final RenderType RENDER_TYPE = getRenderType(CUBE_TEXTURE);

    private final ModelRenderer[] leds1;
    private final ModelRenderer[] leds2;
    private final ModelRenderer[] ports;
    private final ModelRenderer[] connectors;
    private final ModelRenderer frame12;
    private final ModelRenderer frame11;
    private final ModelRenderer frame10;
    private final ModelRenderer frame9;
    private final ModelRenderer frame8;
    private final ModelRenderer frame7;
    private final ModelRenderer frame6;
    private final ModelRenderer frame5;
    private final ModelRenderer frame4;
    private final ModelRenderer frame3;
    private final ModelRenderer frame2;
    private final ModelRenderer frame1;
    private final ModelRenderer corner8;
    private final ModelRenderer corner7;
    private final ModelRenderer corner6;
    private final ModelRenderer corner5;
    private final ModelRenderer corner4;
    private final ModelRenderer corner3;
    private final ModelRenderer corner2;
    private final ModelRenderer corner1;

    public ModelEnergyCube() {
        super(RenderType::getEntitySolid);
        textureWidth = 64;
        textureHeight = 64;

        frame12 = new ModelRenderer(this, 0, 0);
        frame12.addBox(0F, 0F, 0F, 3, 10, 3, false);
        frame12.setRotationPoint(-8F, 11F, 5F);
        frame12.setTextureSize(64, 64);
        frame12.mirror = true;
        setRotation(frame12, 0F, 0F, 0F);
        frame11 = new ModelRenderer(this, 0, 0);
        frame11.addBox(0F, 0F, 0F, 3, 10, 3, false);
        frame11.setRotationPoint(5F, 11F, -8F);
        frame11.setTextureSize(64, 64);
        frame11.mirror = true;
        setRotation(frame11, 0F, 0F, 0F);
        frame10 = new ModelRenderer(this, 0, 13);
        frame10.addBox(0F, 0F, 0F, 10, 3, 3, false);
        frame10.setRotationPoint(-5F, 21F, 5F);
        frame10.setTextureSize(64, 64);
        frame10.mirror = true;
        setRotation(frame10, 0F, 0F, 0F);
        frame9 = new ModelRenderer(this, 12, 0);
        frame9.addBox(0F, 0F, 0F, 3, 3, 10, false);
        frame9.setRotationPoint(5F, 21F, -5F);
        frame9.setTextureSize(64, 64);
        frame9.mirror = true;
        setRotation(frame9, 0F, 0F, 0F);
        frame8 = new ModelRenderer(this, 0, 13);
        frame8.addBox(0F, 0F, 0F, 10, 3, 3, false);
        frame8.setRotationPoint(-5F, 8F, 5F);
        frame8.setTextureSize(64, 64);
        frame8.mirror = true;
        setRotation(frame8, 0F, 0F, 0F);
        frame7 = new ModelRenderer(this, 0, 13);
        frame7.addBox(0F, 0F, 0F, 10, 3, 3, false);
        frame7.setRotationPoint(-5F, 21F, -8F);
        frame7.setTextureSize(64, 64);
        frame7.mirror = true;
        setRotation(frame7, 0F, 0F, 0F);
        frame6 = new ModelRenderer(this, 0, 0);
        frame6.addBox(0F, 0F, 0F, 3, 10, 3, false);
        frame6.setRotationPoint(5F, 11F, 5F);
        frame6.setTextureSize(64, 64);
        frame6.mirror = true;
        setRotation(frame6, 0F, 0F, 0F);
        frame5 = new ModelRenderer(this, 0, 0);
        frame5.addBox(0F, 0F, 0F, 3, 10, 3, false);
        frame5.setRotationPoint(-8F, 11F, -8F);
        frame5.setTextureSize(64, 64);
        frame5.mirror = true;
        setRotation(frame5, 0F, 0F, 0F);
        frame4 = new ModelRenderer(this, 12, 0);
        frame4.addBox(0F, 0F, 0F, 3, 3, 10, false);
        frame4.setRotationPoint(5F, 8F, -5F);
        frame4.setTextureSize(64, 64);
        frame4.mirror = true;
        setRotation(frame4, 0F, 0F, 0F);
        frame3 = new ModelRenderer(this, 12, 0);
        frame3.addBox(0F, 0F, 0F, 3, 3, 10, false);
        frame3.setRotationPoint(-8F, 21F, -5F);
        frame3.setTextureSize(64, 64);
        frame3.mirror = true;
        setRotation(frame3, 0F, 0F, 0F);
        frame2 = new ModelRenderer(this, 12, 0);
        frame2.addBox(0F, 0F, 0F, 3, 3, 10, false);
        frame2.setRotationPoint(-8F, 8F, -5F);
        frame2.setTextureSize(64, 64);
        frame2.mirror = true;
        setRotation(frame2, 0F, 0F, 0F);
        frame1 = new ModelRenderer(this, 0, 13);
        frame1.addBox(0F, 0F, 0F, 10, 3, 3, false);
        frame1.setRotationPoint(-5F, 8F, -8F);
        frame1.setTextureSize(64, 64);
        frame1.mirror = true;
        setRotation(frame1, 0F, 0F, 0F);
        corner8 = new ModelRenderer(this, 26, 13);
        corner8.addBox(0F, 0F, 0F, 3, 3, 3, false);
        corner8.setRotationPoint(5F, 21F, 5F);
        corner8.setTextureSize(64, 64);
        corner8.mirror = true;
        setRotation(corner8, 0F, 0F, 0F);
        corner7 = new ModelRenderer(this, 26, 13);
        corner7.addBox(0F, 0F, 0F, 3, 3, 3, false);
        corner7.setRotationPoint(5F, 21F, -8F);
        corner7.setTextureSize(64, 64);
        corner7.mirror = true;
        setRotation(corner7, 0F, 0F, 0F);
        corner6 = new ModelRenderer(this, 26, 13);
        corner6.addBox(0F, 0F, 0F, 3, 3, 3, false);
        corner6.setRotationPoint(-8F, 21F, 5F);
        corner6.setTextureSize(64, 64);
        corner6.mirror = true;
        setRotation(corner6, 0F, 0F, 0F);
        corner5 = new ModelRenderer(this, 26, 13);
        corner5.addBox(0F, 0F, 0F, 3, 3, 3, false);
        corner5.setRotationPoint(-8F, 21F, -8F);
        corner5.setTextureSize(64, 64);
        corner5.mirror = true;
        setRotation(corner5, 0F, 0F, 0F);
        corner4 = new ModelRenderer(this, 26, 13);
        corner4.addBox(0F, 0F, 0F, 3, 3, 3, false);
        corner4.setRotationPoint(5F, 8F, 5F);
        corner4.setTextureSize(64, 64);
        corner4.mirror = true;
        setRotation(corner4, 0F, 0F, 0F);
        corner3 = new ModelRenderer(this, 26, 13);
        corner3.addBox(0F, 0F, 0F, 3, 3, 3, false);
        corner3.setRotationPoint(5F, 8F, -8F);
        corner3.setTextureSize(64, 64);
        corner3.mirror = true;
        setRotation(corner3, 0F, 0F, 0F);
        corner2 = new ModelRenderer(this, 26, 13);
        corner2.addBox(0F, 0F, 0F, 3, 3, 3, false);
        corner2.setRotationPoint(-8F, 8F, 5F);
        corner2.setTextureSize(64, 64);
        corner2.mirror = true;
        setRotation(corner2, 0F, 0F, 0F);
        corner1 = new ModelRenderer(this, 26, 13);
        corner1.addBox(0F, 0F, 0F, 3, 3, 3, false);
        corner1.setRotationPoint(-8F, 8F, -8F);
        corner1.setTextureSize(64, 64);
        corner1.mirror = true;
        setRotation(corner1, 0F, 0F, 0F);
        ModelRenderer connectorBackToggle = new ModelRenderer(this, 38, 16);
        connectorBackToggle.addBox(0F, 0F, 0F, 10, 6, 1, false);
        connectorBackToggle.setRotationPoint(-5F, 13F, 6F);
        connectorBackToggle.setTextureSize(64, 64);
        connectorBackToggle.mirror = true;
        setRotation(connectorBackToggle, 0F, 0F, 0F);
        ModelRenderer connectorRightToggle = new ModelRenderer(this, 38, 0);
        connectorRightToggle.addBox(0F, 0F, 0F, 1, 6, 10, false);
        connectorRightToggle.setRotationPoint(6F, 13F, -5F);
        connectorRightToggle.setTextureSize(64, 64);
        connectorRightToggle.mirror = true;
        setRotation(connectorRightToggle, 0F, 0F, 0F);
        ModelRenderer connectorBottomToggle = new ModelRenderer(this, 0, 19);
        connectorBottomToggle.addBox(0F, 0F, 0F, 10, 1, 6, false);
        connectorBottomToggle.setRotationPoint(-5F, 22F, -3F);
        connectorBottomToggle.setTextureSize(64, 64);
        connectorBottomToggle.mirror = true;
        setRotation(connectorBottomToggle, 0F, 0F, 0F);
        ModelRenderer connectorLeftToggle = new ModelRenderer(this, 38, 0);
        connectorLeftToggle.addBox(0F, 0F, 0F, 1, 6, 10, false);
        connectorLeftToggle.setRotationPoint(-7F, 13F, -5F);
        connectorLeftToggle.setTextureSize(64, 64);
        connectorLeftToggle.mirror = true;
        setRotation(connectorLeftToggle, 0F, 0F, 0F);
        ModelRenderer connectorFrontToggle = new ModelRenderer(this, 38, 16);
        connectorFrontToggle.addBox(0F, 0F, 0F, 10, 6, 1, false);
        connectorFrontToggle.setRotationPoint(-5F, 13F, -7F);
        connectorFrontToggle.setTextureSize(64, 64);
        connectorFrontToggle.mirror = true;
        setRotation(connectorFrontToggle, 0F, 0F, 0F);
        ModelRenderer connectorTopToggle = new ModelRenderer(this, 0, 19);
        connectorTopToggle.addBox(0F, 0F, 0F, 10, 1, 6, false);
        connectorTopToggle.setRotationPoint(-5F, 9F, -3F);
        connectorTopToggle.setTextureSize(64, 64);
        connectorTopToggle.mirror = true;
        setRotation(connectorTopToggle, 0F, 0F, 0F);
        ModelRenderer portBackToggle = new ModelRenderer(this, 18, 35);
        portBackToggle.addBox(0F, 0F, 0F, 8, 8, 1, false);
        portBackToggle.setRotationPoint(-4F, 12F, 7F);
        portBackToggle.setTextureSize(64, 64);
        portBackToggle.mirror = true;
        setRotation(portBackToggle, 0F, 0F, 0F);
        ModelRenderer portBottomToggle = new ModelRenderer(this, 0, 26);
        portBottomToggle.addBox(0F, 0F, 0F, 8, 1, 8, false);
        portBottomToggle.setRotationPoint(-4F, 23F, -4F);
        portBottomToggle.setTextureSize(64, 64);
        portBottomToggle.mirror = true;
        setRotation(portBottomToggle, 0F, 0F, 0F);
        ModelRenderer portFrontToggle = new ModelRenderer(this, 18, 35);
        portFrontToggle.addBox(0F, 0F, 0F, 8, 8, 1, false);
        portFrontToggle.setRotationPoint(-4F, 12F, -8F);
        portFrontToggle.setTextureSize(64, 64);
        portFrontToggle.mirror = true;
        setRotation(portFrontToggle, 0F, 0F, 0F);
        ModelRenderer portLeftToggle = new ModelRenderer(this, 0, 35);
        portLeftToggle.addBox(0F, 0F, 0F, 1, 8, 8, false);
        portLeftToggle.setRotationPoint(-8F, 12F, -4F);
        portLeftToggle.setTextureSize(64, 64);
        portLeftToggle.mirror = true;
        setRotation(portLeftToggle, 0F, 0F, 0F);
        ModelRenderer portRightToggle = new ModelRenderer(this, 0, 35);
        portRightToggle.addBox(0F, 0F, 0F, 1, 8, 8, false);
        portRightToggle.setRotationPoint(7F, 12F, -4F);
        portRightToggle.setTextureSize(64, 64);
        portRightToggle.mirror = true;
        setRotation(portRightToggle, 0F, 0F, 0F);
        ModelRenderer portTopToggle = new ModelRenderer(this, 0, 26);
        portTopToggle.addBox(0F, 0F, 0F, 8, 1, 8, false);
        portTopToggle.setRotationPoint(-4F, 8F, -4F);
        portTopToggle.setTextureSize(64, 64);
        portTopToggle.mirror = true;
        setRotation(portTopToggle, 0F, 0F, 0F);
        ModelRenderer ledTop1 = new ModelRenderer(this, 0, 51);
        ledTop1.addBox(0F, 0F, 0F, 1, 1, 1, false);
        ledTop1.setRotationPoint(-5.5F, 8.1F, -0.5F);
        ledTop1.setTextureSize(64, 64);
        ledTop1.mirror = true;
        setRotation(ledTop1, 0F, 0F, 0F);
        ModelRenderer ledTop2 = new ModelRenderer(this, 0, 51);
        ledTop2.addBox(0F, 0F, 0F, 1, 1, 1, false);
        ledTop2.setRotationPoint(4.5F, 8.1F, -0.5F);
        ledTop2.setTextureSize(64, 64);
        ledTop2.mirror = true;
        setRotation(ledTop2, 0F, 0F, 0F);
        ModelRenderer ledBack1 = new ModelRenderer(this, 0, 51);
        ledBack1.addBox(0F, 0F, 0F, 1, 1, 1, false);
        ledBack1.setRotationPoint(-5.5F, 15.5F, 6.9F);
        ledBack1.setTextureSize(64, 64);
        ledBack1.mirror = true;
        setRotation(ledBack1, 0F, 0F, 0F);
        ModelRenderer ledBack2 = new ModelRenderer(this, 0, 51);
        ledBack2.addBox(0F, 0F, 0F, 1, 1, 1, false);
        ledBack2.setRotationPoint(4.5F, 15.5F, 6.9F);
        ledBack2.setTextureSize(64, 64);
        ledBack2.mirror = true;
        setRotation(ledBack2, 0F, 0F, 0F);
        ModelRenderer ledBottom2 = new ModelRenderer(this, 0, 51);
        ledBottom2.addBox(0F, 0F, 0F, 1, 1, 1, false);
        ledBottom2.setRotationPoint(4.5F, 22.9F, -0.5F);
        ledBottom2.setTextureSize(64, 64);
        ledBottom2.mirror = true;
        setRotation(ledBottom2, 0F, 0F, 0F);
        ModelRenderer ledBottom1 = new ModelRenderer(this, 0, 51);
        ledBottom1.addBox(0F, 0F, 0F, 1, 1, 1, false);
        ledBottom1.setRotationPoint(-5.5F, 22.9F, -0.5F);
        ledBottom1.setTextureSize(64, 64);
        ledBottom1.mirror = true;
        setRotation(ledBottom1, 0F, 0F, 0F);
        ModelRenderer ledFront1 = new ModelRenderer(this, 0, 51);
        ledFront1.addBox(0F, 0F, 0F, 1, 1, 1, false);
        ledFront1.setRotationPoint(-5.5F, 15.5F, -7.9F);
        ledFront1.setTextureSize(64, 64);
        ledFront1.mirror = true;
        setRotation(ledFront1, 0F, 0F, 0F);
        ModelRenderer ledFront2 = new ModelRenderer(this, 0, 51);
        ledFront2.addBox(0F, 0F, 0F, 1, 1, 1, false);
        ledFront2.setRotationPoint(4.5F, 15.5F, -7.9F);
        ledFront2.setTextureSize(64, 64);
        ledFront2.mirror = true;
        setRotation(ledFront2, 0F, 0F, 0F);
        ModelRenderer ledRight2 = new ModelRenderer(this, 0, 51);
        ledRight2.addBox(0F, 0F, 0F, 1, 1, 1, false);
        ledRight2.setRotationPoint(6.9F, 15.5F, 4.5F);
        ledRight2.setTextureSize(64, 64);
        ledRight2.mirror = true;
        setRotation(ledRight2, 0F, 0F, 0F);
        ModelRenderer ledRight1 = new ModelRenderer(this, 0, 51);
        ledRight1.addBox(0F, 0F, 0F, 1, 1, 1, false);
        ledRight1.setRotationPoint(6.9F, 15.5F, -5.5F);
        ledRight1.setTextureSize(64, 64);
        ledRight1.mirror = true;
        setRotation(ledRight1, 0F, 0F, 0F);
        ModelRenderer ledLeft1 = new ModelRenderer(this, 0, 51);
        ledLeft1.addBox(0F, 0F, 0F, 1, 1, 1, false);
        ledLeft1.setRotationPoint(-7.9F, 15.5F, 4.5F);
        ledLeft1.setTextureSize(64, 64);
        ledLeft1.mirror = true;
        setRotation(ledLeft1, 0F, 0F, 0F);
        ModelRenderer ledLeft2 = new ModelRenderer(this, 0, 51);
        ledLeft2.addBox(0F, 0F, 0F, 1, 1, 1, false);
        ledLeft2.setRotationPoint(-7.9F, 15.5F, -5.5F);
        ledLeft2.setTextureSize(64, 64);
        ledLeft2.mirror = true;
        setRotation(ledLeft2, 0F, 0F, 0F);

        leds1 = new ModelRenderer[]{ledFront1, ledLeft1, ledRight1, ledBack1, ledTop1, ledBottom1};
        leds2 = new ModelRenderer[]{ledFront2, ledLeft2, ledRight2, ledBack2, ledTop2, ledBottom2};

        ports = new ModelRenderer[]{portFrontToggle, portLeftToggle, portRightToggle, portBackToggle, portTopToggle, portBottomToggle};
        connectors = new ModelRenderer[]{connectorFrontToggle, connectorLeftToggle, connectorRightToggle, connectorBackToggle, connectorTopToggle, connectorBottomToggle};
    }

    public void render(@Nonnull MatrixStack matrix, @Nonnull IRenderTypeBuffer renderer, int light, int overlayLight, EnergyCubeTier tier, boolean renderMain, boolean hasEffect) {
        if (renderMain) {
            render(matrix, getVertexBuilder(renderer, RENDER_TYPE, hasEffect), light, overlayLight, 1, 1, 1, 1);
        }
        EnumColor color = tier.getBaseTier().getColor();
        renderCorners(matrix, getVertexBuilder(renderer, RENDER_TYPE_BASE, hasEffect), MekanismRenderer.FULL_LIGHT, overlayLight, color.getColor(0),
              color.getColor(1), color.getColor(2), 1);
    }

    @Override
    public void render(@Nonnull MatrixStack matrix, @Nonnull IVertexBuilder vertexBuilder, int light, int overlayLight, float red, float green, float blue, float alpha) {
        frame12.render(matrix, vertexBuilder, light, overlayLight, red, green, blue, alpha);
        frame11.render(matrix, vertexBuilder, light, overlayLight, red, green, blue, alpha);
        frame10.render(matrix, vertexBuilder, light, overlayLight, red, green, blue, alpha);
        frame9.render(matrix, vertexBuilder, light, overlayLight, red, green, blue, alpha);
        frame8.render(matrix, vertexBuilder, light, overlayLight, red, green, blue, alpha);
        frame7.render(matrix, vertexBuilder, light, overlayLight, red, green, blue, alpha);
        frame6.render(matrix, vertexBuilder, light, overlayLight, red, green, blue, alpha);
        frame5.render(matrix, vertexBuilder, light, overlayLight, red, green, blue, alpha);
        frame4.render(matrix, vertexBuilder, light, overlayLight, red, green, blue, alpha);
        frame3.render(matrix, vertexBuilder, light, overlayLight, red, green, blue, alpha);
        frame2.render(matrix, vertexBuilder, light, overlayLight, red, green, blue, alpha);
        frame1.render(matrix, vertexBuilder, light, overlayLight, red, green, blue, alpha);

        corner8.render(matrix, vertexBuilder, light, overlayLight, red, green, blue, alpha);
        corner7.render(matrix, vertexBuilder, light, overlayLight, red, green, blue, alpha);
        corner6.render(matrix, vertexBuilder, light, overlayLight, red, green, blue, alpha);
        corner5.render(matrix, vertexBuilder, light, overlayLight, red, green, blue, alpha);
        corner4.render(matrix, vertexBuilder, light, overlayLight, red, green, blue, alpha);
        corner3.render(matrix, vertexBuilder, light, overlayLight, red, green, blue, alpha);
        corner2.render(matrix, vertexBuilder, light, overlayLight, red, green, blue, alpha);
        corner1.render(matrix, vertexBuilder, light, overlayLight, red, green, blue, alpha);
    }

    private void renderCorners(@Nonnull MatrixStack matrix, @Nonnull IVertexBuilder vertexBuilder, int light, int overlayLight, float red, float green, float blue,
          float alpha) {
        matrix.push();
        matrix.scale(1.001F, 1.005F, 1.001F);
        matrix.translate(0, -0.0061, 0);
        corner8.render(matrix, vertexBuilder, light, overlayLight, red, green, blue, alpha);
        corner7.render(matrix, vertexBuilder, light, overlayLight, red, green, blue, alpha);
        corner6.render(matrix, vertexBuilder, light, overlayLight, red, green, blue, alpha);
        corner5.render(matrix, vertexBuilder, light, overlayLight, red, green, blue, alpha);
        corner4.render(matrix, vertexBuilder, light, overlayLight, red, green, blue, alpha);
        corner3.render(matrix, vertexBuilder, light, overlayLight, red, green, blue, alpha);
        corner2.render(matrix, vertexBuilder, light, overlayLight, red, green, blue, alpha);
        corner1.render(matrix, vertexBuilder, light, overlayLight, red, green, blue, alpha);
        matrix.pop();
    }

    public void renderSidesBatched(@Nonnull TileEntityEnergyCube tile, @Nonnull MatrixStack matrix, @Nonnull IRenderTypeBuffer renderer, int light, int overlayLight) {
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

    public void renderSidesBatched(@Nonnull ItemStack stack, EnergyCubeTier tier, @Nonnull MatrixStack matrix, @Nonnull IRenderTypeBuffer renderer, int light,
          int overlayLight, boolean hasEffect) {
        Set<RelativeSide> enabledSides;
        Set<RelativeSide> outputSides;
        CompoundNBT configData = ItemDataUtils.getDataMapIfPresent(stack);
        if (configData != null && configData.contains(NBTConstants.COMPONENT_CONFIG, NBT.TAG_COMPOUND)) {
            enabledSides = EnumSet.noneOf(RelativeSide.class);
            outputSides = EnumSet.noneOf(RelativeSide.class);
            CompoundNBT sideConfig = configData.getCompound(NBTConstants.COMPONENT_CONFIG).getCompound(NBTConstants.CONFIG + TransmissionType.ENERGY.ordinal());
            //TODO: Maybe improve on this, but for now this is a decent way of making it not have disabled sides show
            for (RelativeSide side : EnumUtils.SIDES) {
                DataType dataType = DataType.byIndexStatic(sideConfig.getInt(NBTConstants.SIDE + side.ordinal()));
                if (dataType.equals(DataType.INPUT)) {
                    enabledSides.add(side);
                } else if (dataType.equals(DataType.OUTPUT)) {
                    enabledSides.add(side);
                    outputSides.add(side);
                }
            }
        } else {
            enabledSides = EnumSet.allOf(RelativeSide.class);
            if (tier == EnergyCubeTier.CREATIVE) {
                outputSides = EnumSet.allOf(RelativeSide.class);
            } else {
                outputSides = EnumSet.of(RelativeSide.FRONT);
            }
        }
        renderSidesBatched(matrix, renderer, light, overlayLight, enabledSides, outputSides, hasEffect);
    }

    /**
     * Batched version of to render sides of the energy cube that render all sides per render type before switching to the next render type. This is because the way
     * Minecraft draws custom render types, is it flushes and instantly draws as soon as it gets a new type if it doesn't know how to handle the type.
     */
    private void renderSidesBatched(@Nonnull MatrixStack matrix, @Nonnull IRenderTypeBuffer renderer, int light, int overlayLight, Set<RelativeSide> enabledSides,
          Set<RelativeSide> outputSides, boolean hasEffect) {
        if (!enabledSides.isEmpty()) {
            IVertexBuilder buffer = getVertexBuilder(renderer, RENDER_TYPE, hasEffect);
            for (RelativeSide enabledSide : enabledSides) {
                int sideOrdinal = enabledSide.ordinal();
                connectors[sideOrdinal].render(matrix, buffer, light, overlayLight, 1, 1, 1, 1);
                ports[sideOrdinal].render(matrix, buffer, light, overlayLight, 1, 1, 1, 1);
            }
            if (!outputSides.isEmpty()) {
                buffer = getVertexBuilder(renderer, RENDER_TYPE_BASE, hasEffect);
                for (RelativeSide outputSide : outputSides) {
                    ports[outputSide.ordinal()].render(matrix, buffer, MekanismRenderer.FULL_LIGHT, overlayLight, 1, 1, 1, 1);
                }
                renderLEDS(outputSides, getVertexBuilder(renderer, RENDER_TYPE_ON, hasEffect), matrix, MekanismRenderer.FULL_LIGHT, overlayLight);
            }
        }
        if (outputSides.size() < EnumUtils.SIDES.length) {
            Set<RelativeSide> remainingSides = EnumSet.allOf(RelativeSide.class);
            remainingSides.removeAll(outputSides);
            renderLEDS(remainingSides, getVertexBuilder(renderer, RENDER_TYPE_OFF, hasEffect), matrix, light, overlayLight);
        }
    }

    private void renderLEDS(Set<RelativeSide> sides, IVertexBuilder ledBuffer, MatrixStack matrix, int light, int overlayLight) {
        for (RelativeSide side : sides) {
            int sideOrdinal = side.ordinal();
            leds1[sideOrdinal].render(matrix, ledBuffer, light, overlayLight, 1, 1, 1, 1);
            leds2[sideOrdinal].render(matrix, ledBuffer, light, overlayLight, 1, 1, 1, 1);
        }
    }

    public static class ModelEnergyCore extends MekanismJavaModel {

        private static final ResourceLocation CORE_TEXTURE = MekanismUtils.getResource(ResourceType.RENDER, "energy_core.png");

        private final RenderType RENDER_TYPE = getRenderType(CORE_TEXTURE);
        private final ModelRenderer cube;

        public ModelEnergyCore() {
            super(MekanismRenderType::mekStandard);
            textureWidth = 32;
            textureHeight = 32;

            cube = new ModelRenderer(this, 0, 0);
            cube.addBox(-8, -8, -8, 16, 16, 16, false);
            cube.setRotationPoint(0, 0, 0);
            cube.setTextureSize(32, 32);
            cube.mirror = true;
        }

        public IVertexBuilder getBuffer(@Nonnull IRenderTypeBuffer renderer) {
            return renderer.getBuffer(RENDER_TYPE);
        }

        public void render(@Nonnull MatrixStack matrix, @Nonnull IRenderTypeBuffer renderer, int light, int overlayLight, EnumColor color, float energyPercentage) {
            render(matrix, getBuffer(renderer), light, overlayLight, color.getColor(0), color.getColor(1), color.getColor(2),
                  energyPercentage);
        }

        public void render(@Nonnull MatrixStack matrix, @Nonnull IVertexBuilder buffer, int light, int overlayLight, EnumColor color, float energyPercentage) {
            cube.render(matrix, buffer, light, overlayLight, color.getColor(0), color.getColor(1), color.getColor(2), energyPercentage);
        }

        @Override
        public void render(@Nonnull MatrixStack matrix, @Nonnull IVertexBuilder vertexBuilder, int light, int overlayLight, float red, float green, float blue,
              float alpha) {
            cube.render(matrix, vertexBuilder, light, overlayLight, red, green, blue, alpha);
        }
    }
}