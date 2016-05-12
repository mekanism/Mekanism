package mekanism.generators.client.model;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.client.renderer.GlStateManager;

public class ModelTurbine extends ModelBase 
{
	private static float BLADE_ROTATE = 0.418879F;
	
    public ModelRenderer rod;
    public ModelRenderer extension_north;
    public ModelRenderer blade_north;
    public ModelRenderer extension_south;
    public ModelRenderer extension_west;
    public ModelRenderer extension_east;
    public ModelRenderer blade_south;
    public ModelRenderer blade_east;
    public ModelRenderer blade_west;

    public ModelTurbine() 
    {
        textureWidth = 64;
        textureHeight = 64;
        extension_south = new ModelRenderer(this, 0, 0);
        extension_south.setRotationPoint(0.0F, 20.0F, 0.0F);
        extension_south.addBox(-1.0F, 0.0F, 1.0F, 2, 1, 3, 0.0F);
        setRotateAngle(extension_south, 0.0F, 0.0F, -BLADE_ROTATE);
        extension_west = new ModelRenderer(this, 0, 4);
        extension_west.setRotationPoint(0.0F, 20.0F, 0.0F);
        extension_west.addBox(-4.0F, 0.0F, -1.0F, 3, 1, 2, 0.0F);
        setRotateAngle(extension_west, BLADE_ROTATE, 0.0F, 0.0F);
        blade_east = new ModelRenderer(this, 10, 5);
        blade_east.setRotationPoint(0.0F, 20.0F, 0.0F);
        blade_east.addBox(4.0F, 0.0F, -1.5F, 4, 1, 3, 0.0F);
        setRotateAngle(blade_east, -BLADE_ROTATE, 0.0F, 0.0F);
        blade_north = new ModelRenderer(this, 10, 0);
        blade_north.setRotationPoint(0.0F, 20.0F, 0.0F);
        blade_north.addBox(-1.5F, 0.0F, -8.0F, 3, 1, 4, 0.0F);
        setRotateAngle(blade_north, 0.0F, 0.0F, BLADE_ROTATE);
        extension_east = new ModelRenderer(this, 0, 4);
        extension_east.setRotationPoint(0.0F, 20.0F, 0.0F);
        extension_east.addBox(1.0F, 0.0F, -1.0F, 3, 1, 2, 0.0F);
        setRotateAngle(extension_east, -BLADE_ROTATE, 0.0F, 0.0F);
        rod = new ModelRenderer(this, 0, 44);
        rod.setRotationPoint(-2.0F, 8.0F, -2.0F);
        rod.addBox(0.0F, 0.0F, 0.0F, 4, 16, 4, 0.0F);
        blade_south = new ModelRenderer(this, 10, 0);
        blade_south.setRotationPoint(0.0F, 20.0F, 0.0F);
        blade_south.addBox(-1.5F, 0.0F, 4.0F, 3, 1, 4, 0.0F);
        setRotateAngle(blade_south, 0.0F, 0.0F, -BLADE_ROTATE);
        extension_north = new ModelRenderer(this, 0, 0);
        extension_north.setRotationPoint(0.0F, 20.0F, 0.0F);
        extension_north.addBox(-1.0F, 0.0F, -4.0F, 2, 1, 3, 0.0F);
        setRotateAngle(extension_north, 0.0F, 0.0F, BLADE_ROTATE);
        blade_west = new ModelRenderer(this, 10, 5);
        blade_west.setRotationPoint(0.0F, 20.0F, 0.0F);
        blade_west.addBox(-8.0F, 0.0F, -1.5F, 4, 1, 3, 0.0F);
        setRotateAngle(blade_west, BLADE_ROTATE, 0.0F, 0.0F);
    }

    public void render(float size, int index) 
    {
    	GlStateManager.pushMatrix();
    	
    	GlStateManager.rotate(index*5, 0.0F, 1.0F, 0.0F);
    	
    	float scale = index*0.5F;
    	float widthDiv = 16;
    	
        extension_south.render(size);
        extension_west.render(size);
        extension_east.render(size);
        extension_north.render(size);
        
        GlStateManager.pushMatrix();
        GlStateManager.translate(-0.25F, 0.0F, 0.0F);
        GlStateManager.scale(1.0F + scale, 1.0F, 1.0F + scale/widthDiv);
        GlStateManager.translate(0.25F, 0.0F, 0.0F);
        blade_west.render(size);
        GlStateManager.popMatrix();
        
        GlStateManager.pushMatrix();
        GlStateManager.translate(0.25F, 0.0F, 0.0F);
        GlStateManager.scale(1.0F + scale, 1.0F, 1.0F + scale/widthDiv);
        GlStateManager.translate(-0.25F, 0.0F, 0.0F);
        blade_east.render(size);
        GlStateManager.popMatrix();
        
        GlStateManager.pushMatrix();
        GlStateManager.translate(0.0F, 0.0F, -0.25F);
        GlStateManager.scale(1.0F + scale/widthDiv, 1.0F, 1.0F + scale);
        GlStateManager.translate(0.0F, 0.0F, 0.25F);
        blade_north.render(size);
        GlStateManager.popMatrix();
        
        GlStateManager.pushMatrix();
        GlStateManager.translate(0.0F, 0.0F, 0.25F);
        GlStateManager.scale(1.0F + scale/widthDiv, 1.0F, 1.0F + scale);
        GlStateManager.translate(0.0F, 0.0F, -0.25F);
        blade_south.render(size);
        GlStateManager.popMatrix();
        
        GlStateManager.popMatrix();
    }

    public void setRotateAngle(ModelRenderer modelRenderer, float x, float y, float z) 
    {
        modelRenderer.rotateAngleX = x;
        modelRenderer.rotateAngleY = y;
        modelRenderer.rotateAngleZ = z;
    }
}
