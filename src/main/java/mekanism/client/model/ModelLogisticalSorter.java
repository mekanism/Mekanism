package mekanism.client.model;

import mekanism.client.render.MekanismRenderer;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class ModelLogisticalSorter extends ModelBase
{
	ModelRenderer LeftThing;
	ModelRenderer RightThing;
	ModelRenderer BottomPlate;
	ModelRenderer TopPlate;
	ModelRenderer LeftPlate;
	ModelRenderer RightPlate;
	ModelRenderer BR1Block1;
	ModelRenderer BL1Block1;
	ModelRenderer TL1Block1;
	ModelRenderer TR1Block1;
	ModelRenderer BR1Block2;
	ModelRenderer BL1Block2;
	ModelRenderer TL1Block2;
	ModelRenderer TR1Block2;
	ModelRenderer PoleBR;
	ModelRenderer PoleTL;
	ModelRenderer PoleTR;
	ModelRenderer PoleBL;
	ModelRenderer Base;
	ModelRenderer PipeBase;
	ModelRenderer DecorPlate;

	public ModelLogisticalSorter()
	{
		textureWidth = 128;
		textureHeight = 64;

		LeftThing = new ModelRenderer(this, 0, 29);
		LeftThing.addBox(0F, 0F, 0F, 1, 12, 1);
		LeftThing.setRotationPoint(5.5F, 10F, 5F);
		LeftThing.setTextureSize(128, 64);
		LeftThing.mirror = true;
		setRotation(LeftThing, 0F, 0F, 0F);
		RightThing = new ModelRenderer(this, 0, 29);
		RightThing.addBox(0F, 0F, 0F, 1, 12, 1);
		RightThing.setRotationPoint(5.5F, 10F, -6F);
		RightThing.setTextureSize(128, 64);
		RightThing.mirror = true;
		setRotation(RightThing, 0F, 0F, 0F);
		BottomPlate = new ModelRenderer(this, 60, 7);
		BottomPlate.addBox(0F, 0F, 0F, 12, 1, 4);
		BottomPlate.setRotationPoint(-6F, 18F, -2F);
		BottomPlate.setTextureSize(128, 64);
		BottomPlate.mirror = true;
		setRotation(BottomPlate, 0F, 0F, 0F);
		TopPlate = new ModelRenderer(this, 60, 7);
		TopPlate.addBox(0F, 0F, 0F, 12, 1, 4);
		TopPlate.setRotationPoint(-6F, 13F, -2F);
		TopPlate.setTextureSize(128, 64);
		TopPlate.mirror = true;
		setRotation(TopPlate, 0F, 0F, 0F);
		LeftPlate = new ModelRenderer(this, 33, 5);
		LeftPlate.addBox(0F, 0F, 0F, 12, 6, 1);
		LeftPlate.setRotationPoint(-6F, 13F, 2F);
		LeftPlate.setTextureSize(128, 64);
		LeftPlate.mirror = true;
		setRotation(LeftPlate, 0F, 0F, 0F);
		RightPlate = new ModelRenderer(this, 33, 5);
		RightPlate.addBox(0F, 0F, 0F, 12, 6, 1);
		RightPlate.setRotationPoint(-6F, 13F, -3F);
		RightPlate.setTextureSize(128, 64);
		RightPlate.mirror = true;
		setRotation(RightPlate, 0F, 0F, 0F);
		BR1Block1 = new ModelRenderer(this, 33, 0);
		BR1Block1.addBox(0F, 0F, 0F, 1, 2, 2);
		BR1Block1.setRotationPoint(4F, 17.5F, -3.5F);
		BR1Block1.setTextureSize(128, 64);
		BR1Block1.mirror = true;
		setRotation(BR1Block1, 0F, 0.0174533F, 0F);
		BL1Block1 = new ModelRenderer(this, 33, 0);
		BL1Block1.addBox(0F, 0F, 0F, 1, 2, 2);
		BL1Block1.setRotationPoint(4F, 17.5F, 1.473333F);
		BL1Block1.setTextureSize(128, 64);
		BL1Block1.mirror = true;
		setRotation(BL1Block1, 0F, 0F, 0F);
		TL1Block1 = new ModelRenderer(this, 33, 0);
		TL1Block1.addBox(0F, 0F, 0F, 1, 2, 2);
		TL1Block1.setRotationPoint(4F, 12.5F, 1.473333F);
		TL1Block1.setTextureSize(128, 64);
		TL1Block1.mirror = true;
		setRotation(TL1Block1, 0F, 0F, 0F);
		TR1Block1 = new ModelRenderer(this, 33, 0);
		TR1Block1.addBox(0F, 0F, 0F, 1, 2, 2);
		TR1Block1.setRotationPoint(4F, 12.5F, -3.5F);
		TR1Block1.setTextureSize(128, 64);
		TR1Block1.mirror = true;
		setRotation(TR1Block1, 0F, 0F, 0F);
		BR1Block2 = new ModelRenderer(this, 33, 0);
		BR1Block2.addBox(0F, 0F, 0F, 1, 2, 2);
		BR1Block2.setRotationPoint(-5F, 17.5F, -3.5F);
		BR1Block2.setTextureSize(128, 64);
		BR1Block2.mirror = true;
		setRotation(BR1Block2, 0F, 0F, 0F);
		BL1Block2 = new ModelRenderer(this, 33, 0);
		BL1Block2.addBox(0F, 0F, 0F, 1, 2, 2);
		BL1Block2.setRotationPoint(-5F, 17.5F, 1.473333F);
		BL1Block2.setTextureSize(128, 64);
		BL1Block2.mirror = true;
		setRotation(BL1Block2, 0F, 0F, 0F);
		TL1Block2 = new ModelRenderer(this, 33, 0);
		TL1Block2.addBox(0F, 0F, 0F, 1, 2, 2);
		TL1Block2.setRotationPoint(-5F, 12.5F, 1.473333F);
		TL1Block2.setTextureSize(128, 64);
		TL1Block2.mirror = true;
		setRotation(TL1Block2, 0F, 0F, 0F);
		TR1Block2 = new ModelRenderer(this, 33, 0);
		TR1Block2.addBox(0F, 0F, 0F, 1, 2, 2);
		TR1Block2.setRotationPoint(-5F, 12.5F, -3.5F);
		TR1Block2.setTextureSize(128, 64);
		TR1Block2.mirror = true;
		setRotation(TR1Block2, 0F, 0F, 0F);
		PoleBR = new ModelRenderer(this, 40, 0);
		PoleBR.addBox(0F, 0F, 0F, 8, 1, 1);
		PoleBR.setRotationPoint(-4F, 18.2F, 2.2F);
		PoleBR.setTextureSize(128, 64);
		PoleBR.mirror = true;
		setRotation(PoleBR, 0F, 0F, 0F);
		PoleTL = new ModelRenderer(this, 40, 0);
		PoleTL.addBox(0F, 0F, 0F, 8, 1, 1);
		PoleTL.setRotationPoint(-4F, 12.8F, -3.2F);
		PoleTL.setTextureSize(128, 64);
		PoleTL.mirror = true;
		setRotation(PoleTL, 0F, 0F, 0F);
		PoleTR = new ModelRenderer(this, 40, 0);
		PoleTR.addBox(0F, 0F, 0F, 8, 1, 1);
		PoleTR.setRotationPoint(-4F, 12.8F, 2.2F);
		PoleTR.setTextureSize(128, 64);
		PoleTR.mirror = true;
		setRotation(PoleTR, 0F, 0F, 0F);
		PoleBL = new ModelRenderer(this, 40, 0);
		PoleBL.addBox(0F, 0F, 0F, 8, 1, 1);
		PoleBL.setRotationPoint(-4F, 18.2F, -3.2F);
		PoleBL.setTextureSize(128, 64);
		PoleBL.mirror = true;
		setRotation(PoleBL, 0F, 0F, 0F);
		Base = new ModelRenderer(this, 0, 0);
		Base.addBox(0F, 0F, 0F, 2, 14, 14);
		Base.setRotationPoint(6F, 9F, -7F);
		Base.setTextureSize(128, 64);
		Base.mirror = true;
		setRotation(Base, 0F, 0F, 0F);
		PipeBase = new ModelRenderer(this, 33, 13);
		PipeBase.addBox(0F, 0F, 0F, 3, 8, 8);
		PipeBase.setRotationPoint(-8F, 12F, -4F);
		PipeBase.setTextureSize(128, 64);
		PipeBase.mirror = true;
		setRotation(PipeBase, 0F, 0F, 0F);
		DecorPlate = new ModelRenderer(this, 5, 29);
		DecorPlate.addBox(0F, 0F, 0F, 1, 8, 8);
		DecorPlate.setRotationPoint(5F, 12F, -4F);
		DecorPlate.setTextureSize(128, 64);
		DecorPlate.mirror = true;
		setRotation(DecorPlate, 0F, 0F, 0F);
	}

	public void render(float size, boolean active)
	{
		LeftThing.render(size);
		RightThing.render(size);
		BottomPlate.render(size);
		TopPlate.render(size);
		LeftPlate.render(size);
		RightPlate.render(size);

		if(active)
		{
			MekanismRenderer.glowOn();
		}

		BR1Block1.render(size);
		BL1Block1.render(size);
		TL1Block1.render(size);
		TR1Block1.render(size);
		BR1Block2.render(size);
		BL1Block2.render(size);
		TL1Block2.render(size);
		TR1Block2.render(size);

		if(active)
		{
			MekanismRenderer.glowOff();
		}

		PoleBR.render(size);
		PoleTL.render(size);
		PoleTR.render(size);
		PoleBL.render(size);
		Base.render(size);
		PipeBase.render(size);
		DecorPlate.render(size);
	}

	private void setRotation(ModelRenderer model, float x, float y, float z)
	{
		model.rotateAngleX = x;
		model.rotateAngleY = y;
		model.rotateAngleZ = z;
	}
}
