package mekanism.client.model;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class ModelElectricPump extends ModelBase
{
	ModelRenderer pumpRingTop;
	ModelRenderer pumpPortTop;
	ModelRenderer pumpCasing;
	ModelRenderer pumpBase;
	ModelRenderer powerPort;
	ModelRenderer powerConnector;
	ModelRenderer powerConnectorFrame4;
	ModelRenderer powerConnectorFrame3;
	ModelRenderer powerConnectorFrame2;
	ModelRenderer powerConnectorFrame1;
	ModelRenderer pipeToggleBack;
	ModelRenderer pipeToggleRingBack;
	ModelRenderer pipeTogglePortBack;
	ModelRenderer pipeToggleLeft;
	ModelRenderer pipeToggleRingLeft;
	ModelRenderer pipeTogglePortLeft;
	ModelRenderer pipeToggleRight;
	ModelRenderer pipeToggleRingRight;
	ModelRenderer pipeTogglePortRight;
	ModelRenderer pumpPipe;

	public ModelElectricPump() 
	{
		textureWidth = 128;
		textureHeight = 64;

		pumpRingTop = new ModelRenderer(this, 68, 9);
		pumpRingTop.addBox(0F, 0F, 0F, 8, 1, 8);
		pumpRingTop.setRotationPoint(-4F, 10F, -4F);
		pumpRingTop.setTextureSize(128, 64);
		pumpRingTop.mirror = true;
		setRotation(pumpRingTop, 0F, 0F, 0F);
		pumpPortTop = new ModelRenderer(this, 68, 0);
		pumpPortTop.addBox(0F, 0F, 0F, 8, 1, 8);
		pumpPortTop.setRotationPoint(-4F, 8F, -4F);
		pumpPortTop.setTextureSize(128, 64);
		pumpPortTop.mirror = true;
		setRotation(pumpPortTop, 0F, 0F, 0F);
		pumpCasing = new ModelRenderer(this, 0, 0);
		pumpCasing.addBox(0F, 0F, 0F, 7, 12, 7);
		pumpCasing.setRotationPoint(-3.5F, 11F, -3.5F);
		pumpCasing.setTextureSize(128, 64);
		pumpCasing.mirror = true;
		setRotation(pumpCasing, 0F, 0F, 0F);
		pumpBase = new ModelRenderer(this, 28, 0);
		pumpBase.addBox(0F, 0F, 0F, 6, 15, 6);
		pumpBase.setRotationPoint(-3F, 9F, -3F);
		pumpBase.setTextureSize(128, 64);
		pumpBase.mirror = true;
		setRotation(pumpBase, 0F, 0F, 0F);
		powerPort = new ModelRenderer(this, 38, 29);
		powerPort.addBox(0F, 0F, 0F, 8, 8, 1);
		powerPort.setRotationPoint(-4F, 12F, -8F);
		powerPort.setTextureSize(128, 64);
		powerPort.mirror = true;
		setRotation(powerPort, 0F, 0F, 0F);
		powerConnector = new ModelRenderer(this, 0, 29);
		powerConnector.addBox(0F, 0F, 0F, 5, 5, 4);
		powerConnector.setRotationPoint(-2.5F, 13.5F, -7F);
		powerConnector.setTextureSize(128, 64);
		powerConnector.mirror = true;
		setRotation(powerConnector, 0F, 0F, 0F);
		powerConnectorFrame4 = new ModelRenderer(this, 38, 21);
		powerConnectorFrame4.addBox(0F, 0F, 0F, 1, 1, 4);
		powerConnectorFrame4.setRotationPoint(2F, 18F, -7F);
		powerConnectorFrame4.setTextureSize(128, 64);
		powerConnectorFrame4.mirror = true;
		setRotation(powerConnectorFrame4, 0F, 0F, 0F);
		powerConnectorFrame3 = new ModelRenderer(this, 38, 21);
		powerConnectorFrame3.addBox(0F, 0F, 0F, 1, 1, 4);
		powerConnectorFrame3.setRotationPoint(-3F, 18F, -7F);
		powerConnectorFrame3.setTextureSize(128, 64);
		powerConnectorFrame3.mirror = true;
		setRotation(powerConnectorFrame3, 0F, 0F, 0F);
		powerConnectorFrame2 = new ModelRenderer(this, 38, 21);
		powerConnectorFrame2.addBox(0F, 0F, 0F, 1, 1, 4);
		powerConnectorFrame2.setRotationPoint(2F, 13F, -7F);
		powerConnectorFrame2.setTextureSize(128, 64);
		powerConnectorFrame2.mirror = true;
		setRotation(powerConnectorFrame2, 0F, 0F, 0F);
		powerConnectorFrame1 = new ModelRenderer(this, 38, 21);
		powerConnectorFrame1.addBox(0F, 0F, 0F, 1, 1, 4);
		powerConnectorFrame1.setRotationPoint(-3F, 13F, -7F);
		powerConnectorFrame1.setTextureSize(128, 64);
		powerConnectorFrame1.mirror = true;
		setRotation(powerConnectorFrame1, 0F, 0F, 0F);
		pipeToggleBack = new ModelRenderer(this, 0, 19);
		pipeToggleBack.addBox(0F, 0F, 0F, 6, 6, 4);
		pipeToggleBack.setRotationPoint(-3F, 13F, 3F);
		pipeToggleBack.setTextureSize(128, 64);
		pipeToggleBack.mirror = true;
		setRotation(pipeToggleBack, 0F, 0F, 0F);
		pipeToggleRingBack = new ModelRenderer(this, 18, 30);
		pipeToggleRingBack.addBox(0F, 0F, 0F, 7, 7, 1);
		pipeToggleRingBack.setRotationPoint(-3.5F, 12.5F, 5F);
		pipeToggleRingBack.setTextureSize(128, 64);
		pipeToggleRingBack.mirror = true;
		setRotation(pipeToggleRingBack, 0F, 0F, 0F);
		pipeTogglePortBack = new ModelRenderer(this, 20, 21);
		pipeTogglePortBack.addBox(0F, 0F, 0F, 8, 8, 1);
		pipeTogglePortBack.setRotationPoint(-4F, 12F, 7F);
		pipeTogglePortBack.setTextureSize(128, 64);
		pipeTogglePortBack.mirror = true;
		setRotation(pipeTogglePortBack, 0F, 0F, 0F);
		pipeToggleLeft = new ModelRenderer(this, 34, 38);
		pipeToggleLeft.addBox(0F, 0F, 0F, 4, 6, 6);
		pipeToggleLeft.setRotationPoint(3F, 13F, -3F);
		pipeToggleLeft.setTextureSize(128, 64);
		pipeToggleLeft.mirror = true;
		setRotation(pipeToggleLeft, 0F, 0F, 0F);
		pipeToggleRingLeft = new ModelRenderer(this, 18, 38);
		pipeToggleRingLeft.addBox(0F, 0F, 0F, 1, 7, 7);
		pipeToggleRingLeft.setRotationPoint(5F, 12.5F, -3.5F);
		pipeToggleRingLeft.setTextureSize(128, 64);
		pipeToggleRingLeft.mirror = true;
		setRotation(pipeToggleRingLeft, 0F, 0F, 0F);
		pipeTogglePortLeft = new ModelRenderer(this, 0, 38);
		pipeTogglePortLeft.addBox(0F, 0F, 0F, 1, 8, 8);
		pipeTogglePortLeft.setRotationPoint(7F, 12F, -4F);
		pipeTogglePortLeft.setTextureSize(128, 64);
		pipeTogglePortLeft.mirror = true;
		setRotation(pipeTogglePortLeft, 0F, 0F, 0F);
		pipeToggleRight = new ModelRenderer(this, 34, 38);
		pipeToggleRight.mirror = true;
		pipeToggleRight.addBox(0F, 0F, 0F, 4, 6, 6);
		pipeToggleRight.setRotationPoint(-7F, 13F, -3F);
		pipeToggleRight.setTextureSize(128, 64);
		setRotation(pipeToggleRight, 0F, 0F, 0F);
		pipeToggleRingRight = new ModelRenderer(this, 18, 38);
		pipeToggleRingRight.addBox(0F, 0F, 0F, 1, 7, 7);
		pipeToggleRingRight.setRotationPoint(-6F, 12.5F, -3.5F);
		pipeToggleRingRight.setTextureSize(128, 64);
		pipeToggleRingRight.mirror = true;
		setRotation(pipeToggleRingRight, 0F, 0F, 0F);
		pipeTogglePortRight = new ModelRenderer(this, 0, 38);
		pipeTogglePortRight.addBox(0F, 0F, 0F, 1, 8, 8);
		pipeTogglePortRight.setRotationPoint(-8F, 12F, -4F);
		pipeTogglePortRight.setTextureSize(128, 64);
		pipeTogglePortRight.mirror = true;
		setRotation(pipeTogglePortRight, 0F, 0F, 0F);
		pumpPipe = new ModelRenderer(this, 52, 0);
		pumpPipe.addBox(0F, 0F, 0F, 4, 8, 4);
		pumpPipe.setRotationPoint(-2F, 24F, -2F);
		pumpPipe.setTextureSize(128, 64);
		pumpPipe.mirror = true;
		setRotation(pumpPipe, 0F, 0F, 0F);
	}

	public void render(float size)
	{
		render(size, false);
	}

	public void render(float size, boolean inventory)
	{
		if (!inventory) pumpRingTop.render(size);
		pumpPortTop.render(size);
		pumpCasing.render(size);
		pumpBase.render(size);
		powerPort.render(size);
		powerConnector.render(size);
		if (!inventory) { powerConnectorFrame4.render(size);
		powerConnectorFrame3.render(size);
		powerConnectorFrame2.render(size);
		powerConnectorFrame1.render(size);}
		/*pipeToggleBack.render(size);
		pipeToggleRingBack.render(size);
		pipeTogglePortBack.render(size);
		pipeToggleLeft.render(size);
		pipeToggleRingLeft.render(size);
		pipeTogglePortLeft.render(size);
		pipeToggleRight.render(size);
		pipeToggleRingRight.render(size);
		pipeTogglePortRight.render(size);
		pumpPipe.render(size);*/
	}

	private void setRotation(ModelRenderer model, float x, float y, float z)
	{
		model.rotateAngleX = x;
		model.rotateAngleY = y;
		model.rotateAngleZ = z;
	}
}
