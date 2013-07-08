package mekanism.client;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.entity.Entity;
import net.minecraftforge.common.ForgeDirection;

@SideOnly(Side.CLIENT)
public class ModelTransmitter extends ModelBase 
{
	ModelRenderer UpOn;
	ModelRenderer DownOn;
	ModelRenderer FrontOn;
	ModelRenderer BackOn;
	ModelRenderer LeftOn;
	ModelRenderer RightOn;
	ModelRenderer UpOff;
	ModelRenderer DownOff;
	ModelRenderer FrontOff;
	ModelRenderer BackOff;
	ModelRenderer LeftOff;
	ModelRenderer RightOff;

	public ModelTransmitter() 
	{
		textureWidth = 64;
		textureHeight = 64;

		UpOn = new ModelRenderer(this, 0, 13);
		UpOn.addBox(0F, 0F, 0F, 6, 5, 6);
		UpOn.setRotationPoint(-3F, 8F, -3F);
		UpOn.setTextureSize(64, 64);
		UpOn.mirror = true;
		setRotation(UpOn, 0F, 0F, 0F);
		DownOn = new ModelRenderer(this, 26, 13);
		DownOn.addBox(0F, 0F, 0F, 6, 5, 6);
		DownOn.setRotationPoint(-3F, 19F, -3F);
		DownOn.setTextureSize(64, 64);
		DownOn.mirror = true;
		setRotation(DownOn, 0F, 0F, 0F);
		FrontOn = new ModelRenderer(this, 0, 26);
		FrontOn.addBox(0F, 0F, 0F, 5, 6, 6);
		FrontOn.setRotationPoint(-3F, 13F, -3F);
		FrontOn.setTextureSize(64, 64);
		FrontOn.mirror = true;
		setRotation(FrontOn, 0F, 1.570796F, 0F);
		BackOn = new ModelRenderer(this, 0, 41);
		BackOn.addBox(0F, 0F, 0F, 5, 6, 6);
		BackOn.setRotationPoint(-3F, 13F, 8F);
		BackOn.setTextureSize(64, 64);
		BackOn.mirror = true;
		setRotation(BackOn, 0F, 1.570796F, 0F);
		LeftOn = new ModelRenderer(this, 26, 0);
		LeftOn.addBox(0F, 0F, 0F, 6, 5, 6);
		LeftOn.setRotationPoint(3F, 19F, 3F);
		LeftOn.setTextureSize(64, 64);
		LeftOn.mirror = true;
		setRotation(LeftOn, 1.570796F, 1.570796F, 0F);
		RightOn = new ModelRenderer(this, 26, 26);
		RightOn.addBox(0F, 0F, 0F, 6, 5, 6);
		RightOn.setRotationPoint(-8F, 19F, 3F);
		RightOn.setTextureSize(64, 64);
		RightOn.mirror = true;
		setRotation(RightOn, 1.570796F, 1.570796F, 0F);
		UpOff = new ModelRenderer(this, 0, 0);
		UpOff.addBox(0F, 0F, 0F, 6, 0, 6);
		UpOff.setRotationPoint(-3F, 13F, -3F);
		UpOff.setTextureSize(64, 64);
		UpOff.mirror = true;
		setRotation(UpOff, 0F, 0F, 0F);
		DownOff = new ModelRenderer(this, 0, 0);
		DownOff.addBox(0F, 0F, 0F, 6, 0, 6);
		DownOff.setRotationPoint(-3F, 19F, -3F);
		DownOff.setTextureSize(64, 64);
		DownOff.mirror = true;
		setRotation(DownOff, 0F, 0F, 0F);
		FrontOff = new ModelRenderer(this, 0, 0);
		FrontOff.addBox(0F, 0F, 0F, 0, 6, 6);
		FrontOff.setRotationPoint(-3F, 13F, -3F);
		FrontOff.setTextureSize(64, 64);
		FrontOff.mirror = true;
		setRotation(FrontOff, 0F, 1.570796F, 0F);
		BackOff = new ModelRenderer(this, 0, 0);
		BackOff.addBox(0F, 0F, 0F, 0, 6, 6);
		BackOff.setRotationPoint(-3F, 13F, 3F);
		BackOff.setTextureSize(64, 64);
		BackOff.mirror = true;
		setRotation(BackOff, 0F, 1.570796F, 0F);
		LeftOff = new ModelRenderer(this, 0, 0);
		LeftOff.addBox(0F, 0F, 0F, 6, 0, 6);
		LeftOff.setRotationPoint(3F, 19F, 3F);
		LeftOff.setTextureSize(64, 64);
		LeftOff.mirror = true;
		setRotation(LeftOff, 1.570796F, 1.570796F, 0F);
		RightOff = new ModelRenderer(this, 0, 0);
		RightOff.addBox(0F, 0F, 0F, 6, 0, 6);
		RightOff.setRotationPoint(-3F, 19F, 3F);
		RightOff.setTextureSize(64, 64);
		RightOff.mirror = true;
		setRotation(RightOff, 1.570796F, 1.570796F, 0F);
	}

	@Override
	public void render(Entity entity, float f, float f1, float f2, float f3, float f4, float f5)
	{
		super.render(entity, f, f1, f2, f3, f4, f5);
		setRotationAngles(f, f1, f2, f3, f4, f5, entity);

		for(ForgeDirection orientation : ForgeDirection.VALID_DIRECTIONS) 
		{
			renderSide(orientation, true);
		}
	}

	public void renderSide(ForgeDirection orientation, boolean on) 
	{
		if(on)
		{
			switch (orientation) 
			{
				case DOWN:
					DownOn.render(0.0625F);
					break;
				case UP:
					UpOn.render(0.0625F);
					break;
				case NORTH:
					BackOn.render(0.0625F);
					break;
				case SOUTH:
					FrontOn.render(0.0625F);
					break;
				case WEST:
					RightOn.render(0.0625F);
					break;
				case EAST:
					LeftOn.render(0.0625F);
					break;
				default:
					return;
			}
		} else
		{
			switch (orientation) 
			{
				case DOWN:
					DownOff.render(0.0625F);
					break;
				case UP:
					UpOff.render(0.0625F);
					break;
				case NORTH:
					BackOff.render(0.0625F);
					break;
				case SOUTH:
					FrontOff.render(0.0625F);
					break;
				case WEST:
					RightOff.render(0.0625F);
					break;
				case EAST:
					LeftOff.render(0.0625F);
					break;
				default:
					return;
			}
		}
	}

	private void setRotation(ModelRenderer model, float x, float y, float z)
	{
		model.rotateAngleX = x;
		model.rotateAngleY = y;
		model.rotateAngleZ = z;
	}
}
