package mekanism.client.model;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.platform.GlStateManager.DestFactor;
import com.mojang.blaze3d.platform.GlStateManager.SourceFactor;
import mekanism.client.render.MekanismRenderer;
import mekanism.common.tier.FluidTankTier;
import net.minecraft.client.renderer.entity.model.RendererModel;
import net.minecraft.client.renderer.model.Model;
import org.lwjgl.opengl.GL11;

//TODO: Replace usage of this by using the json model and drawing fluid inside of it?
public class ModelFluidTank extends Model {

    private final RendererModel Base;
    private final RendererModel PoleFL;
    private final RendererModel PoleLB;
    private final RendererModel PoleBR;
    private final RendererModel PoleRF;
    private final RendererModel Top;
    private final RendererModel FrontGlass;
    private final RendererModel BackGlass;
    private final RendererModel RightGlass;
    private final RendererModel LeftGlass;

    public ModelFluidTank() {
        textureWidth = 128;
        textureHeight = 128;

        Base = new RendererModel(this, 0, 0);
        Base.addBox(0F, 0F, 0F, 12, 1, 12);
        Base.setRotationPoint(-6F, 23F, -6F);
        Base.setTextureSize(128, 128);
        Base.mirror = true;
        setRotation(Base, 0F, 0F, 0F);
        PoleFL = new RendererModel(this, 48, 0);
        PoleFL.addBox(0F, 0F, 0F, 1, 14, 1);
        PoleFL.setRotationPoint(5F, 9F, -6F);
        PoleFL.setTextureSize(128, 128);
        PoleFL.mirror = true;
        setRotation(PoleFL, 0F, 0F, 0F);
        PoleLB = new RendererModel(this, 48, 0);
        PoleLB.addBox(0F, 0F, 0F, 1, 14, 1);
        PoleLB.setRotationPoint(5F, 9F, 5F);
        PoleLB.setTextureSize(128, 128);
        PoleLB.mirror = true;
        setRotation(PoleLB, 0F, 0F, 0F);
        PoleBR = new RendererModel(this, 48, 0);
        PoleBR.addBox(0F, 0F, 0F, 1, 14, 1);
        PoleBR.setRotationPoint(-6F, 9F, 5F);
        PoleBR.setTextureSize(128, 128);
        PoleBR.mirror = true;
        setRotation(PoleBR, 0F, 0F, 0F);
        PoleRF = new RendererModel(this, 48, 0);
        PoleRF.addBox(0F, 0F, 0F, 1, 14, 1);
        PoleRF.setRotationPoint(-6F, 9F, -6F);
        PoleRF.setTextureSize(128, 128);
        PoleRF.mirror = true;
        setRotation(PoleRF, 0F, 0F, 0F);
        Top = new RendererModel(this, 0, 0);
        Top.addBox(0F, 0F, 0F, 12, 1, 12);
        Top.setRotationPoint(-6F, 8F, -6F);
        Top.setTextureSize(128, 128);
        Top.mirror = true;
        setRotation(Top, 0F, 0F, 0F);
        FrontGlass = new RendererModel(this, 0, 13);
        FrontGlass.addBox(0F, 0F, 0F, 10, 14, 1);
        FrontGlass.setRotationPoint(-5F, 9F, -6F);
        FrontGlass.setTextureSize(128, 128);
        FrontGlass.mirror = true;
        setRotation(FrontGlass, 0F, 0F, 0F);
        BackGlass = new RendererModel(this, 0, 28);
        BackGlass.addBox(0F, 0F, 3F, 10, 14, 1);
        BackGlass.setRotationPoint(-5F, 9F, 2F);
        BackGlass.setTextureSize(128, 128);
        BackGlass.mirror = true;
        setRotation(BackGlass, 0F, 0F, 0F);
        RightGlass = new RendererModel(this, 22, 13);
        RightGlass.addBox(0F, 0F, 0F, 1, 14, 10);
        RightGlass.setRotationPoint(-6F, 9F, -5F);
        RightGlass.setTextureSize(128, 128);
        RightGlass.mirror = true;
        setRotation(RightGlass, 0F, 0F, 0F);
        LeftGlass = new RendererModel(this, 22, 37);
        LeftGlass.addBox(0F, 0F, 0F, 1, 14, 10);
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

    private void setRotation(RendererModel model, float x, float y, float z) {
        model.rotateAngleX = x;
        model.rotateAngleY = y;
        model.rotateAngleZ = z;
    }
}