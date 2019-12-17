package mekanism.client.model;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.platform.GlStateManager.DestFactor;
import com.mojang.blaze3d.platform.GlStateManager.SourceFactor;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import javax.annotation.Nonnull;
import mekanism.client.render.MekanismRenderer;
import mekanism.client.render.MekanismRenderer.GlowInfo;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.ResourceType;
import net.minecraft.client.renderer.model.Model;
import net.minecraft.client.renderer.model.ModelRenderer;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;

public class ModelSecurityDesk extends Model {

    private static final ResourceLocation OVERLAY = MekanismUtils.getResource(ResourceType.RENDER, "security_desk_overlay.png");

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
        textureWidth = 128;
        textureHeight = 64;

        deskTop = new ModelRenderer(this, 0, 0);
        deskTop.func_228304_a_(0F, 0F, 0F, 16, 7, 16, false);
        deskTop.setRotationPoint(-8F, 11F, -8F);
        deskTop.setTextureSize(128, 64);
        deskTop.mirror = true;
        setRotation(deskTop, 0F, 0F, 0F);
        deskBase = new ModelRenderer(this, 0, 38);
        deskBase.func_228304_a_(0F, 0F, 0F, 16, 5, 16, false);
        deskBase.setRotationPoint(-8F, 19F, -8F);
        deskBase.setTextureSize(128, 64);
        deskBase.mirror = true;
        setRotation(deskBase, 0F, 0F, 0F);
        led = new ModelRenderer(this, 0, 0);
        led.func_228304_a_(12F, 4.5F, -1.5F, 1, 1, 1, false);
        led.setRotationPoint(-7F, 5F, 4F);
        led.setTextureSize(128, 64);
        led.mirror = true;
        setRotation(led, -0.4712389F, 0F, 0F);
        monitorBack = new ModelRenderer(this, 82, 0);
        monitorBack.func_228304_a_(1F, -3F, 0F, 12, 6, 1, false);
        monitorBack.setRotationPoint(-7F, 5F, 4F);
        monitorBack.setTextureSize(128, 64);
        monitorBack.mirror = true;
        setRotation(monitorBack, -0.4712389F, 0F, 0F);
        keyboard = new ModelRenderer(this, 64, 27);
        keyboard.func_228304_a_(0F, 0F, 0F, 10, 1, 5, false);
        keyboard.setRotationPoint(-5F, 10.5F, -6F);
        keyboard.setTextureSize(128, 64);
        keyboard.mirror = true;
        setRotation(keyboard, 0.0872665F, 0F, 0F);
        monitor = new ModelRenderer(this, 64, 10);
        monitor.func_228304_a_(0F, -5F, -2F, 14, 10, 2, false);
        monitor.setRotationPoint(-7F, 5F, 4F);
        monitor.setTextureSize(128, 64);
        monitor.mirror = true;
        setRotation(monitor, -0.4712389F, 0F, 0F);
        standNeck = new ModelRenderer(this, 96, 7);
        standNeck.func_228304_a_(0F, -7F, -1F, 2, 7, 1, false);
        standNeck.setRotationPoint(-1F, 10F, 6F);
        standNeck.setTextureSize(128, 64);
        standNeck.mirror = true;
        setRotation(standNeck, 0.0698132F, 0F, 0F);
        standBase = new ModelRenderer(this, 64, 22);
        standBase.func_228304_a_(0F, 0F, -4F, 8, 1, 4, false);
        standBase.setRotationPoint(-4F, 10F, 6F);
        standBase.setTextureSize(128, 64);
        standBase.mirror = true;
        setRotation(standBase, 0.1047198F, 0F, 0F);
        deskMiddle = new ModelRenderer(this, 0, 23);
        deskMiddle.func_228304_a_(0F, 0F, 0F, 14, 1, 14, false);
        deskMiddle.setRotationPoint(-7F, 18F, -7F);
        deskMiddle.setTextureSize(128, 64);
        deskMiddle.mirror = true;
        setRotation(deskMiddle, 0F, 0F, 0F);
        monitorScreen = new ModelRenderer(this, 64, 33);
        monitorScreen.func_228304_a_(0.5F, -4.5F, -2.01F, 13, 9, 2, false);
        monitorScreen.setRotationPoint(-7F, 5F, 4F);
        monitorScreen.setTextureSize(128, 64);
        monitorScreen.mirror = true;
        setRotation(monitorScreen, -0.4712389F, 0F, 0F);
    }

    @Override
    public void func_225598_a_(@Nonnull MatrixStack matrix, @Nonnull IVertexBuilder vertexBuilder, int light, int otherLight, float red, float green, float blue, float alpha) {
        //public void render(float size, TextureManager manager) {
        RenderSystem.pushMatrix();
        RenderSystem.shadeModel(GL11.GL_SMOOTH);
        RenderSystem.disableAlphaTest();
        RenderSystem.enableBlend();
        RenderSystem.blendFunc(SourceFactor.SRC_ALPHA, DestFactor.ONE_MINUS_SRC_ALPHA);

        doRender(matrix, vertexBuilder, light, otherLight, red, green, blue, alpha);

        manager.bindTexture(OVERLAY);
        RenderSystem.scalef(1.001F, 1.001F, 1.001F);
        RenderSystem.translatef(0, -0.0011F, 0);
        GlowInfo glowInfo = MekanismRenderer.enableGlow();

        doRender(matrix, vertexBuilder, light, otherLight, red, green, blue, alpha);

        MekanismRenderer.disableGlow(glowInfo);
        RenderSystem.disableBlend();
        RenderSystem.enableAlphaTest();
        RenderSystem.popMatrix();
    }

    public void doRender(@Nonnull MatrixStack matrix, @Nonnull IVertexBuilder vertexBuilder, int light, int otherLight, float red, float green, float blue, float alpha) {
        deskTop.func_228309_a_(matrix, vertexBuilder, light, otherLight, red, green, blue, alpha);
        deskBase.func_228309_a_(matrix, vertexBuilder, light, otherLight, red, green, blue, alpha);
        led.func_228309_a_(matrix, vertexBuilder, light, otherLight, red, green, blue, alpha);
        monitorBack.func_228309_a_(matrix, vertexBuilder, light, otherLight, red, green, blue, alpha);
        keyboard.func_228309_a_(matrix, vertexBuilder, light, otherLight, red, green, blue, alpha);
        monitor.func_228309_a_(matrix, vertexBuilder, light, otherLight, red, green, blue, alpha);
        standNeck.func_228309_a_(matrix, vertexBuilder, light, otherLight, red, green, blue, alpha);
        standBase.func_228309_a_(matrix, vertexBuilder, light, otherLight, red, green, blue, alpha);
        deskMiddle.func_228309_a_(matrix, vertexBuilder, light, otherLight, red, green, blue, alpha);
        monitorScreen.func_228309_a_(matrix, vertexBuilder, light, otherLight, red, green, blue, alpha);
    }

    private void setRotation(ModelRenderer model, float x, float y, float z) {
        model.rotateAngleX = x;
        model.rotateAngleY = y;
        model.rotateAngleZ = z;
    }
}