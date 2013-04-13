package mekanism.generators.client;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.entity.Entity;

@SideOnly(Side.CLIENT)
public class ModelAdvancedSolarGenerator extends ModelBase
{
	ModelRenderer BASE;
	ModelRenderer SUPPORT_1_ROTATES;
	ModelRenderer MECHANISM_ROTATES;
	ModelRenderer PANEL_1_ROTATES;
	ModelRenderer PANEL_2_ROTATES;
	ModelRenderer PANEL_2_SUPPORT_1_ROTATES;
	ModelRenderer PANEL_2_SUPPORT_2_ROTATES;
	ModelRenderer PANEL_2_SUPPORT_3_ROTATES;
	ModelRenderer PANEL_2_SUPPORT_4_ROTATES;
	ModelRenderer PANEL_1_SUPPORT_1_ROTATES;
	ModelRenderer PANEL_1_SUPPORT_2_ROTATES;
	ModelRenderer PANEL_1_SUPPORT_3_ROTATES;
	ModelRenderer PANEL_1_SUPPORT_4_ROTATES;
	ModelRenderer SUPPORT_2_ROTATES;
	ModelRenderer SUPPORT_3_ROTATES;
	  
	public ModelAdvancedSolarGenerator()
    {
		textureWidth = 256;
		textureHeight = 256;
	    
		BASE = new ModelRenderer(this, 0, 54);
		BASE.addBox(0F, 0F, 0F, 8, 6, 8);
		BASE.setRotationPoint(-4F, 18F, -4F);
		BASE.setTextureSize(256, 256);
		BASE.mirror = true;
		setRotation(BASE, 0F, 0F, 0F);
		SUPPORT_1_ROTATES = new ModelRenderer(this, 0, 72);
		SUPPORT_1_ROTATES.addBox(-2F, 0F, -2F, 4, 42, 4);
		SUPPORT_1_ROTATES.setRotationPoint(0F, -22F, 0F);
		SUPPORT_1_ROTATES.setTextureSize(256, 256);
		SUPPORT_1_ROTATES.mirror = true;
		setRotation(SUPPORT_1_ROTATES, 0F, 0F, 0F);
		MECHANISM_ROTATES = new ModelRenderer(this, 0, 121);
		MECHANISM_ROTATES.addBox(-3F, -3F, -3F, 6, 6, 6);
		MECHANISM_ROTATES.setRotationPoint(0F, -21F, 0F);
		MECHANISM_ROTATES.setTextureSize(256, 256);
		MECHANISM_ROTATES.mirror = true;
		setRotation(MECHANISM_ROTATES, 0.7853982F, 0F, 0F);
		PANEL_1_ROTATES = new ModelRenderer(this, 0, 0);
		PANEL_1_ROTATES.addBox(3F, -1F, -24F, 19, 2, 48);
		PANEL_1_ROTATES.setRotationPoint(0F, -21F, 0F);
		PANEL_1_ROTATES.setTextureSize(256, 256);
		PANEL_1_ROTATES.mirror = true;
		setRotation(PANEL_1_ROTATES, 0F, 0F, 0F);
		PANEL_2_ROTATES = new ModelRenderer(this, 0, 0);
		PANEL_2_ROTATES.addBox(-22F, -1F, -24F, 19, 2, 48);
		PANEL_2_ROTATES.setRotationPoint(0F, -21F, 0F);
		PANEL_2_ROTATES.setTextureSize(256, 256);
		PANEL_2_ROTATES.mirror = true;
		setRotation(PANEL_2_ROTATES, 0F, 0F, 0F);
		PANEL_2_SUPPORT_1_ROTATES = new ModelRenderer(this, 43, 53);
		PANEL_2_SUPPORT_1_ROTATES.addBox(-24F, -2F, -25F, 21, 4, 4);
		PANEL_2_SUPPORT_1_ROTATES.setRotationPoint(0F, -21F, 0F);
		PANEL_2_SUPPORT_1_ROTATES.setTextureSize(256, 256);
		PANEL_2_SUPPORT_1_ROTATES.mirror = true;
		setRotation(PANEL_2_SUPPORT_1_ROTATES, 0F, 0F, 0F);
		PANEL_2_SUPPORT_2_ROTATES = new ModelRenderer(this, 43, 53);
		PANEL_2_SUPPORT_2_ROTATES.addBox(-24F, -2F, 22F, 21, 4, 4);
		PANEL_2_SUPPORT_2_ROTATES.setRotationPoint(0F, -21F, 0F);
		PANEL_2_SUPPORT_2_ROTATES.setTextureSize(256, 256);
		PANEL_2_SUPPORT_2_ROTATES.mirror = true;
		setRotation(PANEL_2_SUPPORT_2_ROTATES, 0F, 0F, 0F);
		PANEL_2_SUPPORT_3_ROTATES = new ModelRenderer(this, 43, 53);
		PANEL_2_SUPPORT_3_ROTATES.addBox(-24F, -2F, -2F, 21, 4, 4);
		PANEL_2_SUPPORT_3_ROTATES.setRotationPoint(0F, -21F, 0F);
		PANEL_2_SUPPORT_3_ROTATES.setTextureSize(256, 256);
		PANEL_2_SUPPORT_3_ROTATES.mirror = true;
		setRotation(PANEL_2_SUPPORT_3_ROTATES, 0F, 0F, 0F);
		PANEL_2_SUPPORT_4_ROTATES = new ModelRenderer(this, 43, 63);
		PANEL_2_SUPPORT_4_ROTATES.addBox(-24F, -2F, -21F, 4, 4, 43);
		PANEL_2_SUPPORT_4_ROTATES.setRotationPoint(0F, -21F, 0F);
		PANEL_2_SUPPORT_4_ROTATES.setTextureSize(256, 256);
		PANEL_2_SUPPORT_4_ROTATES.mirror = true;
		setRotation(PANEL_2_SUPPORT_4_ROTATES, 0F, 0F, 0F);
		PANEL_1_SUPPORT_1_ROTATES = new ModelRenderer(this, 43, 53);
		PANEL_1_SUPPORT_1_ROTATES.addBox(3F, -2F, -25F, 21, 4, 4);
		PANEL_1_SUPPORT_1_ROTATES.setRotationPoint(0F, -21F, 0F);
		PANEL_1_SUPPORT_1_ROTATES.setTextureSize(256, 256);
		PANEL_1_SUPPORT_1_ROTATES.mirror = true;
		setRotation(PANEL_1_SUPPORT_1_ROTATES, 0F, 0F, 0F);
		PANEL_1_SUPPORT_2_ROTATES = new ModelRenderer(this, 43, 63);
		PANEL_1_SUPPORT_2_ROTATES.addBox(20F, -2F, -21F, 4, 4, 43);
		PANEL_1_SUPPORT_2_ROTATES.setRotationPoint(0F, -21F, 0F);
		PANEL_1_SUPPORT_2_ROTATES.setTextureSize(256, 256);
		PANEL_1_SUPPORT_2_ROTATES.mirror = true;
		setRotation(PANEL_1_SUPPORT_2_ROTATES, 0F, 0F, 0F);
		PANEL_1_SUPPORT_3_ROTATES = new ModelRenderer(this, 43, 53);
		PANEL_1_SUPPORT_3_ROTATES.addBox(3F, -2F, -2F, 21, 4, 4);
		PANEL_1_SUPPORT_3_ROTATES.setRotationPoint(0F, -21F, 0F);
		PANEL_1_SUPPORT_3_ROTATES.setTextureSize(256, 256);
		PANEL_1_SUPPORT_3_ROTATES.mirror = true;
		setRotation(PANEL_1_SUPPORT_3_ROTATES, 0F, 0F, 0F);
		PANEL_1_SUPPORT_4_ROTATES = new ModelRenderer(this, 43, 53);
		PANEL_1_SUPPORT_4_ROTATES.addBox(3F, -2F, 22F, 21, 4, 4);
		PANEL_1_SUPPORT_4_ROTATES.setRotationPoint(0F, -21F, 0F);
		PANEL_1_SUPPORT_4_ROTATES.setTextureSize(256, 256);
		PANEL_1_SUPPORT_4_ROTATES.mirror = true;
		setRotation(PANEL_1_SUPPORT_4_ROTATES, 0F, 0F, 0F);
		SUPPORT_2_ROTATES = new ModelRenderer(this, 20, 72);
		SUPPORT_2_ROTATES.addBox(2F, -19F, -1F, 1, 37, 2);
		SUPPORT_2_ROTATES.setRotationPoint(0F, 0F, 0F);
		SUPPORT_2_ROTATES.setTextureSize(256, 256);
		SUPPORT_2_ROTATES.mirror = true;
		setRotation(SUPPORT_2_ROTATES, 0F, 0F, 0F);
		SUPPORT_3_ROTATES = new ModelRenderer(this, 30, 72);
		SUPPORT_3_ROTATES.addBox(-3F, -19F, -1F, 1, 37, 2);
		SUPPORT_3_ROTATES.setRotationPoint(0F, 0F, 0F);
		SUPPORT_3_ROTATES.setTextureSize(256, 256);
		SUPPORT_3_ROTATES.mirror = true;
		setRotation(SUPPORT_3_ROTATES, 0F, 0F, 0F);
    }
	  
	public void render(float rotation, float size)
	{
		BASE.render(size);
		SUPPORT_1_ROTATES.render(size);
		MECHANISM_ROTATES.render(size);
		MECHANISM_ROTATES.rotateAngleX = rotation;
		PANEL_1_ROTATES.render(size);
		PANEL_1_ROTATES.rotateAngleX = rotation;
		PANEL_2_ROTATES.render(size);
		PANEL_2_ROTATES.rotateAngleX = rotation;
		PANEL_2_SUPPORT_1_ROTATES.render(size);
		PANEL_2_SUPPORT_1_ROTATES.rotateAngleX = rotation;
		PANEL_2_SUPPORT_2_ROTATES.render(size);
		PANEL_2_SUPPORT_2_ROTATES.rotateAngleX = rotation;
		PANEL_2_SUPPORT_3_ROTATES.render(size);
		PANEL_2_SUPPORT_3_ROTATES.rotateAngleX = rotation;
		PANEL_2_SUPPORT_4_ROTATES.render(size);
		PANEL_2_SUPPORT_4_ROTATES.rotateAngleX = rotation;
		PANEL_1_SUPPORT_1_ROTATES.render(size);
		PANEL_1_SUPPORT_1_ROTATES.rotateAngleX = rotation;
		PANEL_1_SUPPORT_2_ROTATES.render(size);
		PANEL_1_SUPPORT_2_ROTATES.rotateAngleX = rotation;
		PANEL_1_SUPPORT_3_ROTATES.render(size);
		PANEL_1_SUPPORT_3_ROTATES.rotateAngleX = rotation;
		PANEL_1_SUPPORT_4_ROTATES.render(size);
		PANEL_1_SUPPORT_4_ROTATES.rotateAngleX = rotation;
		SUPPORT_2_ROTATES.render(size);
		SUPPORT_3_ROTATES.render(size);
	}
  
	private void setRotation(ModelRenderer model, float x, float y, float z)
	{
		model.rotateAngleX = x;
		model.rotateAngleY = y;
		model.rotateAngleZ = z;
	}
}
