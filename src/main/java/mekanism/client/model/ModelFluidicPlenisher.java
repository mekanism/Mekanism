package mekanism.client.model;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class ModelFluidicPlenisher extends ModelBase 
{
	ModelRenderer PumpBase;
	ModelRenderer PumpTube;
	ModelRenderer CoreStart;
	ModelRenderer Core;
	ModelRenderer FluidOutput;
	ModelRenderer EnergyInput;
	ModelRenderer EnergyTube;
	ModelRenderer Screen;
	ModelRenderer Keyboard;
	ModelRenderer RightThing;
	ModelRenderer LeftThing;
	ModelRenderer RightCable;
	ModelRenderer LeftCable;
	ModelRenderer LeftConnector;
	ModelRenderer RightConnector;

	public ModelFluidicPlenisher() 
	{
		textureWidth = 128;
		textureHeight = 128;

		PumpBase = new ModelRenderer(this, 0, 29);
		PumpBase.addBox(0F, 0F, 0F, 6, 1, 6);
		PumpBase.setRotationPoint(-3F, 23F, -3F);
		PumpBase.setTextureSize(128, 128);
		PumpBase.mirror = true;
		setRotation(PumpBase, 0F, 0F, 0F);
		PumpTube = new ModelRenderer(this, 40, 5);
		PumpTube.addBox(0F, 0F, 0F, 2, 3, 2);
		PumpTube.setRotationPoint(-1F, 20F, -1F);
		PumpTube.setTextureSize(128, 128);
		PumpTube.mirror = true;
		setRotation(PumpTube, 0F, 0F, 0F);
		CoreStart = new ModelRenderer(this, 40, 0);
		CoreStart.addBox(0F, 0F, 0F, 4, 1, 4);
		CoreStart.setRotationPoint(-2F, 19F, -2F);
		CoreStart.setTextureSize(128, 128);
		CoreStart.mirror = true;
		setRotation(CoreStart, 0F, 0F, 0F);
		Core = new ModelRenderer(this, 0, 0);
		Core.addBox(0F, 0F, 0F, 10, 8, 10);
		Core.setRotationPoint(-5F, 11F, -5F);
		Core.setTextureSize(128, 128);
		Core.mirror = true;
		setRotation(Core, 0F, 0F, 0F);
		FluidOutput = new ModelRenderer(this, 0, 18);
		FluidOutput.addBox(0F, 0F, 0F, 8, 3, 8);
		FluidOutput.setRotationPoint(-4F, 8F, -4F);
		FluidOutput.setTextureSize(128, 128);
		FluidOutput.mirror = true;
		setRotation(FluidOutput, 0F, 0F, 0F);
		EnergyInput = new ModelRenderer(this, 0, 36);
		EnergyInput.addBox(0F, 0F, 0F, 6, 6, 1);
		EnergyInput.setRotationPoint(-3F, 13F, 7F);
		EnergyInput.setTextureSize(128, 128);
		EnergyInput.mirror = true;
		setRotation(EnergyInput, 0F, 0F, 0F);
		EnergyTube = new ModelRenderer(this, 14, 36);
		EnergyTube.addBox(0F, 0F, 0F, 2, 2, 2);
		EnergyTube.setRotationPoint(-1F, 15F, 5F);
		EnergyTube.setTextureSize(128, 128);
		EnergyTube.mirror = true;
		setRotation(EnergyTube, 0F, 0F, 0F);
		Screen = new ModelRenderer(this, 40, 17);
		Screen.addBox(0F, 0F, 0F, 8, 5, 1);
		Screen.setRotationPoint(-4F, 12F, -6F);
		Screen.setTextureSize(128, 128);
		Screen.mirror = true;
		setRotation(Screen, 0F, 0F, 0F);
		Keyboard = new ModelRenderer(this, 40, 13);
		Keyboard.addBox(0F, 0F, 0F, 8, 1, 3);
		Keyboard.setRotationPoint(-4F, 17F, -8F);
		Keyboard.setTextureSize(128, 128);
		Keyboard.mirror = true;
		setRotation(Keyboard, 0.3490659F, 0F, 0F);
		RightThing = new ModelRenderer(this, 24, 29);
		RightThing.addBox(0F, 0F, 0F, 1, 4, 4);
		RightThing.setRotationPoint(-6F, 13F, -2F);
		RightThing.setTextureSize(128, 128);
		RightThing.mirror = true;
		setRotation(RightThing, 0F, 0F, 0F);
		LeftThing = new ModelRenderer(this, 24, 29);
		LeftThing.addBox(0F, 0F, 0F, 1, 4, 4);
		LeftThing.setRotationPoint(5F, 13F, -2F);
		LeftThing.setTextureSize(128, 128);
		LeftThing.mirror = true;
		setRotation(LeftThing, 0F, 0F, 0F);
		RightCable = new ModelRenderer(this, 32, 18);
		RightCable.addBox(0F, 0F, 0F, 1, 3, 2);
		RightCable.setRotationPoint(-6F, 17F, -1F);
		RightCable.setTextureSize(128, 128);
		RightCable.mirror = true;
		setRotation(RightCable, 0F, 0F, 0F);
		LeftCable = new ModelRenderer(this, 32, 18);
		LeftCable.addBox(0F, 0F, 0F, 1, 3, 2);
		LeftCable.setRotationPoint(5F, 17F, -1F);
		LeftCable.setTextureSize(128, 128);
		LeftCable.mirror = true;
		setRotation(LeftCable, 0F, 0F, 0F);
		LeftConnector = new ModelRenderer(this, 40, 10);
		LeftConnector.addBox(0F, 0F, 0F, 3, 1, 2);
		LeftConnector.setRotationPoint(2F, 19F, -1F);
		LeftConnector.setTextureSize(128, 128);
		LeftConnector.mirror = true;
		setRotation(LeftConnector, 0F, 0F, 0F);
		RightConnector = new ModelRenderer(this, 40, 10);
		RightConnector.addBox(0F, 0F, 0F, 3, 1, 2);
		RightConnector.setRotationPoint(-5F, 19F, -1F);
		RightConnector.setTextureSize(128, 128);
		RightConnector.mirror = true;
		setRotation(RightConnector, 0F, 0F, 0F);
	}

	public void render(float size)
	{
		PumpBase.render(size);
		PumpTube.render(size);
		CoreStart.render(size);
		Core.render(size);
		FluidOutput.render(size);
		EnergyInput.render(size);
		EnergyTube.render(size);
		Screen.render(size);
		Keyboard.render(size);
		RightThing.render(size);
		LeftThing.render(size);
		RightCable.render(size);
		LeftCable.render(size);
		LeftConnector.render(size);
		RightConnector.render(size);
	}

	private void setRotation(ModelRenderer model, float x, float y, float z) 
	{
		model.rotateAngleX = x;
		model.rotateAngleY = y;
		model.rotateAngleZ = z;
	}
}
