package mekanism.client.model;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class ModelObsidianTNT extends ModelBase
{
	ModelRenderer Wick9;
	ModelRenderer Wick8;
	ModelRenderer Wick7;
	ModelRenderer Wick6;
	ModelRenderer Wick5;
	ModelRenderer Wick4;
	ModelRenderer Wick3;
	ModelRenderer Wick2;
	ModelRenderer Wick1;
	ModelRenderer Wooden2;
	ModelRenderer Wooden1;
	ModelRenderer Rod1;
	ModelRenderer Rod2;
	ModelRenderer Rod3;
	ModelRenderer Rod4;
	ModelRenderer Rod5;
	ModelRenderer Rod6;
	ModelRenderer Rod7;
	ModelRenderer Rod8;
	ModelRenderer Rod9;

	public ModelObsidianTNT()
	{
		textureWidth = 64;
		textureHeight = 64;

		Wick9 = new ModelRenderer(this, 0, 0);
		Wick9.addBox(0F, 0F, 0F, 1, 2, 1);
		Wick9.setRotationPoint(-0.5F, 9.2F, -0.5F);
		Wick9.setTextureSize(64, 64);
		Wick9.mirror = true;
		setRotation(Wick9, 0F, 0F, 0.2268928F);
		Wick8 = new ModelRenderer(this, 0, 0);
		Wick8.addBox(0F, 0F, 0F, 1, 2, 1);
		Wick8.setRotationPoint(-0.5F, 9.5F, -5.5F);
		Wick8.setTextureSize(64, 64);
		Wick8.mirror = true;
		setRotation(Wick8, 0F, 0F, -0.2379431F);
		Wick7 = new ModelRenderer(this, 0, 0);
		Wick7.addBox(0F, 0F, 0F, 1, 2, 1);
		Wick7.setRotationPoint(-0.5F, 9.5F, 4.5F);
		Wick7.setTextureSize(64, 64);
		Wick7.mirror = true;
		setRotation(Wick7, 0F, 0F, -0.2379431F);
		Wick6 = new ModelRenderer(this, 0, 0);
		Wick6.addBox(0F, 0F, 0F, 1, 2, 1);
		Wick6.setRotationPoint(-5.5F, 9.2F, -5.5F);
		Wick6.setTextureSize(64, 64);
		Wick6.mirror = true;
		setRotation(Wick6, 0F, 0F, 0.2268928F);
		Wick5 = new ModelRenderer(this, 0, 0);
		Wick5.addBox(0F, 0F, 0F, 1, 2, 1);
		Wick5.setRotationPoint(-5.5F, 9.5F, -0.5F);
		Wick5.setTextureSize(64, 64);
		Wick5.mirror = true;
		setRotation(Wick5, 0F, 0F, -0.2379431F);
		Wick4 = new ModelRenderer(this, 0, 0);
		Wick4.addBox(0F, 0F, 0F, 1, 2, 1);
		Wick4.setRotationPoint(-5.5F, 9.2F, 4.5F);
		Wick4.setTextureSize(64, 64);
		Wick4.mirror = true;
		setRotation(Wick4, 0F, 0F, 0.2268928F);
		Wick3 = new ModelRenderer(this, 0, 0);
		Wick3.addBox(0F, 0F, 0F, 1, 2, 1);
		Wick3.setRotationPoint(4.5F, 9.2F, -5.5F);
		Wick3.setTextureSize(64, 64);
		Wick3.mirror = true;
		setRotation(Wick3, 0F, 0F, 0.2268928F);
		Wick2 = new ModelRenderer(this, 0, 0);
		Wick2.addBox(0F, 0F, 0F, 1, 2, 1);
		Wick2.setRotationPoint(4.5F, 9.5F, -0.5F);
		Wick2.setTextureSize(64, 64);
		Wick2.mirror = true;
		setRotation(Wick2, 0F, 0F, -0.2379431F);
		Wick1 = new ModelRenderer(this, 0, 0);
		Wick1.addBox(0F, 0F, 0F, 1, 2, 1);
		Wick1.setRotationPoint(4.5F, 9.2F, 4.5F);
		Wick1.setTextureSize(64, 64);
		Wick1.mirror = true;
		setRotation(Wick1, 0F, 0F, 0.2268928F);
		Wooden2 = new ModelRenderer(this, 0, 0);
		Wooden2.addBox(0F, 0F, 0F, 16, 3, 16);
		Wooden2.setRotationPoint(-8F, 12F, -8F);
		Wooden2.setTextureSize(64, 64);
		Wooden2.mirror = true;
		setRotation(Wooden2, 0F, 0F, 0F);
		Wooden1 = new ModelRenderer(this, 0, 0);
		Wooden1.addBox(0F, 0F, 0F, 16, 3, 16);
		Wooden1.setRotationPoint(-8F, 20F, -8F);
		Wooden1.setTextureSize(64, 64);
		Wooden1.mirror = true;
		setRotation(Wooden1, 0F, 0F, 0F);
		Rod1 = new ModelRenderer(this, 0, 20);
		Rod1.addBox(0F, 0F, 0F, 4, 13, 4);
		Rod1.setRotationPoint(3F, 11F, 3F);
		Rod1.setTextureSize(64, 64);
		Rod1.mirror = true;
		setRotation(Rod1, 0F, 0F, 0F);
		Rod2 = new ModelRenderer(this, 0, 20);
		Rod2.addBox(0F, 0F, 0F, 4, 13, 4);
		Rod2.setRotationPoint(3F, 11F, -2F);
		Rod2.setTextureSize(64, 64);
		Rod2.mirror = true;
		setRotation(Rod2, 0F, 0F, 0F);
		Rod3 = new ModelRenderer(this, 0, 20);
		Rod3.addBox(0F, 0F, 0F, 4, 13, 4);
		Rod3.setRotationPoint(3F, 11F, -7F);
		Rod3.setTextureSize(64, 64);
		Rod3.mirror = true;
		setRotation(Rod3, 0F, 0F, 0F);
		Rod4 = new ModelRenderer(this, 0, 20);
		Rod4.addBox(0F, 0F, 0F, 4, 13, 4);
		Rod4.setRotationPoint(-2F, 11F, -7F);
		Rod4.setTextureSize(64, 64);
		Rod4.mirror = true;
		setRotation(Rod4, 0F, 0F, 0F);
		Rod5 = new ModelRenderer(this, 0, 20);
		Rod5.addBox(0F, 0F, 0F, 4, 13, 4);
		Rod5.setRotationPoint(-2F, 11F, -2F);
		Rod5.setTextureSize(64, 64);
		Rod5.mirror = true;
		setRotation(Rod5, 0F, 0F, 0F);
		Rod6 = new ModelRenderer(this, 0, 20);
		Rod6.addBox(0F, 0F, 0F, 4, 13, 4);
		Rod6.setRotationPoint(-2F, 11F, 3F);
		Rod6.setTextureSize(64, 64);
		Rod6.mirror = true;
		setRotation(Rod6, 0F, 0F, 0F);
		Rod7 = new ModelRenderer(this, 0, 20);
		Rod7.addBox(0F, 0F, 0F, 4, 13, 4);
		Rod7.setRotationPoint(-7F, 11F, -2F);
		Rod7.setTextureSize(64, 64);
		Rod7.mirror = true;
		setRotation(Rod7, 0F, 0F, 0F);
		Rod8 = new ModelRenderer(this, 0, 20);
		Rod8.addBox(0F, 0F, 0F, 4, 13, 4);
		Rod8.setRotationPoint(-7F, 11F, 3F);
		Rod8.setTextureSize(64, 64);
		Rod8.mirror = true;
		setRotation(Rod8, 0F, 0F, 0F);
		Rod9 = new ModelRenderer(this, 0, 20);
		Rod9.addBox(0F, 0F, 0F, 4, 13, 4);
		Rod9.setRotationPoint(-7F, 11F, -7F);
		Rod9.setTextureSize(64, 64);
		Rod9.mirror = true;
		setRotation(Rod9, 0F, 0F, 0F);
	}

	public void render(float size)
	{
		Wick9.render(size);
		Wick8.render(size);
		Wick7.render(size);
		Wick6.render(size);
		Wick5.render(size);
		Wick4.render(size);
		Wick3.render(size);
		Wick2.render(size);
		Wick1.render(size);
		Wooden2.render(size);
		Wooden1.render(size);
		Rod1.render(size);
		Rod2.render(size);
		Rod3.render(size);
		Rod4.render(size);
		Rod5.render(size);
		Rod6.render(size);
		Rod7.render(size);
		Rod8.render(size);
		Rod9.render(size);
	}

	private void setRotation(ModelRenderer model, float x, float y, float z)
	{
		model.rotateAngleX = x;
		model.rotateAngleY = y;
		model.rotateAngleZ = z;
	}
}
