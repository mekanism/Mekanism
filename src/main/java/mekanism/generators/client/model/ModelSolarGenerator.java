package mekanism.generators.client.model;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;

public class ModelSolarGenerator extends ModelBase
{
	ModelRenderer Base;
	ModelRenderer Base2;
	ModelRenderer Stand;
	ModelRenderer PhotovoltaicCells;

	public ModelSolarGenerator()
	{
		textureWidth = 64;
		textureHeight = 32;

		Base = new ModelRenderer(this, 0, 16);
		Base.addBox(0F, 0F, 0F, 6, 1, 6);
		Base.setRotationPoint(-3F, 23F, -3F);
		Base.setTextureSize(64, 32);
		Base.mirror = true;
		setRotation(Base, 0F, 0F, 0F);
		Base2 = new ModelRenderer(this, 0, 24);
		Base2.addBox(0F, 0F, 0F, 3, 1, 3);
		Base2.setRotationPoint(-1.5F, 22F, -1.5F);
		Base2.setTextureSize(64, 32);
		Base2.mirror = true;
		setRotation(Base2, 0F, 0F, 0F);
		Stand = new ModelRenderer(this, 25, 16);
		Stand.addBox(0F, 0F, 0F, 1, 7, 1);
		Stand.setRotationPoint(-0.5F, 15F, -0.5F);
		Stand.setTextureSize(64, 32);
		Stand.mirror = true;
		setRotation(Stand, 0F, 0F, 0F);
		PhotovoltaicCells = new ModelRenderer(this, 0, 0);
		PhotovoltaicCells.addBox(-7F, -1F, -7F, 14, 1, 14);
		PhotovoltaicCells.setRotationPoint(0F, 15F, 0F);
		PhotovoltaicCells.setTextureSize(64, 32);
		PhotovoltaicCells.mirror = true;
		setRotation(PhotovoltaicCells, 0F, 0F, 0F);
	}

	public void render(float size)
	{
		Base.render(size);
		Base2.render(size);
		Stand.render(size);
		PhotovoltaicCells.render(size);
	}

	private void setRotation(ModelRenderer model, float x, float y, float z)
	{
		model.rotateAngleX = x;
		model.rotateAngleY = y;
		model.rotateAngleZ = z;
	}
}
