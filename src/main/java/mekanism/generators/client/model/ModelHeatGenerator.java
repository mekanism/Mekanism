package mekanism.generators.client.model;

import mekanism.client.render.MekanismRenderer;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.ResourceType;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class ModelHeatGenerator extends ModelBase
{
	public static ResourceLocation OVERLAY_ON = MekanismUtils.getResource(ResourceType.RENDER, "HeatGenerator_OverlayOn.png");
	public static ResourceLocation OVERLAY_OFF = MekanismUtils.getResource(ResourceType.RENDER, "HeatGenerator_OverlayOff.png");
	
	ModelRenderer drum;
	ModelRenderer ring1;
	ModelRenderer ring2;
	ModelRenderer back;
	ModelRenderer bar1;
	ModelRenderer bar2;
	ModelRenderer plate;
	ModelRenderer fin8;
	ModelRenderer fin7;
	ModelRenderer fin1;
	ModelRenderer fin2;
	ModelRenderer fin3;
	ModelRenderer fin4;
	ModelRenderer fin5;
	ModelRenderer fin6;
	ModelRenderer base;

	public ModelHeatGenerator() 
	{
		textureWidth = 128;
		textureHeight = 64;

		drum = new ModelRenderer(this, 0, 22);
		drum.addBox(0F, 0F, 0F, 16, 9, 9);
		drum.setRotationPoint(-8F, 8.5F, -7.5F);
		drum.setTextureSize(128, 64);
		drum.mirror = true;
		setRotation(drum, 0F, 0F, 0F);
		ring1 = new ModelRenderer(this, 88, 0);
		ring1.addBox(0F, 0F, 0F, 2, 10, 10);
		ring1.setRotationPoint(3F, 8F, -8F);
		ring1.setTextureSize(128, 64);
		ring1.mirror = true;
		setRotation(ring1, 0F, 0F, 0F);
		ring2 = new ModelRenderer(this, 88, 0);
		ring2.addBox(0F, 0F, 0F, 2, 10, 10);
		ring2.setRotationPoint(-5F, 8F, -8F);
		ring2.setTextureSize(128, 64);
		ring2.mirror = true;
		setRotation(ring2, 0F, 0F, 0F);
		back = new ModelRenderer(this, 48, 0);
		back.addBox(0F, 0F, 0F, 16, 10, 4);
		back.setRotationPoint(-8F, 8F, 2F);
		back.setTextureSize(128, 64);
		back.mirror = true;
		setRotation(back, 0F, 0F, 0F);
		bar1 = new ModelRenderer(this, 88, 0);
		bar1.addBox(0F, 0F, 0F, 2, 9, 1);
		bar1.setRotationPoint(3F, 9F, 6F);
		bar1.setTextureSize(128, 64);
		bar1.mirror = true;
		setRotation(bar1, 0F, 0F, 0F);
		bar2 = new ModelRenderer(this, 88, 0);
		bar2.addBox(0F, 0F, 0F, 2, 9, 1);
		bar2.setRotationPoint(-5F, 9F, 6F);
		bar2.setTextureSize(128, 64);
		bar2.mirror = true;
		setRotation(bar2, 0F, 0F, 0F);
		plate = new ModelRenderer(this, 41, 22);
		plate.addBox(0F, 0F, 0F, 8, 6, 2);
		plate.setRotationPoint(-4F, 12F, 6F);
		plate.setTextureSize(128, 64);
		plate.mirror = true;
		setRotation(plate, 0F, 0F, 0F);
		fin8 = new ModelRenderer(this, 14, 40);
		fin8.addBox(0F, 0F, 0F, 16, 1, 2);
		fin8.setRotationPoint(-8F, 8F, 6F);
		fin8.setTextureSize(128, 64);
		fin8.mirror = true;
		setRotation(fin8, 0F, 0F, 0F);
		fin7 = new ModelRenderer(this, 14, 40);
		fin7.addBox(0F, 0F, 0F, 16, 1, 2);
		fin7.setRotationPoint(-8F, 10F, 6F);
		fin7.setTextureSize(128, 64);
		fin7.mirror = true;
		setRotation(fin7, 0F, 0F, 0F);
		fin1 = new ModelRenderer(this, 0, 40);
		fin1.addBox(0F, 0F, 0F, 4, 1, 2);
		fin1.setRotationPoint(4F, 12F, 6F);
		fin1.setTextureSize(128, 64);
		fin1.mirror = true;
		setRotation(fin1, 0F, 0F, 0F);
		fin1.mirror = false;
		fin2 = new ModelRenderer(this, 0, 40);
		fin2.addBox(0F, 0F, 0F, 4, 1, 2);
		fin2.setRotationPoint(4F, 14F, 6F);
		fin2.setTextureSize(128, 64);
		fin2.mirror = true;
		setRotation(fin2, 0F, 0F, 0F);
		fin2.mirror = false;
		fin3 = new ModelRenderer(this, 0, 40);
		fin3.addBox(0F, 0F, 0F, 4, 1, 2);
		fin3.setRotationPoint(4F, 16F, 6F);
		fin3.setTextureSize(128, 64);
		fin3.mirror = true;
		setRotation(fin3, 0F, 0F, 0F);
		fin3.mirror = false;
		fin4 = new ModelRenderer(this, 0, 40);
		fin4.addBox(0F, 0F, 0F, 4, 1, 2);
		fin4.setRotationPoint(-8F, 12F, 6F);
		fin4.setTextureSize(128, 64);
		fin4.mirror = true;
		setRotation(fin4, 0F, 0F, 0F);
		fin5 = new ModelRenderer(this, 0, 40);
		fin5.addBox(0F, 0F, 0F, 4, 1, 2);
		fin5.setRotationPoint(-8F, 14F, 6F);
		fin5.setTextureSize(128, 64);
		fin5.mirror = true;
		setRotation(fin5, 0F, 0F, 0F);
		fin6 = new ModelRenderer(this, 0, 40);
		fin6.addBox(0F, 0F, 0F, 4, 1, 2);
		fin6.setRotationPoint(-8F, 16F, 6F);
		fin6.setTextureSize(128, 64);
		fin6.mirror = true;
		setRotation(fin6, 0F, 0F, 0F);
		base = new ModelRenderer(this, 0, 0);
		base.addBox(0F, 0F, 0F, 16, 6, 16);
		base.setRotationPoint(-8F, 18F, -8F);
		base.setTextureSize(128, 64);
		base.mirror = true;
		setRotation(base, 0F, 0F, 0F);
	}
	
	public void render(float size, boolean on, TextureManager manager)
	{
		GlStateManager.pushMatrix();
		MekanismRenderer.blendOn();
		
		doRender(size);
		
		manager.bindTexture(on ? OVERLAY_ON : OVERLAY_OFF);
		GlStateManager.scale(1.001F, 1.001F, 1.001F);
		GlStateManager.translate(0, -0.0011F, 0);
		MekanismRenderer.glowOn();
		
		doRender(size);
		
		MekanismRenderer.glowOff();
		MekanismRenderer.blendOff();
		GlStateManager.popMatrix();
	}

	private void doRender(float size) 
	{
		drum.render(size);
		ring1.render(size);
		ring2.render(size);
		back.render(size);
		bar1.render(size);
		bar2.render(size);
		plate.render(size);
		fin8.render(size);
		fin7.render(size);
		fin1.render(size);
		fin2.render(size);
		fin3.render(size);
		fin4.render(size);
		fin5.render(size);
		fin6.render(size);
		base.render(size);
	}

	private void setRotation(ModelRenderer model, float x, float y, float z)
	{
		model.rotateAngleX = x;
		model.rotateAngleY = y;
		model.rotateAngleZ = z;
	}
}
