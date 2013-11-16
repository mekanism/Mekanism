package mekanism.induction.client.model;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class ModelTeslaBottom extends ModelBase
{
	// fields
	ModelRenderer Base;
	ModelRenderer BackBottomSide;
	ModelRenderer FrontBottomSide;
	ModelRenderer SlantedFrontPanel;
	ModelRenderer SlantedPanelBase;
	ModelRenderer TopBase;
	ModelRenderer FrontTopPole;
	ModelRenderer SideTopPole;
	ModelRenderer LeftAntennae;
	ModelRenderer RightAntennae;
	ModelRenderer BackAntennae;
	ModelRenderer FrontAntennae;
	ModelRenderer TopBasePanel;
	ModelRenderer ChargePack;
	ModelRenderer WireLeftBottomPole;
	ModelRenderer WireLeftTopPole;
	ModelRenderer WireRightBottomPole;
	ModelRenderer WireRightTopPole;
	ModelRenderer BackRightConnector;
	ModelRenderer BackLeftConnector;
	ModelRenderer FrontLeftConnector;
	ModelRenderer FrontRightConnector;

	public ModelTeslaBottom()
	{
		textureWidth = 128;
		textureHeight = 128;

		Base = new ModelRenderer(this, 0, 0);
		Base.addBox(0F, 0F, 0F, 9, 10, 9);
		Base.setRotationPoint(-4.5F, 14F, -4.5F);
		Base.setTextureSize(128, 128);
		Base.mirror = true;
		setRotation(Base, 0F, 0F, 0F);
		BackBottomSide = new ModelRenderer(this, 38, 0);
		BackBottomSide.addBox(0F, 0F, 0F, 11, 7, 2);
		BackBottomSide.setRotationPoint(-5.5F, 17F, 1F);
		BackBottomSide.setTextureSize(128, 128);
		BackBottomSide.mirror = true;
		setRotation(BackBottomSide, 0F, 0F, 0F);
		FrontBottomSide = new ModelRenderer(this, 38, 0);
		FrontBottomSide.addBox(0F, 0F, 0F, 11, 7, 2);
		FrontBottomSide.setRotationPoint(-5.5F, 17F, -3F);
		FrontBottomSide.setTextureSize(128, 128);
		FrontBottomSide.mirror = true;
		setRotation(FrontBottomSide, 0F, 0F, 0F);
		SlantedFrontPanel = new ModelRenderer(this, 38, 10);
		SlantedFrontPanel.addBox(0F, 0F, 0F, 4, 6, 2);
		SlantedFrontPanel.setRotationPoint(-2F, 17F, -4F);
		SlantedFrontPanel.setTextureSize(128, 128);
		SlantedFrontPanel.mirror = true;
		setRotation(SlantedFrontPanel, -0.4234231F, 0F, 0F);
		SlantedPanelBase = new ModelRenderer(this, 51, 10);
		SlantedPanelBase.addBox(0F, 0F, 0F, 6, 3, 2);
		SlantedPanelBase.setRotationPoint(-3F, 21F, -6.5F);
		SlantedPanelBase.setTextureSize(128, 128);
		SlantedPanelBase.mirror = true;
		setRotation(SlantedPanelBase, 0F, 0F, 0F);
		TopBase = new ModelRenderer(this, 0, 20);
		TopBase.addBox(0F, 0F, 0F, 6, 5, 6);
		TopBase.setRotationPoint(-3F, 9F, -3F);
		TopBase.setTextureSize(128, 128);
		TopBase.mirror = true;
		setRotation(TopBase, 0F, 0F, 0F);
		FrontTopPole = new ModelRenderer(this, 0, 32);
		FrontTopPole.addBox(0F, 0F, 0F, 2, 2, 8);
		FrontTopPole.setRotationPoint(-1F, 10F, -4F);
		FrontTopPole.setTextureSize(128, 128);
		FrontTopPole.mirror = true;
		setRotation(FrontTopPole, 0F, 0F, 0F);
		SideTopPole = new ModelRenderer(this, 0, 43);
		SideTopPole.addBox(0F, 0F, 0F, 8, 2, 2);
		SideTopPole.setRotationPoint(-4F, 10F, -1F);
		SideTopPole.setTextureSize(128, 128);
		SideTopPole.mirror = true;
		setRotation(SideTopPole, 0F, 0F, 0F);
		LeftAntennae = new ModelRenderer(this, 25, 20);
		LeftAntennae.addBox(0F, 0F, 0F, 1, 3, 1);
		LeftAntennae.setRotationPoint(-4.5F, 8.8F, -0.5F);
		LeftAntennae.setTextureSize(128, 128);
		LeftAntennae.mirror = true;
		setRotation(LeftAntennae, 0F, 0F, 0F);
		RightAntennae = new ModelRenderer(this, 30, 20);
		RightAntennae.addBox(0F, 0F, 0F, 1, 3, 1);
		RightAntennae.setRotationPoint(3.5F, 8.8F, -0.5F);
		RightAntennae.setTextureSize(128, 128);
		RightAntennae.mirror = true;
		setRotation(RightAntennae, 0F, 0F, 0F);
		BackAntennae = new ModelRenderer(this, 25, 25);
		BackAntennae.addBox(0F, 0F, 0F, 1, 3, 1);
		BackAntennae.setRotationPoint(-0.5F, 8.8F, 3.5F);
		BackAntennae.setTextureSize(128, 128);
		BackAntennae.mirror = true;
		setRotation(BackAntennae, 0F, 0F, 0F);
		FrontAntennae = new ModelRenderer(this, 30, 25);
		FrontAntennae.addBox(0F, 0F, 0F, 1, 3, 1);
		FrontAntennae.setRotationPoint(-0.5F, 8.8F, -4.5F);
		FrontAntennae.setTextureSize(128, 128);
		FrontAntennae.mirror = true;
		setRotation(FrontAntennae, 0F, 0F, 0F);
		TopBasePanel = new ModelRenderer(this, 36, 20);
		TopBasePanel.addBox(0F, 0F, 0F, 7, 1, 7);
		TopBasePanel.setRotationPoint(-3.5F, 13F, -3.5F);
		TopBasePanel.setTextureSize(128, 128);
		TopBasePanel.mirror = true;
		setRotation(TopBasePanel, 0F, 0F, 0F);
		ChargePack = new ModelRenderer(this, 37, 29);
		ChargePack.addBox(0F, 0F, 0F, 6, 7, 3);
		ChargePack.setRotationPoint(-3F, 17F, 3.5F);
		ChargePack.setTextureSize(128, 128);
		ChargePack.mirror = true;
		setRotation(ChargePack, 0F, 0F, 0F);
		WireLeftBottomPole = new ModelRenderer(this, 21, 32);
		WireLeftBottomPole.addBox(0F, 0F, 0F, 1, 10, 1);
		WireLeftBottomPole.setRotationPoint(-2F, 11.86667F, 6F);
		WireLeftBottomPole.setTextureSize(128, 128);
		WireLeftBottomPole.mirror = true;
		setRotation(WireLeftBottomPole, 0F, 0F, 0F);
		WireLeftTopPole = new ModelRenderer(this, 26, 32);
		WireLeftTopPole.addBox(0F, 0F, 0F, 1, 1, 4);
		WireLeftTopPole.setRotationPoint(-2F, 10.86667F, 3F);
		WireLeftTopPole.setTextureSize(128, 128);
		WireLeftTopPole.mirror = true;
		setRotation(WireLeftTopPole, 0F, 0F, 0F);
		WireRightBottomPole = new ModelRenderer(this, 21, 32);
		WireRightBottomPole.addBox(0F, 0F, 0F, 1, 10, 1);
		WireRightBottomPole.setRotationPoint(1F, 11.86667F, 6F);
		WireRightBottomPole.setTextureSize(128, 128);
		WireRightBottomPole.mirror = true;
		setRotation(WireRightBottomPole, 0F, 0F, 0F);
		WireRightTopPole = new ModelRenderer(this, 26, 38);
		WireRightTopPole.addBox(0F, 0F, 0F, 1, 1, 4);
		WireRightTopPole.setRotationPoint(1F, 10.86667F, 3F);
		WireRightTopPole.setTextureSize(128, 128);
		WireRightTopPole.mirror = true;
		setRotation(WireRightTopPole, 0F, 0F, 0F);
		BackRightConnector = new ModelRenderer(this, 65, 0);
		BackRightConnector.addBox(0F, 0F, 0F, 1, 1, 1);
		BackRightConnector.setRotationPoint(1F, 8F, 1.066667F);
		BackRightConnector.setTextureSize(128, 128);
		BackRightConnector.mirror = true;
		setRotation(BackRightConnector, 0F, 0F, 0F);
		BackLeftConnector = new ModelRenderer(this, 65, 0);
		BackLeftConnector.addBox(0F, 0F, 0F, 1, 1, 1);
		BackLeftConnector.setRotationPoint(-2F, 8F, 1F);
		BackLeftConnector.setTextureSize(128, 128);
		BackLeftConnector.mirror = true;
		setRotation(BackLeftConnector, 0F, 0F, 0F);
		FrontLeftConnector = new ModelRenderer(this, 65, 0);
		FrontLeftConnector.addBox(0F, 0F, 0F, 1, 1, 1);
		FrontLeftConnector.setRotationPoint(-2F, 8F, -2F);
		FrontLeftConnector.setTextureSize(128, 128);
		FrontLeftConnector.mirror = true;
		setRotation(FrontLeftConnector, 0F, 0F, 0F);
		FrontRightConnector = new ModelRenderer(this, 65, 0);
		FrontRightConnector.addBox(0F, 0F, 0F, 1, 1, 1);
		FrontRightConnector.setRotationPoint(1F, 8F, -2F);
		FrontRightConnector.setTextureSize(128, 128);
		FrontRightConnector.mirror = true;
		setRotation(FrontRightConnector, 0F, 0F, 0F);
	}

	public void render(float f5)
	{
		Base.render(f5);
		BackBottomSide.render(f5);
		FrontBottomSide.render(f5);
		SlantedFrontPanel.render(f5);
		SlantedPanelBase.render(f5);
		TopBase.render(f5);
		FrontTopPole.render(f5);
		SideTopPole.render(f5);
		LeftAntennae.render(f5);
		RightAntennae.render(f5);
		BackAntennae.render(f5);
		FrontAntennae.render(f5);
		TopBasePanel.render(f5);
		ChargePack.render(f5);
		WireLeftBottomPole.render(f5);
		WireLeftTopPole.render(f5);
		WireRightBottomPole.render(f5);
		WireRightTopPole.render(f5);
		BackRightConnector.render(f5);
		BackLeftConnector.render(f5);
		FrontLeftConnector.render(f5);
		FrontRightConnector.render(f5);
	}

	private void setRotation(ModelRenderer model, float x, float y, float z)
	{
		model.rotateAngleX = x;
		model.rotateAngleY = y;
		model.rotateAngleZ = z;
	}
}
