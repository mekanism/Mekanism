package mekanism.client.model;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.platform.GlStateManager.DestFactor;
import com.mojang.blaze3d.platform.GlStateManager.SourceFactor;
import mekanism.client.render.MekanismRenderer;
import mekanism.common.tier.FluidTankTier;
import net.minecraft.client.renderer.model.ModelRenderer;
import net.minecraft.client.renderer.model.Model;
import org.lwjgl.opengl.GL11;

//TODO: Replace usage of this by using the json model and drawing fluid inside of it?
public class ModelFluidTank extends Model {

    private final ModelRenderer Base;
    private final ModelRenderer PoleFL;
    private final ModelRenderer PoleLB;
    private final ModelRenderer PoleBR;
    private final ModelRenderer PoleRF;
    private final ModelRenderer Top;
    private final ModelRenderer FrontGlass;
    private final ModelRenderer BackGlass;
    private final ModelRenderer RightGlass;
    private final ModelRenderer LeftGlass;

    public ModelFluidTank() {
        textureWidth = 128;
        textureHeight = 128;

        Base = new ModelRenderer(this, 0, 0);
        Base.func_228304_a_(0F, 0F, 0F, 12, 1, 12, false);
        Base.setRotationPoint(-6F, 23F, -6F);
        Base.setTextureSize(128, 128);
        Base.mirror = true;
        setRotation(Base, 0F, 0F, 0F);
        PoleFL = new ModelRenderer(this, 48, 0);
        PoleFL.func_228304_a_(0F, 0F, 0F, 1, 14, 1, false);
        PoleFL.setRotationPoint(5F, 9F, -6F);
        PoleFL.setTextureSize(128, 128);
        PoleFL.mirror = true;
        setRotation(PoleFL, 0F, 0F, 0F);
        PoleLB = new ModelRenderer(this, 48, 0);
        PoleLB.func_228304_a_(0F, 0F, 0F, 1, 14, 1, false);
        PoleLB.setRotationPoint(5F, 9F, 5F);
        PoleLB.setTextureSize(128, 128);
        PoleLB.mirror = true;
        setRotation(PoleLB, 0F, 0F, 0F);
        PoleBR = new ModelRenderer(this, 48, 0);
        PoleBR.func_228304_a_(0F, 0F, 0F, 1, 14, 1, false);
        PoleBR.setRotationPoint(-6F, 9F, 5F);
        PoleBR.setTextureSize(128, 128);
        PoleBR.mirror = true;
        setRotation(PoleBR, 0F, 0F, 0F);
        PoleRF = new ModelRenderer(this, 48, 0);
        PoleRF.func_228304_a_(0F, 0F, 0F, 1, 14, 1, false);
        PoleRF.setRotationPoint(-6F, 9F, -6F);
        PoleRF.setTextureSize(128, 128);
        PoleRF.mirror = true;
        setRotation(PoleRF, 0F, 0F, 0F);
        Top = new ModelRenderer(this, 0, 0);
        Top.func_228304_a_(0F, 0F, 0F, 12, 1, 12, false);
        Top.setRotationPoint(-6F, 8F, -6F);
        Top.setTextureSize(128, 128);
        Top.mirror = true;
        setRotation(Top, 0F, 0F, 0F);
        FrontGlass = new ModelRenderer(this, 0, 13);
        FrontGlass.func_228304_a_(0F, 0F, 0F, 10, 14, 1, false);
        FrontGlass.setRotationPoint(-5F, 9F, -6F);
        FrontGlass.setTextureSize(128, 128);
        FrontGlass.mirror = true;
        setRotation(FrontGlass, 0F, 0F, 0F);
        BackGlass = new ModelRenderer(this, 0, 28);
        BackGlass.func_228304_a_(0F, 0F, 3F, 10, 14, 1, false);
        BackGlass.setRotationPoint(-5F, 9F, 2F);
        BackGlass.setTextureSize(128, 128);
        BackGlass.mirror = true;
        setRotation(BackGlass, 0F, 0F, 0F);
        RightGlass = new ModelRenderer(this, 22, 13);
        RightGlass.func_228304_a_(0F, 0F, 0F, 1, 14, 10, false);
        RightGlass.setRotationPoint(-6F, 9F, -5F);
        RightGlass.setTextureSize(128, 128);
        RightGlass.mirror = true;
        setRotation(RightGlass, 0F, 0F, 0F);
        LeftGlass = new ModelRenderer(this, 22, 37);
        LeftGlass.func_228304_a_(0F, 0F, 0F, 1, 14, 10, false);
        LeftGlass.setRotationPoint(5F, 9F, -5F);
        LeftGlass.setTextureSize(128, 128);
        LeftGlass.mirror = true;
        setRotation(LeftGlass, 0F, 0F, 0F);
    }

    public void render(float size, FluidTankTier tier) {
        Base.render(size);
        PoleFL.render(size);
        PoleLB.render(size);
        PoleBR.render(size);
        PoleRF.render(size);
        Top.render(size);

        GlStateManager.shadeModel(GL11.GL_SMOOTH);
        GlStateManager.disableAlphaTest();
        GlStateManager.enableBlend();
        GlStateManager.blendFunc(SourceFactor.SRC_ALPHA, DestFactor.ONE_MINUS_SRC_ALPHA);
        MekanismRenderer.color(tier.getBaseTier());
        FrontGlass.render(size);
        BackGlass.render(size);
        RightGlass.render(size);
        LeftGlass.render(size);
        MekanismRenderer.resetColor();
        GlStateManager.disableBlend();
        GlStateManager.enableAlphaTest();
    }

    private void setRotation(ModelRenderer model, float x, float y, float z) {
        model.rotateAngleX = x;
        model.rotateAngleY = y;
        model.rotateAngleZ = z;
    }
}