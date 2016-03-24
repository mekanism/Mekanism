package mekanism.generators.client.model;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;

public class ModelSolarGenerator extends ModelBase 
{
	ModelRenderer solarPanelPipeBase;
	ModelRenderer solarPanel;
	ModelRenderer solarPanelBottom;
	ModelRenderer solarPanelConnector;
	ModelRenderer solarPanelPipeU;
	ModelRenderer solarPanelPipeConnector;
	ModelRenderer solarPanelPipeBase1;
	ModelRenderer solarPanelPipeU1;

	public ModelSolarGenerator() 
	{
		textureWidth = 64;
		textureHeight = 64;

		solarPanelPipeBase = new ModelRenderer(this, 48, 33);
		solarPanelPipeBase.addBox(0F, 0F, 0F, 1, 6, 6);
		solarPanelPipeBase.setRotationPoint(3F, 23F, -3F);
		solarPanelPipeBase.setTextureSize(64, 64);
		solarPanelPipeBase.mirror = true;
		setRotation(solarPanelPipeBase, 0F, 0F, 1.570796F);
		solarPanel = new ModelRenderer(this, 0, 0);
		solarPanel.addBox(0F, 0F, 0F, 16, 2, 16);
		solarPanel.setRotationPoint(-8F, 13F, -8F);
		solarPanel.setTextureSize(64, 64);
		solarPanel.mirror = true;
		setRotation(solarPanel, 0F, 0F, 0F);
		solarPanelBottom = new ModelRenderer(this, 0, 18);
		solarPanelBottom.addBox(0F, 0F, 0F, 14, 1, 14);
		solarPanelBottom.setRotationPoint(-7F, 15F, -7F);
		solarPanelBottom.setTextureSize(64, 64);
		solarPanelBottom.mirror = true;
		setRotation(solarPanelBottom, 0F, 0F, 0F);
		solarPanelConnector = new ModelRenderer(this, 0, 33);
		solarPanelConnector.addBox(0F, 0F, 0F, 4, 2, 4);
		solarPanelConnector.setRotationPoint(-2F, 15F, -2F);
		solarPanelConnector.setTextureSize(64, 64);
		solarPanelConnector.mirror = true;
		setRotation(solarPanelConnector, 0F, 0F, 0F);
		solarPanelPipeU = new ModelRenderer(this, 16, 33);
		solarPanelPipeU.addBox(0F, 0F, 0F, 2, 3, 2);
		solarPanelPipeU.setRotationPoint(-1F, 19F, -1F);
		solarPanelPipeU.setTextureSize(64, 64);
		solarPanelPipeU.mirror = true;
		setRotation(solarPanelPipeU, 0F, 0F, 0F);
		solarPanelPipeConnector = new ModelRenderer(this, 24, 33);
		solarPanelPipeConnector.addBox(0F, 0F, 0F, 3, 3, 3);
		solarPanelPipeConnector.setRotationPoint(-1.5F, 18F, -1.5F);
		solarPanelPipeConnector.setTextureSize(64, 64);
		solarPanelPipeConnector.mirror = true;
		setRotation(solarPanelPipeConnector, 0F, 0F, 0F);
		solarPanelPipeBase1 = new ModelRenderer(this, 36, 33);
		solarPanelPipeBase1.addBox(0F, 0F, 0F, 1, 4, 4);
		solarPanelPipeBase1.setRotationPoint(2F, 22F, -2F);
		solarPanelPipeBase1.setTextureSize(64, 64);
		solarPanelPipeBase1.mirror = true;
		setRotation(solarPanelPipeBase1, 0F, 0F, 1.570796F);
		solarPanelPipeU1 = new ModelRenderer(this, 16, 33);
		solarPanelPipeU1.addBox(0F, 0F, 0F, 2, 3, 2);
		solarPanelPipeU1.setRotationPoint(-1F, 16F, -1F);
		solarPanelPipeU1.setTextureSize(64, 64);
		solarPanelPipeU1.mirror = true;
		setRotation(solarPanelPipeU1, 0F, 0F, 0F);
	}

	public void render(float size)
	{
		solarPanelPipeBase.render(size);
		solarPanel.render(size);
		solarPanelBottom.render(size);
		solarPanelConnector.render(size);
		solarPanelPipeU.render(size);
		solarPanelPipeConnector.render(size);
		solarPanelPipeBase1.render(size);
		solarPanelPipeU1.render(size);
	}

	private void setRotation(ModelRenderer model, float x, float y, float z) 
	{
		model.rotateAngleX = x;
		model.rotateAngleY = y;
		model.rotateAngleZ = z;
	}
}
