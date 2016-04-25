package mekanism.client.model;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class ModelGasTank extends ModelBase
{
	ModelRenderer rim4;
	ModelRenderer rim5;
	ModelRenderer rim2;
	ModelRenderer tankBase;
	ModelRenderer valve;
	ModelRenderer rim3;
	ModelRenderer tank;
	ModelRenderer rim0;
	ModelRenderer valveBase;
	ModelRenderer rim1;

	public ModelGasTank()
	{
		textureWidth = 64;
		textureHeight = 32;

		rim4 = new ModelRenderer(this, 30, 0);
		rim4.addBox(0F, 0F, 0F, 1, 3, 6);
		rim4.setRotationPoint(3F, 8F, -3F);
		rim4.setTextureSize(64, 32);
		rim4.mirror = true;
		setRotation(rim4, 0F, 0F, 0F);
		rim4.mirror = false;
		rim5 = new ModelRenderer(this, 0, 4);
		rim5.addBox(0F, 0F, 0F, 2, 3, 1);
		rim5.setRotationPoint(2F, 8F, -4F);
		rim5.setTextureSize(64, 32);
		rim5.mirror = true;
		setRotation(rim5, 0F, 0F, 0F);
		rim2 = new ModelRenderer(this, 30, 0);
		rim2.addBox(0F, 0F, 0F, 1, 3, 6);
		rim2.setRotationPoint(-4F, 8F, -3F);
		rim2.setTextureSize(64, 32);
		rim2.mirror = true;
		setRotation(rim2, 0F, 0F, 0F);
		tankBase = new ModelRenderer(this, 0, 22);
		tankBase.addBox(0F, 0F, 0F, 9, 1, 9);
		tankBase.setRotationPoint(-4.5F, 22.5F, -4.5F);
		tankBase.setTextureSize(64, 32);
		tankBase.mirror = true;
		setRotation(tankBase, 0F, 0F, 0F);
		valve = new ModelRenderer(this, 46, 0);
		valve.addBox(0F, 0F, 0F, 3, 1, 3);
		valve.setRotationPoint(-1.5F, 8.5F, -1.5F);
		valve.setTextureSize(64, 32);
		valve.mirror = true;
		setRotation(valve, 0F, 0F, 0F);
		rim3 = new ModelRenderer(this, 44, 5);
		rim3.addBox(0F, 0F, 0F, 8, 3, 1);
		rim3.setRotationPoint(-4F, 8F, 3F);
		rim3.setTextureSize(64, 32);
		rim3.mirror = true;
		setRotation(rim3, 0F, 0F, 0F);
		tank = new ModelRenderer(this, 0, 0);
		tank.addBox(0F, 0F, 0F, 10, 12, 10);
		tank.setRotationPoint(-5F, 10.5F, -5F);
		tank.setTextureSize(64, 32);
		tank.mirror = true;
		setRotation(tank, 0F, 0F, 0F);
		rim0 = new ModelRenderer(this, 0, 8);
		rim0.addBox(0F, 0F, 0F, 4, 1, 1);
		rim0.setRotationPoint(-2F, 10F, -4F);
		rim0.setTextureSize(64, 32);
		rim0.mirror = true;
		setRotation(rim0, 0F, 0F, 0F);
		valveBase = new ModelRenderer(this, 38, 0);
		valveBase.addBox(0F, 0F, 0F, 2, 1, 2);
		valveBase.setRotationPoint(-1F, 9.5F, -1F);
		valveBase.setTextureSize(64, 32);
		valveBase.mirror = true;
		setRotation(valveBase, 0F, 0F, 0F);
		rim1 = new ModelRenderer(this, 0, 0);
		rim1.addBox(0F, 0F, 0F, 2, 3, 1);
		rim1.setRotationPoint(-4F, 8F, -4F);
		rim1.setTextureSize(64, 32);
		rim1.mirror = true;
		setRotation(rim1, 0F, 0F, 0F);
	}

	public void render(float size)
	{
		rim4.render(size);
		rim5.render(size);
		rim2.render(size);
		tankBase.render(size);
		valve.render(size);
		rim3.render(size);
		tank.render(size);
		rim0.render(size);
		valveBase.render(size);
		rim1.render(size);
	}

	private void setRotation(ModelRenderer model, float x, float y, float z)
	{
		model.rotateAngleX = x;
		model.rotateAngleY = y;
		model.rotateAngleZ = z;
	}
}
