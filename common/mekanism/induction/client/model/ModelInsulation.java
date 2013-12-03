package mekanism.induction.client.model;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.entity.Entity;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class ModelInsulation extends ModelBase
{
	// fields
	ModelRenderer Middle;
	ModelRenderer ToBLeft;
	ModelRenderer BoBLeft;
	ModelRenderer ToBRight;
	ModelRenderer BoBRight;
	ModelRenderer BoBFront;
	ModelRenderer BoBBack;
	ModelRenderer ToBFront;
	ModelRenderer ToBBack;
	ModelRenderer BoTLeft;
	ModelRenderer ToTRight;
	ModelRenderer BoTRight;
	ModelRenderer BoTFront;
	ModelRenderer ToTLeft;
	ModelRenderer BoTBack;
	ModelRenderer ToTBack;
	ModelRenderer ToTFront;
	ModelRenderer LoFFront;
	ModelRenderer BoFBack;
	ModelRenderer RoFFront;
	ModelRenderer BoFFront;
	ModelRenderer ToFBack;
	ModelRenderer ToFFront;
	ModelRenderer RoFBack;
	ModelRenderer LoFBack;
	ModelRenderer BoBackBack;
	ModelRenderer ToBackBack;
	ModelRenderer RoBackBack;
	ModelRenderer RoBackFront;
	ModelRenderer LoBackFront;
	ModelRenderer BoBackFront;
	ModelRenderer ToBackFront;
	ModelRenderer LoBackFront2;
	ModelRenderer BToRLeft;
	ModelRenderer FoRRight;
	ModelRenderer ToRLeft;
	ModelRenderer BToRRight;
	ModelRenderer BoRLeft;
	ModelRenderer ToRRight;
	ModelRenderer FoRLeft;
	ModelRenderer BoRRight;
	ModelRenderer BoLRight;
	ModelRenderer BToLRight;
	ModelRenderer FoLLeft;
	ModelRenderer ToLRight;
	ModelRenderer FoLRight;
	ModelRenderer BoLLeft;
	ModelRenderer ToLLeft;
	ModelRenderer BToLLeft;

	public ModelInsulation()
	{
		textureWidth = 128;
		textureHeight = 128;

		Middle = new ModelRenderer(this, 0, 0);
		Middle.addBox(0F, 0F, 0F, 6, 6, 6);
		Middle.setRotationPoint(-3F, 13F, -3F);
		Middle.setTextureSize(128, 128);
		Middle.mirror = true;
		setRotation(Middle, 0F, 0F, 0F);
		ToBLeft = new ModelRenderer(this, 25, 0);
		ToBLeft.addBox(0F, 0F, 0F, 1, 2, 4);
		ToBLeft.setRotationPoint(-3F, 19F, -2F);
		ToBLeft.setTextureSize(128, 128);
		ToBLeft.mirror = true;
		setRotation(ToBLeft, 0F, 0F, 0F);
		BoBLeft = new ModelRenderer(this, 25, 0);
		BoBLeft.addBox(0F, 0F, 0F, 1, 2, 4);
		BoBLeft.setRotationPoint(-3F, 22F, -2F);
		BoBLeft.setTextureSize(128, 128);
		BoBLeft.mirror = true;
		setRotation(BoBLeft, 0F, 0F, 0F);
		ToBRight = new ModelRenderer(this, 36, 0);
		ToBRight.addBox(0F, 0F, 0F, 1, 2, 4);
		ToBRight.setRotationPoint(2F, 19F, -2F);
		ToBRight.setTextureSize(128, 128);
		ToBRight.mirror = true;
		setRotation(ToBRight, 0F, 0F, 0F);
		BoBRight = new ModelRenderer(this, 36, 0);
		BoBRight.addBox(0F, 0F, 0F, 1, 2, 4);
		BoBRight.setRotationPoint(2F, 22F, -2F);
		BoBRight.setTextureSize(128, 128);
		BoBRight.mirror = true;
		setRotation(BoBRight, 0F, 0F, 0F);
		BoBFront = new ModelRenderer(this, 25, 7);
		BoBFront.addBox(0F, 0F, 0F, 6, 2, 1);
		BoBFront.setRotationPoint(-3F, 22F, -3F);
		BoBFront.setTextureSize(128, 128);
		BoBFront.mirror = true;
		setRotation(BoBFront, 0F, 0F, 0F);
		BoBBack = new ModelRenderer(this, 40, 7);
		BoBBack.addBox(0F, 0F, 0F, 6, 2, 1);
		BoBBack.setRotationPoint(-3F, 22F, 2F);
		BoBBack.setTextureSize(128, 128);
		BoBBack.mirror = true;
		setRotation(BoBBack, 0F, 0F, 0F);
		ToBFront = new ModelRenderer(this, 25, 7);
		ToBFront.addBox(0F, 0F, 0F, 6, 2, 1);
		ToBFront.setRotationPoint(-3F, 19F, -3F);
		ToBFront.setTextureSize(128, 128);
		ToBFront.mirror = true;
		setRotation(ToBFront, 0F, 0F, 0F);
		ToBBack = new ModelRenderer(this, 40, 7);
		ToBBack.addBox(0F, 0F, 0F, 6, 2, 1);
		ToBBack.setRotationPoint(-3F, 19F, 2F);
		ToBBack.setTextureSize(128, 128);
		ToBBack.mirror = true;
		setRotation(ToBBack, 0F, 0F, 0F);
		BoTLeft = new ModelRenderer(this, 57, 0);
		BoTLeft.addBox(0F, 0F, 0F, 1, 2, 4);
		BoTLeft.setRotationPoint(-3F, 11F, -2F);
		BoTLeft.setTextureSize(128, 128);
		BoTLeft.mirror = true;
		setRotation(BoTLeft, 0F, 0F, 0F);
		ToTRight = new ModelRenderer(this, 68, 0);
		ToTRight.addBox(0F, 0F, 0F, 1, 2, 4);
		ToTRight.setRotationPoint(2F, 8F, -2F);
		ToTRight.setTextureSize(128, 128);
		ToTRight.mirror = true;
		setRotation(ToTRight, 0F, 0F, 0F);
		BoTRight = new ModelRenderer(this, 68, 0);
		BoTRight.addBox(0F, 0F, 0F, 1, 2, 4);
		BoTRight.setRotationPoint(2F, 11F, -2F);
		BoTRight.setTextureSize(128, 128);
		BoTRight.mirror = true;
		setRotation(BoTRight, 0F, 0F, 0F);
		BoTFront = new ModelRenderer(this, 57, 7);
		BoTFront.addBox(0F, 0F, 0F, 6, 2, 1);
		BoTFront.setRotationPoint(-3F, 11F, -3F);
		BoTFront.setTextureSize(128, 128);
		BoTFront.mirror = true;
		setRotation(BoTFront, 0F, 0F, 0F);
		ToTLeft = new ModelRenderer(this, 57, 0);
		ToTLeft.addBox(0F, 0F, 0F, 1, 2, 4);
		ToTLeft.setRotationPoint(-3F, 8F, -2F);
		ToTLeft.setTextureSize(128, 128);
		ToTLeft.mirror = true;
		setRotation(ToTLeft, 0F, 0F, 0F);
		BoTBack = new ModelRenderer(this, 72, 7);
		BoTBack.addBox(0F, 0F, 0F, 6, 2, 1);
		BoTBack.setRotationPoint(-3F, 11F, 2F);
		BoTBack.setTextureSize(128, 128);
		BoTBack.mirror = true;
		setRotation(BoTBack, 0F, 0F, 0F);
		ToTBack = new ModelRenderer(this, 72, 7);
		ToTBack.addBox(0F, 0F, 0F, 6, 2, 1);
		ToTBack.setRotationPoint(-3F, 8F, 2F);
		ToTBack.setTextureSize(128, 128);
		ToTBack.mirror = true;
		setRotation(ToTBack, 0F, 0F, 0F);
		ToTFront = new ModelRenderer(this, 57, 7);
		ToTFront.addBox(0F, 0F, 0F, 6, 2, 1);
		ToTFront.setRotationPoint(-3F, 8F, -3F);
		ToTFront.setTextureSize(128, 128);
		ToTFront.mirror = true;
		setRotation(ToTFront, 0F, 0F, 0F);
		LoFFront = new ModelRenderer(this, 25, 14);
		LoFFront.addBox(0F, 0F, 0F, 1, 4, 2);
		LoFFront.setRotationPoint(-3F, 14F, -8F);
		LoFFront.setTextureSize(128, 128);
		LoFFront.mirror = true;
		setRotation(LoFFront, 0F, 0F, 0F);
		BoFBack = new ModelRenderer(this, 32, 14);
		BoFBack.addBox(0F, 0F, 0F, 6, 1, 2);
		BoFBack.setRotationPoint(-3F, 18F, -5F);
		BoFBack.setTextureSize(128, 128);
		BoFBack.mirror = true;
		setRotation(BoFBack, 0F, 0F, 0F);
		RoFFront = new ModelRenderer(this, 25, 21);
		RoFFront.addBox(0F, 0F, 0F, 1, 4, 2);
		RoFFront.setRotationPoint(2F, 14F, -8F);
		RoFFront.setTextureSize(128, 128);
		RoFFront.mirror = true;
		setRotation(RoFFront, 0F, 0F, 0F);
		BoFFront = new ModelRenderer(this, 32, 14);
		BoFFront.addBox(0F, 0F, 0F, 6, 1, 2);
		BoFFront.setRotationPoint(-3F, 18F, -8F);
		BoFFront.setTextureSize(128, 128);
		BoFFront.mirror = true;
		setRotation(BoFFront, 0F, 0F, 0F);
		ToFBack = new ModelRenderer(this, 32, 18);
		ToFBack.addBox(0F, 0F, 0F, 6, 1, 2);
		ToFBack.setRotationPoint(-3F, 13F, -5F);
		ToFBack.setTextureSize(128, 128);
		ToFBack.mirror = true;
		setRotation(ToFBack, 0F, 0F, 0F);
		ToFFront = new ModelRenderer(this, 32, 18);
		ToFFront.addBox(0F, 0F, 0F, 6, 1, 2);
		ToFFront.setRotationPoint(-3F, 13F, -8F);
		ToFFront.setTextureSize(128, 128);
		ToFFront.mirror = true;
		setRotation(ToFFront, 0F, 0F, 0F);
		RoFBack = new ModelRenderer(this, 25, 21);
		RoFBack.addBox(0F, 0F, 0F, 1, 4, 2);
		RoFBack.setRotationPoint(2F, 14F, -5F);
		RoFBack.setTextureSize(128, 128);
		RoFBack.mirror = true;
		setRotation(RoFBack, 0F, 0F, 0F);
		LoFBack = new ModelRenderer(this, 25, 14);
		LoFBack.addBox(0F, 0F, 0F, 1, 4, 2);
		LoFBack.setRotationPoint(-3F, 14F, -5F);
		LoFBack.setTextureSize(128, 128);
		LoFBack.mirror = true;
		setRotation(LoFBack, 0F, 0F, 0F);
		BoBackBack = new ModelRenderer(this, 57, 14);
		BoBackBack.addBox(0F, 0F, 0F, 6, 1, 2);
		BoBackBack.setRotationPoint(-3F, 18F, 6F);
		BoBackBack.setTextureSize(128, 128);
		BoBackBack.mirror = true;
		setRotation(BoBackBack, 0F, 0F, 0F);
		ToBackBack = new ModelRenderer(this, 57, 18);
		ToBackBack.addBox(0F, 0F, 0F, 6, 1, 2);
		ToBackBack.setRotationPoint(-3F, 13F, 6F);
		ToBackBack.setTextureSize(128, 128);
		ToBackBack.mirror = true;
		setRotation(ToBackBack, 0F, 0F, 0F);
		RoBackBack = new ModelRenderer(this, 74, 14);
		RoBackBack.addBox(0F, 0F, 0F, 1, 4, 2);
		RoBackBack.setRotationPoint(-3F, 14F, 6F);
		RoBackBack.setTextureSize(128, 128);
		RoBackBack.mirror = true;
		setRotation(RoBackBack, 0F, 0F, 0F);
		RoBackFront = new ModelRenderer(this, 74, 14);
		RoBackFront.addBox(0F, 0F, 0F, 1, 4, 2);
		RoBackFront.setRotationPoint(-3F, 14F, 3F);
		RoBackFront.setTextureSize(128, 128);
		RoBackFront.mirror = true;
		setRotation(RoBackFront, 0F, 0F, 0F);
		LoBackFront = new ModelRenderer(this, 74, 21);
		LoBackFront.addBox(0F, 0F, 0F, 1, 4, 2);
		LoBackFront.setRotationPoint(2F, 14F, 3F);
		LoBackFront.setTextureSize(128, 128);
		LoBackFront.mirror = true;
		setRotation(LoBackFront, 0F, 0F, 0F);
		BoBackFront = new ModelRenderer(this, 57, 14);
		BoBackFront.addBox(0F, 0F, 0F, 6, 1, 2);
		BoBackFront.setRotationPoint(-3F, 18F, 3F);
		BoBackFront.setTextureSize(128, 128);
		BoBackFront.mirror = true;
		setRotation(BoBackFront, 0F, 0F, 0F);
		ToBackFront = new ModelRenderer(this, 57, 18);
		ToBackFront.addBox(0F, 0F, 0F, 6, 1, 2);
		ToBackFront.setRotationPoint(-3F, 13F, 3F);
		ToBackFront.setTextureSize(128, 128);
		ToBackFront.mirror = true;
		setRotation(ToBackFront, 0F, 0F, 0F);
		LoBackFront2 = new ModelRenderer(this, 74, 21);
		LoBackFront2.addBox(0F, 0F, 0F, 1, 4, 2);
		LoBackFront2.setRotationPoint(2F, 14F, 6F);
		LoBackFront2.setTextureSize(128, 128);
		LoBackFront2.mirror = true;
		setRotation(LoBackFront2, 0F, 0F, 0F);
		BToRLeft = new ModelRenderer(this, 0, 30);
		BToRLeft.addBox(0F, 0F, 0F, 2, 1, 4);
		BToRLeft.setRotationPoint(3F, 18F, -2F);
		BToRLeft.setTextureSize(128, 128);
		BToRLeft.mirror = true;
		setRotation(BToRLeft, 0F, 0F, 0F);
		FoRRight = new ModelRenderer(this, 0, 22);
		FoRRight.addBox(0F, 0F, 0F, 2, 6, 1);
		FoRRight.setRotationPoint(6F, 13F, -3F);
		FoRRight.setTextureSize(128, 128);
		FoRRight.mirror = true;
		setRotation(FoRRight, 0F, 0F, 0F);
		ToRLeft = new ModelRenderer(this, 0, 36);
		ToRLeft.addBox(0F, 0F, 0F, 2, 1, 4);
		ToRLeft.setRotationPoint(3F, 13F, -2F);
		ToRLeft.setTextureSize(128, 128);
		ToRLeft.mirror = true;
		setRotation(ToRLeft, 0F, 0F, 0F);
		BToRRight = new ModelRenderer(this, 0, 30);
		BToRRight.addBox(0F, 0F, 0F, 2, 1, 4);
		BToRRight.setRotationPoint(6F, 18F, -2F);
		BToRRight.setTextureSize(128, 128);
		BToRRight.mirror = true;
		setRotation(BToRRight, 0F, 0F, 0F);
		BoRLeft = new ModelRenderer(this, 7, 22);
		BoRLeft.addBox(0F, 0F, 0F, 2, 6, 1);
		BoRLeft.setRotationPoint(3F, 13F, 2F);
		BoRLeft.setTextureSize(128, 128);
		BoRLeft.mirror = true;
		setRotation(BoRLeft, 0F, 0F, 0F);
		ToRRight = new ModelRenderer(this, 0, 36);
		ToRRight.addBox(0F, 0F, 0F, 2, 1, 4);
		ToRRight.setRotationPoint(6F, 13F, -2F);
		ToRRight.setTextureSize(128, 128);
		ToRRight.mirror = true;
		setRotation(ToRRight, 0F, 0F, 0F);
		FoRLeft = new ModelRenderer(this, 0, 22);
		FoRLeft.addBox(0F, 0F, 0F, 2, 6, 1);
		FoRLeft.setRotationPoint(3F, 13F, -3F);
		FoRLeft.setTextureSize(128, 128);
		FoRLeft.mirror = true;
		setRotation(FoRLeft, 0F, 0F, 0F);
		BoRRight = new ModelRenderer(this, 7, 22);
		BoRRight.addBox(0F, 0F, 0F, 2, 6, 1);
		BoRRight.setRotationPoint(6F, 13F, 2F);
		BoRRight.setTextureSize(128, 128);
		BoRRight.mirror = true;
		setRotation(BoRRight, 0F, 0F, 0F);
		BoLRight = new ModelRenderer(this, 0, 45);
		BoLRight.addBox(0F, 0F, 0F, 2, 6, 1);
		BoLRight.setRotationPoint(-5F, 13F, 2F);
		BoLRight.setTextureSize(128, 128);
		BoLRight.mirror = true;
		setRotation(BoLRight, 0F, 0F, 0F);
		BToLRight = new ModelRenderer(this, 0, 53);
		BToLRight.addBox(0F, 0F, 0F, 2, 1, 4);
		BToLRight.setRotationPoint(-5F, 18F, -2F);
		BToLRight.setTextureSize(128, 128);
		BToLRight.mirror = true;
		setRotation(BToLRight, 0F, 0F, 0F);
		FoLLeft = new ModelRenderer(this, 7, 45);
		FoLLeft.addBox(0F, 0F, 0F, 2, 6, 1);
		FoLLeft.setRotationPoint(-8F, 13F, -3F);
		FoLLeft.setTextureSize(128, 128);
		FoLLeft.mirror = true;
		setRotation(FoLLeft, 0F, 0F, 0F);
		ToLRight = new ModelRenderer(this, 0, 59);
		ToLRight.addBox(0F, 0F, 0F, 2, 1, 4);
		ToLRight.setRotationPoint(-5F, 13F, -2F);
		ToLRight.setTextureSize(128, 128);
		ToLRight.mirror = true;
		setRotation(ToLRight, 0F, 0F, 0F);
		FoLRight = new ModelRenderer(this, 7, 45);
		FoLRight.addBox(0F, 0F, 0F, 2, 6, 1);
		FoLRight.setRotationPoint(-5F, 13F, -3F);
		FoLRight.setTextureSize(128, 128);
		FoLRight.mirror = true;
		setRotation(FoLRight, 0F, 0F, 0F);
		BoLLeft = new ModelRenderer(this, 0, 45);
		BoLLeft.addBox(0F, 0F, 0F, 2, 6, 1);
		BoLLeft.setRotationPoint(-8F, 13F, 2F);
		BoLLeft.setTextureSize(128, 128);
		BoLLeft.mirror = true;
		setRotation(BoLLeft, 0F, 0F, 0F);
		ToLLeft = new ModelRenderer(this, 0, 59);
		ToLLeft.addBox(0F, 0F, 0F, 2, 1, 4);
		ToLLeft.setRotationPoint(-8F, 13F, -2F);
		ToLLeft.setTextureSize(128, 128);
		ToLLeft.mirror = true;
		setRotation(ToLLeft, 0F, 0F, 0F);
		BToLLeft = new ModelRenderer(this, 0, 53);
		BToLLeft.addBox(0F, 0F, 0F, 2, 1, 4);
		BToLLeft.setRotationPoint(-8F, 18F, -2F);
		BToLLeft.setTextureSize(128, 128);
		BToLLeft.mirror = true;
		setRotation(BToLLeft, 0F, 0F, 0F);
	}

	public void renderMiddle(float f5)
	{
		Middle.render(f5);
	}

	public void renderBottom(float f5)
	{
		ToBLeft.render(f5);
		BoBLeft.render(f5);
		ToBRight.render(f5);
		BoBRight.render(f5);
		BoBFront.render(f5);
		BoBBack.render(f5);
		ToBFront.render(f5);
		ToBBack.render(f5);
	}

	public void renderTop(float f5)
	{
		BoTLeft.render(f5);
		ToTRight.render(f5);
		BoTRight.render(f5);
		BoTFront.render(f5);
		ToTLeft.render(f5);
		BoTBack.render(f5);
		ToTBack.render(f5);
		ToTFront.render(f5);
	}

	public void renderLeft(float f5)
	{
		BoLRight.render(f5);
		BToLRight.render(f5);
		FoLLeft.render(f5);
		ToLRight.render(f5);
		FoLRight.render(f5);
		BoLLeft.render(f5);
		ToLLeft.render(f5);
		BToLLeft.render(f5);
	}

	public void renderRight(float f5)
	{
		BToRLeft.render(f5);
		FoRRight.render(f5);
		ToRLeft.render(f5);
		BToRRight.render(f5);
		BoRLeft.render(f5);
		ToRRight.render(f5);
		FoRLeft.render(f5);
		BoRRight.render(f5);
	}

	public void renderBack(float f5)
	{
		BoBackBack.render(f5);
		ToBackBack.render(f5);
		RoBackBack.render(f5);
		RoBackFront.render(f5);
		LoBackFront.render(f5);
		BoBackFront.render(f5);
		ToBackFront.render(f5);
		LoBackFront2.render(f5);
	}

	public void renderFront(float f5)
	{
		LoFFront.render(f5);
		BoFBack.render(f5);
		RoFFront.render(f5);
		BoFFront.render(f5);
		ToFBack.render(f5);
		ToFFront.render(f5);
		RoFBack.render(f5);
		LoFBack.render(f5);

	}

	private void setRotation(ModelRenderer model, float x, float y, float z)
	{
		model.rotateAngleX = x;
		model.rotateAngleY = y;
		model.rotateAngleZ = z;
	}

	@Override
	public void setRotationAngles(float x, float y, float z, float f3, float f4, float f5, Entity entity)
	{
		super.setRotationAngles(x, y, z, f3, f4, f5, entity);
	}
}
