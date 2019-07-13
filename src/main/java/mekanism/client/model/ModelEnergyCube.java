package mekanism.client.model;

import mekanism.client.render.MekanismRenderer;
import mekanism.client.render.MekanismRenderer.GlowInfo;
import mekanism.common.SideData.IOState;
import mekanism.common.tier.EnergyCubeTier;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.ResourceType;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class ModelEnergyCube extends ModelBase {

    public static ResourceLocation OVERLAY_ON = MekanismUtils.getResource(ResourceType.RENDER, "EnergyCube_OverlayOn.png");
    public static ResourceLocation OVERLAY_OFF = MekanismUtils.getResource(ResourceType.RENDER, "EnergyCube_OverlayOff.png");
    public static ResourceLocation BASE_OVERLAY = MekanismUtils.getResource(ResourceType.RENDER, "EnergyCube_OverlayBase.png");
    public ModelRenderer[] leds1;
    public ModelRenderer[] leds2;
    public ModelRenderer[] ports;
    public ModelRenderer[] connectors;
    ModelRenderer frame12;
    ModelRenderer frame11;
    ModelRenderer frame10;
    ModelRenderer frame9;
    ModelRenderer frame8;
    ModelRenderer frame7;
    ModelRenderer frame6;
    ModelRenderer frame5;
    ModelRenderer frame4;
    ModelRenderer frame3;
    ModelRenderer frame2;
    ModelRenderer frame1;
    ModelRenderer corner8;
    ModelRenderer corner7;
    ModelRenderer corner6;
    ModelRenderer corner5;
    ModelRenderer corner4;
    ModelRenderer corner3;
    ModelRenderer corner2;
    ModelRenderer corner1;
    ModelRenderer connectorBackToggle;
    ModelRenderer connectorRightToggle;
    ModelRenderer connectorBottomToggle;
    ModelRenderer connectorLeftToggle;
    ModelRenderer connectorFrontToggle;
    ModelRenderer connectorTopToggle;
    ModelRenderer portBackToggle;
    ModelRenderer portBottomToggle;
    ModelRenderer portFrontToggle;
    ModelRenderer portLeftToggle;
    ModelRenderer portRightToggle;
    ModelRenderer portTopToggle;
    ModelRenderer ledTop1;
    ModelRenderer ledTop2;
    ModelRenderer ledBack1;
    ModelRenderer ledBack2;
    ModelRenderer ledBottom2;
    ModelRenderer ledBottom1;
    ModelRenderer ledFront1;
    ModelRenderer ledFront2;
    ModelRenderer ledRight2;
    ModelRenderer ledRight1;
    ModelRenderer ledLeft1;
    ModelRenderer ledLeft2;

    public ModelEnergyCube() {
        textureWidth = 64;
        textureHeight = 64;

        frame12 = new ModelRenderer(this, 0, 0);
        frame12.addBox(0F, 0F, 0F, 3, 10, 3);
        frame12.setRotationPoint(-8F, 11F, 5F);
        frame12.setTextureSize(64, 64);
        frame12.mirror = true;
        setRotation(frame12, 0F, 0F, 0F);
        frame11 = new ModelRenderer(this, 0, 0);
        frame11.addBox(0F, 0F, 0F, 3, 10, 3);
        frame11.setRotationPoint(5F, 11F, -8F);
        frame11.setTextureSize(64, 64);
        frame11.mirror = true;
        setRotation(frame11, 0F, 0F, 0F);
        frame10 = new ModelRenderer(this, 0, 13);
        frame10.addBox(0F, 0F, 0F, 10, 3, 3);
        frame10.setRotationPoint(-5F, 21F, 5F);
        frame10.setTextureSize(64, 64);
        frame10.mirror = true;
        setRotation(frame10, 0F, 0F, 0F);
        frame9 = new ModelRenderer(this, 12, 0);
        frame9.addBox(0F, 0F, 0F, 3, 3, 10);
        frame9.setRotationPoint(5F, 21F, -5F);
        frame9.setTextureSize(64, 64);
        frame9.mirror = true;
        setRotation(frame9, 0F, 0F, 0F);
        frame8 = new ModelRenderer(this, 0, 13);
        frame8.addBox(0F, 0F, 0F, 10, 3, 3);
        frame8.setRotationPoint(-5F, 8F, 5F);
        frame8.setTextureSize(64, 64);
        frame8.mirror = true;
        setRotation(frame8, 0F, 0F, 0F);
        frame7 = new ModelRenderer(this, 0, 13);
        frame7.addBox(0F, 0F, 0F, 10, 3, 3);
        frame7.setRotationPoint(-5F, 21F, -8F);
        frame7.setTextureSize(64, 64);
        frame7.mirror = true;
        setRotation(frame7, 0F, 0F, 0F);
        frame6 = new ModelRenderer(this, 0, 0);
        frame6.addBox(0F, 0F, 0F, 3, 10, 3);
        frame6.setRotationPoint(5F, 11F, 5F);
        frame6.setTextureSize(64, 64);
        frame6.mirror = true;
        setRotation(frame6, 0F, 0F, 0F);
        frame5 = new ModelRenderer(this, 0, 0);
        frame5.addBox(0F, 0F, 0F, 3, 10, 3);
        frame5.setRotationPoint(-8F, 11F, -8F);
        frame5.setTextureSize(64, 64);
        frame5.mirror = true;
        setRotation(frame5, 0F, 0F, 0F);
        frame4 = new ModelRenderer(this, 12, 0);
        frame4.addBox(0F, 0F, 0F, 3, 3, 10);
        frame4.setRotationPoint(5F, 8F, -5F);
        frame4.setTextureSize(64, 64);
        frame4.mirror = true;
        setRotation(frame4, 0F, 0F, 0F);
        frame3 = new ModelRenderer(this, 12, 0);
        frame3.addBox(0F, 0F, 0F, 3, 3, 10);
        frame3.setRotationPoint(-8F, 21F, -5F);
        frame3.setTextureSize(64, 64);
        frame3.mirror = true;
        setRotation(frame3, 0F, 0F, 0F);
        frame2 = new ModelRenderer(this, 12, 0);
        frame2.addBox(0F, 0F, 0F, 3, 3, 10);
        frame2.setRotationPoint(-8F, 8F, -5F);
        frame2.setTextureSize(64, 64);
        frame2.mirror = true;
        setRotation(frame2, 0F, 0F, 0F);
        frame1 = new ModelRenderer(this, 0, 13);
        frame1.addBox(0F, 0F, 0F, 10, 3, 3);
        frame1.setRotationPoint(-5F, 8F, -8F);
        frame1.setTextureSize(64, 64);
        frame1.mirror = true;
        setRotation(frame1, 0F, 0F, 0F);
        corner8 = new ModelRenderer(this, 26, 13);
        corner8.addBox(0F, 0F, 0F, 3, 3, 3);
        corner8.setRotationPoint(5F, 21F, 5F);
        corner8.setTextureSize(64, 64);
        corner8.mirror = true;
        setRotation(corner8, 0F, 0F, 0F);
        corner7 = new ModelRenderer(this, 26, 13);
        corner7.addBox(0F, 0F, 0F, 3, 3, 3);
        corner7.setRotationPoint(5F, 21F, -8F);
        corner7.setTextureSize(64, 64);
        corner7.mirror = true;
        setRotation(corner7, 0F, 0F, 0F);
        corner6 = new ModelRenderer(this, 26, 13);
        corner6.addBox(0F, 0F, 0F, 3, 3, 3);
        corner6.setRotationPoint(-8F, 21F, 5F);
        corner6.setTextureSize(64, 64);
        corner6.mirror = true;
        setRotation(corner6, 0F, 0F, 0F);
        corner5 = new ModelRenderer(this, 26, 13);
        corner5.addBox(0F, 0F, 0F, 3, 3, 3);
        corner5.setRotationPoint(-8F, 21F, -8F);
        corner5.setTextureSize(64, 64);
        corner5.mirror = true;
        setRotation(corner5, 0F, 0F, 0F);
        corner4 = new ModelRenderer(this, 26, 13);
        corner4.addBox(0F, 0F, 0F, 3, 3, 3);
        corner4.setRotationPoint(5F, 8F, 5F);
        corner4.setTextureSize(64, 64);
        corner4.mirror = true;
        setRotation(corner4, 0F, 0F, 0F);
        corner3 = new ModelRenderer(this, 26, 13);
        corner3.addBox(0F, 0F, 0F, 3, 3, 3);
        corner3.setRotationPoint(5F, 8F, -8F);
        corner3.setTextureSize(64, 64);
        corner3.mirror = true;
        setRotation(corner3, 0F, 0F, 0F);
        corner2 = new ModelRenderer(this, 26, 13);
        corner2.addBox(0F, 0F, 0F, 3, 3, 3);
        corner2.setRotationPoint(-8F, 8F, 5F);
        corner2.setTextureSize(64, 64);
        corner2.mirror = true;
        setRotation(corner2, 0F, 0F, 0F);
        corner1 = new ModelRenderer(this, 26, 13);
        corner1.addBox(0F, 0F, 0F, 3, 3, 3);
        corner1.setRotationPoint(-8F, 8F, -8F);
        corner1.setTextureSize(64, 64);
        corner1.mirror = true;
        setRotation(corner1, 0F, 0F, 0F);
        connectorBackToggle = new ModelRenderer(this, 38, 16);
        connectorBackToggle.addBox(0F, 0F, 0F, 10, 6, 1);
        connectorBackToggle.setRotationPoint(-5F, 13F, 6F);
        connectorBackToggle.setTextureSize(64, 64);
        connectorBackToggle.mirror = true;
        setRotation(connectorBackToggle, 0F, 0F, 0F);
        connectorRightToggle = new ModelRenderer(this, 38, 0);
        connectorRightToggle.addBox(0F, 0F, 0F, 1, 6, 10);
        connectorRightToggle.setRotationPoint(6F, 13F, -5F);
        connectorRightToggle.setTextureSize(64, 64);
        connectorRightToggle.mirror = true;
        setRotation(connectorRightToggle, 0F, 0F, 0F);
        connectorBottomToggle = new ModelRenderer(this, 0, 19);
        connectorBottomToggle.addBox(0F, 0F, 0F, 10, 1, 6);
        connectorBottomToggle.setRotationPoint(-5F, 22F, -3F);
        connectorBottomToggle.setTextureSize(64, 64);
        connectorBottomToggle.mirror = true;
        setRotation(connectorBottomToggle, 0F, 0F, 0F);
        connectorLeftToggle = new ModelRenderer(this, 38, 0);
        connectorLeftToggle.addBox(0F, 0F, 0F, 1, 6, 10);
        connectorLeftToggle.setRotationPoint(-7F, 13F, -5F);
        connectorLeftToggle.setTextureSize(64, 64);
        connectorLeftToggle.mirror = true;
        setRotation(connectorLeftToggle, 0F, 0F, 0F);
        connectorFrontToggle = new ModelRenderer(this, 38, 16);
        connectorFrontToggle.addBox(0F, 0F, 0F, 10, 6, 1);
        connectorFrontToggle.setRotationPoint(-5F, 13F, -7F);
        connectorFrontToggle.setTextureSize(64, 64);
        connectorFrontToggle.mirror = true;
        setRotation(connectorFrontToggle, 0F, 0F, 0F);
        connectorTopToggle = new ModelRenderer(this, 0, 19);
        connectorTopToggle.addBox(0F, 0F, 0F, 10, 1, 6);
        connectorTopToggle.setRotationPoint(-5F, 9F, -3F);
        connectorTopToggle.setTextureSize(64, 64);
        connectorTopToggle.mirror = true;
        setRotation(connectorTopToggle, 0F, 0F, 0F);
        portBackToggle = new ModelRenderer(this, 18, 35);
        portBackToggle.addBox(0F, 0F, 0F, 8, 8, 1);
        portBackToggle.setRotationPoint(-4F, 12F, 7F);
        portBackToggle.setTextureSize(64, 64);
        portBackToggle.mirror = true;
        setRotation(portBackToggle, 0F, 0F, 0F);
        portBottomToggle = new ModelRenderer(this, 0, 26);
        portBottomToggle.addBox(0F, 0F, 0F, 8, 1, 8);
        portBottomToggle.setRotationPoint(-4F, 23F, -4F);
        portBottomToggle.setTextureSize(64, 64);
        portBottomToggle.mirror = true;
        setRotation(portBottomToggle, 0F, 0F, 0F);
        portFrontToggle = new ModelRenderer(this, 18, 35);
        portFrontToggle.addBox(0F, 0F, 0F, 8, 8, 1);
        portFrontToggle.setRotationPoint(-4F, 12F, -8F);
        portFrontToggle.setTextureSize(64, 64);
        portFrontToggle.mirror = true;
        setRotation(portFrontToggle, 0F, 0F, 0F);
        portLeftToggle = new ModelRenderer(this, 0, 35);
        portLeftToggle.addBox(0F, 0F, 0F, 1, 8, 8);
        portLeftToggle.setRotationPoint(-8F, 12F, -4F);
        portLeftToggle.setTextureSize(64, 64);
        portLeftToggle.mirror = true;
        setRotation(portLeftToggle, 0F, 0F, 0F);
        portRightToggle = new ModelRenderer(this, 0, 35);
        portRightToggle.addBox(0F, 0F, 0F, 1, 8, 8);
        portRightToggle.setRotationPoint(7F, 12F, -4F);
        portRightToggle.setTextureSize(64, 64);
        portRightToggle.mirror = true;
        setRotation(portRightToggle, 0F, 0F, 0F);
        portTopToggle = new ModelRenderer(this, 0, 26);
        portTopToggle.addBox(0F, 0F, 0F, 8, 1, 8);
        portTopToggle.setRotationPoint(-4F, 8F, -4F);
        portTopToggle.setTextureSize(64, 64);
        portTopToggle.mirror = true;
        setRotation(portTopToggle, 0F, 0F, 0F);
        ledTop1 = new ModelRenderer(this, 0, 51);
        ledTop1.addBox(0F, 0F, 0F, 1, 1, 1);
        ledTop1.setRotationPoint(-5.5F, 8.1F, -0.5F);
        ledTop1.setTextureSize(64, 64);
        ledTop1.mirror = true;
        setRotation(ledTop1, 0F, 0F, 0F);
        ledTop2 = new ModelRenderer(this, 0, 51);
        ledTop2.addBox(0F, 0F, 0F, 1, 1, 1);
        ledTop2.setRotationPoint(4.5F, 8.1F, -0.5F);
        ledTop2.setTextureSize(64, 64);
        ledTop2.mirror = true;
        setRotation(ledTop2, 0F, 0F, 0F);
        ledBack1 = new ModelRenderer(this, 0, 51);
        ledBack1.addBox(0F, 0F, 0F, 1, 1, 1);
        ledBack1.setRotationPoint(-5.5F, 15.5F, 6.9F);
        ledBack1.setTextureSize(64, 64);
        ledBack1.mirror = true;
        setRotation(ledBack1, 0F, 0F, 0F);
        ledBack2 = new ModelRenderer(this, 0, 51);
        ledBack2.addBox(0F, 0F, 0F, 1, 1, 1);
        ledBack2.setRotationPoint(4.5F, 15.5F, 6.9F);
        ledBack2.setTextureSize(64, 64);
        ledBack2.mirror = true;
        setRotation(ledBack2, 0F, 0F, 0F);
        ledBottom2 = new ModelRenderer(this, 0, 51);
        ledBottom2.addBox(0F, 0F, 0F, 1, 1, 1);
        ledBottom2.setRotationPoint(4.5F, 22.9F, -0.5F);
        ledBottom2.setTextureSize(64, 64);
        ledBottom2.mirror = true;
        setRotation(ledBottom2, 0F, 0F, 0F);
        ledBottom1 = new ModelRenderer(this, 0, 51);
        ledBottom1.addBox(0F, 0F, 0F, 1, 1, 1);
        ledBottom1.setRotationPoint(-5.5F, 22.9F, -0.5F);
        ledBottom1.setTextureSize(64, 64);
        ledBottom1.mirror = true;
        setRotation(ledBottom1, 0F, 0F, 0F);
        ledFront1 = new ModelRenderer(this, 0, 51);
        ledFront1.addBox(0F, 0F, 0F, 1, 1, 1);
        ledFront1.setRotationPoint(-5.5F, 15.5F, -7.9F);
        ledFront1.setTextureSize(64, 64);
        ledFront1.mirror = true;
        setRotation(ledFront1, 0F, 0F, 0F);
        ledFront2 = new ModelRenderer(this, 0, 51);
        ledFront2.addBox(0F, 0F, 0F, 1, 1, 1);
        ledFront2.setRotationPoint(4.5F, 15.5F, -7.9F);
        ledFront2.setTextureSize(64, 64);
        ledFront2.mirror = true;
        setRotation(ledFront2, 0F, 0F, 0F);
        ledRight2 = new ModelRenderer(this, 0, 51);
        ledRight2.addBox(0F, 0F, 0F, 1, 1, 1);
        ledRight2.setRotationPoint(6.9F, 15.5F, 4.5F);
        ledRight2.setTextureSize(64, 64);
        ledRight2.mirror = true;
        setRotation(ledRight2, 0F, 0F, 0F);
        ledRight1 = new ModelRenderer(this, 0, 51);
        ledRight1.addBox(0F, 0F, 0F, 1, 1, 1);
        ledRight1.setRotationPoint(6.9F, 15.5F, -5.5F);
        ledRight1.setTextureSize(64, 64);
        ledRight1.mirror = true;
        setRotation(ledRight1, 0F, 0F, 0F);
        ledLeft1 = new ModelRenderer(this, 0, 51);
        ledLeft1.addBox(0F, 0F, 0F, 1, 1, 1);
        ledLeft1.setRotationPoint(-7.9F, 15.5F, 4.5F);
        ledLeft1.setTextureSize(64, 64);
        ledLeft1.mirror = true;
        setRotation(ledLeft1, 0F, 0F, 0F);
        ledLeft2 = new ModelRenderer(this, 0, 51);
        ledLeft2.addBox(0F, 0F, 0F, 1, 1, 1);
        ledLeft2.setRotationPoint(-7.9F, 15.5F, -5.5F);
        ledLeft2.setTextureSize(64, 64);
        ledLeft2.mirror = true;
        setRotation(ledLeft2, 0F, 0F, 0F);

        leds1 = new ModelRenderer[]{ledBottom1, ledTop1, ledFront1, ledBack1, ledLeft1, ledRight1};
        leds2 = new ModelRenderer[]{ledBottom2, ledTop2, ledFront2, ledBack2, ledLeft2, ledRight2};

        ports = new ModelRenderer[]{portBottomToggle, portTopToggle, portFrontToggle, portBackToggle, portLeftToggle, portRightToggle};
        connectors = new ModelRenderer[]{connectorBottomToggle, connectorTopToggle, connectorFrontToggle, connectorBackToggle, connectorLeftToggle, connectorRightToggle};
    }

    public void render(float size, EnergyCubeTier tier, TextureManager manager, boolean renderMain) {
        if (renderMain) {
            frame12.render(size);
            frame11.render(size);
            frame10.render(size);
            frame9.render(size);
            frame8.render(size);
            frame7.render(size);
            frame6.render(size);
            frame5.render(size);
            frame4.render(size);
            frame3.render(size);
            frame2.render(size);
            frame1.render(size);

            corner8.render(size);
            corner7.render(size);
            corner6.render(size);
            corner5.render(size);
            corner4.render(size);
            corner3.render(size);
            corner2.render(size);
            corner1.render(size);
        }

        GlStateManager.pushMatrix();
        GlStateManager.scale(1.001F, 1.005F, 1.001F);
        GlStateManager.translate(0, -0.0061F, 0);
        manager.bindTexture(BASE_OVERLAY);
        MekanismRenderer.color(tier.getBaseTier().getColor());

        corner8.render(size);
        corner7.render(size);
        corner6.render(size);
        corner5.render(size);
        corner4.render(size);
        corner3.render(size);
        corner2.render(size);
        corner1.render(size);

        MekanismRenderer.resetColor();
        GlStateManager.popMatrix();
    }

    public void renderSide(float size, EnumFacing side, IOState state, EnergyCubeTier tier, TextureManager renderer) {
        if (state != IOState.OFF) { //input or output
            connectors[side.ordinal()].render(size);
            ports[side.ordinal()].render(size);
        }

        GlowInfo glowInfo;
        if (state == IOState.OUTPUT) {
            glowInfo = MekanismRenderer.enableGlow();
            renderer.bindTexture(BASE_OVERLAY);
            ports[side.ordinal()].render(size);
        } else {
            glowInfo = MekanismRenderer.NO_GLOW;
        }

        renderer.bindTexture(state == IOState.OUTPUT ? OVERLAY_ON : OVERLAY_OFF);

        leds1[side.ordinal()].render(size);
        leds2[side.ordinal()].render(size);
        MekanismRenderer.disableGlow(glowInfo);
    }

    private void setRotation(ModelRenderer model, float x, float y, float z) {
        model.rotateAngleX = x;
        model.rotateAngleY = y;
        model.rotateAngleZ = z;
    }

    public static class ModelEnergyCore extends ModelBase {

        private ModelRenderer cube;

        public ModelEnergyCore() {
            textureWidth = 32;
            textureHeight = 32;

            cube = new ModelRenderer(this, 0, 0);
            cube.addBox(-8, -8, -8, 16, 16, 16);
            cube.setRotationPoint(0, 0, 0);
            cube.setTextureSize(32, 32);
            cube.mirror = true;
        }

        public void render(float size) {
            cube.render(size);
        }
    }
}