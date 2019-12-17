package mekanism.client.model;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.platform.GlStateManager.DestFactor;
import com.mojang.blaze3d.platform.GlStateManager.SourceFactor;
import mekanism.client.render.MekanismRenderer;
import mekanism.client.render.MekanismRenderer.GlowInfo;
import net.minecraft.client.renderer.model.ModelRenderer;
import net.minecraft.client.renderer.model.Model;
import org.lwjgl.opengl.GL11;

public class ModelJetpack extends Model {

    private final ModelRenderer Packtop;
    private final ModelRenderer Packbottom;
    private final ModelRenderer Thrusterleft;
    private final ModelRenderer Thrusterright;
    private final ModelRenderer Fueltuberight;
    private final ModelRenderer Fueltubeleft;
    private final ModelRenderer Packmid;
    private final ModelRenderer Packcore;
    private final ModelRenderer WingsupportL;
    private final ModelRenderer WingsupportR;
    private final ModelRenderer Packtoprear;
    private final ModelRenderer ExtendosupportL;
    private final ModelRenderer ExtendosupportR;
    private final ModelRenderer WingbladeL;
    private final ModelRenderer WingbladeR;
    private final ModelRenderer Packdoodad2;
    private final ModelRenderer Packdoodad3;
    private final ModelRenderer Bottomthruster;
    private final ModelRenderer light1;
    private final ModelRenderer light2;
    private final ModelRenderer light3;

    public ModelJetpack() {
        textureWidth = 128;
        textureHeight = 64;

        Packtop = new ModelRenderer(this, 92, 28);
        Packtop.func_228304_a_(-4F, 0F, 4F, 8, 4, 1, false);
        Packtop.setRotationPoint(0F, 0F, 0F);
        Packtop.setTextureSize(128, 64);
        Packtop.mirror = true;
        setRotation(Packtop, 0.2094395F, 0F, 0F);
        Packbottom = new ModelRenderer(this, 92, 42);
        Packbottom.func_228304_a_(-4F, 4.1F, 1.5F, 8, 4, 4, false);
        Packbottom.setRotationPoint(0F, 0F, 0F);
        Packbottom.setTextureSize(128, 64);
        Packbottom.mirror = true;
        setRotation(Packbottom, -0.0872665F, 0F, 0F);
        Thrusterleft = new ModelRenderer(this, 69, 30);
        Thrusterleft.func_228304_a_(7.8F, 1.5F, -3.5F, 3, 3, 3, false);
        Thrusterleft.setRotationPoint(0F, 0F, 0F);
        Thrusterleft.setTextureSize(128, 64);
        Thrusterleft.mirror = true;
        setRotation(Thrusterleft, 0.7853982F, -0.715585F, 0.3490659F);
        Thrusterright = new ModelRenderer(this, 69, 30);
        Thrusterright.func_228304_a_(-10.8F, 1.5F, -3.5F, 3, 3, 3, false);
        Thrusterright.setRotationPoint(0F, 0F, 0F);
        Thrusterright.setTextureSize(128, 64);
        Thrusterright.mirror = true;
        setRotation(Thrusterright, 0.7853982F, 0.715585F, -0.3490659F);
        Fueltuberight = new ModelRenderer(this, 92, 23);
        Fueltuberight.func_228304_a_(-11.2F, 2F, -3F, 8, 2, 2, false);
        Fueltuberight.setRotationPoint(0F, 0F, 0F);
        Fueltuberight.setTextureSize(128, 64);
        Fueltuberight.mirror = true;
        setRotation(Fueltuberight, 0.7853982F, 0.715585F, -0.3490659F);
        Fueltubeleft = new ModelRenderer(this, 92, 23);
        Fueltubeleft.func_228304_a_(3.2F, 2F, -3F, 8, 2, 2, false);
        Fueltubeleft.setRotationPoint(0F, 0F, 0F);
        Fueltubeleft.setTextureSize(128, 64);
        Fueltubeleft.mirror = true;
        setRotation(Fueltubeleft, 0.7853982F, -0.715585F, 0.3490659F);
        Packmid = new ModelRenderer(this, 92, 34);
        Packmid.func_228304_a_(-4F, 3.3F, 1.5F, 8, 1, 4, false);
        Packmid.setRotationPoint(0F, 0F, 0F);
        Packmid.setTextureSize(128, 64);
        Packmid.mirror = true;
        setRotation(Packmid, 0F, 0F, 0F);
        Packcore = new ModelRenderer(this, 69, 2);
        Packcore.func_228304_a_(-3.5F, 3F, 2F, 7, 1, 3, false);
        Packcore.setRotationPoint(0F, 0F, 0F);
        Packcore.setTextureSize(128, 64);
        Packcore.mirror = true;
        setRotation(Packcore, 0F, 0F, 0F);
        WingsupportL = new ModelRenderer(this, 71, 55);
        WingsupportL.func_228304_a_(3F, -1F, 2.2F, 7, 2, 2, false);
        WingsupportL.setRotationPoint(0F, 0F, 0F);
        WingsupportL.setTextureSize(128, 64);
        WingsupportL.mirror = true;
        setRotation(WingsupportL, 0F, 0F, 0.2792527F);
        WingsupportR = new ModelRenderer(this, 71, 55);
        WingsupportR.func_228304_a_(-10F, -1F, 2.2F, 7, 2, 2, false);
        WingsupportR.setRotationPoint(0F, 0F, 0F);
        WingsupportR.setTextureSize(128, 64);
        WingsupportR.mirror = true;
        setRotation(WingsupportR, 0F, 0F, -0.2792527F);
        Packtoprear = new ModelRenderer(this, 106, 28);
        Packtoprear.func_228304_a_(-4F, 1F, 1F, 8, 3, 3, false);
        Packtoprear.setRotationPoint(0F, 0F, 0F);
        Packtoprear.setTextureSize(128, 64);
        Packtoprear.mirror = true;
        setRotation(Packtoprear, 0.2094395F, 0F, 0F);
        ExtendosupportL = new ModelRenderer(this, 94, 16);
        ExtendosupportL.func_228304_a_(8F, -0.2F, 2.5F, 9, 1, 1, false);
        ExtendosupportL.setRotationPoint(0F, 0F, 0F);
        ExtendosupportL.setTextureSize(128, 64);
        ExtendosupportL.mirror = true;
        setRotation(ExtendosupportL, 0F, 0F, 0.2792527F);
        ExtendosupportR = new ModelRenderer(this, 94, 16);
        ExtendosupportR.func_228304_a_(-17F, -0.2F, 2.5F, 9, 1, 1, false);
        ExtendosupportR.setRotationPoint(0F, 0F, 0F);
        ExtendosupportR.setTextureSize(128, 64);
        ExtendosupportR.mirror = true;
        setRotation(ExtendosupportR, 0F, 0F, -0.2792527F);
        WingbladeL = new ModelRenderer(this, 62, 5);
        WingbladeL.func_228304_a_(3.3F, 1.1F, 3F, 14, 2, 0, false);
        WingbladeL.setRotationPoint(0F, 0F, 0F);
        WingbladeL.setTextureSize(128, 64);
        WingbladeL.mirror = true;
        setRotation(WingbladeL, 0F, 0F, 0.2094395F);
        WingbladeR = new ModelRenderer(this, 62, 5);
        WingbladeR.func_228304_a_(-17.3F, 1.1F, 3F, 14, 2, 0, false);
        WingbladeR.setRotationPoint(0F, 0F, 0F);
        WingbladeR.setTextureSize(128, 64);
        WingbladeR.mirror = true;
        setRotation(WingbladeR, 0F, 0F, -0.2094395F);
        Packdoodad2 = new ModelRenderer(this, 116, 0);
        Packdoodad2.func_228304_a_(1F, 0.5F, 4.2F, 2, 1, 1, false);
        Packdoodad2.setRotationPoint(0F, 0F, 0F);
        Packdoodad2.setTextureSize(128, 64);
        Packdoodad2.mirror = true;
        setRotation(Packdoodad2, 0.2094395F, 0F, 0F);
        Packdoodad3 = new ModelRenderer(this, 116, 0);
        Packdoodad3.func_228304_a_(1F, 2F, 4.2F, 2, 1, 1, false);
        Packdoodad3.setRotationPoint(0F, 0F, 0F);
        Packdoodad3.setTextureSize(128, 64);
        Packdoodad3.mirror = true;
        setRotation(Packdoodad3, 0.2094395F, 0F, 0F);
        Bottomthruster = new ModelRenderer(this, 68, 26);
        Bottomthruster.func_228304_a_(-3F, 8F, 2.333333F, 6, 1, 2, false);
        Bottomthruster.setRotationPoint(0F, 0F, 0F);
        Bottomthruster.setTextureSize(128, 64);
        Bottomthruster.mirror = true;
        setRotation(Bottomthruster, 0F, 0F, 0F);
        light1 = new ModelRenderer(this, 55, 2);
        light1.func_228304_a_(2F, 6.55F, 4F, 1, 1, 1, false);
        light1.setRotationPoint(0F, 0F, 0F);
        light1.setTextureSize(128, 64);
        light1.mirror = true;
        setRotation(light1, 0F, 0F, 0F);
        light2 = new ModelRenderer(this, 55, 2);
        light2.func_228304_a_(0F, 6.55F, 4F, 1, 1, 1, false);
        light2.setRotationPoint(0F, 0F, 0F);
        light2.setTextureSize(128, 64);
        light2.mirror = true;
        setRotation(light2, 0F, 0F, 0F);
        light3 = new ModelRenderer(this, 55, 2);
        light3.func_228304_a_(-3F, 6.55F, 4F, 1, 1, 1, false);
        light3.setRotationPoint(0F, 0F, 0F);
        light3.setTextureSize(128, 64);
        light3.mirror = true;
        setRotation(light3, 0F, 0F, 0F);
    }

    public void render(float size) {
        Packtop.render(size);
        Packbottom.render(size);
        Thrusterleft.render(size);
        Thrusterright.render(size);
        Fueltuberight.render(size);
        Fueltubeleft.render(size);
        Packmid.render(size);

        WingsupportL.render(size);
        WingsupportR.render(size);
        Packtoprear.render(size);
        ExtendosupportL.render(size);
        ExtendosupportR.render(size);

        Packdoodad2.render(size);
        Packdoodad3.render(size);
        Bottomthruster.render(size);

        GlowInfo glowInfo = MekanismRenderer.enableGlow();
        Packcore.render(size);

        light1.render(size);
        light2.render(size);
        light3.render(size);
        Packcore.render(size);

        //Wing blades need some more special stuff
        GlStateManager.pushMatrix();
        GlStateManager.shadeModel(GL11.GL_SMOOTH);
        GlStateManager.disableAlphaTest();
        GlStateManager.enableBlend();
        GlStateManager.blendFunc(SourceFactor.SRC_ALPHA, DestFactor.ONE_MINUS_SRC_ALPHA);
        GlStateManager.enableCull();
        GlStateManager.color4f(1, 1, 1, 0.2F);

        WingbladeL.render(size);
        WingbladeR.render(size);

        MekanismRenderer.resetColor();
        GlStateManager.disableCull();
        GlStateManager.disableBlend();
        GlStateManager.enableAlphaTest();
        GlStateManager.popMatrix();
        MekanismRenderer.disableGlow(glowInfo);
    }

    private void setRotation(ModelRenderer model, float x, float y, float z) {
        model.rotateAngleX = x;
        model.rotateAngleY = y;
        model.rotateAngleZ = z;
    }
}