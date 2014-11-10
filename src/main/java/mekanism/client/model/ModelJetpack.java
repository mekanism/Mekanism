package mekanism.client.model;

import mekanism.client.render.MekanismRenderer;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

import org.lwjgl.opengl.GL11;

@SideOnly(Side.CLIENT)
public class ModelJetpack extends ModelBase
{
	ModelRenderer Packtop;
	ModelRenderer Packbottom;
	ModelRenderer Thrusterleft;
	ModelRenderer Thrusterright;
	ModelRenderer Fueltuberight;
	ModelRenderer Fueltubeleft;
	ModelRenderer Packmid;
	ModelRenderer Packcore;
	ModelRenderer WingsupportL;
	ModelRenderer WingsupportR;
	ModelRenderer Packtoprear;
	ModelRenderer ExtendosupportL;
	ModelRenderer ExtendosupportR;
	ModelRenderer WingbladeL;
	ModelRenderer WingbladeR;
	ModelRenderer Packdoodad2;
	ModelRenderer Packdoodad3;
	ModelRenderer Bottomthruster;
	ModelRenderer light1;
	ModelRenderer light2;
	ModelRenderer light3;

	public ModelJetpack()
	{
		textureWidth = 128;
		textureHeight = 64;

		Packtop = new ModelRenderer(this, 92, 28);
		Packtop.addBox(-4F, 0F, 4F, 8, 4, 1);
		Packtop.setRotationPoint(0F, 0F, 0F);
		Packtop.setTextureSize(128, 64);
		Packtop.mirror = true;
		setRotation(Packtop, 0.2094395F, 0F, 0F);
		Packbottom = new ModelRenderer(this, 92, 42);
		Packbottom.addBox(-4F, 4.1F, 1.5F, 8, 4, 4);
		Packbottom.setRotationPoint(0F, 0F, 0F);
		Packbottom.setTextureSize(128, 64);
		Packbottom.mirror = true;
		setRotation(Packbottom, -0.0872665F, 0F, 0F);
		Thrusterleft = new ModelRenderer(this, 69, 30);
		Thrusterleft.addBox(7.8F, 1.5F, -3.5F, 3, 3, 3);
		Thrusterleft.setRotationPoint(0F, 0F, 0F);
		Thrusterleft.setTextureSize(128, 64);
		Thrusterleft.mirror = true;
		setRotation(Thrusterleft, 0.7853982F, -0.715585F, 0.3490659F);
		Thrusterright = new ModelRenderer(this, 69, 30);
		Thrusterright.addBox(-10.8F, 1.5F, -3.5F, 3, 3, 3);
		Thrusterright.setRotationPoint(0F, 0F, 0F);
		Thrusterright.setTextureSize(128, 64);
		Thrusterright.mirror = true;
		setRotation(Thrusterright, 0.7853982F, 0.715585F, -0.3490659F);
		Fueltuberight = new ModelRenderer(this, 92, 23);
		Fueltuberight.addBox(-11.2F, 2F, -3F, 8, 2, 2);
		Fueltuberight.setRotationPoint(0F, 0F, 0F);
		Fueltuberight.setTextureSize(128, 64);
		Fueltuberight.mirror = true;
		setRotation(Fueltuberight, 0.7853982F, 0.715585F, -0.3490659F);
		Fueltubeleft = new ModelRenderer(this, 92, 23);
		Fueltubeleft.addBox(3.2F, 2F, -3F, 8, 2, 2);
		Fueltubeleft.setRotationPoint(0F, 0F, 0F);
		Fueltubeleft.setTextureSize(128, 64);
		Fueltubeleft.mirror = true;
		setRotation(Fueltubeleft, 0.7853982F, -0.715585F, 0.3490659F);
		Packmid = new ModelRenderer(this, 92, 34);
		Packmid.addBox(-4F, 3.3F, 1.5F, 8, 1, 4);
		Packmid.setRotationPoint(0F, 0F, 0F);
		Packmid.setTextureSize(128, 64);
		Packmid.mirror = true;
		setRotation(Packmid, 0F, 0F, 0F);
		Packcore = new ModelRenderer(this, 69, 2);
		Packcore.addBox(-3.5F, 3F, 2F, 7, 1, 3);
		Packcore.setRotationPoint(0F, 0F, 0F);
		Packcore.setTextureSize(128, 64);
		Packcore.mirror = true;
		setRotation(Packcore, 0F, 0F, 0F);
		WingsupportL = new ModelRenderer(this, 71, 55);
		WingsupportL.addBox(3F, -1F, 2.2F, 7, 2, 2);
		WingsupportL.setRotationPoint(0F, 0F, 0F);
		WingsupportL.setTextureSize(128, 64);
		WingsupportL.mirror = true;
		setRotation(WingsupportL, 0F, 0F, 0.2792527F);
		WingsupportR = new ModelRenderer(this, 71, 55);
		WingsupportR.addBox(-10F, -1F, 2.2F, 7, 2, 2);
		WingsupportR.setRotationPoint(0F, 0F, 0F);
		WingsupportR.setTextureSize(128, 64);
		WingsupportR.mirror = true;
		setRotation(WingsupportR, 0F, 0F, -0.2792527F);
		Packtoprear = new ModelRenderer(this, 106, 28);
		Packtoprear.addBox(-4F, 1F, 1F, 8, 3, 3);
		Packtoprear.setRotationPoint(0F, 0F, 0F);
		Packtoprear.setTextureSize(128, 64);
		Packtoprear.mirror = true;
		setRotation(Packtoprear, 0.2094395F, 0F, 0F);
		ExtendosupportL = new ModelRenderer(this, 94, 16);
		ExtendosupportL.addBox(8F, -0.2F, 2.5F, 9, 1, 1);
		ExtendosupportL.setRotationPoint(0F, 0F, 0F);
		ExtendosupportL.setTextureSize(128, 64);
		ExtendosupportL.mirror = true;
		setRotation(ExtendosupportL, 0F, 0F, 0.2792527F);
		ExtendosupportR = new ModelRenderer(this, 94, 16);
		ExtendosupportR.addBox(-17F, -0.2F, 2.5F, 9, 1, 1);
		ExtendosupportR.setRotationPoint(0F, 0F, 0F);
		ExtendosupportR.setTextureSize(128, 64);
		ExtendosupportR.mirror = true;
		setRotation(ExtendosupportR, 0F, 0F, -0.2792527F);
		WingbladeL = new ModelRenderer(this, 62, 5);
		WingbladeL.addBox(3.3F, 1.1F, 3F, 14, 2, 0);
		WingbladeL.setRotationPoint(0F, 0F, 0F);
		WingbladeL.setTextureSize(128, 64);
		WingbladeL.mirror = true;
		setRotation(WingbladeL, 0F, 0F, 0.2094395F);
		WingbladeR = new ModelRenderer(this, 62, 5);
		WingbladeR.addBox(-17.3F, 1.1F, 3F, 14, 2, 0);
		WingbladeR.setRotationPoint(0F, 0F, 0F);
		WingbladeR.setTextureSize(128, 64);
		WingbladeR.mirror = true;
		setRotation(WingbladeR, 0F, 0F, -0.2094395F);
		Packdoodad2 = new ModelRenderer(this, 116, 0);
		Packdoodad2.addBox(1F, 0.5F, 4.2F, 2, 1, 1);
		Packdoodad2.setRotationPoint(0F, 0F, 0F);
		Packdoodad2.setTextureSize(128, 64);
		Packdoodad2.mirror = true;
		setRotation(Packdoodad2, 0.2094395F, 0F, 0F);
		Packdoodad3 = new ModelRenderer(this, 116, 0);
		Packdoodad3.addBox(1F, 2F, 4.2F, 2, 1, 1);
		Packdoodad3.setRotationPoint(0F, 0F, 0F);
		Packdoodad3.setTextureSize(128, 64);
		Packdoodad3.mirror = true;
		setRotation(Packdoodad3, 0.2094395F, 0F, 0F);
		Bottomthruster = new ModelRenderer(this, 68, 26);
		Bottomthruster.addBox(-3F, 8F, 2.333333F, 6, 1, 2);
		Bottomthruster.setRotationPoint(0F, 0F, 0F);
		Bottomthruster.setTextureSize(128, 64);
		Bottomthruster.mirror = true;
		setRotation(Bottomthruster, 0F, 0F, 0F);
		light1 = new ModelRenderer(this, 55, 2);
		light1.addBox(2F, 6.55F, 4F, 1, 1, 1);
		light1.setRotationPoint(0F, 0F, 0F);
		light1.setTextureSize(128, 64);
		light1.mirror = true;
		setRotation(light1, 0F, 0F, 0F);
		light2 = new ModelRenderer(this, 55, 2);
		light2.addBox(0F, 6.55F, 4F, 1, 1, 1);
		light2.setRotationPoint(0F, 0F, 0F);
		light2.setTextureSize(128, 64);
		light2.mirror = true;
		setRotation(light2, 0F, 0F, 0F);
		light3 = new ModelRenderer(this, 55, 2);
		light3.addBox(-3F, 6.55F, 4F, 1, 1, 1);
		light3.setRotationPoint(0F, 0F, 0F);
		light3.setTextureSize(128, 64);
		light3.mirror = true;
		setRotation(light3, 0F, 0F, 0F);
	}

	public void render(float size)
	{
		Packtop.render(size);
		Packbottom.render(size);
		Thrusterleft.render(size);
		Thrusterright.render(size);
		Fueltuberight.render(size);
		Fueltubeleft.render(size);
		Packmid.render(size);

		MekanismRenderer.glowOn();
		Packcore.render(size);
		MekanismRenderer.glowOff();

		WingsupportL.render(size);
		WingsupportR.render(size);
		Packtoprear.render(size);
		ExtendosupportL.render(size);
		ExtendosupportR.render(size);

		GL11.glPushMatrix();
		MekanismRenderer.blendOn();
		MekanismRenderer.glowOn();
		GL11.glEnable(GL11.GL_CULL_FACE);
		GL11.glColor4f(1, 1, 1, 0.2F);

		WingbladeL.render(size);
		WingbladeR.render(size);

		GL11.glColor4f(1, 1, 1, 1);
		GL11.glDisable(GL11.GL_CULL_FACE);
		MekanismRenderer.glowOff();
		MekanismRenderer.blendOff();
		GL11.glPopMatrix();

		Packdoodad2.render(size);
		Packdoodad3.render(size);
		Bottomthruster.render(size);

		MekanismRenderer.glowOn();
		light1.render(size);
		light2.render(size);
		light3.render(size);
		Packcore.render(size);
		MekanismRenderer.glowOff();
	}

	private void setRotation(ModelRenderer model, float x, float y, float z)
	{
		model.rotateAngleX = x;
		model.rotateAngleY = y;
		model.rotateAngleZ = z;
	}
}