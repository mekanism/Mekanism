package mekanism.client.model;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import javax.annotation.Nonnull;
import mekanism.client.render.MekanismRenderType;
import mekanism.client.render.MekanismRenderer;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.ResourceType;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.model.Model;
import net.minecraft.client.renderer.model.ModelRenderer;
import net.minecraft.util.ResourceLocation;

public class ModelSecurityDesk extends Model {

    private static final ResourceLocation DESK_TEXTURE = MekanismUtils.getResource(ResourceType.RENDER, "security_desk.png");
    private static final ResourceLocation OVERLAY = MekanismUtils.getResource(ResourceType.RENDER, "security_desk_overlay.png");
    private static final RenderType RENDER_TYPE_OVERLAY = MekanismRenderType.mekStandard(OVERLAY);
    private final RenderType RENDER_TYPE = func_228282_a_(DESK_TEXTURE);

    private final ModelRenderer deskTop;
    private final ModelRenderer deskBase;
    private final ModelRenderer led;
    private final ModelRenderer monitorBack;
    private final ModelRenderer keyboard;
    private final ModelRenderer monitor;
    private final ModelRenderer standNeck;
    private final ModelRenderer standBase;
    private final ModelRenderer deskMiddle;
    private final ModelRenderer monitorScreen;

    public ModelSecurityDesk() {
        super(RenderType::entitySolid);
        textureWidth = 128;
        textureHeight = 64;

        deskTop = new ModelRenderer(this, 0, 0);
        deskTop.addBox(0F, 0F, 0F, 16, 7, 16, false);
        deskTop.setRotationPoint(-8F, 11F, -8F);
        deskTop.setTextureSize(128, 64);
        deskTop.mirror = true;
        setRotation(deskTop, 0F, 0F, 0F);
        deskBase = new ModelRenderer(this, 0, 38);
        deskBase.addBox(0F, 0F, 0F, 16, 5, 16, false);
        deskBase.setRotationPoint(-8F, 19F, -8F);
        deskBase.setTextureSize(128, 64);
        deskBase.mirror = true;
        setRotation(deskBase, 0F, 0F, 0F);
        led = new ModelRenderer(this, 0, 0);
        led.addBox(12F, 4.5F, -1.5F, 1, 1, 1, false);
        led.setRotationPoint(-7F, 5F, 4F);
        led.setTextureSize(128, 64);
        led.mirror = true;
        setRotation(led, -0.4712389F, 0F, 0F);
        monitorBack = new ModelRenderer(this, 82, 0);
        monitorBack.addBox(1F, -3F, 0F, 12, 6, 1, false);
        monitorBack.setRotationPoint(-7F, 5F, 4F);
        monitorBack.setTextureSize(128, 64);
        monitorBack.mirror = true;
        setRotation(monitorBack, -0.4712389F, 0F, 0F);
        keyboard = new ModelRenderer(this, 64, 27);
        keyboard.addBox(0F, 0F, 0F, 10, 1, 5, false);
        keyboard.setRotationPoint(-5F, 10.5F, -6F);
        keyboard.setTextureSize(128, 64);
        keyboard.mirror = true;
        setRotation(keyboard, 0.0872665F, 0F, 0F);
        monitor = new ModelRenderer(this, 64, 10);
        monitor.addBox(0F, -5F, -2F, 14, 10, 2, false);
        monitor.setRotationPoint(-7F, 5F, 4F);
        monitor.setTextureSize(128, 64);
        monitor.mirror = true;
        setRotation(monitor, -0.4712389F, 0F, 0F);
        standNeck = new ModelRenderer(this, 96, 7);
        standNeck.addBox(0F, -7F, -1F, 2, 7, 1, false);
        standNeck.setRotationPoint(-1F, 10F, 6F);
        standNeck.setTextureSize(128, 64);
        standNeck.mirror = true;
        setRotation(standNeck, 0.0698132F, 0F, 0F);
        standBase = new ModelRenderer(this, 64, 22);
        standBase.addBox(0F, 0F, -4F, 8, 1, 4, false);
        standBase.setRotationPoint(-4F, 10F, 6F);
        standBase.setTextureSize(128, 64);
        standBase.mirror = true;
        setRotation(standBase, 0.1047198F, 0F, 0F);
        deskMiddle = new ModelRenderer(this, 0, 23);
        deskMiddle.addBox(0F, 0F, 0F, 14, 1, 14, false);
        deskMiddle.setRotationPoint(-7F, 18F, -7F);
        deskMiddle.setTextureSize(128, 64);
        deskMiddle.mirror = true;
        setRotation(deskMiddle, 0F, 0F, 0F);
        monitorScreen = new ModelRenderer(this, 64, 33);
        monitorScreen.addBox(0.5F, -4.5F, -2.01F, 13, 9, 2, false);
        monitorScreen.setRotationPoint(-7F, 5F, 4F);
        monitorScreen.setTextureSize(128, 64);
        monitorScreen.mirror = true;
        setRotation(monitorScreen, -0.4712389F, 0F, 0F);
    }

    public void render(@Nonnull MatrixStack matrix, @Nonnull IRenderTypeBuffer renderer, int light, int overlayLight) {
        render(matrix, renderer.getBuffer(RENDER_TYPE), light, overlayLight, 1, 1, 1, 1);
        matrix.push();
        matrix.scale(1.001F, 1.001F, 1.001F);
        matrix.translate(0, -0.0011, 0);
        render(matrix, renderer.getBuffer(RENDER_TYPE_OVERLAY), MekanismRenderer.FULL_LIGHT, overlayLight, 1, 1, 1, 1);
        matrix.pop();
    }

    @Override
    public void render(@Nonnull MatrixStack matrix, @Nonnull IVertexBuilder vertexBuilder, int light, int overlayLight, float red, float green, float blue,
          float alpha) {
        deskTop.render(matrix, vertexBuilder, light, overlayLight, red, green, blue, alpha);
        deskBase.render(matrix, vertexBuilder, light, overlayLight, red, green, blue, alpha);
        led.render(matrix, vertexBuilder, light, overlayLight, red, green, blue, alpha);
        monitorBack.render(matrix, vertexBuilder, light, overlayLight, red, green, blue, alpha);
        keyboard.render(matrix, vertexBuilder, light, overlayLight, red, green, blue, alpha);
        monitor.render(matrix, vertexBuilder, light, overlayLight, red, green, blue, alpha);
        standNeck.render(matrix, vertexBuilder, light, overlayLight, red, green, blue, alpha);
        standBase.render(matrix, vertexBuilder, light, overlayLight, red, green, blue, alpha);
        deskMiddle.render(matrix, vertexBuilder, light, overlayLight, red, green, blue, alpha);
        monitorScreen.render(matrix, vertexBuilder, light, overlayLight, red, green, blue, alpha);
    }

    private void setRotation(ModelRenderer model, float x, float y, float z) {
        model.rotateAngleX = x;
        model.rotateAngleY = y;
        model.rotateAngleZ = z;
    }
}