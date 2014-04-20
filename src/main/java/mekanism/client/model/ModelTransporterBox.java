package mekanism.client.model;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class ModelTransporterBox extends ModelBase
{
	ModelRenderer box;

	public ModelTransporterBox()
	{
		textureWidth = 64;
		textureHeight = 64;

		box = new ModelRenderer(this, 0, 0);
		box.addBox(0F, 0F, 0F, 7, 7, 7);
		box.setRotationPoint(-3.5F, 0, -3.5F);
		box.setTextureSize(64, 64);
		box.mirror = true;
		setRotation(box, 0F, 0F, 0F);
	}

	public void render(float size)
	{
		box.render(size);
	}

	private void setRotation(ModelRenderer model, float x, float y, float z)
	{
		model.rotateAngleX = x;
		model.rotateAngleY = y;
		model.rotateAngleZ = z;
	}
}
