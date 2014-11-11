package mekanism.client.model;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

import org.lwjgl.opengl.GL11;

@SideOnly(Side.CLIENT)
public class ModelSeismicVibrator extends ModelBase 
{
	ModelRenderer Base;
	ModelRenderer Tower;
	ModelRenderer TowerTop;
	ModelRenderer TopPanel;
	ModelRenderer Vibrator;
	ModelRenderer PoleBR;
	ModelRenderer PoleLB;
	ModelRenderer PoleFL;
	ModelRenderer PoleRF;
	ModelRenderer PoleL;
	ModelRenderer PoleR;
	ModelRenderer Cable;
	ModelRenderer ScreenFront;
	ModelRenderer Keyboard;

	public ModelSeismicVibrator() 
	{
		textureWidth = 128;
		textureHeight = 128;

		Base = new ModelRenderer(this, 0, 0);
		Base.addBox(0F, 0F, 0F, 16, 1, 16);
		Base.setRotationPoint(-8F, 23F, -8F);
		Base.setTextureSize(128, 128);
		Base.mirror = true;
		setRotation(Base, 0F, 0F, 0F);
		Tower = new ModelRenderer(this, 64, 0);
		Tower.addBox(0F, 0F, 0F, 4, 11, 4);
		Tower.setRotationPoint(-2F, 12F, -2F);
		Tower.setTextureSize(128, 128);
		Tower.mirror = true;
		setRotation(Tower, 0F, 0F, 0F);
		TowerTop = new ModelRenderer(this, 80, 0);
		TowerTop.addBox(0F, 0F, 0F, 2, 1, 2);
		TowerTop.setRotationPoint(-1F, 10F, -1F);
		TowerTop.setTextureSize(128, 128);
		TowerTop.mirror = true;
		setRotation(TowerTop, 0F, 0F, 0F);
		TopPanel = new ModelRenderer(this, 0, 17);
		TopPanel.addBox(0F, 0F, 0F, 8, 1, 8);
		TopPanel.setRotationPoint(-4F, 11F, -4F);
		TopPanel.setTextureSize(128, 128);
		TopPanel.mirror = true;
		setRotation(TopPanel, 0F, 0F, 0F);
		Vibrator = new ModelRenderer(this, 32, 17);
		Vibrator.addBox(0F, 0F, 0F, 8, 4, 8);
		Vibrator.setRotationPoint(-4F, 19F, -4F);
		Vibrator.setTextureSize(128, 128);
		Vibrator.mirror = true;
		setRotation(Vibrator, 0F, 0F, 0F);
		PoleBR = new ModelRenderer(this, 0, 26);
		PoleBR.addBox(0F, 0F, 0F, 1, 13, 1);
		PoleBR.setRotationPoint(-5F, 10F, 4F);
		PoleBR.setTextureSize(128, 128);
		PoleBR.mirror = true;
		setRotation(PoleBR, 0F, 0F, 0F);
		PoleLB = new ModelRenderer(this, 0, 26);
		PoleLB.addBox(0F, 0F, 0F, 1, 13, 1);
		PoleLB.setRotationPoint(4F, 10F, 4F);
		PoleLB.setTextureSize(128, 128);
		PoleLB.mirror = true;
		setRotation(PoleLB, 0F, 0F, 0F);
		PoleFL = new ModelRenderer(this, 0, 26);
		PoleFL.addBox(0F, 0F, 0F, 1, 13, 1);
		PoleFL.setRotationPoint(4F, 10F, -5F);
		PoleFL.setTextureSize(128, 128);
		PoleFL.mirror = true;
		setRotation(PoleFL, 0F, 0F, 0F);
		PoleRF = new ModelRenderer(this, 0, 26);
		PoleRF.addBox(0F, 0F, 0F, 1, 13, 1);
		PoleRF.setRotationPoint(-5F, 10F, -5F);
		PoleRF.setTextureSize(128, 128);
		PoleRF.mirror = true;
		setRotation(PoleRF, 0F, 0F, 0F);
		PoleL = new ModelRenderer(this, 4, 26);
		PoleL.addBox(0F, 0F, 0F, 1, 1, 8);
		PoleL.setRotationPoint(4F, 10F, -4F);
		PoleL.setTextureSize(128, 128);
		PoleL.mirror = true;
		setRotation(PoleL, 0F, 0F, 0F);
		PoleR = new ModelRenderer(this, 4, 26);
		PoleR.addBox(0F, 0F, 0F, 1, 1, 8);
		PoleR.setRotationPoint(-5F, 10F, -4F);
		PoleR.setTextureSize(128, 128);
		PoleR.mirror = true;
		setRotation(PoleR, 0F, 0F, 0F);
		Cable = new ModelRenderer(this, 64, 15);
		Cable.addBox(0F, 0F, 0F, 6, 2, 6);
		Cable.setRotationPoint(-3F, 8F, -3F);
		Cable.setTextureSize(128, 128);
		Cable.mirror = true;
		setRotation(Cable, 0F, 0F, 0F);
		ScreenFront = new ModelRenderer(this, 0, 40);
		ScreenFront.addBox(0F, 0F, 0F, 8, 6, 1);
		ScreenFront.setRotationPoint(-4F, 11F, -6F);
		ScreenFront.setTextureSize(128, 128);
		ScreenFront.mirror = true;
		setRotation(ScreenFront, 0F, 0F, 0F);
		Keyboard = new ModelRenderer(this, 0, 47);
		Keyboard.addBox(0F, 0F, 0F, 8, 1, 3);
		Keyboard.setRotationPoint(-4F, 18F, -8F);
		Keyboard.setTextureSize(128, 128);
		Keyboard.mirror = true;
		setRotation(Keyboard, 0.4363323F, 0F, 0F);
	}

	public void render(float size) 
	{
		Base.render(size);
		Tower.render(size);
		TowerTop.render(size);
		TopPanel.render(size);
		Vibrator.render(size);
		PoleBR.render(size);
		PoleLB.render(size);
		PoleFL.render(size);
		PoleRF.render(size);
		PoleL.render(size);
		PoleR.render(size);
		Cable.render(size);
		ScreenFront.render(size);
		
		GL11.glScalef(1.01F, 1.01F, 1.01F);
		Keyboard.render(size);
	}
	
	public void renderWithPiston(float piston, float size)
	{
		Vibrator.rotationPointY = 19 - (piston*7);
		render(size);
	}

	private void setRotation(ModelRenderer model, float x, float y, float z) 
	{
		model.rotateAngleX = x;
		model.rotateAngleY = y;
		model.rotateAngleZ = z;
	}
}
