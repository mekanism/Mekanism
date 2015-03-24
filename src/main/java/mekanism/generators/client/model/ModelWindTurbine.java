package mekanism.generators.client.model;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;

public class ModelWindTurbine extends ModelBase
{
	ModelRenderer Base;
	ModelRenderer TowerFront;
	ModelRenderer TowerLeft;
	ModelRenderer TowerBack;
	ModelRenderer TowerRight;
	ModelRenderer TowerMoterFront;
	ModelRenderer TowerBaseMotor;
	ModelRenderer TowerBaseMotorBack;
	ModelRenderer TowerMotor;
	ModelRenderer Rotor;
	ModelRenderer RotorCover;
	ModelRenderer BladeBaseC;
	ModelRenderer BladeBaseB;
	ModelRenderer BladeBaseA;

	public ModelWindTurbine()
	{
		textureWidth = 128;
		textureHeight = 64;

		Base = new ModelRenderer(this, 0, 0);
		Base.addBox(-8F, 0F, -8F, 16, 6, 16);
		Base.setRotationPoint(0F, 18F, 0F);
		Base.setTextureSize(128, 64);
		Base.mirror = true;
		setRotation(Base, 0F, 0F, 0F);
		TowerFront = new ModelRenderer(this, 104, 0);
		TowerFront.addBox(-5F, -62F, -7F, 10, 63, 2);
		TowerFront.setRotationPoint(0F, 19F, 0F);
		TowerFront.setTextureSize(128, 64);
		TowerFront.mirror = true;
		setRotation(TowerFront, -0.0872665F, 0F, 0F);
		TowerLeft = new ModelRenderer(this, 104, 0);
		TowerLeft.addBox(-5F, -62F, 5F, 10, 63, 2);
		TowerLeft.setRotationPoint(0F, 19F, 0F);
		TowerLeft.setTextureSize(128, 64);
		TowerLeft.mirror = true;
		setRotation(TowerLeft, 0.0872665F, 1.570796F, 0F);
		TowerBack = new ModelRenderer(this, 104, 0);
		TowerBack.addBox(-5F, -62F, 5F, 10, 63, 2);
		TowerBack.setRotationPoint(0F, 19F, 0F);
		TowerBack.setTextureSize(128, 64);
		TowerBack.mirror = true;
		setRotation(TowerBack, 0.0872665F, 0F, 0F);
		TowerRight = new ModelRenderer(this, 104, 0);
		TowerRight.addBox(-5F, -62F, 5F, 10, 63, 2);
		TowerRight.setRotationPoint(0F, 19F, 0F);
		TowerRight.setTextureSize(128, 64);
		TowerRight.mirror = true;
		setRotation(TowerRight, 0.0872665F, -1.570796F, 0F);
		TowerMoterFront = new ModelRenderer(this, 40, 38);
		TowerMoterFront.addBox(-6F, -7.3F, -5F, 12, 9, 6);
		TowerMoterFront.setRotationPoint(0F, -45F, -1F);
		TowerMoterFront.setTextureSize(128, 64);
		TowerMoterFront.mirror = true;
		setRotation(TowerMoterFront, 0F, 0F, 0F);
		TowerBaseMotor = new ModelRenderer(this, 65, 0);
		TowerBaseMotor.addBox(-6F, -0.3F, 0F, 12, 2, 7);
		TowerBaseMotor.setRotationPoint(0F, -45F, 0F);
		TowerBaseMotor.setTextureSize(128, 64);
		TowerBaseMotor.mirror = true;
		setRotation(TowerBaseMotor, 0F, 0F, 0F);
		TowerBaseMotorBack = new ModelRenderer(this, 65, 30);
		TowerBaseMotorBack.addBox(-4F, -3.3F, 7F, 8, 4, 3);
		TowerBaseMotorBack.setRotationPoint(0F, -45F, 0F);
		TowerBaseMotorBack.setTextureSize(128, 64);
		TowerBaseMotorBack.mirror = true;
		setRotation(TowerBaseMotorBack, 0F, 0F, 0F);
		TowerMotor = new ModelRenderer(this, 65, 15);
		TowerMotor.addBox(-4F, -6.3F, 0F, 8, 6, 7);
		TowerMotor.setRotationPoint(0F, -45F, 0F);
		TowerMotor.setTextureSize(128, 64);
		TowerMotor.mirror = true;
		setRotation(TowerMotor, 0F, 0F, 0F);
		Rotor = new ModelRenderer(this, 88, 30);
		Rotor.addBox(-0.5F, -0.5F, 0F, 1, 1, 3);
		Rotor.setRotationPoint(0F, -48F, -8F);
		Rotor.setTextureSize(128, 64);
		Rotor.mirror = true;
		setRotation(Rotor, 0F, 0F, 0F);
		RotorCover = new ModelRenderer(this, 88, 35);
		RotorCover.addBox(-2F, -2F, -1F, 4, 4, 1);
		RotorCover.setRotationPoint(0F, -48F, -8F);
		RotorCover.setTextureSize(128, 64);
		RotorCover.mirror = true;
		setRotation(RotorCover, 0F, 0F, 0F);
		BladeBaseC = new ModelRenderer(this, 0, 54);
		BladeBaseC.addBox(1F, -1F, 0F, 32, 2, 1);
		BladeBaseC.setRotationPoint(0F, -48F, -8F);
		BladeBaseC.setTextureSize(128, 64);
		BladeBaseC.mirror = true;
		setRotation(BladeBaseC, 0F, 0F, getRotation(120));
		BladeBaseB = new ModelRenderer(this, 0, 54);
		BladeBaseB.addBox(1F, -1F, 0F, 32, 2, 1);
		BladeBaseB.setRotationPoint(0F, -48F, -8F);
		BladeBaseB.setTextureSize(128, 64);
		BladeBaseB.mirror = true;
		setRotation(BladeBaseB, 0F, 0F, 0F);
		BladeBaseA = new ModelRenderer(this, 0, 54);
		BladeBaseA.addBox(1F, -1F, 0F, 32, 2, 1);
		BladeBaseA.setRotationPoint(0F, -48F, -8F);
		BladeBaseA.setTextureSize(128, 64);
		BladeBaseA.mirror = true;
		setRotation(BladeBaseA, 0F, 0F, getRotation(240));
	}

	public void render(float size, double angle)
	{
		Base.render(size);
		TowerFront.render(size);
		TowerLeft.render(size);
		TowerBack.render(size);
		TowerRight.render(size);
		TowerMoterFront.render(size);
		TowerBaseMotor.render(size);
		TowerBaseMotorBack.render(size);
		TowerMotor.render(size);
		Rotor.render(size);
		RotorCover.render(size);
		setRotation(BladeBaseC, 0.0F, 0.0F, getRotation(getAbsoluteAngle(120 + angle)));
		BladeBaseC.render(size);
		setRotation(BladeBaseB, 0.0F, 0.0F, getRotation(getAbsoluteAngle(angle)));
		BladeBaseB.render(size);
		setRotation(BladeBaseA, 0.0F, 0.0F, getRotation(getAbsoluteAngle(240 + angle)));
		BladeBaseA.render(size);
	}

	public float getRotation(double angle)
	{
		return ((float)angle/(float)180)*(float)Math.PI;
	}

	public double getAbsoluteAngle(double angle)
	{
		return angle % 360;
	}

	private void setRotation(ModelRenderer model, float x, float y, float z)
	{
		model.rotateAngleX = x;
		model.rotateAngleY = y;
		model.rotateAngleZ = z;
	}
}
