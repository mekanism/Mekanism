package mekanism.client.model;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class ModelPressurizedReactionChamber extends ModelBase 
{
	ModelRenderer Base;
	ModelRenderer GasRight;
	ModelRenderer GasLeft;
	ModelRenderer GasConnector;
	ModelRenderer FluidBack;
	ModelRenderer CoreBase;
	ModelRenderer Core;
	ModelRenderer PoleRF;
	ModelRenderer PoleFL;
	ModelRenderer PoleLB;
	ModelRenderer PoleBR;
	ModelRenderer PoleR;
	ModelRenderer PoleL;
	ModelRenderer FrontPanel;
	ModelRenderer TubeThing;
	ModelRenderer CenterCore;
	ModelRenderer BackConnector;
	ModelRenderer BackCore;
	ModelRenderer TopPanel;

	public ModelPressurizedReactionChamber()
	{
		textureWidth = 128;
		textureHeight = 128;

		Base = new ModelRenderer(this, 0, 0);
		Base.addBox(0F, 0F, 0F, 16, 1, 16);
		Base.setRotationPoint(-8F, 23F, -8F);
		Base.setTextureSize(128, 128);
		Base.mirror = true;
		setRotation(Base, 0F, 0F, 0F);
		GasRight = new ModelRenderer(this, 64, 12);
		GasRight.addBox(0F, 0F, 0F, 1, 6, 6);
		GasRight.setRotationPoint(-8F, 13F, -3F);
		GasRight.setTextureSize(128, 128);
		GasRight.mirror = true;
		setRotation(GasRight, 0F, 0F, 0F);
		GasLeft = new ModelRenderer(this, 64, 12);
		GasLeft.addBox(0F, 0F, 0F, 1, 6, 6);
		GasLeft.setRotationPoint(7F, 13F, -3F);
		GasLeft.setTextureSize(128, 128);
		GasLeft.mirror = true;
		setRotation(GasLeft, 0F, 0F, 0F);
		GasConnector = new ModelRenderer(this, 22, 38);
		GasConnector.addBox(0F, 0F, 0F, 14, 2, 2);
		GasConnector.setRotationPoint(-7F, 15F, -1F);
		GasConnector.setTextureSize(128, 128);
		GasConnector.mirror = true;
		setRotation(GasConnector, 0F, 0F, 0F);
		FluidBack = new ModelRenderer(this, 50, 24);
		FluidBack.addBox(0F, 0F, 0F, 8, 8, 1);
		FluidBack.setRotationPoint(-4F, 12F, 7F);
		FluidBack.setTextureSize(128, 128);
		FluidBack.mirror = true;
		setRotation(FluidBack, 0F, 0F, 0F);
		CoreBase = new ModelRenderer(this, 64, 0);
		CoreBase.addBox(0F, 0F, 0F, 10, 1, 11);
		CoreBase.setRotationPoint(-5F, 22F, -6F);
		CoreBase.setTextureSize(128, 128);
		CoreBase.mirror = true;
		setRotation(CoreBase, 0F, 0F, 0F);
		Core = new ModelRenderer(this, 0, 17);
		Core.addBox(0F, 0F, 0F, 12, 8, 13);
		Core.setRotationPoint(-6F, 14F, -7F);
		Core.setTextureSize(128, 128);
		Core.mirror = true;
		setRotation(Core, 0F, 0F, 0F);
		PoleRF = new ModelRenderer(this, 38, 42);
		PoleRF.addBox(0F, 0F, 0F, 1, 6, 1);
		PoleRF.setRotationPoint(-6F, 8F, -7F);
		PoleRF.setTextureSize(128, 128);
		PoleRF.mirror = true;
		setRotation(PoleRF, 0F, 0F, 0F);
		PoleFL = new ModelRenderer(this, 38, 42);
		PoleFL.addBox(0F, 0F, 0F, 1, 6, 1);
		PoleFL.setRotationPoint(5F, 8F, -7F);
		PoleFL.setTextureSize(128, 128);
		PoleFL.mirror = true;
		setRotation(PoleFL, 0F, 0F, 0F);
		PoleLB = new ModelRenderer(this, 38, 42);
		PoleLB.addBox(0F, 0F, 0F, 1, 6, 1);
		PoleLB.setRotationPoint(5F, 8F, 5F);
		PoleLB.setTextureSize(128, 128);
		PoleLB.mirror = true;
		setRotation(PoleLB, 0F, 0F, 0F);
		PoleBR = new ModelRenderer(this, 38, 42);
		PoleBR.addBox(0F, -2F, 0F, 1, 6, 1);
		PoleBR.setRotationPoint(-6F, 10F, 5F);
		PoleBR.setTextureSize(128, 128);
		PoleBR.mirror = true;
		setRotation(PoleBR, 0F, 0F, 0F);
		PoleR = new ModelRenderer(this, 0, 57);
		PoleR.addBox(0F, 0F, 0F, 1, 1, 11);
		PoleR.setRotationPoint(-6F, 8F, -6F);
		PoleR.setTextureSize(128, 128);
		PoleR.mirror = true;
		setRotation(PoleR, 0F, 0F, 0F);
		PoleL = new ModelRenderer(this, 0, 57);
		PoleL.addBox(0F, 0F, 0F, 1, 1, 11);
		PoleL.setRotationPoint(5F, 8F, -6F);
		PoleL.setTextureSize(128, 128);
		PoleL.mirror = true;
		setRotation(PoleL, 0F, 0F, 0F);
		FrontPanel = new ModelRenderer(this, 0, 38);
		FrontPanel.addBox(0F, 0F, 0F, 10, 12, 1);
		FrontPanel.setRotationPoint(-5F, 9F, -8F);
		FrontPanel.setTextureSize(128, 128);
		FrontPanel.mirror = true;
		setRotation(FrontPanel, 0F, 0F, 0F);
		TubeThing = new ModelRenderer(this, 22, 42);
		TubeThing.addBox(0F, 0F, 0F, 2, 2, 6);
		TubeThing.setRotationPoint(-1F, 12F, -7F);
		TubeThing.setTextureSize(128, 128);
		TubeThing.mirror = true;
		setRotation(TubeThing, 0F, 0F, 0F);
		CenterCore = new ModelRenderer(this, 84, 12);
		CenterCore.addBox(0F, 0F, 0F, 4, 1, 4);
		CenterCore.setRotationPoint(-2F, 13F, -3F);
		CenterCore.setTextureSize(128, 128);
		CenterCore.mirror = true;
		setRotation(CenterCore, 0F, 0F, 0F);
		BackConnector = new ModelRenderer(this, 78, 12);
		BackConnector.addBox(0F, 0F, 0F, 2, 5, 1);
		BackConnector.setRotationPoint(-1F, 13F, 6F);
		BackConnector.setTextureSize(128, 128);
		BackConnector.mirror = true;
		setRotation(BackConnector, 0F, 0F, 0F);
		BackCore = new ModelRenderer(this, 0, 51);
		BackCore.addBox(0F, 0F, 0F, 2, 1, 5);
		BackCore.setRotationPoint(-1F, 13F, 1F);
		BackCore.setTextureSize(128, 128);
		BackCore.mirror = true;
		setRotation(BackCore, 0F, 0F, 0F);
		TopPanel = new ModelRenderer(this, 0, 69);
		TopPanel.addBox(0F, 0F, 0F, 10, 1, 13);
		TopPanel.setRotationPoint(-5F, 8F, -7F);
		TopPanel.setTextureSize(128, 128);
		TopPanel.mirror = true;
		setRotation(TopPanel, 0F, 0F, 0F);
	}

	public void render(float size)
	{
		Base.render(size);
		GasRight.render(size);
		GasLeft.render(size);
		GasConnector.render(size);
		FluidBack.render(size);
		CoreBase.render(size);
		Core.render(size);
		PoleRF.render(size);
		PoleFL.render(size);
		PoleLB.render(size);
		PoleBR.render(size);
		PoleR.render(size);
		PoleL.render(size);
		FrontPanel.render(size);
		TubeThing.render(size);
		CenterCore.render(size);
		BackConnector.render(size);
		BackCore.render(size);
		TopPanel.render(size);
	}

	private void setRotation(ModelRenderer model, float x, float y, float z) 
	{
		model.rotateAngleX = x;
		model.rotateAngleY = y;
		model.rotateAngleZ = z;
	}
}
