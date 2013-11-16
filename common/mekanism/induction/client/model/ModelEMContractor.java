package mekanism.induction.client.model;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;

public class ModelEMContractor extends ModelBase
{
	public boolean doSpin;

	// fields
	ModelRenderer frame1;
	ModelRenderer frame2;
	ModelRenderer frame3;
	ModelRenderer frame4;
	ModelRenderer frame5;
	ModelRenderer frame6;
	ModelRenderer frame7;
	ModelRenderer frame8;
	ModelRenderer left_frame_connector;
	ModelRenderer right_frame_connector;
	ModelRenderer teslapole;
	ModelRenderer Coil1;
	ModelRenderer coil2;
	ModelRenderer coil3;
	ModelRenderer coil4;
	ModelRenderer pole1;
	ModelRenderer pole2;
	ModelRenderer pole3;
	ModelRenderer pole4;
	ModelRenderer poletop1;
	ModelRenderer poletop2;
	ModelRenderer poletop3;
	ModelRenderer poletop4;
	ModelRenderer base1;
	ModelRenderer base2;
	ModelRenderer base3;

	public ModelEMContractor(boolean spin)
	{
		doSpin = spin;
		textureWidth = 128;
		textureHeight = 128;

		frame1 = new ModelRenderer(this, 0, 24);
		frame1.addBox(0F, 0F, 0F, 1, 8, 1);
		frame1.setRotationPoint(-3F, 15F, -2F);
		frame1.setTextureSize(128, 128);
		frame1.mirror = true;
		setRotation(frame1, 0F, 0F, 0F);
		frame2 = new ModelRenderer(this, 0, 24);
		frame2.addBox(0F, 0F, 0F, 1, 8, 1);
		frame2.setRotationPoint(1F, 15F, 2F);
		frame2.setTextureSize(128, 128);
		frame2.mirror = true;
		setRotation(frame2, 0F, 0F, 0F);
		frame3 = new ModelRenderer(this, 0, 24);
		frame3.addBox(0F, 0F, 0F, 1, 8, 1);
		frame3.setRotationPoint(2F, 15F, -2F);
		frame3.setTextureSize(128, 128);
		frame3.mirror = true;
		setRotation(frame3, 0F, 0F, 0F);
		frame4 = new ModelRenderer(this, 0, 24);
		frame4.addBox(0F, 0F, 0F, 1, 8, 1);
		frame4.setRotationPoint(-3F, 15F, 1F);
		frame4.setTextureSize(128, 128);
		frame4.mirror = true;
		setRotation(frame4, 0F, 0F, 0F);
		frame5 = new ModelRenderer(this, 0, 24);
		frame5.addBox(0F, 0F, 0F, 1, 8, 1);
		frame5.setRotationPoint(2F, 15F, 1F);
		frame5.setTextureSize(128, 128);
		frame5.mirror = true;
		setRotation(frame5, 0F, 0F, 0F);
		frame6 = new ModelRenderer(this, 0, 24);
		frame6.addBox(0F, 0F, 0F, 1, 8, 1);
		frame6.setRotationPoint(1F, 15F, -3F);
		frame6.setTextureSize(128, 128);
		frame6.mirror = true;
		setRotation(frame6, 0F, 0F, 0F);
		frame7 = new ModelRenderer(this, 0, 24);
		frame7.addBox(0F, 0F, 0F, 1, 8, 1);
		frame7.setRotationPoint(-2F, 15F, 2F);
		frame7.setTextureSize(128, 128);
		frame7.mirror = true;
		setRotation(frame7, 0F, 0F, 0F);
		frame8 = new ModelRenderer(this, 0, 24);
		frame8.addBox(0F, 0F, 0F, 1, 8, 1);
		frame8.setRotationPoint(-2F, 15F, -3F);
		frame8.setTextureSize(128, 128);
		frame8.mirror = true;
		setRotation(frame8, 0F, 0F, 0F);
		left_frame_connector = new ModelRenderer(this, 0, 20);
		left_frame_connector.addBox(0F, 0F, 0F, 1, 1, 2);
		left_frame_connector.setRotationPoint(-3F, 15F, -1F);
		left_frame_connector.setTextureSize(128, 128);
		left_frame_connector.mirror = true;
		setRotation(left_frame_connector, 0F, 0F, 0F);
		right_frame_connector = new ModelRenderer(this, 0, 20);
		right_frame_connector.addBox(0F, 0F, 0F, 1, 1, 2);
		right_frame_connector.setRotationPoint(2F, 15F, -1F);
		right_frame_connector.setTextureSize(128, 128);
		right_frame_connector.mirror = true;
		setRotation(right_frame_connector, 0F, 0F, 0F);
		teslapole = new ModelRenderer(this, 0, 0);
		teslapole.addBox(-1F, -1F, -1F, 2, 15, 2);
		teslapole.setRotationPoint(0F, 9F, 0F);
		teslapole.setTextureSize(128, 128);
		teslapole.mirror = true;
		setRotation(teslapole, 0F, 0F, 0F);
		Coil1 = new ModelRenderer(this, 17, 0);
		Coil1.addBox(-1.5F, -0.5F, -1.5F, 3, 1, 3);
		Coil1.setRotationPoint(0F, 12.5F, 0F);
		Coil1.setTextureSize(128, 128);
		Coil1.mirror = true;
		setRotation(Coil1, 0F, 0F, 0F);
		coil2 = new ModelRenderer(this, 17, 0);
		coil2.addBox(-1.5F, -0.5F, -1.5F, 3, 1, 3);
		coil2.setRotationPoint(0F, 14F, 0F);
		coil2.setTextureSize(128, 128);
		coil2.mirror = true;
		setRotation(coil2, 0F, 0F, 0F);
		coil3 = new ModelRenderer(this, 17, 0);
		coil3.addBox(-1.5F, -0.5F, -1.5F, 3, 1, 3);
		coil3.setRotationPoint(0F, 9.5F, 0F);
		coil3.setTextureSize(128, 128);
		coil3.mirror = true;
		setRotation(coil3, 0F, 0F, 0F);
		coil4 = new ModelRenderer(this, 17, 0);
		coil4.addBox(-1.5F, -0.5F, -1.5F, 3, 1, 3);
		coil4.setRotationPoint(0F, 11F, 0F);
		coil4.setTextureSize(128, 128);
		coil4.mirror = true;
		setRotation(coil4, 0F, 0F, 0F);
		pole1 = new ModelRenderer(this, 5, 26);
		pole1.addBox(-0.5F, -1F, -0.5F, 1, 6, 1);
		pole1.setRotationPoint(0F, 18F, 6.5F);
		pole1.setTextureSize(128, 128);
		pole1.mirror = true;
		setRotation(pole1, 0F, 0F, 0F);
		pole2 = new ModelRenderer(this, 5, 26);
		pole2.addBox(-0.5F, -1F, -0.5F, 1, 6, 1);
		pole2.setRotationPoint(0F, 18F, -6.5F);
		pole2.setTextureSize(128, 128);
		pole2.mirror = true;
		setRotation(pole2, 0F, 0F, 0F);
		pole3 = new ModelRenderer(this, 5, 26);
		pole3.addBox(-0.5F, -1F, -0.5F, 1, 6, 1);
		pole3.setRotationPoint(-6.5F, 18F, 0F);
		pole3.setTextureSize(128, 128);
		pole3.mirror = true;
		setRotation(pole3, 0F, 0F, 0F);
		pole4 = new ModelRenderer(this, 5, 26);
		pole4.addBox(-0.5F, -1F, -0.5F, 1, 6, 1);
		pole4.setRotationPoint(6.5F, 18F, 0F);
		pole4.setTextureSize(128, 128);
		pole4.mirror = true;
		setRotation(pole4, 0F, 0F, 0F);
		poletop1 = new ModelRenderer(this, 31, 0);
		poletop1.addBox(-1F, -1F, -1F, 2, 2, 2);
		poletop1.setRotationPoint(0F, 16.5F, -6.5F);
		poletop1.setTextureSize(128, 128);
		poletop1.mirror = true;
		setRotation(poletop1, 0F, 0F, 0F);
		poletop2 = new ModelRenderer(this, 31, 0);
		poletop2.addBox(-1F, -1F, -1F, 2, 2, 2);
		poletop2.setRotationPoint(0F, 16.5F, 6.5F);
		poletop2.setTextureSize(128, 128);
		poletop2.mirror = true;
		setRotation(poletop2, 0F, 0F, 0F);
		poletop3 = new ModelRenderer(this, 31, 0);
		poletop3.addBox(-1F, -1F, -1F, 2, 2, 2);
		poletop3.setRotationPoint(6.5F, 16.5F, 0F);
		poletop3.setTextureSize(128, 128);
		poletop3.mirror = true;
		setRotation(poletop3, 0F, 0F, 0F);
		poletop4 = new ModelRenderer(this, 31, 0);
		poletop4.addBox(-1F, -1F, -1F, 2, 2, 2);
		poletop4.setRotationPoint(-6.5F, 16.5F, 0F);
		poletop4.setTextureSize(128, 128);
		poletop4.mirror = true;
		setRotation(poletop4, 0F, 0F, 0F);
		base1 = new ModelRenderer(this, 0, 55);
		base1.addBox(0F, 0F, 0F, 16, 1, 8);
		base1.setRotationPoint(-8F, 23F, -4F);
		base1.setTextureSize(128, 128);
		base1.mirror = true;
		setRotation(base1, 0F, 0F, 0F);
		base2 = new ModelRenderer(this, 0, 68);
		base2.addBox(0F, 0F, 0F, 8, 1, 5);
		base2.setRotationPoint(-4F, 23F, 3F);
		base2.setTextureSize(128, 128);
		base2.mirror = true;
		setRotation(base2, 0F, 0F, 0F);
		base3 = new ModelRenderer(this, 0, 79);
		base3.addBox(0F, 0F, 0F, 8, 1, 5);
		base3.setRotationPoint(-4F, 23F, -8F);
		base3.setTextureSize(128, 128);
		base3.mirror = true;
		setRotation(base3, 0F, 0F, 0F);
	}

	public void render(float f5)
	{
		if (doSpin)
		{
			Coil1.rotateAngleY = (float) Math.toRadians(Math.toDegrees(Coil1.rotateAngleY) + 3 < 360 ? Math.toDegrees(Coil1.rotateAngleY) + 3 : 0);
			coil2.rotateAngleY = (float) Math.toRadians(Math.toDegrees(coil2.rotateAngleY) + 3 < 360 ? Math.toDegrees(coil2.rotateAngleY) + 3 : 0);
			coil3.rotateAngleY = (float) Math.toRadians(Math.toDegrees(coil3.rotateAngleY) + 3 < 360 ? Math.toDegrees(coil3.rotateAngleY) + 3 : 0);
			coil4.rotateAngleY = (float) Math.toRadians(Math.toDegrees(coil4.rotateAngleY) + 3 < 360 ? Math.toDegrees(coil4.rotateAngleY) + 3 : 0);
		}

		frame1.render(f5);
		frame2.render(f5);
		frame3.render(f5);
		frame4.render(f5);
		frame5.render(f5);
		frame6.render(f5);
		frame7.render(f5);
		frame8.render(f5);
		left_frame_connector.render(f5);
		right_frame_connector.render(f5);
		teslapole.render(f5);
		Coil1.render(f5);
		coil2.render(f5);
		coil3.render(f5);
		coil4.render(f5);
		pole1.render(f5);
		pole2.render(f5);
		pole3.render(f5);
		pole4.render(f5);
		poletop1.render(f5);
		poletop2.render(f5);
		poletop3.render(f5);
		poletop4.render(f5);
		base1.render(f5);
		base2.render(f5);
		base3.render(f5);
	}

	private void setRotation(ModelRenderer model, float x, float y, float z)
	{
		model.rotateAngleX = x;
		model.rotateAngleY = y;
		model.rotateAngleZ = z;
	}
}
