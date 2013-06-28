package mekanism.generators.client;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.entity.Entity;

@SideOnly(Side.CLIENT)
public class ModelBioGenerator extends ModelBase
{
    ModelRenderer MAIN_BASE;
    ModelRenderer MATERIAL_INLET;
    ModelRenderer CONVEYOR_BELT_1;
    ModelRenderer CONVEYOR_BELT_SUPPORT;
    ModelRenderer PISTON_SUPPORT_1;
    ModelRenderer PISTON_SUPPORT_2;
    ModelRenderer CONVEYOR_BELT_2;
    ModelRenderer FUEL_CONTAINER_WALL_1;
    ModelRenderer FUEL_CONTAINER_WALL_2;
    ModelRenderer FUEL_CONTAINER_WALL_3;
    ModelRenderer FUEL_CONTAINER_WALL_4;
    ModelRenderer FUEL_CONTAINER_TOP_1;
    ModelRenderer FUEL_CONTAINER_TOP_2;
    ModelRenderer FUEL_OUTLET_1;
    ModelRenderer FUEL_OUTLET_2;
    ModelRenderer PLUG_PANEL;
    ModelRenderer PISTON_HEAD_MOVES;
    ModelRenderer PISTON_ARM_MOVES;
    ModelRenderer PISTON_BODY;
  
    public ModelBioGenerator()
    {
    	textureWidth = 128;
    	textureHeight = 128;
    
    	MAIN_BASE = new ModelRenderer(this, 0, 0);
    	MAIN_BASE.addBox(0F, 0F, 0F, 16, 1, 16);
    	MAIN_BASE.setRotationPoint(-8F, 23F, -8F);
    	MAIN_BASE.setTextureSize(128, 128);
    	MAIN_BASE.mirror = true;
    	setRotation(MAIN_BASE, 0F, 0F, 0F);
    	MATERIAL_INLET = new ModelRenderer(this, 0, 18);
    	MATERIAL_INLET.addBox(0F, 0F, 0F, 4, 6, 4);
    	MATERIAL_INLET.setRotationPoint(3F, 17F, -7F);
    	MATERIAL_INLET.setTextureSize(128, 128);
    	MATERIAL_INLET.mirror = true;
    	setRotation(MATERIAL_INLET, 0F, 0F, 0F);
    	CONVEYOR_BELT_1 = new ModelRenderer(this, 0, 29);
    	CONVEYOR_BELT_1.addBox(0F, 0F, 0F, 6, 1, 4);
    	CONVEYOR_BELT_1.setRotationPoint(-3F, 17F, -7F);
    	CONVEYOR_BELT_1.setTextureSize(128, 128);
    	CONVEYOR_BELT_1.mirror = true;
    	setRotation(CONVEYOR_BELT_1, 0F, 0F, 0F);
    	CONVEYOR_BELT_SUPPORT = new ModelRenderer(this, 17, 18);
    	CONVEYOR_BELT_SUPPORT.addBox(0F, 0F, 0F, 4, 6, 4);
    	CONVEYOR_BELT_SUPPORT.setRotationPoint(-7F, 17F, -7F);
    	CONVEYOR_BELT_SUPPORT.setTextureSize(128, 128);
    	CONVEYOR_BELT_SUPPORT.mirror = true;
    	setRotation(CONVEYOR_BELT_SUPPORT, 0F, 0F, 0F);
    	PISTON_SUPPORT_1 = new ModelRenderer(this, 0, 50);
    	PISTON_SUPPORT_1.addBox(0F, 0F, 0F, 2, 10, 1);
    	PISTON_SUPPORT_1.setRotationPoint(-1F, 13F, -8F);
    	PISTON_SUPPORT_1.setTextureSize(128, 128);
    	PISTON_SUPPORT_1.mirror = true;
    	setRotation(PISTON_SUPPORT_1, 0F, 0F, 0F);
    	PISTON_SUPPORT_2 = new ModelRenderer(this, 0, 50);
    	PISTON_SUPPORT_2.addBox(0F, 0F, 0F, 2, 10, 1);
    	PISTON_SUPPORT_2.setRotationPoint(-1F, 13F, -3F);
    	PISTON_SUPPORT_2.setTextureSize(128, 128);
    	PISTON_SUPPORT_2.mirror = true;
    	setRotation(PISTON_SUPPORT_2, 0F, 0F, 0F);
    	CONVEYOR_BELT_2 = new ModelRenderer(this, 0, 36);
    	CONVEYOR_BELT_2.addBox(0F, 0F, 0F, 7, 1, 4);
    	CONVEYOR_BELT_2.setRotationPoint(-3F, 17F, -3F);
    	CONVEYOR_BELT_2.setTextureSize(128, 128);
    	CONVEYOR_BELT_2.mirror = true;
    	setRotation(CONVEYOR_BELT_2, 0F, -1.570796F, 0.33161255787892263F);
    	FUEL_CONTAINER_WALL_1 = new ModelRenderer(this, 65, 15);
    	FUEL_CONTAINER_WALL_1.addBox(0F, 0F, 0F, 4, 13, 1);
    	FUEL_CONTAINER_WALL_1.setRotationPoint(-7F, 10F, 3F);
    	FUEL_CONTAINER_WALL_1.setTextureSize(128, 128);
      	FUEL_CONTAINER_WALL_1.mirror = true;
      	setRotation(FUEL_CONTAINER_WALL_1, 0F, 0F, 0F);
      	FUEL_CONTAINER_WALL_2 = new ModelRenderer(this, 77, 0);
      	FUEL_CONTAINER_WALL_2.addBox(0F, 0F, 0F, 1, 13, 3);
      	FUEL_CONTAINER_WALL_2.setRotationPoint(-8F, 10F, 4F);
      	FUEL_CONTAINER_WALL_2.setTextureSize(128, 128);
      	FUEL_CONTAINER_WALL_2.mirror = true;
      	setRotation(FUEL_CONTAINER_WALL_2, 0F, 0F, 0F);
      	FUEL_CONTAINER_WALL_3 = new ModelRenderer(this, 65, 0);
      	FUEL_CONTAINER_WALL_3.addBox(0F, 0F, 0F, 4, 13, 1);
      	FUEL_CONTAINER_WALL_3.setRotationPoint(-7F, 10F, 7F);
      	FUEL_CONTAINER_WALL_3.setTextureSize(128, 128);
      	FUEL_CONTAINER_WALL_3.mirror = true;
      	setRotation(FUEL_CONTAINER_WALL_3, 0F, 0F, 0F);
      	FUEL_CONTAINER_WALL_4 = new ModelRenderer(this, 77, 0);
      	FUEL_CONTAINER_WALL_4.addBox(0F, 0F, 0F, 1, 13, 3);
      	FUEL_CONTAINER_WALL_4.setRotationPoint(-3F, 10F, 4F);
      	FUEL_CONTAINER_WALL_4.setTextureSize(128, 128);
      	FUEL_CONTAINER_WALL_4.mirror = true;
      	setRotation(FUEL_CONTAINER_WALL_4, 0F, 0F, 0F);
      	FUEL_CONTAINER_TOP_1 = new ModelRenderer(this, 86, 0);
      	FUEL_CONTAINER_TOP_1.addBox(0F, 0F, 0F, 6, 1, 5);
      	FUEL_CONTAINER_TOP_1.setRotationPoint(-8F, 9F, 3F);
      	FUEL_CONTAINER_TOP_1.setTextureSize(128, 128);
      	FUEL_CONTAINER_TOP_1.mirror = true;
      	setRotation(FUEL_CONTAINER_TOP_1, 0F, 0F, 0F);
      	FUEL_CONTAINER_TOP_2 = new ModelRenderer(this, 86, 7);
      	FUEL_CONTAINER_TOP_2.addBox(0F, 0F, 0F, 2, 1, 1);
      	FUEL_CONTAINER_TOP_2.setRotationPoint(-6F, 8F, 5F);
      	FUEL_CONTAINER_TOP_2.setTextureSize(128, 128);
      	FUEL_CONTAINER_TOP_2.mirror = true;
      	setRotation(FUEL_CONTAINER_TOP_2, 0F, 0F, 0F);
      	FUEL_OUTLET_1 = new ModelRenderer(this, 86, 10);
      	FUEL_OUTLET_1.addBox(0F, 0F, 0F, 4, 1, 1);
      	FUEL_OUTLET_1.setRotationPoint(-2F, 16F, 5F);
      	FUEL_OUTLET_1.setTextureSize(128, 128);
      	FUEL_OUTLET_1.mirror = true;
      	setRotation(FUEL_OUTLET_1, 0F, 0F, 0.2617994F);
      	FUEL_OUTLET_2 = new ModelRenderer(this, 86, 14);
      	FUEL_OUTLET_2.addBox(0F, 0F, 0F, 1, 2, 1);
      	FUEL_OUTLET_2.setRotationPoint(1F, 17F, 5F);
      	FUEL_OUTLET_2.setTextureSize(128, 128);
      	FUEL_OUTLET_2.mirror = true;
      	setRotation(FUEL_OUTLET_2, 0F, 0F, 0F);
      	PLUG_PANEL = new ModelRenderer(this, 44, 18);
      	PLUG_PANEL.addBox(0F, 0F, 0F, 4, 9, 6);
      	PLUG_PANEL.setRotationPoint(4F, 14F, -3F);
      	PLUG_PANEL.setTextureSize(128, 128);
      	PLUG_PANEL.mirror = true;
      	setRotation(PLUG_PANEL, 0F, 0F, 0F);
      	PISTON_ARM_MOVES = new ModelRenderer(this, 8, 57);
      	PISTON_ARM_MOVES.addBox(0F, 0F, 0F, 2, 2, 2);
      	PISTON_ARM_MOVES.setRotationPoint(-1F, 11F, -6F);
      	PISTON_ARM_MOVES.setTextureSize(128, 128);
      	PISTON_ARM_MOVES.mirror = true;
      	setRotation(PISTON_ARM_MOVES, 0F, 0F, 0F);
      	PISTON_BODY = new ModelRenderer(this, 0, 63);
      	PISTON_BODY.addBox(0F, 0F, 0F, 6, 4, 6);
      	PISTON_BODY.setRotationPoint(-3F, 9F, -8F);
      	PISTON_BODY.setTextureSize(128, 128);
     	PISTON_BODY.mirror = true;
     	setRotation(PISTON_BODY, 0F, 0F, 0F);
    }

    public void render(float size, float depth)
    {
    	MAIN_BASE.render(size);
    	MATERIAL_INLET.render(size);
    	CONVEYOR_BELT_1.render(size);
    	CONVEYOR_BELT_SUPPORT.render(size);
    	PISTON_SUPPORT_1.render(size);
    	PISTON_SUPPORT_2.render(size);
    	CONVEYOR_BELT_2.render(size);
    	FUEL_CONTAINER_WALL_1.render(size);
    	FUEL_CONTAINER_WALL_2.render(size);
    	FUEL_CONTAINER_WALL_3.render(size);
    	FUEL_CONTAINER_WALL_4.render(size);
    	FUEL_CONTAINER_TOP_1.render(size);
    	FUEL_CONTAINER_TOP_2.render(size);
    	FUEL_OUTLET_1.render(size);
    	FUEL_OUTLET_2.render(size);
    	PLUG_PANEL.render(size);
    	
      	PISTON_HEAD_MOVES = new ModelRenderer(this, 8, 50);
      	PISTON_HEAD_MOVES.addBox(0F, depth, 0F, 4, 2, 4);
      	PISTON_HEAD_MOVES.setRotationPoint(-2F, 13F, -7F);
      	PISTON_HEAD_MOVES.setTextureSize(128, 128);
      	PISTON_HEAD_MOVES.mirror = true;
      	setRotation(PISTON_HEAD_MOVES, 0F, 0F, 0F);
    	
    	PISTON_HEAD_MOVES.render(size);
    	PISTON_ARM_MOVES.render(size);
    	PISTON_BODY.render(size);
    }
  
    private void setRotation(ModelRenderer model, float x, float y, float z)
    {
    	model.rotateAngleX = x;
    	model.rotateAngleY = y;
    	model.rotateAngleZ = z;
    }
}
