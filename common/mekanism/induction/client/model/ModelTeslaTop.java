package mekanism.induction.client.model;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;

public class ModelTeslaTop extends ModelBase
{
	// fields
	ModelRenderer Base;
	ModelRenderer Collumn1;
	ModelRenderer Collumn2;
	ModelRenderer Collumn3;
	ModelRenderer Collumn4;
	ModelRenderer CrossCollumn1;
	ModelRenderer CrossCollumn2;
	ModelRenderer CrossCollumn3;
	ModelRenderer CrossCollumn4;
	ModelRenderer CrossCollumn5;
	ModelRenderer CrossCollumn6;
	ModelRenderer CrossCollumn7;
	ModelRenderer CrossCollumn8;
	ModelRenderer HolderLeft;
	ModelRenderer FrontCoil;
	ModelRenderer RightCoil;
	ModelRenderer BackCoil;
	ModelRenderer LeftCoil;
	ModelRenderer LFSCoil;
	ModelRenderer RFSCoil;
	ModelRenderer RBSCoil;
	ModelRenderer LBSCoil;

	public ModelTeslaTop()
	{
		textureWidth = 128;
		textureHeight = 128;

		Base = new ModelRenderer(this, 0, 0);
		Base.addBox(0F, 0F, 0F, 4, 4, 4);
		Base.setRotationPoint(-2F, 20F, -2F);
		Base.setTextureSize(128, 128);
		Base.mirror = true;
		setRotation(Base, 0F, 0F, 0F);
		Collumn1 = new ModelRenderer(this, 0, 9);
		Collumn1.addBox(0F, 0F, 0F, 1, 6, 1);
		Collumn1.setRotationPoint(1F, 14F, 1F);
		Collumn1.setTextureSize(128, 128);
		Collumn1.mirror = true;
		setRotation(Collumn1, 0F, 0F, 0F);
		Collumn2 = new ModelRenderer(this, 0, 9);
		Collumn2.addBox(0F, 0F, 0F, 1, 6, 1);
		Collumn2.setRotationPoint(1F, 14F, -2F);
		Collumn2.setTextureSize(128, 128);
		Collumn2.mirror = true;
		setRotation(Collumn2, 0F, 0F, 0F);
		Collumn3 = new ModelRenderer(this, 0, 9);
		Collumn3.addBox(0F, 0F, 0F, 1, 6, 1);
		Collumn3.setRotationPoint(-2F, 14F, -2F);
		Collumn3.setTextureSize(128, 128);
		Collumn3.mirror = true;
		setRotation(Collumn3, 0F, 0F, 0F);
		Collumn4 = new ModelRenderer(this, 0, 9);
		Collumn4.addBox(0F, 0F, 0F, 1, 6, 1);
		Collumn4.setRotationPoint(-2F, 14F, 1F);
		Collumn4.setTextureSize(128, 128);
		Collumn4.mirror = true;
		setRotation(Collumn4, 0F, 0F, 0F);
		CrossCollumn1 = new ModelRenderer(this, 17, 0);
		CrossCollumn1.addBox(0F, 0F, 0F, 1, 1, 2);
		CrossCollumn1.setRotationPoint(-2F, 16.5F, -1F);
		CrossCollumn1.setTextureSize(128, 128);
		CrossCollumn1.mirror = true;
		setRotation(CrossCollumn1, 0F, 0F, 0F);
		CrossCollumn2 = new ModelRenderer(this, 17, 0);
		CrossCollumn2.addBox(0F, 0F, 0F, 2, 1, 1);
		CrossCollumn2.setRotationPoint(-1F, 15.5F, -2F);
		CrossCollumn2.setTextureSize(128, 128);
		CrossCollumn2.mirror = true;
		setRotation(CrossCollumn2, 0F, 0F, 0F);
		CrossCollumn3 = new ModelRenderer(this, 17, 0);
		CrossCollumn3.addBox(0F, 0F, 0F, 1, 1, 2);
		CrossCollumn3.setRotationPoint(-2F, 14.5F, -1F);
		CrossCollumn3.setTextureSize(128, 128);
		CrossCollumn3.mirror = true;
		setRotation(CrossCollumn3, 0F, 0F, 0F);
		CrossCollumn4 = new ModelRenderer(this, 17, 0);
		CrossCollumn4.addBox(0F, 0F, 0F, 1, 1, 2);
		CrossCollumn4.setRotationPoint(1F, 14.5F, -1F);
		CrossCollumn4.setTextureSize(128, 128);
		CrossCollumn4.mirror = true;
		setRotation(CrossCollumn4, 0F, 0F, 0F);
		CrossCollumn5 = new ModelRenderer(this, 17, 0);
		CrossCollumn5.addBox(0F, 0F, 0F, 1, 1, 2);
		CrossCollumn5.setRotationPoint(1F, 16.5F, -1F);
		CrossCollumn5.setTextureSize(128, 128);
		CrossCollumn5.mirror = true;
		setRotation(CrossCollumn5, 0F, 0F, 0F);
		CrossCollumn6 = new ModelRenderer(this, 17, 0);
		CrossCollumn6.addBox(0F, 0F, 0F, 2, 1, 1);
		CrossCollumn6.setRotationPoint(-1F, 15.5F, 1F);
		CrossCollumn6.setTextureSize(128, 128);
		CrossCollumn6.mirror = true;
		setRotation(CrossCollumn6, 0F, 0F, 0F);
		CrossCollumn7 = new ModelRenderer(this, 17, 0);
		CrossCollumn7.addBox(0F, 0F, 0F, 2, 1, 1);
		CrossCollumn7.setRotationPoint(-1F, 17.5F, -2F);
		CrossCollumn7.setTextureSize(128, 128);
		CrossCollumn7.mirror = true;
		setRotation(CrossCollumn7, 0F, 0F, 0F);
		CrossCollumn8 = new ModelRenderer(this, 17, 0);
		CrossCollumn8.addBox(0F, 0F, 0F, 2, 1, 1);
		CrossCollumn8.setRotationPoint(-1F, 17.5F, 1F);
		CrossCollumn8.setTextureSize(128, 128);
		CrossCollumn8.mirror = true;
		setRotation(CrossCollumn8, 0F, 0F, 0F);
		HolderLeft = new ModelRenderer(this, 5, 9);
		HolderLeft.addBox(0F, 0F, 0F, 2, 5, 2);
		HolderLeft.setRotationPoint(-1F, 10.5F, -1F);
		HolderLeft.setTextureSize(128, 128);
		HolderLeft.mirror = true;
		setRotation(HolderLeft, 0F, 0F, 0F);
		FrontCoil = new ModelRenderer(this, 26, 0);
		FrontCoil.addBox(0F, 0F, 0F, 6, 2, 1);
		FrontCoil.setRotationPoint(-3F, 11F, -4F);
		FrontCoil.setTextureSize(128, 128);
		FrontCoil.mirror = true;
		setRotation(FrontCoil, 0F, 0F, 0F);
		RightCoil = new ModelRenderer(this, 26, 4);
		RightCoil.addBox(0F, 0F, 0F, 1, 2, 6);
		RightCoil.setRotationPoint(3F, 11.02222F, -3F);
		RightCoil.setTextureSize(128, 128);
		RightCoil.mirror = true;
		setRotation(RightCoil, 0F, 0F, 0F);
		BackCoil = new ModelRenderer(this, 26, 0);
		BackCoil.addBox(0F, 0F, 0F, 6, 2, 1);
		BackCoil.setRotationPoint(-3F, 11F, 3F);
		BackCoil.setTextureSize(128, 128);
		BackCoil.mirror = true;
		setRotation(BackCoil, 0F, 0F, 0F);
		LeftCoil = new ModelRenderer(this, 26, 4);
		LeftCoil.addBox(0F, 0F, 0F, 1, 2, 6);
		LeftCoil.setRotationPoint(-4F, 11.02222F, -3F);
		LeftCoil.setTextureSize(128, 128);
		LeftCoil.mirror = true;
		setRotation(LeftCoil, 0F, 0F, 0F);
		LFSCoil = new ModelRenderer(this, 0, 20);
		LFSCoil.addBox(0F, 0F, 0F, 1, 2, 1);
		LFSCoil.setRotationPoint(-3F, 11F, -3F);
		LFSCoil.setTextureSize(128, 128);
		LFSCoil.mirror = true;
		setRotation(LFSCoil, 0F, 0F, 0F);
		RFSCoil = new ModelRenderer(this, 0, 20);
		RFSCoil.addBox(0F, 0F, 0F, 1, 2, 1);
		RFSCoil.setRotationPoint(2F, 11F, -3F);
		RFSCoil.setTextureSize(128, 128);
		RFSCoil.mirror = true;
		setRotation(RFSCoil, 0F, 0F, 0F);
		RBSCoil = new ModelRenderer(this, 0, 20);
		RBSCoil.addBox(0F, 0F, 0F, 1, 2, 1);
		RBSCoil.setRotationPoint(2F, 11F, 2F);
		RBSCoil.setTextureSize(128, 128);
		RBSCoil.mirror = true;
		setRotation(RBSCoil, 0F, 0F, 0F);
		LBSCoil = new ModelRenderer(this, 0, 20);
		LBSCoil.addBox(0F, 0F, 0F, 1, 2, 1);
		LBSCoil.setRotationPoint(-3F, 11F, 2F);
		LBSCoil.setTextureSize(128, 128);
		LBSCoil.mirror = true;
		setRotation(LBSCoil, 0F, 0F, 0F);
	}

	public void render(float f5)
	{
		Base.render(f5);
		Collumn1.render(f5);
		Collumn2.render(f5);
		Collumn3.render(f5);
		Collumn4.render(f5);
		CrossCollumn1.render(f5);
		CrossCollumn2.render(f5);
		CrossCollumn3.render(f5);
		CrossCollumn4.render(f5);
		CrossCollumn5.render(f5);
		CrossCollumn6.render(f5);
		CrossCollumn7.render(f5);
		CrossCollumn8.render(f5);
		HolderLeft.render(f5);
		FrontCoil.render(f5);
		RightCoil.render(f5);
		BackCoil.render(f5);
		LeftCoil.render(f5);
		LFSCoil.render(f5);
		RFSCoil.render(f5);
		RBSCoil.render(f5);
		LBSCoil.render(f5);
	}

	private void setRotation(ModelRenderer model, float x, float y, float z)
	{
		model.rotateAngleX = x;
		model.rotateAngleY = y;
		model.rotateAngleZ = z;
	}

}
