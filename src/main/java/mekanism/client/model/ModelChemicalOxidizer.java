package mekanism.client.model;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class ModelChemicalOxidizer extends ModelBase
{
	ModelRenderer stand;
	ModelRenderer tank;
	ModelRenderer pipe2;
	ModelRenderer bridge;
	ModelRenderer pipe1;
	ModelRenderer tower2;
	ModelRenderer tower1;
	ModelRenderer base;
	ModelRenderer connector;
	ModelRenderer connectorToggle;

	public ModelChemicalOxidizer()
	{
		textureWidth = 128;
		textureHeight = 64;

		stand = new ModelRenderer(this, 0, 20);
		stand.addBox(0F, 0F, 0F, 5, 1, 13);
		stand.setRotationPoint(-5.5F, 19F, -6.5F);
		stand.setTextureSize(128, 64);
		stand.mirror = true;
		setRotation(stand, 0F, 0F, 0F);
		tank = new ModelRenderer(this, 66, 0);
		tank.addBox(0F, 0F, 0F, 7, 12, 16);
		tank.setRotationPoint(1F, 8F, -8F);
		tank.setTextureSize(128, 64);
		tank.mirror = true;
		setRotation(tank, 0F, 0F, 0F);
		pipe2 = new ModelRenderer(this, 82, 28);
		pipe2.addBox(0F, 0F, 0F, 2, 6, 6);
		pipe2.setRotationPoint(-7F, 13F, -3F);
		pipe2.setTextureSize(128, 64);
		pipe2.mirror = true;
		setRotation(pipe2, 0F, 0F, 0F);
		bridge = new ModelRenderer(this, 70, 0);
		bridge.addBox(0F, 0F, 0F, 5, 10, 1);
		bridge.setRotationPoint(-5.5F, 8.5F, -2F);
		bridge.setTextureSize(128, 64);
		bridge.mirror = true;
		setRotation(bridge, 0F, 0F, 0F);
		pipe1 = new ModelRenderer(this, 0, 0);
		pipe1.addBox(0F, 0F, 0F, 1, 3, 3);
		pipe1.setRotationPoint(0F, 14F, 1F);
		pipe1.setTextureSize(128, 64);
		pipe1.mirror = true;
		setRotation(pipe1, 0F, 0F, 0F);
		tower2 = new ModelRenderer(this, 36, 20);
		tower2.addBox(0F, 0F, 0F, 6, 11, 8);
		tower2.setRotationPoint(-6F, 8F, -1F);
		tower2.setTextureSize(128, 64);
		tower2.mirror = true;
		setRotation(tower2, 0F, 0F, 0F);
		tower1 = new ModelRenderer(this, 48, 0);
		tower1.addBox(0F, 0F, 0F, 6, 11, 5);
		tower1.setRotationPoint(-6F, 8F, -7F);
		tower1.setTextureSize(128, 64);
		tower1.mirror = true;
		setRotation(tower1, 0F, 0F, 0F);
		base = new ModelRenderer(this, 0, 0);
		base.addBox(0F, 0F, 0F, 16, 4, 16);
		base.setRotationPoint(-8F, 20F, -8F);
		base.setTextureSize(128, 64);
		base.mirror = true;
		setRotation(base, 0F, 0F, 0F);
		connector = new ModelRenderer(this, 0, 34);
		connector.addBox(0F, 0F, 0F, 1, 10, 10);
		connector.setRotationPoint(-8F, 11F, -5F);
		connector.setTextureSize(128, 64);
		connector.mirror = true;
		setRotation(connector, 0F, 0F, 0F);
		connectorToggle = new ModelRenderer(this, 64, 28);
		connectorToggle.addBox(0F, 0F, 0F, 1, 8, 8);
		connectorToggle.setRotationPoint(7.01F, 12F, -4F);
		connectorToggle.setTextureSize(128, 64);
		connectorToggle.mirror = true;
		setRotation(connectorToggle, 0F, 0F, 0F);
	}

	public void render(float size)
	{
		render(size, false);
	}

	public void render(float size, boolean inventory)
	{
		stand.render(size);
		tank.render(size);
		if (!inventory) { pipe2.render(size);
		bridge.render(size);
		pipe1.render(size);}
		tower2.render(size);
		tower1.render(size);
		base.render(size);
		if (!inventory) { connector.render(size);
		connectorToggle.render(size);}
	}

	private void setRotation(ModelRenderer model, float x, float y, float z)
	{
		model.rotateAngleX = x;
		model.rotateAngleY = y;
		model.rotateAngleZ = z;
	}
}