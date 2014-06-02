package mekanism.client.model;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class ModelElectricPump extends ModelBase
{
	ModelRenderer PumpHead;
	ModelRenderer Connector;
	ModelRenderer Panel1;
	ModelRenderer Panel2;
	ModelRenderer Panel3;
	ModelRenderer Body;
	ModelRenderer Axil;
	ModelRenderer Axil2;
	ModelRenderer Ring1;
	ModelRenderer Ring2;
	ModelRenderer Plug;
	ModelRenderer Ring3;

	public ModelElectricPump()
	{
		textureWidth = 64;
		textureHeight = 64;

		PumpHead = new ModelRenderer(this, 33, 0);
		PumpHead.addBox(0F, 0F, 0F, 6, 3, 6);
		PumpHead.setRotationPoint(-3F, 19F, -3F);
		PumpHead.setTextureSize(64, 64);
		PumpHead.mirror = true;
		setRotation(PumpHead, 0F, 0F, 0F);
		Connector = new ModelRenderer(this, 33, 10);
		Connector.addBox(0F, 0F, 0F, 4, 1, 5);
		Connector.setRotationPoint(-2F, 14F, 2.5F);
		Connector.setTextureSize(64, 64);
		Connector.mirror = true;
		setRotation(Connector, 0F, 0F, 0F);
		Panel1 = new ModelRenderer(this, 0, 15);
		Panel1.addBox(-3F, -6F, 0F, 6, 6, 1);
		Panel1.setRotationPoint(0F, 21F, -3F);
		Panel1.setTextureSize(64, 64);
		Panel1.mirror = true;
		setRotation(Panel1, 0.5585054F, 0F, 0F);
		Panel2 = new ModelRenderer(this, 15, 15);
		Panel2.addBox(0F, -6F, -3F, 1, 6, 6);
		Panel2.setRotationPoint(-3F, 21F, 0F);
		Panel2.setTextureSize(64, 64);
		Panel2.mirror = true;
		setRotation(Panel2, 0F, 0F, -0.5585054F);
		Panel3 = new ModelRenderer(this, 15, 15);
		Panel3.addBox(-1F, -6F, -3F, 1, 6, 6);
		Panel3.setRotationPoint(3F, 21F, 0F);
		Panel3.setTextureSize(64, 64);
		Panel3.mirror = true;
		setRotation(Panel3, 0F, 0F, 0.5585054F);
		Body = new ModelRenderer(this, 30, 17);
		Body.addBox(0F, 0F, 0F, 6, 10, 6);
		Body.setRotationPoint(-3F, 9F, -3F);
		Body.setTextureSize(64, 64);
		Body.mirror = true;
		setRotation(Body, 0F, 0F, 0F);
		Axil = new ModelRenderer(this, 0, 28);
		Axil.addBox(0F, 0F, 0F, 9, 2, 4);
		Axil.setRotationPoint(-4.5F, 17F, -2F);
		Axil.setTextureSize(64, 64);
		Axil.mirror = true;
		setRotation(Axil, 0F, 0F, 0F);
		Axil2 = new ModelRenderer(this, 0, 0);
		Axil2.addBox(0F, 0F, 0F, 4, 2, 12);
		Axil2.setRotationPoint(-2F, 17F, -4.5F);
		Axil2.setTextureSize(64, 64);
		Axil2.mirror = true;
		setRotation(Axil2, 0F, 0F, 0F);
		Ring1 = new ModelRenderer(this, 0, 35);
		Ring1.addBox(0F, 0F, 0F, 8, 1, 8);
		Ring1.setRotationPoint(-4F, 12F, -4F);
		Ring1.setTextureSize(64, 64);
		Ring1.mirror = true;
		setRotation(Ring1, 0F, 0F, 0F);
		Ring2 = new ModelRenderer(this, 0, 35);
		Ring2.addBox(0F, 0F, 0F, 8, 1, 8);
		Ring2.setRotationPoint(-4F, 10F, -4F);
		Ring2.setTextureSize(64, 64);
		Ring2.mirror = true;
		setRotation(Ring2, 0F, 0F, 0F);
		Plug = new ModelRenderer(this, 0, 45);
		Plug.addBox(0F, 0F, 0F, 6, 6, 1);
		Plug.setRotationPoint(-3F, 13F, 7F);
		Plug.setTextureSize(64, 64);
		Plug.mirror = true;
		setRotation(Plug, 0F, 0F, 0F);
		Ring3 = new ModelRenderer(this, 0, 35);
		Ring3.addBox(0F, 0F, 0F, 8, 1, 8);
		Ring3.setRotationPoint(-4F, 8F, -4F);
		Ring3.setTextureSize(64, 64);
		Ring3.mirror = true;
		setRotation(Ring3, 0F, 0F, 0F);
	}

	public void render(float size)
	{
		PumpHead.render(size);
		Connector.render(size);
		Panel1.render(size);
		Panel2.render(size);
		Panel3.render(size);
		Body.render(size);
		Axil.render(size);
		Axil2.render(size);
		Ring1.render(size);
		Ring2.render(size);
		Plug.render(size);
		Ring3.render(size);
	}

	private void setRotation(ModelRenderer model, float x, float y, float z)
	{
		model.rotateAngleX = x;
		model.rotateAngleY = y;
		model.rotateAngleZ = z;
	}
}
