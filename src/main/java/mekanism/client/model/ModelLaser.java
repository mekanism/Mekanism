package mekanism.client.model;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;

public class ModelLaser extends ModelBase 
{
	ModelRenderer LeftOuterRail;
	ModelRenderer LowerCache;
	ModelRenderer LeftInnerRail;
	ModelRenderer RightInnerRail;
	ModelRenderer RightOuterRail;
	ModelRenderer RightTopDecor;
	ModelRenderer Base;
	ModelRenderer LeftTopDecor;
	ModelRenderer RightBottomDecor;
	ModelRenderer LeftBottomDecor;
	ModelRenderer HigherCache;
	ModelRenderer LaserTip;
	ModelRenderer LaserCable1;
	ModelRenderer LaserCable2;
	ModelRenderer LaserCable3;
	ModelRenderer LaserCable4;
	ModelRenderer LaserBase;
	ModelRenderer LaserDecor;
	ModelRenderer LaserDecor2;

	public ModelLaser() 
	{
		textureWidth = 64;
		textureHeight = 64;

		LeftOuterRail = new ModelRenderer(this, 22, 29);
		LeftOuterRail.addBox(0F, 0F, 0F, 1, 2, 14);
		LeftOuterRail.setRotationPoint(-7F, 18F, -7F);
		LeftOuterRail.setTextureSize(64, 64);
		LeftOuterRail.mirror = true;
		setRotation(LeftOuterRail, 0F, 0F, 0F);
		LowerCache = new ModelRenderer(this, 0, 18);
		LowerCache.addBox(0F, -4F, 0F, 12, 1, 10);
		LowerCache.setRotationPoint(-6F, 23F, -5F);
		LowerCache.setTextureSize(64, 64);
		LowerCache.mirror = true;
		setRotation(LowerCache, 0F, 0F, 0F);
		LeftInnerRail = new ModelRenderer(this, 22, 29);
		LeftInnerRail.addBox(0F, 0F, 0F, 1, 2, 14);
		LeftInnerRail.setRotationPoint(-5F, 18F, -7F);
		LeftInnerRail.setTextureSize(64, 64);
		LeftInnerRail.mirror = true;
		setRotation(LeftInnerRail, 0F, 0F, 0F);
		RightInnerRail = new ModelRenderer(this, 22, 29);
		RightInnerRail.addBox(0F, 0F, 0F, 1, 2, 14);
		RightInnerRail.setRotationPoint(4F, 18F, -7F);
		RightInnerRail.setTextureSize(64, 64);
		RightInnerRail.mirror = true;
		setRotation(RightInnerRail, 0F, 0F, 0F);
		RightOuterRail = new ModelRenderer(this, 22, 29);
		RightOuterRail.addBox(0F, 0F, 0F, 1, 2, 14);
		RightOuterRail.setRotationPoint(6F, 18F, -7F);
		RightOuterRail.setTextureSize(64, 64);
		RightOuterRail.mirror = true;
		setRotation(RightOuterRail, 0F, 0F, 0F);
		RightTopDecor = new ModelRenderer(this, 0, 43);
		RightTopDecor.addBox(0F, 0F, 0F, 1, 1, 8);
		RightTopDecor.setRotationPoint(7F, 19F, -4F);
		RightTopDecor.setTextureSize(64, 64);
		RightTopDecor.mirror = true;
		setRotation(RightTopDecor, 0F, 0F, 0F);
		Base = new ModelRenderer(this, 0, 0);
		Base.addBox(0F, 0F, 0F, 14, 4, 14);
		Base.setRotationPoint(-7F, 20F, -7F);
		Base.setTextureSize(64, 64);
		Base.mirror = true;
		setRotation(Base, 0F, 0F, 0F);
		LeftTopDecor = new ModelRenderer(this, 0, 43);
		LeftTopDecor.addBox(0F, 0F, 0F, 1, 1, 8);
		LeftTopDecor.setRotationPoint(-8F, 19F, -4F);
		LeftTopDecor.setTextureSize(64, 64);
		LeftTopDecor.mirror = true;
		setRotation(LeftTopDecor, 0F, 0F, 0F);
		RightBottomDecor = new ModelRenderer(this, 0, 29);
		RightBottomDecor.addBox(0F, 0F, 0F, 1, 4, 10);
		RightBottomDecor.setRotationPoint(7F, 20F, -5F);
		RightBottomDecor.setTextureSize(64, 64);
		RightBottomDecor.mirror = true;
		setRotation(RightBottomDecor, 0F, 0F, 0F);
		LeftBottomDecor = new ModelRenderer(this, 0, 29);
		LeftBottomDecor.addBox(0F, 0F, 0F, 1, 4, 10);
		LeftBottomDecor.setRotationPoint(-8F, 20F, -5F);
		LeftBottomDecor.setTextureSize(64, 64);
		LeftBottomDecor.mirror = true;
		setRotation(LeftBottomDecor, 0F, 0F, 0F);
		HigherCache = new ModelRenderer(this, 0, 18);
		HigherCache.addBox(0F, -4F, 0F, 12, 1, 10);
		HigherCache.setRotationPoint(-6F, 21.5F, -5F);
		HigherCache.setTextureSize(64, 64);
		HigherCache.mirror = true;
		setRotation(HigherCache, 0F, 0F, 0F);
		LaserTip = new ModelRenderer(this, 30, 45);
		LaserTip.addBox(0F, -4F, 0F, 2, 3, 2);
		LaserTip.setRotationPoint(-1F, 13.5F, -1F);
		LaserTip.setTextureSize(64, 64);
		LaserTip.mirror = true;
		setRotation(LaserTip, 0F, 0F, 0F);
		LaserCable1 = new ModelRenderer(this, 22, 45);
		LaserCable1.addBox(0F, 0F, 0F, 1, 1, 3);
		LaserCable1.setRotationPoint(1F, 11.5F, -3F);
		LaserCable1.setTextureSize(64, 64);
		LaserCable1.mirror = true;
		setRotation(LaserCable1, 0F, 0F, 0F);
		LaserCable2 = new ModelRenderer(this, 18, 43);
		LaserCable2.addBox(0F, -4F, 0F, 1, 6, 1);
		LaserCable2.setRotationPoint(1F, 15.5F, -4F);
		LaserCable2.setTextureSize(64, 64);
		LaserCable2.mirror = true;
		setRotation(LaserCable2, 0F, 0F, 0F);
		LaserCable3 = new ModelRenderer(this, 22, 45);
		LaserCable3.addBox(0F, 0F, 0F, 1, 1, 3);
		LaserCable3.setRotationPoint(-2F, 11.5F, -3F);
		LaserCable3.setTextureSize(64, 64);
		LaserCable3.mirror = true;
		setRotation(LaserCable3, 0F, 0F, 0F);
		LaserCable4 = new ModelRenderer(this, 18, 43);
		LaserCable4.addBox(0F, -4F, 0F, 1, 6, 1);
		LaserCable4.setRotationPoint(-2F, 15.5F, -4F);
		LaserCable4.setTextureSize(64, 64);
		LaserCable4.mirror = true;
		setRotation(LaserCable4, 0F, 0F, 0F);
		LaserBase = new ModelRenderer(this, 0, 52);
		LaserBase.addBox(0F, -4F, 0F, 6, 5, 6);
		LaserBase.setRotationPoint(-3F, 16.5F, -3F);
		LaserBase.setTextureSize(64, 64);
		LaserBase.mirror = true;
		setRotation(LaserBase, 0F, 0F, 0F);
		LaserDecor = new ModelRenderer(this, 44, 18);
		LaserDecor.addBox(-0.5F, 0F, 0F, 1, 4, 4);
		LaserDecor.setRotationPoint(2.5F, 14.7F, -2F);
		LaserDecor.setTextureSize(64, 64);
		LaserDecor.mirror = true;
		setRotation(LaserDecor, 0F, 0F, -0.6320361F);
		LaserDecor2 = new ModelRenderer(this, 44, 18);
		LaserDecor2.addBox(-0.5F, 0F, 0F, 1, 4, 4);
		LaserDecor2.setRotationPoint(-2.5F, 14.7F, -2F);
		LaserDecor2.setTextureSize(64, 64);
		LaserDecor2.mirror = true;
		setRotation(LaserDecor2, 0F, 0F, 0.6320364F);
	}

	public void render(float size)
	{
		LeftOuterRail.render(size);
		LowerCache.render(size);
		LeftInnerRail.render(size);
		RightInnerRail.render(size);
		RightOuterRail.render(size);
		RightTopDecor.render(size);
		Base.render(size);
		LeftTopDecor.render(size);
		RightBottomDecor.render(size);
		LeftBottomDecor.render(size);
		HigherCache.render(size);
		LaserTip.render(size);
		LaserCable1.render(size);
		LaserCable2.render(size);
		LaserCable3.render(size);
		LaserCable4.render(size);
		LaserBase.render(size);
		LaserDecor.render(size);
		LaserDecor2.render(size);
	}

	private void setRotation(ModelRenderer model, float x, float y, float z) 
	{
		model.rotateAngleX = x;
		model.rotateAngleY = y;
		model.rotateAngleZ = z;
	}
}
