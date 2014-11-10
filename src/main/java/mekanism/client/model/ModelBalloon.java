package mekanism.client.model;

import mekanism.api.EnumColor;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

import org.lwjgl.opengl.GL11;

@SideOnly(Side.CLIENT)
public class ModelBalloon extends ModelBase
{
	ModelRenderer Balloon2;
	ModelRenderer Balloon1;
	ModelRenderer Balloon3;
	ModelRenderer Balloonnub;
	ModelRenderer String;

	public ModelBalloon()
	{
		textureWidth = 64;
		textureHeight = 32;

		Balloon2 = new ModelRenderer(this, 0, 0);
		Balloon2.addBox(-2.5F, -2F, -2F, 5, 4, 4);
		Balloon2.setRotationPoint(0F, 0F, 0F);
		Balloon2.setTextureSize(64, 32);
		Balloon2.mirror = true;
		setRotation(Balloon2, 0F, 0F, 0F);
		Balloon1 = new ModelRenderer(this, 0, 8);
		Balloon1.addBox(-2F, -2F, -2.5F, 4, 4, 5);
		Balloon1.setRotationPoint(0F, 0F, 0F);
		Balloon1.setTextureSize(64, 32);
		Balloon1.mirror = true;
		setRotation(Balloon1, 0F, 0F, 0F);
		Balloon3 = new ModelRenderer(this, 18, 0);
		Balloon3.addBox(-2F, -2.5F, -2F, 4, 5, 4);
		Balloon3.setRotationPoint(0F, 0F, 0F);
		Balloon3.setTextureSize(64, 32);
		Balloon3.mirror = true;
		setRotation(Balloon3, 0F, 0F, 0F);
		Balloonnub = new ModelRenderer(this, 18, 9);
		Balloonnub.addBox(-0.5F, 2.5F, -0.5F, 1, 1, 1);
		Balloonnub.setRotationPoint(0F, 0F, 0F);
		Balloonnub.setTextureSize(64, 32);
		Balloonnub.mirror = true;
		setRotation(Balloonnub, 0F, 0F, 0F);
		String = new ModelRenderer(this, 34, 0);
		String.addBox(-0.5F, 3.5F, -0.5F, 1, 11, 1);
		String.setRotationPoint(0F, 0F, 0F);
		String.setTextureSize(64, 32);
		String.mirror = true;
		setRotation(String, 0F, 0F, 0F);
	}

	public void render(float size, EnumColor color)
	{
		GL11.glPushMatrix();
		GL11.glColor3f(color.getColor(0), color.getColor(1), color.getColor(2));
		GL11.glScalef(1.5F, 1.5F, 1.5F);
		GL11.glTranslatef(0, -0.07F, 0);

		Balloon2.render(size);
		Balloon1.render(size);
		Balloon3.render(size);
		Balloonnub.render(size);

		GL11.glColor3f(1, 1, 1);
		GL11.glPopMatrix();

		GL11.glPushMatrix();
		GL11.glScalef(0.2F, 1, 0.2F);
		String.render(size);
		GL11.glPopMatrix();
	}

	private void setRotation(ModelRenderer model, float x, float y, float z)
	{
		model.rotateAngleX = x;
		model.rotateAngleY = y;
		model.rotateAngleZ = z;
	}
}