package mekanism.client.model;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class ModelGasTank extends ModelBase
{
	ModelRenderer Panel1;
	ModelRenderer Panel2;
	ModelRenderer Panel3;
	ModelRenderer Tank;
	ModelRenderer Panel4;
	ModelRenderer Top;
	ModelRenderer Exit;

	public ModelGasTank()
	{
		textureWidth = 32;
		textureHeight = 64;

		Panel1 = new ModelRenderer(this, 0, 22);
		Panel1.addBox(0F, 0F, 0F, 5, 13, 1);
		Panel1.setRotationPoint(-2.5F, 11F, 3.5F);
		Panel1.setTextureSize(32, 64);
		Panel1.mirror = true;
		setRotation(Panel1, 0F, 0F, 0F);
		Panel2 = new ModelRenderer(this, 0, 37);
		Panel2.addBox(0F, 0F, 0F, 1, 13, 5);
		Panel2.setRotationPoint(3.5F, 11F, -2.5F);
		Panel2.setTextureSize(32, 64);
		Panel2.mirror = true;
		setRotation(Panel2, 0F, 0F, 0F);
		Panel3 = new ModelRenderer(this, 0, 22);
		Panel3.addBox(0F, 0F, 0F, 5, 13, 1);
		Panel3.setRotationPoint(-2.5F, 11F, -4.5F);
		Panel3.setTextureSize(32, 64);
		Panel3.mirror = true;
		setRotation(Panel3, 0F, 0F, 0F);
		Tank = new ModelRenderer(this, 0, 0);
		Tank.addBox(0F, 0F, 0F, 7, 14, 7);
		Tank.setRotationPoint(-3.5F, 10F, -3.5F);
		Tank.setTextureSize(32, 64);
		Tank.mirror = true;
		setRotation(Tank, 0F, 0F, 0F);
		Panel4 = new ModelRenderer(this, 0, 37);
		Panel4.addBox(0F, 0F, 0F, 1, 13, 5);
		Panel4.setRotationPoint(-4.5F, 11F, -2.5F);
		Panel4.setTextureSize(32, 64);
		Panel4.mirror = true;
		setRotation(Panel4, 0F, 0F, 0F);
		Top = new ModelRenderer(this, 13, 22);
		Top.addBox(0F, 0F, 0F, 2, 2, 2);
		Top.setRotationPoint(-1F, 8F, -1F);
		Top.setTextureSize(32, 64);
		Top.mirror = true;
		setRotation(Top, 0F, 0F, 0F);
		Exit = new ModelRenderer(this, 22, 22);
		Exit.addBox(0F, 0F, 0F, 1, 1, 1);
		Exit.setRotationPoint(1F, 8.2F, -0.5F);
		Exit.setTextureSize(32, 64);
		Exit.mirror = true;
		setRotation(Exit, 0F, 0F, 0F);
	}

	public void render(float size)
	{
		Panel1.render(size);
		Panel2.render(size);
		Panel3.render(size);
		Tank.render(size);
		Panel4.render(size);
		Top.render(size);
		Exit.render(size);
	}

	private void setRotation(ModelRenderer model, float x, float y, float z)
	{
		model.rotateAngleX = x;
		model.rotateAngleY = y;
		model.rotateAngleZ = z;
	}
}
