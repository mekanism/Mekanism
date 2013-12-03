package mekanism.induction.client.model;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class ModelTeslaMiddle extends ModelBase
{
	// fields
	ModelRenderer Base;
	ModelRenderer Collumn1;
	ModelRenderer Collumn2;
	ModelRenderer CrossCollumn1;
	ModelRenderer Collumn3;
	ModelRenderer Ball;
	ModelRenderer Plate;
	ModelRenderer TopBase;
	ModelRenderer FrontPole;
	ModelRenderer SidePole;
	ModelRenderer FrontAntennae;
	ModelRenderer BackAntennae;
	ModelRenderer LeftAntennae;
	ModelRenderer RightAntennae;
	ModelRenderer CrossCollumn2;
	ModelRenderer CrossCollumn3;
	ModelRenderer Collumn4;
	ModelRenderer CrossCollumn4;
	ModelRenderer BallStand;
	ModelRenderer FrontTopLeftPole;
	ModelRenderer FrontTopRightPole;
	ModelRenderer SideTopFirstPole;
	ModelRenderer SodeTopLastPole;

	public ModelTeslaMiddle()
	{
		textureWidth = 128;
		textureHeight = 128;

		Base = new ModelRenderer(this, 0, 0);
		Base.addBox(0F, 0F, 0F, 6, 6, 6);
		Base.setRotationPoint(-3F, 18F, -3F);
		Base.setTextureSize(128, 128);
		Base.mirror = true;
		setRotation(Base, 0F, 0F, 0F);
		Collumn1 = new ModelRenderer(this, 0, 20);
		Collumn1.addBox(0F, 0F, 0F, 1, 4, 1);
		Collumn1.setRotationPoint(-3F, 14F, -3F);
		Collumn1.setTextureSize(128, 128);
		Collumn1.mirror = true;
		setRotation(Collumn1, 0F, 0F, 0F);
		Collumn2 = new ModelRenderer(this, 0, 20);
		Collumn2.addBox(0F, 0F, 0F, 1, 4, 1);
		Collumn2.setRotationPoint(2F, 14F, -3F);
		Collumn2.setTextureSize(128, 128);
		Collumn2.mirror = true;
		setRotation(Collumn2, 0F, 0F, 0F);
		CrossCollumn1 = new ModelRenderer(this, 5, 20);
		CrossCollumn1.addBox(0F, 0F, 0F, 4, 1, 1);
		CrossCollumn1.setRotationPoint(-2F, 15.5F, -3F);
		CrossCollumn1.setTextureSize(128, 128);
		CrossCollumn1.mirror = true;
		setRotation(CrossCollumn1, 0F, 0F, 0F);
		Collumn3 = new ModelRenderer(this, 0, 20);
		Collumn3.addBox(0F, 0F, 0F, 1, 4, 1);
		Collumn3.setRotationPoint(2F, 14F, 2F);
		Collumn3.setTextureSize(128, 128);
		Collumn3.mirror = true;
		setRotation(Collumn3, 0F, 0F, 0F);
		Ball = new ModelRenderer(this, 0, 15);
		Ball.addBox(-1F, -1F, -1F, 2, 2, 2);
		Ball.setRotationPoint(0F, 16F, 0F);
		Ball.setTextureSize(128, 128);
		Ball.mirror = true;
		setRotation(Ball, 0F, 0F, 0F);
		Plate = new ModelRenderer(this, 25, 0);
		Plate.addBox(0F, 0F, 0F, 7, 1, 7);
		Plate.setRotationPoint(-3.5F, 13F, -3.5F);
		Plate.setTextureSize(128, 128);
		Plate.mirror = true;
		setRotation(Plate, 0F, 0F, 0F);
		TopBase = new ModelRenderer(this, 25, 9);
		TopBase.addBox(0F, 0F, 0F, 4, 5, 4);
		TopBase.setRotationPoint(-2F, 8F, -2F);
		TopBase.setTextureSize(128, 128);
		TopBase.mirror = true;
		setRotation(TopBase, 0F, 0F, 0F);
		FrontPole = new ModelRenderer(this, 0, 26);
		FrontPole.addBox(0F, 0F, 0F, 2, 2, 8);
		FrontPole.setRotationPoint(-1F, 20F, -4F);
		FrontPole.setTextureSize(128, 128);
		FrontPole.mirror = true;
		setRotation(FrontPole, 0F, 0F, 0F);
		SidePole = new ModelRenderer(this, 0, 37);
		SidePole.addBox(0F, 0F, 0F, 8, 2, 2);
		SidePole.setRotationPoint(-4F, 20F, -1F);
		SidePole.setTextureSize(128, 128);
		SidePole.mirror = true;
		setRotation(SidePole, 0F, 0F, 0F);
		FrontAntennae = new ModelRenderer(this, 25, 19);
		FrontAntennae.addBox(0F, 0F, 0F, 1, 3, 1);
		FrontAntennae.setRotationPoint(-0.5F, 18.8F, -4.466667F);
		FrontAntennae.setTextureSize(128, 128);
		FrontAntennae.mirror = true;
		setRotation(FrontAntennae, 0F, 0F, 0F);
		BackAntennae = new ModelRenderer(this, 25, 19);
		BackAntennae.addBox(0F, 0F, 0F, 1, 3, 1);
		BackAntennae.setRotationPoint(-0.5F, 18.8F, 3.533333F);
		BackAntennae.setTextureSize(128, 128);
		BackAntennae.mirror = true;
		setRotation(BackAntennae, 0F, 0F, 0F);
		LeftAntennae = new ModelRenderer(this, 25, 19);
		LeftAntennae.addBox(0F, 0F, 0F, 1, 3, 1);
		LeftAntennae.setRotationPoint(-4.5F, 18.8F, -0.5F);
		LeftAntennae.setTextureSize(128, 128);
		LeftAntennae.mirror = true;
		setRotation(LeftAntennae, 0F, 0F, 0F);
		RightAntennae = new ModelRenderer(this, 25, 19);
		RightAntennae.addBox(0F, 0F, 0F, 1, 3, 1);
		RightAntennae.setRotationPoint(3.5F, 18.8F, -0.5F);
		RightAntennae.setTextureSize(128, 128);
		RightAntennae.mirror = true;
		setRotation(RightAntennae, 0F, 0F, 0F);
		CrossCollumn2 = new ModelRenderer(this, 30, 19);
		CrossCollumn2.addBox(0F, 0F, 0F, 1, 1, 4);
		CrossCollumn2.setRotationPoint(2F, 15.5F, -2F);
		CrossCollumn2.setTextureSize(128, 128);
		CrossCollumn2.mirror = true;
		setRotation(CrossCollumn2, 0F, 0F, 0F);
		CrossCollumn3 = new ModelRenderer(this, 5, 20);
		CrossCollumn3.addBox(0F, 0F, 0F, 4, 1, 1);
		CrossCollumn3.setRotationPoint(-2F, 15.5F, 2F);
		CrossCollumn3.setTextureSize(128, 128);
		CrossCollumn3.mirror = true;
		setRotation(CrossCollumn3, 0F, 0F, 0F);
		Collumn4 = new ModelRenderer(this, 0, 20);
		Collumn4.addBox(0F, 0F, 0F, 1, 4, 1);
		Collumn4.setRotationPoint(-3F, 14F, 2F);
		Collumn4.setTextureSize(128, 128);
		Collumn4.mirror = true;
		setRotation(Collumn4, 0F, 0F, 0F);
		CrossCollumn4 = new ModelRenderer(this, 30, 19);
		CrossCollumn4.addBox(0F, 0F, 0F, 1, 1, 4);
		CrossCollumn4.setRotationPoint(-3F, 15.5F, -2F);
		CrossCollumn4.setTextureSize(128, 128);
		CrossCollumn4.mirror = true;
		setRotation(CrossCollumn4, 0F, 0F, 0F);
		BallStand = new ModelRenderer(this, 9, 16);
		BallStand.addBox(0F, 0F, 0F, 1, 1, 1);
		BallStand.setRotationPoint(-0.5F, 17F, -0.5F);
		BallStand.setTextureSize(128, 128);
		BallStand.mirror = true;
		setRotation(BallStand, 0F, 0F, 0F);
		FrontTopLeftPole = new ModelRenderer(this, 42, 9);
		FrontTopLeftPole.addBox(0F, 0F, 0F, 1, 4, 5);
		FrontTopLeftPole.setRotationPoint(-1.5F, 9F, -2.5F);
		FrontTopLeftPole.setTextureSize(128, 128);
		FrontTopLeftPole.mirror = true;
		setRotation(FrontTopLeftPole, 0F, 0F, 0F);
		FrontTopRightPole = new ModelRenderer(this, 42, 9);
		FrontTopRightPole.addBox(0F, 0F, 0F, 1, 4, 5);
		FrontTopRightPole.setRotationPoint(0.5F, 9F, -2.5F);
		FrontTopRightPole.setTextureSize(128, 128);
		FrontTopRightPole.mirror = true;
		setRotation(FrontTopRightPole, 0F, 0F, 0F);
		SideTopFirstPole = new ModelRenderer(this, 42, 19);
		SideTopFirstPole.addBox(0F, 0F, 0F, 5, 4, 1);
		SideTopFirstPole.setRotationPoint(-2.5F, 9F, -1.5F);
		SideTopFirstPole.setTextureSize(128, 128);
		SideTopFirstPole.mirror = true;
		setRotation(SideTopFirstPole, 0F, 0F, 0F);
		SodeTopLastPole = new ModelRenderer(this, 42, 19);
		SodeTopLastPole.addBox(0F, 0F, 0F, 5, 4, 1);
		SodeTopLastPole.setRotationPoint(-2.5F, 9F, 0.5F);
		SodeTopLastPole.setTextureSize(128, 128);
		SodeTopLastPole.mirror = true;
		setRotation(SodeTopLastPole, 0F, 0F, 0F);
	}

	public void render(float f5)
	{
		Base.render(f5);
		Collumn1.render(f5);
		Collumn2.render(f5);
		CrossCollumn1.render(f5);
		Collumn3.render(f5);
		Ball.render(f5);
		Plate.render(f5);
		TopBase.render(f5);
		FrontPole.render(f5);
		SidePole.render(f5);
		FrontAntennae.render(f5);
		BackAntennae.render(f5);
		LeftAntennae.render(f5);
		RightAntennae.render(f5);
		CrossCollumn2.render(f5);
		CrossCollumn3.render(f5);
		Collumn4.render(f5);
		CrossCollumn4.render(f5);
		BallStand.render(f5);
		FrontTopLeftPole.render(f5);
		FrontTopRightPole.render(f5);
		SideTopFirstPole.render(f5);
		SodeTopLastPole.render(f5);
	}

	private void setRotation(ModelRenderer model, float x, float y, float z)
	{
		model.rotateAngleX = x;
		model.rotateAngleY = y;
		model.rotateAngleZ = z;
	}

}
