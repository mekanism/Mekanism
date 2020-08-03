package mekanism.client.model;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class ModelPressurizedReactionChamber extends ModelBase 
{
	ModelRenderer frontDivider1;
	ModelRenderer base;
	ModelRenderer front;
	ModelRenderer bar1;
	ModelRenderer body;
	ModelRenderer bar5;
	ModelRenderer bar4;
	ModelRenderer bar3;
	ModelRenderer bar2;
	ModelRenderer frontDivider2;

	public ModelPressurizedReactionChamber() 
	{
		textureWidth = 128;
		textureHeight = 64;

		frontDivider1 = new ModelRenderer(this, 52, 20);
		frontDivider1.addBox(0F, 0F, 0F, 2, 12, 6);
		frontDivider1.setRotationPoint(-7F, 8.5F, -7.5F);
		frontDivider1.setTextureSize(128, 64);
		frontDivider1.mirror = true;
		setRotation(frontDivider1, 0F, 0F, 0F);
		base = new ModelRenderer(this, 0, 0);
		base.addBox(0F, 0F, 0F, 16, 4, 16);
		base.setRotationPoint(-8F, 20F, -8F);
		base.setTextureSize(128, 64);
		base.mirror = true;
		setRotation(base, 0F, 0F, 0F);
		front = new ModelRenderer(this, 48, 0);
		front.addBox(0F, 0F, 0F, 9, 11, 5);
		front.setRotationPoint(-2F, 9F, -7F);
		front.setTextureSize(128, 64);
		front.mirror = true;
		setRotation(front, 0F, 0F, 0F);
		bar1 = new ModelRenderer(this, 0, 0);
		bar1.addBox(0F, 0F, 0F, 1, 1, 5);
		bar1.setRotationPoint(-5F, 18F, -7F);
		bar1.setTextureSize(128, 64);
		bar1.mirror = true;
		setRotation(bar1, 0F, 0F, 0F);
		body = new ModelRenderer(this, 0, 20);
		body.addBox(0F, 0F, 0F, 16, 12, 10);
		body.setRotationPoint(-8F, 8F, -2F);
		body.setTextureSize(128, 64);
		body.mirror = true;
		setRotation(body, 0F, 0F, 0F);
		bar5 = new ModelRenderer(this, 0, 0);
		bar5.addBox(0F, 0F, 0F, 1, 1, 5);
		bar5.setRotationPoint(-5F, 10F, -7F);
		bar5.setTextureSize(128, 64);
		bar5.mirror = true;
		setRotation(bar5, 0F, 0F, 0F);
		bar4 = new ModelRenderer(this, 0, 0);
		bar4.addBox(0F, 0F, 0F, 1, 1, 5);
		bar4.setRotationPoint(-5F, 12F, -7F);
		bar4.setTextureSize(128, 64);
		bar4.mirror = true;
		setRotation(bar4, 0F, 0F, 0F);
		bar3 = new ModelRenderer(this, 0, 0);
		bar3.addBox(0F, 0F, 0F, 1, 1, 5);
		bar3.setRotationPoint(-5F, 14F, -7F);
		bar3.setTextureSize(128, 64);
		bar3.mirror = true;
		setRotation(bar3, 0F, 0F, 0F);
		bar2 = new ModelRenderer(this, 0, 0);
		bar2.addBox(0F, 0F, 0F, 1, 1, 5);
		bar2.setRotationPoint(-5F, 16F, -7F);
		bar2.setTextureSize(128, 64);
		bar2.mirror = true;
		setRotation(bar2, 0F, 0F, 0F);
		frontDivider2 = new ModelRenderer(this, 52, 20);
		frontDivider2.mirror = true;
		frontDivider2.addBox(0F, 0F, 0F, 2, 12, 6);
		frontDivider2.setRotationPoint(-4F, 8.5F, -7.5F);
		frontDivider2.setTextureSize(128, 64);
		setRotation(frontDivider2, 0F, 0F, 0F);
	}

	public void render(float size)
	{
		render(size, false);
	}

	public void render(float size, boolean inventory) 
	{
		frontDivider1.render(size);
		base.render(size);
		front.render(size);
		if (!inventory) bar1.render(size);
		body.render(size);
		if (!inventory) { bar5.render(size);
		bar4.render(size);
		bar3.render(size);
		bar2.render(size);}
		frontDivider2.render(size);
	}

	private void setRotation(ModelRenderer model, float x, float y, float z) 
	{
		model.rotateAngleX = x;
		model.rotateAngleY = y;
		model.rotateAngleZ = z;
	}
}
