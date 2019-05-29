package mekanism.client.model;

import mekanism.client.render.MekanismRenderHelper;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.ResourceType;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class ModelSecurityDesk extends ModelBase {

    public static ResourceLocation OVERLAY = MekanismUtils.getResource(ResourceType.RENDER, "SecurityDesk_Overlay.png");

    ModelRenderer deskTop;
    ModelRenderer deskBase;
    ModelRenderer led;
    ModelRenderer monitorBack;
    ModelRenderer keyboard;
    ModelRenderer monitor;
    ModelRenderer standNeck;
    ModelRenderer standBase;
    ModelRenderer deskMiddle;
    ModelRenderer monitorScreen;

    public ModelSecurityDesk() {
        textureWidth = 128;
        textureHeight = 64;

        deskTop = new ModelRenderer(this, 0, 0);
        deskTop.addBox(0F, 0F, 0F, 16, 7, 16);
        deskTop.setRotationPoint(-8F, 11F, -8F);
        deskTop.setTextureSize(128, 64);
        deskTop.mirror = true;
        setRotation(deskTop, 0F, 0F, 0F);
        deskBase = new ModelRenderer(this, 0, 38);
        deskBase.addBox(0F, 0F, 0F, 16, 5, 16);
        deskBase.setRotationPoint(-8F, 19F, -8F);
        deskBase.setTextureSize(128, 64);
        deskBase.mirror = true;
        setRotation(deskBase, 0F, 0F, 0F);
        led = new ModelRenderer(this, 0, 0);
        led.addBox(12F, 4.5F, -1.5F, 1, 1, 1);
        led.setRotationPoint(-7F, 5F, 4F);
        led.setTextureSize(128, 64);
        led.mirror = true;
        setRotation(led, -0.4712389F, 0F, 0F);
        monitorBack = new ModelRenderer(this, 82, 0);
        monitorBack.addBox(1F, -3F, 0F, 12, 6, 1);
        monitorBack.setRotationPoint(-7F, 5F, 4F);
        monitorBack.setTextureSize(128, 64);
        monitorBack.mirror = true;
        setRotation(monitorBack, -0.4712389F, 0F, 0F);
        keyboard = new ModelRenderer(this, 64, 27);
        keyboard.addBox(0F, 0F, 0F, 10, 1, 5);
        keyboard.setRotationPoint(-5F, 10.5F, -6F);
        keyboard.setTextureSize(128, 64);
        keyboard.mirror = true;
        setRotation(keyboard, 0.0872665F, 0F, 0F);
        monitor = new ModelRenderer(this, 64, 10);
        monitor.addBox(0F, -5F, -2F, 14, 10, 2);
        monitor.setRotationPoint(-7F, 5F, 4F);
        monitor.setTextureSize(128, 64);
        monitor.mirror = true;
        setRotation(monitor, -0.4712389F, 0F, 0F);
        standNeck = new ModelRenderer(this, 96, 7);
        standNeck.addBox(0F, -7F, -1F, 2, 7, 1);
        standNeck.setRotationPoint(-1F, 10F, 6F);
        standNeck.setTextureSize(128, 64);
        standNeck.mirror = true;
        setRotation(standNeck, 0.0698132F, 0F, 0F);
        standBase = new ModelRenderer(this, 64, 22);
        standBase.addBox(0F, 0F, -4F, 8, 1, 4);
        standBase.setRotationPoint(-4F, 10F, 6F);
        standBase.setTextureSize(128, 64);
        standBase.mirror = true;
        setRotation(standBase, 0.1047198F, 0F, 0F);
        deskMiddle = new ModelRenderer(this, 0, 23);
        deskMiddle.addBox(0F, 0F, 0F, 14, 1, 14);
        deskMiddle.setRotationPoint(-7F, 18F, -7F);
        deskMiddle.setTextureSize(128, 64);
        deskMiddle.mirror = true;
        setRotation(deskMiddle, 0F, 0F, 0F);
        monitorScreen = new ModelRenderer(this, 64, 33);
        monitorScreen.addBox(0.5F, -4.5F, -2.01F, 13, 9, 2);
        monitorScreen.setRotationPoint(-7F, 5F, 4F);
        monitorScreen.setTextureSize(128, 64);
        monitorScreen.mirror = true;
        setRotation(monitorScreen, -0.4712389F, 0F, 0F);
    }

    public void render(float size, TextureManager manager) {
        MekanismRenderHelper renderHelper = new MekanismRenderHelper(true).enableBlendPreset();

        doRender(size);

        manager.bindTexture(OVERLAY);
        renderHelper.scale(1.001F).translateY(-0.0011F).enableGlow();

        doRender(size);

        renderHelper.cleanup();
    }

    private void doRender(float size) {
        deskTop.render(size);
        deskBase.render(size);
        led.render(size);
        monitorBack.render(size);
        keyboard.render(size);
        monitor.render(size);
        standNeck.render(size);
        standBase.render(size);
        deskMiddle.render(size);
        monitorScreen.render(size);
    }

    private void setRotation(ModelRenderer model, float x, float y, float z) {
        model.rotateAngleX = x;
        model.rotateAngleY = y;
        model.rotateAngleZ = z;
    }
}