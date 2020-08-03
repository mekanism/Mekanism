package mekanism.client.model;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;

public class ModelLaser extends ModelBase 
{
	ModelRenderer connector;
	ModelRenderer center;
	ModelRenderer shaft;
	ModelRenderer ring1;
	ModelRenderer port;
	ModelRenderer rod1;
	ModelRenderer fin1;
	ModelRenderer fin2;
	ModelRenderer fin3;
	ModelRenderer fin4;
	ModelRenderer ring2;
	ModelRenderer body;
	ModelRenderer rod2;
	ModelRenderer wire;
	ModelRenderer rod3;
	ModelRenderer fin5;
	ModelRenderer fin6;
	ModelRenderer fin7;
	ModelRenderer fin8;
	ModelRenderer rod4;

	public ModelLaser() 
	{
		textureWidth = 64;
		textureHeight = 32;

		connector = new ModelRenderer(this, 32, 15);
		connector.addBox(0F, 0F, 0F, 6, 1, 6);
		connector.setRotationPoint(-3F, 22F, -3F);
		connector.setTextureSize(64, 32);
		connector.mirror = true;
		setRotation(connector, 0F, 0F, 0F);
		center = new ModelRenderer(this, 18, 9);
		center.addBox(0F, 0F, 0F, 2, 15, 2);
		center.setRotationPoint(-1F, 8F, -1F);
		center.setTextureSize(64, 32);
		center.mirror = true;
		setRotation(center, 0F, 0F, 0F);
		shaft = new ModelRenderer(this, 0, 18);
		shaft.addBox(0F, 0F, 0F, 3, 2, 3);
		shaft.setRotationPoint(-1.5F, 13F, -1.5F);
		shaft.setTextureSize(64, 32);
		shaft.mirror = true;
		setRotation(shaft, 0F, 0F, 0F);
		ring1 = new ModelRenderer(this, 0, 23);
		ring1.addBox(0F, 0F, 0F, 4, 1, 4);
		ring1.setRotationPoint(-2F, 10F, -2F);
		ring1.setTextureSize(64, 32);
		ring1.mirror = true;
		setRotation(ring1, 0F, 0F, 0F);
		port = new ModelRenderer(this, 0, 0);
		port.addBox(0F, 0F, 0F, 8, 1, 8);
		port.setRotationPoint(-4F, 23F, -4F);
		port.setTextureSize(64, 32);
		port.mirror = true;
		setRotation(port, 0F, 0F, 0F);
		rod1 = new ModelRenderer(this, 0, 9);
		rod1.addBox(0F, 0F, 0F, 1, 5, 1);
		rod1.setRotationPoint(2.8F, 16F, 1F);
		rod1.setTextureSize(64, 32);
		rod1.mirror = true;
		setRotation(rod1, 0F, 0F, 0F);
		fin1 = new ModelRenderer(this, 0, 9);
		fin1.addBox(0F, 0F, 0F, 1, 1, 8);
		fin1.setRotationPoint(3F, 21F, -4F);
		fin1.setTextureSize(64, 32);
		fin1.mirror = true;
		setRotation(fin1, 0F, 0F, 0F);
		fin2 = new ModelRenderer(this, 0, 9);
		fin2.addBox(0F, 0F, 0F, 1, 1, 8);
		fin2.setRotationPoint(3F, 19F, -4F);
		fin2.setTextureSize(64, 32);
		fin2.mirror = true;
		setRotation(fin2, 0F, 0F, 0F);
		fin3 = new ModelRenderer(this, 0, 9);
		fin3.addBox(0F, 0F, 0F, 1, 1, 8);
		fin3.setRotationPoint(3F, 17F, -4F);
		fin3.setTextureSize(64, 32);
		fin3.mirror = true;
		setRotation(fin3, 0F, 0F, 0F);
		fin4 = new ModelRenderer(this, 0, 9);
		fin4.addBox(0F, 0F, 0F, 1, 1, 8);
		fin4.setRotationPoint(3F, 15F, -4F);
		fin4.setTextureSize(64, 32);
		fin4.mirror = true;
		setRotation(fin4, 0F, 0F, 0F);
		ring2 = new ModelRenderer(this, 0, 23);
		ring2.addBox(0F, 0F, 0F, 4, 1, 4);
		ring2.setRotationPoint(-2F, 12F, -2F);
		ring2.setTextureSize(64, 32);
		ring2.mirror = true;
		setRotation(ring2, 0F, 0F, 0F);
		body = new ModelRenderer(this, 32, 0);
		body.addBox(0F, 0F, 0F, 6, 7, 8);
		body.setRotationPoint(-3F, 15F, -4F);
		body.setTextureSize(64, 32);
		body.mirror = true;
		setRotation(body, 0F, 0F, 0F);
		rod2 = new ModelRenderer(this, 0, 9);
		rod2.addBox(0F, 0F, 0F, 1, 5, 1);
		rod2.setRotationPoint(2.8F, 16F, -2F);
		rod2.setTextureSize(64, 32);
		rod2.mirror = true;
		setRotation(rod2, 0F, 0F, 0F);
		wire = new ModelRenderer(this, 10, 9);
		wire.addBox(0F, -1F, 0F, 1, 6, 1);
		wire.setRotationPoint(-1.5F, 11F, -0.5F);
		wire.setTextureSize(64, 32);
		wire.mirror = true;
		setRotation(wire, 0F, 0F, 0.1919862F);
		rod3 = new ModelRenderer(this, 0, 9);
		rod3.addBox(0F, 0F, 0F, 1, 5, 1);
		rod3.setRotationPoint(-3.8F, 16F, 1F);
		rod3.setTextureSize(64, 32);
		rod3.mirror = true;
		setRotation(rod3, 0F, 0F, 0F);
		fin5 = new ModelRenderer(this, 0, 9);
		fin5.addBox(0F, 0F, 0F, 1, 1, 8);
		fin5.setRotationPoint(-4F, 15F, -4F);
		fin5.setTextureSize(64, 32);
		fin5.mirror = true;
		setRotation(fin5, 0F, 0F, 0F);
		fin6 = new ModelRenderer(this, 0, 9);
		fin6.addBox(0F, 0F, 0F, 1, 1, 8);
		fin6.setRotationPoint(-4F, 17F, -4F);
		fin6.setTextureSize(64, 32);
		fin6.mirror = true;
		setRotation(fin6, 0F, 0F, 0F);
		fin7 = new ModelRenderer(this, 0, 9);
		fin7.addBox(0F, 0F, 0F, 1, 1, 8);
		fin7.setRotationPoint(-4F, 19F, -4F);
		fin7.setTextureSize(64, 32);
		fin7.mirror = true;
		setRotation(fin7, 0F, 0F, 0F);
		fin8 = new ModelRenderer(this, 0, 9);
		fin8.addBox(0F, 0F, 0F, 1, 1, 8);
		fin8.setRotationPoint(-4F, 21F, -4F);
		fin8.setTextureSize(64, 32);
		fin8.mirror = true;
		setRotation(fin8, 0F, 0F, 0F);
		rod4 = new ModelRenderer(this, 0, 9);
		rod4.addBox(0F, 0F, 0F, 1, 5, 1);
		rod4.setRotationPoint(-3.8F, 16F, -2F);
		rod4.setTextureSize(64, 32);
		rod4.mirror = true;
		setRotation(rod4, 0F, 0F, 0F);
	}

	public void render(float size)
	{
		render(size, false);
	}

	public void render(float size, boolean inventory)
	{
		if (!inventory) connector.render(size);
		center.render(size);
		shaft.render(size);
		if (!inventory) { ring1.render(size);
		port.render(size);
		rod1.render(size);
		fin1.render(size);
		fin2.render(size);
		fin3.render(size);
		fin4.render(size);
		ring2.render(size);}
		body.render(size);
		if (!inventory) { rod2.render(size);
		wire.render(size);
		rod3.render(size);
		fin5.render(size);
		fin6.render(size);
		fin7.render(size);
		fin8.render(size);
		rod4.render(size);}
	}

	private void setRotation(ModelRenderer model, float x, float y, float z) 
	{
		model.rotateAngleX = x;
		model.rotateAngleY = y;
		model.rotateAngleZ = z;
	}
}
