package mekanism.client.model;

import mekanism.common.multipart.TransmitterType.Size;
import net.minecraft.client.model.ModelBase;
import net.minecraftforge.common.ForgeDirection;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class ModelTransmitter extends ModelBase 
{
	public boolean[][] disabledFaces = {
			{true, true, false, false, false, false},
			{true, true, false, false, false, false},
			{false, false, false, false, true, true},
			{false, false, false, false, true, true},
			{true, true, false, false, false, false},
			{true, true, false, false, false, false},
		};
	
	ModelRendererSelectiveFace Center;
	ModelRendererSelectiveFace Up;
	ModelRendererSelectiveFace Down;
	ModelRendererSelectiveFace Front;
	ModelRendererSelectiveFace Back;
	ModelRendererSelectiveFace Left;
	ModelRendererSelectiveFace Right;

	public ModelTransmitter(Size size) 
	{
		if(size == Size.LARGE)
		{
			textureWidth = 128;
			textureHeight = 128;
			
			Center = new ModelRendererSelectiveFace(this, 0, 0);
			Center.addBox(0F, 0F, 0F, 8, 8, 8);
			Center.setRotationPoint(-4F, 12F, -4F);
			Center.setTextureSize(64, 64);
			Center.mirror = true;
			setRotation(Center, 0F, 0F, 0F);
			Up = new ModelRendererSelectiveFace(this, 0, 13);
			Up.addBox(0F, 0F, 0F, 8, 4, 8);
			Up.setRotationPoint(-4F, 8F, -4F);
			Up.setTextureSize(64, 64);
			Up.mirror = true;
			setRotation(Up, 0F, 0F, 0F);
			Down = new ModelRendererSelectiveFace(this, 34, 13);
			Down.addBox(0F, 0F, 0F, 8, 4, 8);
			Down.setRotationPoint(-4F, 20F, -4F);
			Down.setTextureSize(64, 64);
			Down.mirror = true;
			setRotation(Down, 0F, 0F, 0F);
			Front = new ModelRendererSelectiveFace(this, 0, 30);
			Front.addBox(0F, 0F, 0F, 4, 8, 8);
			Front.setRotationPoint(-4F, 12F, -4F);
			Front.setTextureSize(64, 64);
			Front.mirror = true;
			setRotation(Front, 0F, 1.570796F, 0F);
			Back = new ModelRendererSelectiveFace(this, 0, 49);
			Back.addBox(0F, 0F, 0F, 4, 8, 8);
			Back.setRotationPoint(-4F, 12F, 8F);
			Back.setTextureSize(64, 64);
			Back.mirror = true;
			setRotation(Back, 0F, 1.570796F, 0F);
			Left = new ModelRendererSelectiveFace(this, 34, 0);
			Left.addBox(0F, 0F, 0F, 8, 4, 8);
			Left.setRotationPoint(4F, 20F, 4F);
			Left.setTextureSize(64, 64);
			Left.mirror = true;
			setRotation(Left, 1.570796F, 1.570796F, 0F);
			Right = new ModelRendererSelectiveFace(this, 34, 30);
			Right.addBox(0F, 0F, 0F, 8, 4, 8);
			Right.setRotationPoint(-8F, 20F, 4F);
			Right.setTextureSize(64, 64);
			Right.mirror = true;
			setRotation(Right, 1.570796F, 1.570796F, 0F);
		}
		else {
	       textureWidth = 64;
           textureHeight = 64;
           
           Center = new ModelRendererSelectiveFace(this, 0, 0);
           Center.addBox(0F, 0F, 0F, 6, 6, 6);
           Center.setRotationPoint(-3F, 13F, -3F);
           Center.setTextureSize(64, 64);
           Center.mirror = true;
           setRotation(Center, 0F, 0F, 0F);
           Up = new ModelRendererSelectiveFace(this, 0, 13);
           Up.addBox(0F, 0F, 0F, 6, 5, 6);
           Up.setRotationPoint(-3F, 8F, -3F);
           Up.setTextureSize(64, 64);
           Up.mirror = true;
           setRotation(Up, 0F, 0F, 0F);
           Down = new ModelRendererSelectiveFace(this, 26, 13);
           Down.addBox(0F, 0F, 0F, 6, 5, 6);
           Down.setRotationPoint(-3F, 19F, -3F);
           Down.setTextureSize(64, 64);
           Down.mirror = true;
           setRotation(Down, 0F, 0F, 0F);
           Front = new ModelRendererSelectiveFace(this, 0, 26);
           Front.addBox(0F, 0F, 0F, 5, 6, 6);
           Front.setRotationPoint(-3F, 13F, -3F);
           Front.setTextureSize(64, 64);
           Front.mirror = true;
           setRotation(Front, 0F, 1.570796F, 0F);
           Back = new ModelRendererSelectiveFace(this, 0, 41);
           Back.addBox(0F, 0F, 0F, 5, 6, 6);
           Back.setRotationPoint(-3F, 13F, 8F);
           Back.setTextureSize(64, 64);
           Back.mirror = true;
           setRotation(Back, 0F, 1.570796F, 0F);
           Left = new ModelRendererSelectiveFace(this, 26, 0);
           Left.addBox(0F, 0F, 0F, 6, 5, 6);
           Left.setRotationPoint(3F, 19F, 3F);
           Left.setTextureSize(64, 64);
           Left.mirror = true;
           setRotation(Left, 1.570796F, 1.570796F, 0F);
           Right = new ModelRendererSelectiveFace(this, 26, 26);
           Right.addBox(0F, 0F, 0F, 6, 5, 6);
           Right.setRotationPoint(-8F, 19F, 3F);
           Right.setTextureSize(64, 64);
           Right.mirror = true;
           setRotation(Right, 1.570796F, 1.570796F, 0F);
		}
	}

	public void renderSide(ForgeDirection orientation, boolean on) 
	{
		if(on)
		{
			switch(orientation) 
			{
				case DOWN:
					Down.render(disabledFaces[orientation.ordinal()], 0.0625F);
					break;
				case UP:
					Up.render(disabledFaces[orientation.ordinal()], 0.0625F);
					break;
				case NORTH:
					Back.render(disabledFaces[orientation.ordinal()], 0.0625F);
					break;
				case SOUTH:
					Front.render(disabledFaces[orientation.ordinal()], 0.0625F);
					break;
				case WEST:
					Right.render(disabledFaces[orientation.ordinal()], 0.0625F);
					break;
				case EAST:
					Left.render(disabledFaces[orientation.ordinal()], 0.0625F);
					break;
				default:
					return;
			}
		} 
	}
	
	public void renderCenter(boolean[] connectable)
	{
		Center.render(connectable, 0.0625F);
	}

	private void setRotation(ModelRendererSelectiveFace model, float x, float y, float z)
	{
		model.rotateAngleX = x;
		model.rotateAngleY = y;
		model.rotateAngleZ = z;
	}
}