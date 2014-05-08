package mekanism.generators.client.model;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;

public class ModelBioGenerator extends ModelBase
{
	ModelRenderer Base;
	ModelRenderer Pipe1;
	ModelRenderer Pipe2;
	ModelRenderer ContainerLid;
	ModelRenderer ContainerRight;
	ModelRenderer ContainerBottom;
	ModelRenderer ContainerFront;
	ModelRenderer ContainerBack;
	ModelRenderer ContainerLeft;
	ModelRenderer Decor1;
	ModelRenderer Decor2;
	ModelRenderer BackTop;
	ModelRenderer FrontPanel1;
	ModelRenderer FrontPanel2;
	ModelRenderer LeftPanel;
	ModelRenderer RightPanel;
	ModelRenderer BackPanel;

	public ModelBioGenerator()
	{
		textureWidth = 64;
		textureHeight = 64;

		Base = new ModelRenderer(this, 0, 0);
		Base.addBox(0F, 0F, 0F, 14, 1, 14);
		Base.setRotationPoint(-7F, 23F, -7F);
		Base.setTextureSize(64, 64);
		Base.mirror = true;
		setRotation(Base, 0F, 0F, 0F);
		Pipe1 = new ModelRenderer(this, 0, 16);
		Pipe1.addBox(0F, 0F, 0F, 2, 2, 2);
		Pipe1.setRotationPoint(-1F, 13F, -3F);
		Pipe1.setTextureSize(64, 64);
		Pipe1.mirror = true;
		setRotation(Pipe1, 0F, 0F, 0F);
		Pipe2 = new ModelRenderer(this, 0, 21);
		Pipe2.addBox(0F, 0F, 0F, 2, 2, 8);
		Pipe2.setRotationPoint(-1F, 11F, -3F);
		Pipe2.setTextureSize(64, 64);
		Pipe2.mirror = true;
		setRotation(Pipe2, 0F, 0F, 0F);
		ContainerLid = new ModelRenderer(this, 0, 32);
		ContainerLid.addBox(0F, 0F, 0F, 4, 1, 4);
		ContainerLid.setRotationPoint(-2F, 15F, -5F);
		ContainerLid.setTextureSize(64, 64);
		ContainerLid.mirror = true;
		setRotation(ContainerLid, 0F, 0F, 0F);
		ContainerRight = new ModelRenderer(this, 0, 38);
		ContainerRight.addBox(0F, 0F, 0F, 1, 6, 4);
		ContainerRight.setRotationPoint(2F, 16F, -5F);
		ContainerRight.setTextureSize(64, 64);
		ContainerRight.mirror = true;
		setRotation(ContainerRight, 0F, 0F, 0F);
		ContainerBottom = new ModelRenderer(this, 0, 49);
		ContainerBottom.addBox(0F, 0F, 0F, 6, 1, 6);
		ContainerBottom.setRotationPoint(-3F, 22F, -6F);
		ContainerBottom.setTextureSize(64, 64);
		ContainerBottom.mirror = true;
		setRotation(ContainerBottom, 0F, 0F, 0F);
		ContainerFront = new ModelRenderer(this, 11, 41);
		ContainerFront.addBox(0F, 0F, 0F, 6, 6, 1);
		ContainerFront.setRotationPoint(-3F, 16F, -6F);
		ContainerFront.setTextureSize(64, 64);
		ContainerFront.mirror = true;
		setRotation(ContainerFront, 0F, 0F, 0F);
		ContainerBack = new ModelRenderer(this, 11, 41);
		ContainerBack.addBox(0F, 0F, 0F, 6, 6, 1);
		ContainerBack.setRotationPoint(-3F, 16F, -1F);
		ContainerBack.setTextureSize(64, 64);
		ContainerBack.mirror = true;
		setRotation(ContainerBack, 0F, 0F, 0F);
		ContainerLeft = new ModelRenderer(this, 0, 38);
		ContainerLeft.addBox(0F, 0F, 0F, 1, 6, 4);
		ContainerLeft.setRotationPoint(-3F, 16F, -5F);
		ContainerLeft.setTextureSize(64, 64);
		setRotation(ContainerLeft, 0F, 0F, 0F);
		ContainerLeft.mirror = false;
		Decor1 = new ModelRenderer(this, 21, 20);
		Decor1.addBox(0F, 0F, 0F, 2, 2, 3);
		Decor1.setRotationPoint(4.1F, 15F, 2F);
		Decor1.setTextureSize(64, 64);
		Decor1.mirror = true;
		setRotation(Decor1, 0F, 0F, 0F);
		Decor2 = new ModelRenderer(this, 21, 20);
		Decor2.addBox(0F, 0F, 0F, 2, 2, 3);
		Decor2.setRotationPoint(-6.1F, 15F, 2F);
		Decor2.setTextureSize(64, 64);
		Decor2.mirror = true;
		setRotation(Decor2, 0F, 0F, 0F);
		BackTop = new ModelRenderer(this, 21, 26);
		BackTop.addBox(0F, 0F, 0F, 8, 1, 3);
		BackTop.setRotationPoint(-4F, 10F, 5F);
		BackTop.setTextureSize(64, 64);
		BackTop.mirror = true;
		setRotation(BackTop, 0F, 0F, 0F);
		FrontPanel1 = new ModelRenderer(this, 17, 32);
		FrontPanel1.addBox(0F, 0F, 0F, 3, 6, 2);
		FrontPanel1.setRotationPoint(-7F, 17F, -6F);
		FrontPanel1.setTextureSize(64, 64);
		FrontPanel1.mirror = true;
		setRotation(FrontPanel1, 0F, 0F, 0F);
		FrontPanel2 = new ModelRenderer(this, 17, 32);
		FrontPanel2.addBox(0F, 0F, 0F, 3, 6, 2);
		FrontPanel2.setRotationPoint(4F, 17F, -6F);
		FrontPanel2.setTextureSize(64, 64);
		FrontPanel2.mirror = true;
		setRotation(FrontPanel2, 0F, 0F, 0F);
		LeftPanel = new ModelRenderer(this, 28, 31);
		LeftPanel.addBox(0F, 0F, 0F, 3, 7, 10);
		LeftPanel.setRotationPoint(-7F, 16F, -4F);
		LeftPanel.setTextureSize(64, 64);
		LeftPanel.mirror = true;
		setRotation(LeftPanel, 0F, 0F, 0F);
		RightPanel = new ModelRenderer(this, 28, 31);
		RightPanel.addBox(0F, 0F, 0F, 3, 7, 10);
		RightPanel.setRotationPoint(4F, 16F, -4F);
		RightPanel.setTextureSize(64, 64);
		RightPanel.mirror = true;
		setRotation(RightPanel, 0F, 0F, 0F);
		BackPanel = new ModelRenderer(this, 28, 49);
		BackPanel.addBox(0F, 0F, 0F, 10, 12, 3);
		BackPanel.setRotationPoint(-5F, 11F, 5F);
		BackPanel.setTextureSize(64, 64);
		BackPanel.mirror = true;
		setRotation(BackPanel, 0F, 0F, 0F);
	}

	public void render(float size)
	{
		Base.render(size);
		Pipe1.render(size);
		Pipe2.render(size);
		ContainerLid.render(size);
		ContainerRight.render(size);
		ContainerBottom.render(size);
		ContainerFront.render(size);
		ContainerBack.render(size);
		ContainerLeft.render(size);
		Decor1.render(size);
		Decor2.render(size);
		BackTop.render(size);
		FrontPanel1.render(size);
		FrontPanel2.render(size);
		LeftPanel.render(size);
		RightPanel.render(size);
		BackPanel.render(size);
	}

	private void setRotation(ModelRenderer model, float x, float y, float z)
	{
		model.rotateAngleX = x;
		model.rotateAngleY = y;
		model.rotateAngleZ = z;
	}
}
