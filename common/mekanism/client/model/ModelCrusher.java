package mekanism.client.model;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.entity.Entity;

@SideOnly(Side.CLIENT)
public class ModelCrusher extends ModelBase 
{
	ModelRenderer Top;
	ModelRenderer Base;
	ModelRenderer RightWall;
	ModelRenderer RightWall2;
	ModelRenderer PistonDecor;
	ModelRenderer PistonRod;
	ModelRenderer Pad;
	ModelRenderer PistonHead;
	ModelRenderer BackWall;
	ModelRenderer BackWall2;
	ModelRenderer TopTop;

	public ModelCrusher()
	{
		textureWidth = 128;
		textureHeight = 64;

		Top = new ModelRenderer(this, 0, 18);
		Top.addBox(0F, 0F, 0F, 14, 1, 8);
		Top.setRotationPoint(-7F, 14F, -4F);
		Top.setTextureSize(128, 64);
		Top.mirror = true;
		setRotation(Top, 0F, 0F, 0F);
		Base = new ModelRenderer(this, 0, 0);
		Base.addBox(0F, 0F, 0F, 16, 1, 16);
		Base.setRotationPoint(-8F, 23F, -8F);
		Base.setTextureSize(128, 64);
		Base.mirror = true;
		setRotation(Base, 0F, 0F, 0F);
		RightWall = new ModelRenderer(this, 0, 28);
		RightWall.addBox(0F, 0F, 0F, 4, 6, 14);
		RightWall.setRotationPoint(-8F, 17F, -7F);
		RightWall.setTextureSize(128, 64);
		RightWall.mirror = true;
		setRotation(RightWall, 0F, 0F, 0F);
		RightWall2 = new ModelRenderer(this, 0, 49);
		RightWall2.addBox(0F, 0F, 0F, 4, 2, 12);
		RightWall2.setRotationPoint(-8F, 15F, -6F);
		RightWall2.setTextureSize(128, 64);
		RightWall2.mirror = true;
		setRotation(RightWall2, 0F, 0F, 0F);
		PistonDecor = new ModelRenderer(this, 65, 0);
		PistonDecor.addBox(0F, 0F, 0F, 7, 1, 7);
		PistonDecor.setRotationPoint(-0.5F, 14.5F, -3.5F);
		PistonDecor.setTextureSize(128, 64);
		PistonDecor.mirror = true;
		setRotation(PistonDecor, 0F, 0F, 0F);
		PistonRod = new ModelRenderer(this, 45, 18);
		PistonRod.addBox(0F, 0F, 0F, 1, 6, 1);
		PistonRod.setRotationPoint(2.5F, 13F, -0.5F);
		PistonRod.setTextureSize(128, 64);
		PistonRod.mirror = true;
		setRotation(PistonRod, 0F, 0F, 0F);
		Pad = new ModelRenderer(this, 50, 18);
		Pad.addBox(0F, 0F, 0F, 4, 1, 4);
		Pad.setRotationPoint(1F, 22.7F, -2F);
		Pad.setTextureSize(128, 64);
		Pad.mirror = true;
		setRotation(Pad, 0F, 0F, 0F);
		PistonHead = new ModelRenderer(this, 67, 18);
		PistonHead.addBox(0F, 0F, 0F, 4, 1, 4);
		PistonHead.setRotationPoint(1F, 19F, -2F);
		PistonHead.setTextureSize(128, 64);
		PistonHead.mirror = true;
		setRotation(PistonHead, 0F, 0F, 0F);
		BackWall = new ModelRenderer(this, 94, 0);
		BackWall.addBox(0F, 0F, 0F, 11, 6, 2);
		BackWall.setRotationPoint(-4F, 17F, 5F);
		BackWall.setTextureSize(128, 64);
		BackWall.mirror = true;
		setRotation(BackWall, 0F, 0F, 0F);
		BackWall2 = new ModelRenderer(this, 90, 9);
		BackWall2.addBox(0F, 0F, 0F, 11, 2, 3);
		BackWall2.setRotationPoint(-4F, 15F, 3F);
		BackWall2.setTextureSize(128, 64);
		BackWall2.mirror = true;
		setRotation(BackWall2, 0F, 0F, 0F);
		TopTop = new ModelRenderer(this, 90, 15);
		TopTop.addBox(0F, 0F, 0F, 6, 5, 6);
		TopTop.setRotationPoint(0F, 12F, -3F);
		TopTop.setTextureSize(128, 64);
		TopTop.mirror = true;
		setRotation(TopTop, 0F, 0F, 0F);
	}

	public void render(float size, float depth)
	{
		PistonRod.setRotationPoint(2.5F, 13F+depth, -0.5F);
		PistonHead.setRotationPoint(1F, 19F+depth, -2F);
		
		Top.render(size);
		Base.render(size);
		RightWall.render(size);
		RightWall2.render(size);
		PistonDecor.render(size);
		PistonRod.render(size);
		Pad.render(size);
		PistonHead.render(size);
		BackWall.render(size);
		BackWall2.render(size);
		TopTop.render(size);
	}

	private void setRotation(ModelRenderer model, float x, float y, float z)
	{
		model.rotateAngleX = x;
		model.rotateAngleY = y;
		model.rotateAngleZ = z;
	}
}
