package mekanism.client.model;

import mekanism.client.render.MekanismRenderer;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class ModelPortableTank extends ModelBase
{
	ModelRenderer Base;
	ModelRenderer PoleFL;
	ModelRenderer PoleLB;
	ModelRenderer PoleBR;
	ModelRenderer PoleRF;
	ModelRenderer Top;
	ModelRenderer FrontGlass;
	ModelRenderer BackGlass;
	ModelRenderer RightGlass;
	ModelRenderer LeftGlass;

	public ModelPortableTank() 
	{
		textureWidth = 128;
		textureHeight = 128;

		Base = new ModelRenderer(this, 0, 0);
		Base.addBox(0F, 0F, 0F, 12, 1, 12);
		Base.setRotationPoint(-6F, 23F, -6F);
		Base.setTextureSize(128, 128);
		Base.mirror = true;
		setRotation(Base, 0F, 0F, 0F);
		PoleFL = new ModelRenderer(this, 48, 0);
		PoleFL.addBox(0F, 0F, 0F, 1, 14, 1);
		PoleFL.setRotationPoint(5F, 9F, -6F);
		PoleFL.setTextureSize(128, 128);
		PoleFL.mirror = true;
		setRotation(PoleFL, 0F, 0F, 0F);
		PoleLB = new ModelRenderer(this, 48, 0);
		PoleLB.addBox(0F, 0F, 0F, 1, 14, 1);
		PoleLB.setRotationPoint(5F, 9F, 5F);
		PoleLB.setTextureSize(128, 128);
		PoleLB.mirror = true;
		setRotation(PoleLB, 0F, 0F, 0F);
		PoleBR = new ModelRenderer(this, 48, 0);
		PoleBR.addBox(0F, 0F, 0F, 1, 14, 1);
		PoleBR.setRotationPoint(-6F, 9F, 5F);
		PoleBR.setTextureSize(128, 128);
		PoleBR.mirror = true;
		setRotation(PoleBR, 0F, 0F, 0F);
		PoleRF = new ModelRenderer(this, 48, 0);
		PoleRF.addBox(0F, 0F, 0F, 1, 14, 1);
		PoleRF.setRotationPoint(-6F, 9F, -6F);
		PoleRF.setTextureSize(128, 128);
		PoleRF.mirror = true;
		setRotation(PoleRF, 0F, 0F, 0F);
		Top = new ModelRenderer(this, 0, 0);
		Top.addBox(0F, 0F, 0F, 12, 1, 12);
		Top.setRotationPoint(-6F, 8F, -6F);
		Top.setTextureSize(128, 128);
		Top.mirror = true;
		setRotation(Top, 0F, 0F, 0F);
		FrontGlass = new ModelRenderer(this, 0, 13);
		FrontGlass.addBox(0F, 0F, 0F, 10, 14, 1);
		FrontGlass.setRotationPoint(-5F, 9F, -6F);
		FrontGlass.setTextureSize(128, 128);
		FrontGlass.mirror = true;
		setRotation(FrontGlass, 0F, 0F, 0F);
		BackGlass = new ModelRenderer(this, 0, 28);
		BackGlass.addBox(0F, 0F, 3F, 10, 14, 1);
		BackGlass.setRotationPoint(-5F, 9F, 2F);
		BackGlass.setTextureSize(128, 128);
		BackGlass.mirror = true;
		setRotation(BackGlass, 0F, 0F, 0F);
		RightGlass = new ModelRenderer(this, 22, 13);
		RightGlass.addBox(0F, 0F, 0F, 1, 14, 10);
		RightGlass.setRotationPoint(-6F, 9F, -5F);
		RightGlass.setTextureSize(128, 128);
		RightGlass.mirror = true;
		setRotation(RightGlass, 0F, 0F, 0F);
		LeftGlass = new ModelRenderer(this, 22, 37);
		LeftGlass.addBox(0F, 0F, 0F, 1, 14, 10);
		LeftGlass.setRotationPoint(5F, 9F, -5F);
		LeftGlass.setTextureSize(128, 128);
		LeftGlass.mirror = true;
		setRotation(LeftGlass, 0F, 0F, 0F);
	}

	public void render(float size)
	{
		Base.render(size);
		PoleFL.render(size);
		PoleLB.render(size);
		PoleBR.render(size);
		PoleRF.render(size);
		Top.render(size);
		
		MekanismRenderer.blendOn();
		FrontGlass.render(size);
		BackGlass.render(size);
		RightGlass.render(size);
		LeftGlass.render(size);
		MekanismRenderer.blendOff();
	}

	private void setRotation(ModelRenderer model, float x, float y, float z)
	{
		model.rotateAngleX = x;
		model.rotateAngleY = y;
		model.rotateAngleZ = z;
	}
}
