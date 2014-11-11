package mekanism.client.model;

import mekanism.client.render.MekanismRenderer;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class ModelEnergyCube extends ModelBase
{
	ModelRenderer Corner1;
	ModelRenderer Corner2;
	ModelRenderer Corner3;
	ModelRenderer Corner4;
	ModelRenderer Corner5;
	ModelRenderer Corner6;
	ModelRenderer Corner7;
	ModelRenderer Corner8;
	ModelRenderer Frame4;
	ModelRenderer Frame3;
	ModelRenderer Frame2;
	ModelRenderer Frame1;
	ModelRenderer Connection;
	ModelRenderer HoriPole1;
	ModelRenderer HoriPole2;
	ModelRenderer HoriPole3;
	ModelRenderer HoriPole4;
	ModelRenderer HoriPole5;
	ModelRenderer HoriPole6;
	ModelRenderer HoriPole7;
	ModelRenderer HoriPole8;

	public ModelEnergyCube()
	{
		textureWidth = 64;
		textureHeight = 64;

		Corner1 = new ModelRenderer(this, 0, 17);
		Corner1.addBox(0F, 0F, 0F, 3, 3, 3);
		Corner1.setRotationPoint(5F, 21F, -8F);
		Corner1.setTextureSize(64, 64);
		Corner1.mirror = true;
		setRotation(Corner1, 0F, 0F, 0F);
		Corner2 = new ModelRenderer(this, 0, 17);
		Corner2.addBox(0F, 0F, 0F, 3, 3, 3);
		Corner2.setRotationPoint(-8F, 21F, -8F);
		Corner2.setTextureSize(64, 64);
		Corner2.mirror = true;
		setRotation(Corner2, 0F, 0F, 0F);
		Corner3 = new ModelRenderer(this, 0, 17);
		Corner3.addBox(0F, 0F, 0F, 3, 3, 3);
		Corner3.setRotationPoint(5F, 21F, 5F);
		Corner3.setTextureSize(64, 64);
		Corner3.mirror = true;
		setRotation(Corner3, 0F, 0F, 0F);
		Corner4 = new ModelRenderer(this, 0, 17);
		Corner4.addBox(0F, 0F, 0F, 3, 3, 3);
		Corner4.setRotationPoint(-8F, 21F, 5F);
		Corner4.setTextureSize(64, 64);
		Corner4.mirror = true;
		setRotation(Corner4, 0F, 0F, 0F);
		Corner5 = new ModelRenderer(this, 0, 17);
		Corner5.addBox(0F, 0F, 0F, 3, 3, 3);
		Corner5.setRotationPoint(5F, 8F, -8F);
		Corner5.setTextureSize(64, 64);
		Corner5.mirror = true;
		setRotation(Corner5, 0F, 0F, 0F);
		Corner6 = new ModelRenderer(this, 0, 17);
		Corner6.addBox(0F, 0F, 0F, 3, 3, 3);
		Corner6.setRotationPoint(-8F, 8F, -8F);
		Corner6.setTextureSize(64, 64);
		Corner6.mirror = true;
		setRotation(Corner6, 0F, 0F, 0F);
		Corner7 = new ModelRenderer(this, 0, 17);
		Corner7.addBox(0F, 0F, 0F, 3, 3, 3);
		Corner7.setRotationPoint(-8F, 8F, 5F);
		Corner7.setTextureSize(64, 64);
		Corner7.mirror = true;
		setRotation(Corner7, 0F, 0F, 0F);
		Corner8 = new ModelRenderer(this, 0, 17);
		Corner8.addBox(0F, 0F, 0F, 3, 3, 3);
		Corner8.setRotationPoint(5F, 8F, 5F);
		Corner8.setTextureSize(64, 64);
		Corner8.mirror = true;
		setRotation(Corner8, 0F, 0F, 0F);
		Frame4 = new ModelRenderer(this, 0, 24);
		Frame4.addBox(0F, 0F, 0F, 3, 10, 3);
		Frame4.setRotationPoint(5F, 11F, -8F);
		Frame4.setTextureSize(64, 64);
		Frame4.mirror = true;
		setRotation(Frame4, 0F, 0F, 0F);
		Frame3 = new ModelRenderer(this, 0, 24);
		Frame3.addBox(0F, 0F, 0F, 3, 10, 3);
		Frame3.setRotationPoint(-8F, 11F, -8F);
		Frame3.setTextureSize(64, 64);
		Frame3.mirror = true;
		setRotation(Frame3, 0F, 0F, 0F);
		Frame2 = new ModelRenderer(this, 0, 24);
		Frame2.addBox(0F, 0F, 0F, 3, 10, 3);
		Frame2.setRotationPoint(-8F, 11F, 5F);
		Frame2.setTextureSize(64, 64);
		Frame2.mirror = true;
		setRotation(Frame2, 0F, 0F, 0F);
		Frame1 = new ModelRenderer(this, 0, 24);
		Frame1.addBox(0F, 0F, 0F, 3, 10, 3);
		Frame1.setRotationPoint(5F, 11F, 5F);
		Frame1.setTextureSize(64, 64);
		Frame1.mirror = true;
		setRotation(Frame1, 0F, 0F, 0F);
		Connection = new ModelRenderer(this, 0, 7);
		Connection.addBox(0F, 0F, 0F, 10, 6, 1);
		Connection.setRotationPoint(-5F, 13F, -8F);
		Connection.setTextureSize(64, 64);
		Connection.mirror = true;
		setRotation(Connection, 0F, 0F, 0F);
		HoriPole1 = new ModelRenderer(this, 27, 0);
		HoriPole1.addBox(0F, 0F, 0F, 3, 3, 10);
		HoriPole1.setRotationPoint(5F, 21F, -5F);
		HoriPole1.setTextureSize(64, 64);
		HoriPole1.mirror = true;
		setRotation(HoriPole1, 0F, 0F, 0F);
		HoriPole2 = new ModelRenderer(this, 0, 0);
		HoriPole2.addBox(0F, 0F, 0F, 10, 3, 3);
		HoriPole2.setRotationPoint(-5F, 21F, 5F);
		HoriPole2.setTextureSize(64, 64);
		HoriPole2.mirror = true;
		setRotation(HoriPole2, 0F, 0F, 0F);
		HoriPole3 = new ModelRenderer(this, 27, 0);
		HoriPole3.addBox(0F, 0F, 0F, 3, 3, 10);
		HoriPole3.setRotationPoint(-8F, 21F, -5F);
		HoriPole3.setTextureSize(64, 64);
		HoriPole3.mirror = true;
		setRotation(HoriPole3, 0F, 0F, 0F);
		HoriPole4 = new ModelRenderer(this, 0, 0);
		HoriPole4.addBox(0F, 0F, 0F, 10, 3, 3);
		HoriPole4.setRotationPoint(-5F, 21F, -8F);
		HoriPole4.setTextureSize(64, 64);
		HoriPole4.mirror = true;
		setRotation(HoriPole4, 0F, 0F, 0F);
		HoriPole5 = new ModelRenderer(this, 27, 0);
		HoriPole5.addBox(0F, 0F, 0F, 3, 3, 10);
		HoriPole5.setRotationPoint(5F, 8F, -5F);
		HoriPole5.setTextureSize(64, 64);
		HoriPole5.mirror = true;
		setRotation(HoriPole5, 0F, 0F, 0F);
		HoriPole6 = new ModelRenderer(this, 0, 0);
		HoriPole6.addBox(0F, 0F, 0F, 10, 3, 3);
		HoriPole6.setRotationPoint(-5F, 8F, 5F);
		HoriPole6.setTextureSize(64, 64);
		HoriPole6.mirror = true;
		setRotation(HoriPole6, 0F, 0F, 0F);
		HoriPole7 = new ModelRenderer(this, 27, 0);
		HoriPole7.addBox(0F, 0F, 0F, 3, 3, 10);
		HoriPole7.setRotationPoint(-8F, 8F, -5F);
		HoriPole7.setTextureSize(64, 64);
		HoriPole7.mirror = true;
		setRotation(HoriPole7, 0F, 0F, 0F);
		HoriPole8 = new ModelRenderer(this, 0, 0);
		HoriPole8.addBox(0F, 0F, 0F, 10, 3, 3);
		HoriPole8.setRotationPoint(-5F, 8F, -8F);
		HoriPole8.setTextureSize(64, 64);
		HoriPole8.mirror = true;
		setRotation(HoriPole8, 0F, 0F, 0F);
	}

	public void render(float size)
	{
		MekanismRenderer.glowOn();
		Corner1.render(size);
		Corner2.render(size);
		Corner3.render(size);
		Corner4.render(size);
		Corner5.render(size);
		Corner6.render(size);
		Corner7.render(size);
		Corner8.render(size);
		MekanismRenderer.glowOff();

		Frame4.render(size);
		Frame3.render(size);
		Frame2.render(size);
		Frame1.render(size);
		Connection.render(size);
		HoriPole1.render(size);
		HoriPole2.render(size);
		HoriPole3.render(size);
		HoriPole4.render(size);
		HoriPole5.render(size);
		HoriPole6.render(size);
		HoriPole7.render(size);
		HoriPole8.render(size);
	}

	private void setRotation(ModelRenderer model, float x, float y, float z)
	{
		model.rotateAngleX = x;
		model.rotateAngleY = y;
		model.rotateAngleZ = z;
	}

	public static class ModelEnergyCore extends ModelBase
	{
		private ModelRenderer cube;

		public ModelEnergyCore()
		{
			textureWidth = 32;
			textureHeight = 32;

			cube = new ModelRenderer(this, 0, 0);
			cube.addBox(-8, -8, -8, 16, 16, 16);
			cube.setTextureSize(32, 32);
			cube.mirror = true;
		}

		public void render(float size)
		{
			cube.render(0.0625F);
		}
	}
}
