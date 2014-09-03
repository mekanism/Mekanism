package mekanism.client.model;

import mekanism.client.render.MekanismRenderer;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.entity.Entity;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

import org.lwjgl.opengl.GL11;

@SideOnly(Side.CLIENT)
public class ModelRobit extends ModelBase
{
	public ModelRenderer Body;
	public ModelRenderer Bottom;
	public ModelRenderer RightTrack;
	public ModelRenderer LeftTrack;
	public ModelRenderer Neck;
	public ModelRenderer Head;
	public ModelRenderer Backpack;
	public ModelRenderer headback;
	public ModelRenderer rightarn;
	public ModelRenderer leftarm;
	public ModelRenderer righthand;
	public ModelRenderer lefthand;
	public ModelRenderer backLight;
	public ModelRenderer eyeRight;
	public ModelRenderer eyeLeft;

	public ModelRobit()
	{
		textureWidth = 64;
		textureHeight = 64;

		Body = new ModelRenderer(this, 0, 0);
		Body.addBox(0F, 0F, 1F, 6, 4, 5);
		Body.setRotationPoint(-3F, 17F, -3F);
		Body.setTextureSize(64, 64);
		Body.mirror = true;
		setRotation(Body, 0F, 0F, 0F);
		Bottom = new ModelRenderer(this, 22, 0);
		Bottom.addBox(0F, 0F, 0F, 6, 2, 7);
		Bottom.setRotationPoint(-3F, 21F, -2.5F);
		Bottom.setTextureSize(64, 64);
		Bottom.mirror = true;
		setRotation(Bottom, 0F, 0F, 0F);
		RightTrack = new ModelRenderer(this, 26, 9);
		RightTrack.addBox(0F, 0F, 0F, 2, 3, 9);
		RightTrack.setRotationPoint(3F, 21F, -4F);
		RightTrack.setTextureSize(64, 64);
		RightTrack.mirror = true;
		setRotation(RightTrack, 0F, 0F, 0F);
		LeftTrack = new ModelRenderer(this, 0, 9);
		LeftTrack.addBox(0F, 0F, 0F, 2, 3, 9);
		LeftTrack.setRotationPoint(-5F, 21F, -4F);
		LeftTrack.setTextureSize(64, 64);
		LeftTrack.mirror = true;
		setRotation(LeftTrack, 0F, 0F, 0F);
		Neck = new ModelRenderer(this, 0, 26);
		Neck.addBox(0F, 0F, 0F, 3, 1, 2);
		Neck.setRotationPoint(-1.5F, 16F, -0.5F);
		Neck.setTextureSize(64, 64);
		Neck.mirror = true;
		setRotation(Neck, 0F, 0F, 0F);
		Head = new ModelRenderer(this, 26, 21);
		Head.addBox(0F, 0F, 0F, 7, 3, 4);
		Head.setRotationPoint(-3.5F, 13.5F, -1.533333F);
		Head.setTextureSize(64, 64);
		Head.mirror = true;
		setRotation(Head, 0F, 0F, 0F);
		Backpack = new ModelRenderer(this, 14, 9);
		Backpack.addBox(0F, 0F, 0F, 4, 3, 6);
		Backpack.setRotationPoint(-2F, 16.8F, -4F);
		Backpack.setTextureSize(64, 64);
		Backpack.mirror = true;
		setRotation(Backpack, 0F, 0F, 0F);
		headback = new ModelRenderer(this, 17, 1);
		headback.addBox(0F, 0F, 0F, 5, 2, 1);
		headback.setRotationPoint(-2.5F, 14F, -2F);
		headback.setTextureSize(64, 64);
		headback.mirror = true;
		setRotation(headback, 0F, 0F, 0F);
		rightarn = new ModelRenderer(this, 0, 21);
		rightarn.addBox(0F, 0F, 0F, 1, 1, 4);
		rightarn.setRotationPoint(3F, 17.5F, 0F);
		rightarn.setTextureSize(64, 64);
		rightarn.mirror = true;
		setRotation(rightarn, 0F, 0F, 0F);
		leftarm = new ModelRenderer(this, 12, 21);
		leftarm.addBox(0F, 0F, 0F, 1, 1, 4);
		leftarm.setRotationPoint(-4F, 17.5F, 0F);
		leftarm.setTextureSize(64, 64);
		leftarm.mirror = true;
		setRotation(leftarm, 0F, 0F, 0F);
		righthand = new ModelRenderer(this, 15, 28);
		righthand.addBox(0F, 0F, 0F, 1, 1, 0);
		righthand.setRotationPoint(2.5F, 17.5F, 4F);
		righthand.setTextureSize(64, 64);
		righthand.mirror = true;
		setRotation(righthand, 0F, 0F, 0F);
		lefthand = new ModelRenderer(this, 15, 28);
		lefthand.addBox(0F, 0F, 0F, 1, 1, 0);
		lefthand.setRotationPoint(-3.5F, 17.5F, 4F);
		lefthand.setTextureSize(64, 64);
		lefthand.mirror = true;
		setRotation(lefthand, 0F, 0F, 0F);
		backLight = new ModelRenderer(this, 20, 15);
		backLight.addBox(0F, 0F, 0F, 2, 1, 1);
		backLight.setRotationPoint(-1F, 17.8F, -4.001F);
		backLight.setTextureSize(64, 64);
		backLight.mirror = true;
		setRotation(backLight, 0F, 0F, 0F);
		eyeRight = new ModelRenderer(this, 43, 25);
		eyeRight.addBox(0F, 0F, 0F, 1, 1, 1);
		eyeRight.setRotationPoint(1.5F, 14.5F, 1.50001F);
		eyeRight.setTextureSize(64, 64);
		eyeRight.mirror = true;
		setRotation(eyeRight, 0F, 0F, 0F);
		eyeLeft = new ModelRenderer(this, 43, 25);
		eyeLeft.addBox(0F, 0F, 0F, 1, 1, 1);
		eyeLeft.setRotationPoint(-2.5F, 14.5F, 1.50001F);
		eyeLeft.setTextureSize(64, 64);
		eyeLeft.mirror = true;
		setRotation(eyeLeft, 0F, 0F, 0F);
	}

	@Override
	public void render(Entity entity, float f, float f1, float f2, float f3, float f4, float f5)
	{
		super.render(entity, f, f1, f2, f3, f4, f5);
		setRotationAngles(f, f1, f2, f3, f4, f5, entity);

		GL11.glPushMatrix();
		GL11.glRotatef(180, 0.0F, 1.0F, 0.0F);

		Body.render(f5);
		Bottom.render(f5);
		RightTrack.render(f5);
		LeftTrack.render(f5);
		Neck.render(f5);
		Head.render(f5);
		Backpack.render(f5);
		headback.render(f5);
		rightarn.render(f5);
		leftarm.render(f5);
		righthand.render(f5);
		lefthand.render(f5);

		MekanismRenderer.glowOn();
		backLight.render(f5);
		eyeRight.render(f5);
		eyeLeft.render(f5);
		MekanismRenderer.glowOff();

		GL11.glPopMatrix();
	}

	public void render(float size)
	{
		Body.render(size);
		Bottom.render(size);
		RightTrack.render(size);
		LeftTrack.render(size);
		Neck.render(size);
		Head.render(size);
		Backpack.render(size);
		headback.render(size);
		rightarn.render(size);
		leftarm.render(size);
		righthand.render(size);
		lefthand.render(size);
		backLight.render(size);
		eyeRight.render(size);
		eyeLeft.render(size);
	}

	private void setRotation(ModelRenderer model, float x, float y, float z)
	{
		model.rotateAngleX = x;
		model.rotateAngleY = y;
		model.rotateAngleZ = z;
	}
}
