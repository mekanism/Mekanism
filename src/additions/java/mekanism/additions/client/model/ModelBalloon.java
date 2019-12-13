package mekanism.additions.client.model;

import com.mojang.blaze3d.platform.GlStateManager;
import mekanism.api.text.EnumColor;
import mekanism.client.render.MekanismRenderer;
import net.minecraft.client.renderer.entity.model.RendererModel;
import net.minecraft.client.renderer.model.Model;

public class ModelBalloon extends Model {

    private final RendererModel Balloon2;
    private final RendererModel Balloon1;
    private final RendererModel Balloon3;
    private final RendererModel Balloonnub;
    private final RendererModel String;

    public ModelBalloon() {
        textureWidth = 64;
        textureHeight = 32;

        Balloon2 = new RendererModel(this, 0, 0);
        Balloon2.addBox(-2.5F, -2F, -2F, 5, 4, 4);
        Balloon2.setRotationPoint(0F, 0F, 0F);
        Balloon2.setTextureSize(64, 32);
        Balloon2.mirror = true;
        setRotation(Balloon2, 0F, 0F, 0F);
        Balloon1 = new RendererModel(this, 0, 8);
        Balloon1.addBox(-2F, -2F, -2.5F, 4, 4, 5);
        Balloon1.setRotationPoint(0F, 0F, 0F);
        Balloon1.setTextureSize(64, 32);
        Balloon1.mirror = true;
        setRotation(Balloon1, 0F, 0F, 0F);
        Balloon3 = new RendererModel(this, 18, 0);
        Balloon3.addBox(-2F, -2.5F, -2F, 4, 5, 4);
        Balloon3.setRotationPoint(0F, 0F, 0F);
        Balloon3.setTextureSize(64, 32);
        Balloon3.mirror = true;
        setRotation(Balloon3, 0F, 0F, 0F);
        Balloonnub = new RendererModel(this, 18, 9);
        Balloonnub.addBox(-0.5F, 2.5F, -0.5F, 1, 1, 1);
        Balloonnub.setRotationPoint(0F, 0F, 0F);
        Balloonnub.setTextureSize(64, 32);
        Balloonnub.mirror = true;
        setRotation(Balloonnub, 0F, 0F, 0F);
        String = new RendererModel(this, 34, 0);
        String.addBox(-0.5F, 3.5F, -0.5F, 1, 11, 1);
        String.setRotationPoint(0F, 0F, 0F);
        String.setTextureSize(64, 32);
        String.mirror = true;
        setRotation(String, 0F, 0F, 0F);
    }

    public void render(float size, EnumColor color) {
        GlStateManager.pushMatrix();
        MekanismRenderer.color(color);
        GlStateManager.scalef(1.5F, 1.5F, 1.5F);
        GlStateManager.translatef(0, -0.07F, 0);

        Balloon2.render(size);
        Balloon1.render(size);
        Balloon3.render(size);
        Balloonnub.render(size);

        MekanismRenderer.resetColor();
        GlStateManager.popMatrix();

        GlStateManager.pushMatrix();
        GlStateManager.scalef(0.2F, 1, 0.2F);
        String.render(size);
        GlStateManager.popMatrix();
    }

    private void setRotation(RendererModel model, float x, float y, float z) {
        model.rotateAngleX = x;
        model.rotateAngleY = y;
        model.rotateAngleZ = z;
    }
}