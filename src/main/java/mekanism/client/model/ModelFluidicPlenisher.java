package mekanism.client.model;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class ModelFluidicPlenisher extends ModelBase 
{
	ModelRenderer bearingRight;
	ModelRenderer ringTank;
	ModelRenderer portTop;
	ModelRenderer portBack;
	ModelRenderer Connector;
	ModelRenderer pipeToggle;
	ModelRenderer ringTop;
	ModelRenderer ringBottom;
	ModelRenderer tank;
	ModelRenderer bearingLeft;
	ModelRenderer connectorRing;
	ModelRenderer rod4;
	ModelRenderer rod3;
	ModelRenderer rod2;
	ModelRenderer rod1;

	public ModelFluidicPlenisher() 
	{
		textureWidth = 64;
		textureHeight = 64;

		bearingRight = new ModelRenderer(this, 44, 26);
		bearingRight.mirror = true;
		bearingRight.addBox(0F, 0F, 0F, 1, 4, 4);
		bearingRight.setRotationPoint(4F, 14F, -2F);
		bearingRight.setTextureSize(64, 64);
		setRotation(bearingRight, 0F, 0F, 0F);
		ringTank = new ModelRenderer(this, 0, 32);
		ringTank.addBox(0F, 0F, 0F, 11, 1, 11);
		ringTank.setRotationPoint(-5.5F, 10F, -5.5F);
		ringTank.setTextureSize(64, 64);
		ringTank.mirror = true;
		setRotation(ringTank, 0F, 0F, 0F);
		portTop = new ModelRenderer(this, 0, 21);
		portTop.addBox(0F, 0F, 0F, 10, 1, 10);
		portTop.setRotationPoint(-5F, 8F, -5F);
		portTop.setTextureSize(64, 64);
		portTop.mirror = true;
		setRotation(portTop, 0F, 0F, 0F);
		portBack = new ModelRenderer(this, 36, 0);
		portBack.addBox(0F, 0F, 0F, 8, 8, 1);
		portBack.setRotationPoint(-4F, 12F, 7F);
		portBack.setTextureSize(64, 64);
		portBack.mirror = true;
		setRotation(portBack, 0F, 0F, 0F);
		Connector = new ModelRenderer(this, 36, 9);
		Connector.addBox(0F, 0F, 0F, 5, 5, 4);
		Connector.setRotationPoint(-2.5F, 13.5F, 3F);
		Connector.setTextureSize(64, 64);
		Connector.mirror = true;
		setRotation(Connector, 0F, 0F, 0F);
		pipeToggle = new ModelRenderer(this, 32, 44);
		pipeToggle.addBox(0F, 0F, 0F, 6, 8, 6);
		pipeToggle.setRotationPoint(-3F, 24F, -3F);
		pipeToggle.setTextureSize(64, 64);
		pipeToggle.mirror = true;
		setRotation(pipeToggle, 0F, 0F, 0F);
		ringTop = new ModelRenderer(this, 0, 44);
		ringTop.addBox(0F, 0F, 0F, 8, 1, 8);
		ringTop.setRotationPoint(-4F, 9F, -4F);
		ringTop.setTextureSize(64, 64);
		ringTop.mirror = true;
		setRotation(ringTop, 0F, 0F, 0F);
		ringBottom = new ModelRenderer(this, 0, 53);
		ringBottom.addBox(0F, 0F, 0F, 8, 1, 8);
		ringBottom.setRotationPoint(-4F, 23F, -4F);
		ringBottom.setTextureSize(64, 64);
		ringBottom.mirror = true;
		setRotation(ringBottom, 0F, 0F, 0F);
		tank = new ModelRenderer(this, 0, 0);
		tank.addBox(0F, 0F, 0F, 9, 12, 9);
		tank.setRotationPoint(-4.5F, 11F, -4.5F);
		tank.setTextureSize(64, 64);
		tank.mirror = true;
		setRotation(tank, 0F, 0F, 0F);
		bearingLeft = new ModelRenderer(this, 44, 26);
		bearingLeft.addBox(0F, 0F, 0F, 1, 4, 4);
		bearingLeft.setRotationPoint(-5F, 14F, -2F);
		bearingLeft.setTextureSize(64, 64);
		bearingLeft.mirror = true;
		setRotation(bearingLeft, 0F, 0F, 0F);
		connectorRing = new ModelRenderer(this, 40, 18);
		connectorRing.addBox(0F, 0F, 0F, 7, 7, 1);
		connectorRing.setRotationPoint(-3.5F, 12.5F, 5F);
		connectorRing.setTextureSize(64, 64);
		connectorRing.mirror = true;
		setRotation(connectorRing, 0F, 0F, 0F);
		rod4 = new ModelRenderer(this, 0, 0);
		rod4.addBox(0F, 0F, 0F, 1, 1, 3);
		rod4.setRotationPoint(2F, 18F, 4F);
		rod4.setTextureSize(64, 64);
		rod4.mirror = true;
		setRotation(rod4, 0F, 0F, 0F);
		rod3 = new ModelRenderer(this, 0, 0);
		rod3.addBox(0F, 0F, 0F, 1, 1, 3);
		rod3.setRotationPoint(-3F, 18F, 4F);
		rod3.setTextureSize(64, 64);
		rod3.mirror = true;
		setRotation(rod3, 0F, 0F, 0F);
		rod2 = new ModelRenderer(this, 0, 0);
		rod2.addBox(0F, 0F, 0F, 1, 1, 3);
		rod2.setRotationPoint(2F, 13F, 4F);
		rod2.setTextureSize(64, 64);
		rod2.mirror = true;
		setRotation(rod2, 0F, 0F, 0F);
		rod1 = new ModelRenderer(this, 0, 0);
		rod1.addBox(0F, 0F, 0F, 1, 1, 3);
		rod1.setRotationPoint(-3F, 13F, 4F);
		rod1.setTextureSize(64, 64);
		rod1.mirror = true;
		setRotation(rod1, 0F, 0F, 0F);
	}

	public void render(float size)
	{
		render(size, false);
	}

	public void render(float size, boolean inventory)
	{
		if (!inventory) { bearingRight.render(size);
		ringTank.render(size);
		portTop.render(size);
		portBack.render(size);
		Connector.render(size);
		//pipeToggle.render(size);
		ringTop.render(size);
		ringBottom.render(size);}
		tank.render(size);
		if (!inventory) { bearingLeft.render(size);
		connectorRing.render(size);
		rod4.render(size);
		rod3.render(size);
		rod2.render(size);
		rod1.render(size);}
	}

	private void setRotation(ModelRenderer model, float x, float y, float z) 
	{
		model.rotateAngleX = x;
		model.rotateAngleY = y;
		model.rotateAngleZ = z;
	}
}
