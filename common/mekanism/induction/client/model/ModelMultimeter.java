package mekanism.induction.client.model;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;

public class ModelMultimeter extends ModelBase
{
	// fields
	ModelRenderer Base;
	ModelRenderer secPanel;
	ModelRenderer arm;
	ModelRenderer button;
	ModelRenderer arm2;
	ModelRenderer infopanel;

	public ModelMultimeter()
	{
		textureWidth = 128;
		textureHeight = 128;

		Base = new ModelRenderer(this, 0, 0);
		Base.addBox(0F, 0F, 0F, 14, 14, 1);
		Base.setRotationPoint(-7F, 9F, 7F);
		Base.setTextureSize(128, 128);
		Base.mirror = true;
		setRotation(Base, 0F, 0F, 0F);
		secPanel = new ModelRenderer(this, 0, 18);
		secPanel.addBox(0F, 0F, 0F, 4, 8, 1);
		secPanel.setRotationPoint(-6F, 10F, 6F);
		secPanel.setTextureSize(128, 128);
		secPanel.mirror = true;
		setRotation(secPanel, 0F, 0F, 0F);
		arm = new ModelRenderer(this, 0, 29);
		arm.addBox(0F, 0F, 0F, 1, 9, 2);
		arm.setRotationPoint(-3.5F, 13F, 5.5F);
		arm.setTextureSize(128, 128);
		arm.mirror = true;
		setRotation(arm, 0F, 0F, 0F);
		button = new ModelRenderer(this, 0, 43);
		button.addBox(0F, 0F, 0F, 2, 1, 1);
		button.setRotationPoint(-5F, 11F, 5.5F);
		button.setTextureSize(128, 128);
		button.mirror = true;
		setRotation(button, 0F, 0F, 0F);
		arm2 = new ModelRenderer(this, 10, 29);
		arm2.addBox(0F, 0F, 0F, 1, 9, 2);
		arm2.setRotationPoint(-5.5F, 13F, 5.5F);
		arm2.setTextureSize(128, 128);
		arm2.mirror = true;
		setRotation(arm2, 0F, 0F, 0F);
		infopanel = new ModelRenderer(this, 33, 0);
		infopanel.addBox(0F, 0F, 0F, 7, 12, 1);
		infopanel.setRotationPoint(-1F, 10F, 6.5F);
		infopanel.setTextureSize(128, 128);
		infopanel.mirror = true;
		setRotation(infopanel, 0F, 0F, 0F);
	}

	public void render(float f5)
	{
		Base.render(f5);
		secPanel.render(f5);
		arm.render(f5);
		button.render(f5);
		arm2.render(f5);
		infopanel.render(f5);
	}

	private void setRotation(ModelRenderer model, float x, float y, float z)
	{
		model.rotateAngleX = x;
		model.rotateAngleY = y;
		model.rotateAngleZ = z;
	}
}
