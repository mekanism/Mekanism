package mekanism.generators.client.model;

import com.mojang.blaze3d.platform.GlStateManager;
import net.minecraft.client.renderer.entity.model.RendererModel;
import net.minecraft.client.renderer.model.Model;

public class ModelTurbine extends Model {

    private static float BLADE_ROTATE = 0.418879F;

    public RendererModel rod;
    public RendererModel extension_north;
    public RendererModel blade_north;
    public RendererModel extension_south;
    public RendererModel extension_west;
    public RendererModel extension_east;
    public RendererModel blade_south;
    public RendererModel blade_east;
    public RendererModel blade_west;

    public ModelTurbine() {
        textureWidth = 64;
        textureHeight = 64;
        extension_south = new RendererModel(this, 0, 0);
        extension_south.setRotationPoint(0.0F, 20.0F, 0.0F);
        extension_south.addBox(-1.0F, 0.0F, 1.0F, 2, 1, 3, 0.0F);
        setRotateAngle(extension_south, 0.0F, 0.0F, -BLADE_ROTATE);
        extension_west = new RendererModel(this, 0, 4);
        extension_west.setRotationPoint(0.0F, 20.0F, 0.0F);
        extension_west.addBox(-4.0F, 0.0F, -1.0F, 3, 1, 2, 0.0F);
        setRotateAngle(extension_west, BLADE_ROTATE, 0.0F, 0.0F);
        blade_east = new RendererModel(this, 10, 5);
        blade_east.setRotationPoint(0.0F, 20.0F, 0.0F);
        blade_east.addBox(4.0F, 0.0F, -1.5F, 4, 1, 3, 0.0F);
        setRotateAngle(blade_east, -BLADE_ROTATE, 0.0F, 0.0F);
        blade_north = new RendererModel(this, 10, 0);
        blade_north.setRotationPoint(0.0F, 20.0F, 0.0F);
        blade_north.addBox(-1.5F, 0.0F, -8.0F, 3, 1, 4, 0.0F);
        setRotateAngle(blade_north, 0.0F, 0.0F, BLADE_ROTATE);
        extension_east = new RendererModel(this, 0, 4);
        extension_east.setRotationPoint(0.0F, 20.0F, 0.0F);
        extension_east.addBox(1.0F, 0.0F, -1.0F, 3, 1, 2, 0.0F);
        setRotateAngle(extension_east, -BLADE_ROTATE, 0.0F, 0.0F);
        rod = new RendererModel(this, 0, 44);
        rod.setRotationPoint(-2.0F, 8.0F, -2.0F);
        rod.addBox(0.0F, 0.0F, 0.0F, 4, 16, 4, 0.0F);
        blade_south = new RendererModel(this, 10, 0);
        blade_south.setRotationPoint(0.0F, 20.0F, 0.0F);
        blade_south.addBox(-1.5F, 0.0F, 4.0F, 3, 1, 4, 0.0F);
        setRotateAngle(blade_south, 0.0F, 0.0F, -BLADE_ROTATE);
        extension_north = new RendererModel(this, 0, 0);
        extension_north.setRotationPoint(0.0F, 20.0F, 0.0F);
        extension_north.addBox(-1.0F, 0.0F, -4.0F, 2, 1, 3, 0.0F);
        setRotateAngle(extension_north, 0.0F, 0.0F, BLADE_ROTATE);
        blade_west = new RendererModel(this, 10, 5);
        blade_west.setRotationPoint(0.0F, 20.0F, 0.0F);
        blade_west.addBox(-8.0F, 0.0F, -1.5F, 4, 1, 3, 0.0F);
        setRotateAngle(blade_west, BLADE_ROTATE, 0.0F, 0.0F);
    }

    public void render(float size, int index) {
        GlStateManager.pushMatrix();
        GlStateManager.rotatef(index * 5, 0, 1, 0);
        extension_south.render(size);
        extension_west.render(size);
        extension_east.render(size);
        extension_north.render(size);
        float scale = index * 0.5F;
        float widthDiv = 16;
        renderBlade(blade_west, size, scale, scale / widthDiv, -0.25F, 0.0F);
        renderBlade(blade_east, size, scale, scale / widthDiv, 0.25F, 0.0F);
        renderBlade(blade_north, size, scale / widthDiv, scale, 0.0F, -0.25F);
        renderBlade(blade_south, size, scale / widthDiv, scale, 0.0F, 0.25F);
        GlStateManager.popMatrix();
    }

    private void renderBlade(RendererModel blade, float size, float scaleX, float scaleZ, float transX, float transZ) {
        GlStateManager.pushMatrix();
        GlStateManager.translatef(transX, 0, transZ);
        GlStateManager.scalef(1.0F + scaleX, 1.0F, 1.0F + scaleZ);
        GlStateManager.translatef(-transX, 0, -transZ);
        blade.render(size);
        GlStateManager.popMatrix();
    }

    public void setRotateAngle(RendererModel RendererModel, float x, float y, float z) {
        RendererModel.rotateAngleX = x;
        RendererModel.rotateAngleY = y;
        RendererModel.rotateAngleZ = z;
    }
}