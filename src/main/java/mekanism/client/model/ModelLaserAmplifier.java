package mekanism.client.model;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class ModelLaserAmplifier extends ModelBase 
{
	ModelRenderer S1;
	ModelRenderer S2;
	ModelRenderer S3;
	ModelRenderer S4;
	ModelRenderer S5;
	ModelRenderer Base;
	ModelRenderer S6;

	public ModelLaserAmplifier() 
	{
		textureWidth = 64;
		textureHeight = 64;

		S1 = new ModelRenderer(this, 0, 39);
		S1.addBox(0F, 0F, 0F, 1, 10, 10);
		S1.setRotationPoint(7F, 11F, -5F);
		S1.setTextureSize(64, 64);
		S1.mirror = true;
		setRotation(S1, 0F, 0F, 0F);
		S2 = new ModelRenderer(this, 22, 39);
		S2.addBox(0F, 0F, 0F, 10, 10, 1);
		S2.setRotationPoint(-5F, 11F, 7F);
		S2.setTextureSize(64, 64);
		S2.mirror = true;
		setRotation(S2, 0F, 0F, 0F);
		S3 = new ModelRenderer(this, 0, 39);
		S3.addBox(0F, 0F, 0F, 1, 10, 10);
		S3.setRotationPoint(-8F, 11F, -5F);
		S3.setTextureSize(64, 64);
		S3.mirror = true;
		setRotation(S3, 0F, 0F, 0F);
		S4 = new ModelRenderer(this, 0, 28);
		S4.addBox(0F, 0F, 0F, 10, 1, 10);
		S4.setRotationPoint(-5F, 23F, -5F);
		S4.setTextureSize(64, 64);
		S4.mirror = true;
		setRotation(S4, 0F, 0F, 0F);
		S5 = new ModelRenderer(this, 22, 39);
		S5.addBox(0F, 0F, 0F, 10, 10, 1);
		S5.setRotationPoint(-5F, 11F, -8F);
		S5.setTextureSize(64, 64);
		S5.mirror = true;
		setRotation(S5, 0F, 0F, 0F);
		Base = new ModelRenderer(this, 0, 0);
		Base.addBox(0F, 0F, 0F, 14, 14, 14);
		Base.setRotationPoint(-7F, 9F, -7F);
		Base.setTextureSize(64, 64);
		Base.mirror = true;
		setRotation(Base, 0F, 0F, 0F);
		S6 = new ModelRenderer(this, 0, 28);
		S6.addBox(0F, 0F, 0F, 10, 1, 10);
		S6.setRotationPoint(-5F, 8F, -5F);
		S6.setTextureSize(64, 64);
		S6.mirror = true;
		setRotation(S6, 0F, 0F, 0F);
	}

	public void render(float size)
	{
		S1.render(size);
		S2.render(size);
		S3.render(size);
		S4.render(size);
		S5.render(size);
		Base.render(size);
		S6.render(size);
	}

	private void setRotation(ModelRenderer model, float x, float y, float z) 
	{
		model.rotateAngleX = x;
		model.rotateAngleY = y;
		model.rotateAngleZ = z;
	}
}
