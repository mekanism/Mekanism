package mekanism.client;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.entity.Entity;
import net.minecraftforge.common.ForgeDirection;

public class ModelTransmitter extends ModelBase 
{
	ModelRenderer Center;
	ModelRenderer Up;
	ModelRenderer Down;
	ModelRenderer Front;
	ModelRenderer Back;
	ModelRenderer Left;
	ModelRenderer Right;

	public ModelTransmitter() 
	{
		textureWidth = 64;
		textureHeight = 64;

		Center = new ModelRenderer(this, 0, 0);
		Center.addBox(0F, 0F, 0F, 6, 6, 6);
		Center.setRotationPoint(-3F, 13F, -3F);
		Center.setTextureSize(64, 64);
		Center.mirror = true;
		setRotation(Center, 0F, 0F, 0F);
		Up = new ModelRenderer(this, 0, 13);
		Up.addBox(0F, 0F, 0F, 6, 5, 6);
		Up.setRotationPoint(-3F, 8F, -3F);
		Up.setTextureSize(64, 64);
		Up.mirror = true;
		setRotation(Up, 0F, 0F, 0F);
		Down = new ModelRenderer(this, 26, 13);
		Down.addBox(0F, 0F, 0F, 6, 5, 6);
		Down.setRotationPoint(-3F, 19F, -3F);
		Down.setTextureSize(64, 64);
		Down.mirror = true;
		setRotation(Down, 0F, 0F, 0F);
		Front = new ModelRenderer(this, 0, 26);
		Front.addBox(0F, 0F, 0F, 5, 6, 6);
		Front.setRotationPoint(-3F, 13F, -3F);
		Front.setTextureSize(64, 64);
		Front.mirror = true;
		setRotation(Front, 0F, 1.570796F, 0F);
		Back = new ModelRenderer(this, 0, 41);
		Back.addBox(0F, 0F, 0F, 5, 6, 6);
		Back.setRotationPoint(-3F, 13F, 8F);
		Back.setTextureSize(64, 64);
		Back.mirror = true;
		setRotation(Back, 0F, 1.570796F, 0F);
		Left = new ModelRenderer(this, 26, 0);
		Left.addBox(0F, 0F, 0F, 6, 5, 6);
		Left.setRotationPoint(3F, 19F, 3F);
		Left.setTextureSize(64, 64);
		Left.mirror = true;
		setRotation(Left, 1.570796F, 1.570796F, 0F);
		Right = new ModelRenderer(this, 26, 26);
		Right.addBox(0F, 0F, 0F, 6, 5, 6);
		Right.setRotationPoint(-8F, 19F, 3F);
		Right.setTextureSize(64, 64);
		Right.mirror = true;
		setRotation(Right, 1.570796F, 1.570796F, 0F);
	}

	@Override
	public void render(Entity entity, float f, float f1, float f2, float f3, float f4, float f5)
	{
		super.render(entity, f, f1, f2, f3, f4, f5);
		setRotationAngles(f, f1, f2, f3, f4, f5, entity);

		Center.render(0.0625F);

		for (ForgeDirection orientation : ForgeDirection.VALID_DIRECTIONS) 
		{
			renderSide(orientation);
		}
	}

	public void renderSide(ForgeDirection orientation) 
	{
		switch (orientation) 
		{
			case DOWN:
				Down.render(0.0625F);
				break;
			case UP:
				Up.render(0.0625F);
				break;
			case NORTH:
				Back.render(0.0625F);
				break;
			case SOUTH:
				Front.render(0.0625F);
				break;
			case WEST:
				Right.render(0.0625F);
				break;
			case EAST:
				Left.render(0.0625F);
				break;
			default:
				return;
		}
	}

	private void setRotation(ModelRenderer model, float x, float y, float z)
	{
		model.rotateAngleX = x;
		model.rotateAngleY = y;
		model.rotateAngleZ = z;
	}
}
