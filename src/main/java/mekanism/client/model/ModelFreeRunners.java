package mekanism.client.model;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;

public class ModelFreeRunners extends ModelBase
{
	ModelRenderer SpringL;
	ModelRenderer SpringR;
	ModelRenderer BraceL;
	ModelRenderer BraceR;
	ModelRenderer SupportL;
	ModelRenderer SupportR;

	public ModelFreeRunners()
	{
		textureWidth = 64;
		textureHeight = 32;

		SpringL = new ModelRenderer(this, 8, 0);
		SpringL.addBox(1.5F, 18F, 0F, 1, 6, 1);
		SpringL.setRotationPoint(0F, 0F, 0F);
		SpringL.setTextureSize(64, 32);
		SpringL.mirror = true;
		setRotation(SpringL, 0.1047198F, 0F, 0F);
		SpringR = new ModelRenderer(this, 8, 0);
		SpringR.addBox(-2.5F, 18F, 0F, 1, 6, 1);
		SpringR.setRotationPoint(0F, 0F, 0F);
		SpringR.setTextureSize(64, 32);
		SpringR.mirror = true;
		setRotation(SpringR, 0.1047198F, 0F, 0F);
		SpringR.mirror = false;
		BraceL = new ModelRenderer(this, 12, 0);
		BraceL.addBox(0.2F, 18F, -0.8F, 4, 2, 3);
		BraceL.setRotationPoint(0F, 0F, 0F);
		BraceL.setTextureSize(64, 32);
		BraceL.mirror = true;
		setRotation(BraceL, 0F, 0F, 0F);
		BraceR = new ModelRenderer(this, 12, 0);
		BraceR.addBox(-4.2F, 18F, -0.8F, 4, 2, 3);
		BraceR.setRotationPoint(0F, 0F, 0F);
		BraceR.setTextureSize(64, 32);
		BraceR.mirror = true;
		setRotation(BraceR, 0F, 0F, 0F);
		SupportL = new ModelRenderer(this, 0, 0);
		SupportL.addBox(1F, 16.5F, -4.2F, 2, 4, 2);
		SupportL.setRotationPoint(0F, 0F, 0F);
		SupportL.setTextureSize(64, 32);
		SupportL.mirror = true;
		setRotation(SupportL, 0.296706F, 0F, 0F);
		SupportR = new ModelRenderer(this, 0, 0);
		SupportR.addBox(-3F, 16.5F, -4.2F, 2, 4, 2);
		SupportR.setRotationPoint(0F, 0F, 0F);
		SupportR.setTextureSize(64, 32);
		SupportR.mirror = true;
		setRotation(SupportR, 0.296706F, 0F, 0F);
		SupportR.mirror = false;
	}

	public void render(float size)
	{
		SpringL.render(size);
		SpringR.render(size);
		BraceL.render(size);
		BraceR.render(size);
		SupportL.render(size);
		SupportR.render(size);
	}

	public void renderLeft(float size)
	{
		SpringL.render(size);
		BraceL.render(size);
		SupportL.render(size);
	}

	public void renderRight(float size)
	{
		SpringR.render(size);
		BraceR.render(size);
		SupportR.render(size);
	}

	private void setRotation(ModelRenderer model, float x, float y, float z)
	{
		model.rotateAngleX = x;
		model.rotateAngleY = y;
		model.rotateAngleZ = z;
	}
}
