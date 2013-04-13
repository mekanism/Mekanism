package mekanism.client;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.entity.Entity;

@SideOnly(Side.CLIENT)
public class ModelElectricPump extends ModelBase
{
	ModelRenderer COREBOX;
	ModelRenderer INPUTSLOT2;
	ModelRenderer INPUTSLOT3;
	ModelRenderer INPUTSLOT4;
	ModelRenderer INPUTSLOT5;
	ModelRenderer INPUTSLOT1;
	ModelRenderer PIPE3;
	ModelRenderer PIPE2;
	ModelRenderer TOPPIPE3;
	ModelRenderer PIPE1;
	ModelRenderer CORESLOT;
	ModelRenderer PIPE4;
	ModelRenderer TOPPIPE2;
	ModelRenderer DISPLAY2;
	ModelRenderer TOPPIPE1;
	ModelRenderer TOPPIPE4;
	ModelRenderer DISPLAY1;

	public ModelElectricPump() 
	{
		textureWidth = 128;
		textureHeight = 128;

		COREBOX = new ModelRenderer(this, 0, 0);
		COREBOX.addBox(0F, 0F, 0F, 12, 12, 12);
		COREBOX.setRotationPoint(-6F, 10F, -6F);
		COREBOX.setTextureSize(128, 128);
		COREBOX.mirror = true;
		setRotation(COREBOX, 0F, 0F, 0F);
		INPUTSLOT2 = new ModelRenderer(this, 0, 24);
		INPUTSLOT2.addBox(0F, 0F, 0F, 2, 6, 6);
		INPUTSLOT2.setRotationPoint(6F, 13F, -3F);
		INPUTSLOT2.setTextureSize(128, 128);
		INPUTSLOT2.mirror = true;
		setRotation(INPUTSLOT2, 0F, 0F, 0F);
		INPUTSLOT3 = new ModelRenderer(this, 16, 24);
		INPUTSLOT3.addBox(0F, 0F, 0F, 6, 6, 2);
		INPUTSLOT3.setRotationPoint(-3F, 13F, 6F);
		INPUTSLOT3.setTextureSize(128, 128);
		INPUTSLOT3.mirror = true;
		setRotation(INPUTSLOT3, 0F, 0F, 0F);
		INPUTSLOT4 = new ModelRenderer(this, 16, 24);
		INPUTSLOT4.addBox(0F, 0F, 0F, 6, 6, 2);
		INPUTSLOT4.setRotationPoint(-3F, 13F, -8F);
		INPUTSLOT4.setTextureSize(128, 128);
		INPUTSLOT4.mirror = true;
		setRotation(INPUTSLOT4, 0F, 0F, 0F);
		INPUTSLOT5 = new ModelRenderer(this, 0, 44);
		INPUTSLOT5.addBox(0F, 0F, 0F, 6, 2, 6);
		INPUTSLOT5.setRotationPoint(-3F, 22F, -3F);
		INPUTSLOT5.setTextureSize(128, 128);
		INPUTSLOT5.mirror = true;
		setRotation(INPUTSLOT5, 0F, 0F, 0F);
		INPUTSLOT1 = new ModelRenderer(this, 0, 24);
		INPUTSLOT1.addBox(0F, 0F, 0F, 2, 6, 6);
		INPUTSLOT1.setRotationPoint(-8F, 13F, -3F);
		INPUTSLOT1.setTextureSize(128, 128);
		INPUTSLOT1.mirror = true;
		setRotation(INPUTSLOT1, 0F, 0F, 0F);
		PIPE3 = new ModelRenderer(this, 48, 0);
		PIPE3.addBox(0F, 0F, 0F, 2, 4, 1);
		PIPE3.setRotationPoint(-1F, 9F, 6F);
		PIPE3.setTextureSize(128, 128);
		PIPE3.mirror = true;
		setRotation(PIPE3, 0F, 0F, 0F);
		PIPE2 = new ModelRenderer(this, 54, 0);
		PIPE2.addBox(0F, 0F, 0F, 1, 4, 2);
		PIPE2.setRotationPoint(6F, 9F, -1F);
		PIPE2.setTextureSize(128, 128);
		PIPE2.mirror = true;
		setRotation(PIPE2, 0F, 0F, 0F);
		TOPPIPE3 = new ModelRenderer(this, 60, 6);
		TOPPIPE3.addBox(0F, 0F, 0F, 2, 1, 3);
		TOPPIPE3.setRotationPoint(-1F, 9F, 3F);
		TOPPIPE3.setTextureSize(128, 128);
		TOPPIPE3.mirror = true;
		setRotation(TOPPIPE3, 0F, 0F, 0F);
		PIPE1 = new ModelRenderer(this, 54, 0);
		PIPE1.addBox(0F, 0F, 0F, 1, 4, 2);
		PIPE1.setRotationPoint(-7F, 9F, -1F);
		PIPE1.setTextureSize(128, 128);
		PIPE1.mirror = true;
		setRotation(PIPE1, 0F, 0F, 0F);
		CORESLOT = new ModelRenderer(this, 0, 52);
		CORESLOT.addBox(0F, 0F, 0F, 6, 2, 6);
		CORESLOT.setRotationPoint(-3F, 8F, -3F);
		CORESLOT.setTextureSize(128, 128);
		CORESLOT.mirror = true;
		setRotation(CORESLOT, 0F, 0F, 0F);
		PIPE4 = new ModelRenderer(this, 48, 0);
		PIPE4.addBox(0F, 0F, 0F, 2, 4, 1);
		PIPE4.setRotationPoint(-1F, 9F, -7F);
		PIPE4.setTextureSize(128, 128);
		PIPE4.mirror = true;
		setRotation(PIPE4, 0F, 0F, 0F);
		TOPPIPE2 = new ModelRenderer(this, 60, 0);
		TOPPIPE2.addBox(0F, 0F, 0F, 3, 1, 2);
		TOPPIPE2.setRotationPoint(3F, 9F, -1F);
		TOPPIPE2.setTextureSize(128, 128);
		TOPPIPE2.mirror = true;
		setRotation(TOPPIPE2, 0F, 0F, 0F);
		DISPLAY2 = new ModelRenderer(this, 70, 0);
		DISPLAY2.addBox(-4F, 0F, 0F, 3, 1, 10);
		DISPLAY2.setRotationPoint(6F, 9F, -5F);
		DISPLAY2.setTextureSize(128, 128);
		DISPLAY2.mirror = true;
		setRotation(DISPLAY2, 0F, 0F, 0F);
		TOPPIPE1 = new ModelRenderer(this, 60, 0);
		TOPPIPE1.addBox(0F, 0F, 0F, 3, 1, 2);
		TOPPIPE1.setRotationPoint(-6F, 9F, -1F);
		TOPPIPE1.setTextureSize(128, 128);
		TOPPIPE1.mirror = true;
		setRotation(TOPPIPE1, 0F, 0F, 0F);
		TOPPIPE4 = new ModelRenderer(this, 60, 6);
		TOPPIPE4.addBox(0F, 0F, 0F, 2, 1, 3);
		TOPPIPE4.setRotationPoint(-1F, 9F, -6F);
		TOPPIPE4.setTextureSize(128, 128);
		TOPPIPE4.mirror = true;
		setRotation(TOPPIPE4, 0F, 0F, 0F);
		DISPLAY1 = new ModelRenderer(this, 70, 0);
		DISPLAY1.addBox(-4F, 0F, 0F, 3, 1, 10);
		DISPLAY1.setRotationPoint(-1F, 9F, -5F);
		DISPLAY1.setTextureSize(128, 128);
		DISPLAY1.mirror = true;
		setRotation(DISPLAY1, 0F, 0F, 0F);
	}
	
	public void render(float size)
	{
		COREBOX.render(size);
		INPUTSLOT2.render(size);
		INPUTSLOT3.render(size);
		INPUTSLOT4.render(size);
		INPUTSLOT5.render(size);
		INPUTSLOT1.render(size);
		PIPE3.render(size);
		PIPE2.render(size);
		TOPPIPE3.render(size);
		PIPE1.render(size);
		CORESLOT.render(size);
		PIPE4.render(size);
		TOPPIPE2.render(size);
		DISPLAY2.render(size);
		TOPPIPE1.render(size);
		TOPPIPE4.render(size);
		DISPLAY1.render(size);
	}

	@Override
	public void render(Entity entity, float f, float f1, float f2, float f3, float f4, float f5) 
	{
		super.render(entity, f, f1, f2, f3, f4, f5);
		setRotationAngles(f, f1, f2, f3, f4, f5, entity);
		COREBOX.render(f5);
		INPUTSLOT2.render(f5);
		INPUTSLOT3.render(f5);
		INPUTSLOT4.render(f5);
		INPUTSLOT5.render(f5);
		INPUTSLOT1.render(f5);
		PIPE3.render(f5);
		PIPE2.render(f5);
		TOPPIPE3.render(f5);
		PIPE1.render(f5);
		CORESLOT.render(f5);
		PIPE4.render(f5);
		TOPPIPE2.render(f5);
		DISPLAY2.render(f5);
		TOPPIPE1.render(f5);
		TOPPIPE4.render(f5);
		DISPLAY1.render(f5);
	}

	private void setRotation(ModelRenderer model, float x, float y, float z) 
	{
		model.rotateAngleX = x;
		model.rotateAngleY = y;
		model.rotateAngleZ = z;
	}
}