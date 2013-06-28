package mekanism.client;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.entity.Entity;

@SideOnly(Side.CLIENT)
public class ModelMetallurgicInfuser extends ModelBase
{
	public ModelRenderer PANEL;
	public ModelRenderer MACHINE;
	public ModelRenderer CENTER_CIRCUIT;
	public ModelRenderer PIPE_1;
	public ModelRenderer PIPE_2;
	public ModelRenderer GUI_STAND;
	public ModelRenderer GUI_SCREEN;
	public ModelRenderer PIPE_2_TOP;
	public ModelRenderer PIPE_1_TOP;
	public ModelRenderer FRONT_PANEL;
	public ModelRenderer WIRE_1;
	public ModelRenderer WIRE_2;
	public ModelRenderer SIDE_WIRE_1;
	public ModelRenderer SIDE_WIRE_2;
	public ModelRenderer VERTICAL_WIRE;
	public ModelRenderer TUBE;
	
	public ModelMetallurgicInfuser()
	{
		textureWidth = 256;
		textureHeight = 256;
		
		PANEL = new ModelRenderer(this, 0, 0);
		PANEL.addBox(-8.0F, -0.5F, -8.0F, 16, 1, 16, 0);
		PANEL.setRotationPoint(0.0F, -0.5F, 0.0F);
		PANEL.setTextureSize(256, 256);
		PANEL.mirror = true;
		setRotation(PANEL, 0F, 0F, 0F);
		
		MACHINE = new ModelRenderer(this, 80, 0);
		MACHINE.addBox(-4.0F, -5.0F, -4.0F, 8, 10, 8, 0);
		MACHINE.setRotationPoint(2.0F, -6.0F, 0.0F);
		MACHINE.setTextureSize(256, 256);
		MACHINE.mirror = true;
		setRotation(MACHINE, 0F, 0F, 0F);
		
		CENTER_CIRCUIT = new ModelRenderer(this, 64, 0);
		CENTER_CIRCUIT.addBox(-2.0F, -0.5F, -2.0F, 4, 1, 4, 0);
		CENTER_CIRCUIT.setRotationPoint(2.0F, -11.5F, 0.0F);
		CENTER_CIRCUIT.setTextureSize(256, 256);
		CENTER_CIRCUIT.mirror = true;
		setRotation(CENTER_CIRCUIT, 0F, 0F, 0F);
		
		PIPE_1 = new ModelRenderer(this, 0, 32);
		PIPE_1.addBox(-1.0F, -5.5F, -0.5F, 2, 11, 1, 0);
		PIPE_1.setRotationPoint(2.0F, -6.0F, -4.5F);
		PIPE_1.setTextureSize(256, 256);
		PIPE_1.mirror = true;
		setRotation(PIPE_1, 0F, 0F, 0F);
		
		PIPE_2 = new ModelRenderer(this, 0, 32);
		PIPE_2.addBox(-1.0F, -5.5F, -0.5F, 2, 11, 1, 0);
		PIPE_2.setRotationPoint(2.0F, -6.0F, 4.5F);
		PIPE_2.setTextureSize(256, 256);
		PIPE_2.mirror = true;
		setRotation(PIPE_2, 0F, 0F, 0F);
		
		GUI_STAND = new ModelRenderer(this, 32, 32);
		GUI_STAND.addBox(-0.5F, -3.5F, -0.5F, 1, 7, 1, 0);
		GUI_STAND.setRotationPoint(-5.0F, -4.5F, 0.0F);
		GUI_STAND.setTextureSize(256, 256);
		GUI_STAND.mirror = true;
		setRotation(GUI_STAND, 0F, 0F, 0F);
		
		GUI_SCREEN = new ModelRenderer(this, 48, 32);
		GUI_SCREEN.addBox(-1.5F, -0.5F, -3.0F, 3, 1, 6, 0);
		GUI_SCREEN.setRotationPoint(-5.0F, -8.0F, 0.0F);
		GUI_SCREEN.setTextureSize(256, 256);
		GUI_SCREEN.mirror = true;
		setRotation(GUI_SCREEN, 0F, 0F, -0.5F);
		
		PIPE_2_TOP = new ModelRenderer(this, 16, 32);
		PIPE_2_TOP.addBox(-1.0F, -0.5F, -1.0F, 2, 1, 2, 0);
		PIPE_2_TOP.setRotationPoint(2.0F, -11.0F, 3.0F);
		PIPE_2_TOP.setTextureSize(256, 256);
		PIPE_2_TOP.mirror = true;
		setRotation(PIPE_2_TOP, 0F, 0F, 0F);
		
		PIPE_1_TOP = new ModelRenderer(this, 16, 32);
		PIPE_1_TOP.addBox(-1.0F, -0.5F, -1.0F, 2, 1, 2, 0);
		PIPE_1_TOP.setRotationPoint(2.0F, -11.0F, -3.0F);
		PIPE_1_TOP.setTextureSize(256, 256);
		PIPE_1_TOP.mirror = true;
		setRotation(PIPE_1_TOP, 0F, 0F, 0F);
		
		FRONT_PANEL = new ModelRenderer(this, 32, 64);
		FRONT_PANEL.addBox(-0.5F, -1.5F, -5.0F, 1, 3, 10, 0);
		FRONT_PANEL.setRotationPoint(-7.0F, -2.5F, 0.0F);
		FRONT_PANEL.setTextureSize(256, 256);
		FRONT_PANEL.mirror = true;
		setRotation(FRONT_PANEL, 0F, 0F, 0F);
		
		WIRE_1 = new ModelRenderer(this, 48, 48);
		WIRE_1.addBox(-2.0F, -0.5F, -0.5F, 4, 1, 1, 0);
		WIRE_1.setRotationPoint(-0.5F, -1.0F, -4.5F);
		WIRE_1.setTextureSize(256, 256);
		WIRE_1.mirror = true;
		setRotation(WIRE_1, 0F, 0F, 0F);
		
		WIRE_2 = new ModelRenderer(this, 0, 48);
		WIRE_2.addBox(-3.0F, -0.5F, -0.5F, 6, 1, 1, 0);
		WIRE_2.setRotationPoint(-1.5F, -1.0F, 4.5F);
		WIRE_2.setTextureSize(256, 256);
		WIRE_2.mirror = true;
		setRotation(WIRE_2, 0F, 0F, 0F);
		
		SIDE_WIRE_1 = new ModelRenderer(this, 32, 48);
		SIDE_WIRE_1.addBox(-0.5F, -0.5F, -2.5F, 1, 1, 5, 0);
		SIDE_WIRE_1.setRotationPoint(-4.0F, -1.0F, 2.0F);
		SIDE_WIRE_1.setTextureSize(256, 256);
		SIDE_WIRE_1.mirror = true;
		setRotation(SIDE_WIRE_1, 0F, 0F, 0F);
		
		SIDE_WIRE_2 = new ModelRenderer(this, 64, 48);
		SIDE_WIRE_2.addBox(-0.5F, -0.5F, -1.5F, 1, 1, 3, 0);
		SIDE_WIRE_2.setRotationPoint(-2.0F, -1.0F, -2.5F);
		SIDE_WIRE_2.setTextureSize(256, 256);
		SIDE_WIRE_2.mirror = true;
		setRotation(SIDE_WIRE_2, 0F, 0F, 0F);
		
		VERTICAL_WIRE = new ModelRenderer(this, 0, 64);
		VERTICAL_WIRE.addBox(-0.5F, -3.0F, -0.5F, 1, 6, 1, 0);
		VERTICAL_WIRE.setRotationPoint(-2.0F, -4.5F, -1.5F);
		VERTICAL_WIRE.setTextureSize(256, 256);
		VERTICAL_WIRE.mirror = true;
		setRotation(VERTICAL_WIRE, 0F, 0F, 0F);
		
		TUBE = new ModelRenderer(this, 16, 64);
		TUBE.addBox(-2.5F, -0.5F, -1.0F, 5, 1, 2, 0);
		TUBE.setRotationPoint(-4.0F, -3.0F, 2.0F);
		TUBE.setTextureSize(256, 256);
		TUBE.mirror = true;
		setRotation(TUBE, 0F, 0F, 0F);
		
	}
	
	public void render(float size)
	{
		PANEL.render(size);
		MACHINE.render(size);
		CENTER_CIRCUIT.render(size);
		PIPE_1.render(size);
		PIPE_2.render(size);
		GUI_STAND.render(size);
		GUI_SCREEN.render(size);
		PIPE_2_TOP.render(size);
		PIPE_1_TOP.render(size);
		FRONT_PANEL.render(size);
		WIRE_1.render(size);
		WIRE_2.render(size);
		SIDE_WIRE_1.render(size);
		SIDE_WIRE_2.render(size);
		VERTICAL_WIRE.render(size);
		TUBE.render(size);
	}
	
	@Override
	public void render(Entity entity, float f, float f1, float f2, float f3, float f4, float f5)
	{
		this.setRotationAngles(f, f1, f2, f3, f4, f5, entity);
		PANEL.render(f5);
		MACHINE.render(f5);
		CENTER_CIRCUIT.render(f5);
		PIPE_1.render(f5);
		PIPE_2.render(f5);
		GUI_STAND.render(f5);
		GUI_SCREEN.render(f5);
		PIPE_2_TOP.render(f5);
		PIPE_1_TOP.render(f5);
		FRONT_PANEL.render(f5);
		WIRE_1.render(f5);
		WIRE_2.render(f5);
		SIDE_WIRE_1.render(f5);
		SIDE_WIRE_2.render(f5);
		VERTICAL_WIRE.render(f5);
		TUBE.render(f5);
	}
	
	private void setRotation(ModelRenderer model, float x, float y, float z) 
	{
		model.rotateAngleX = x;
		model.rotateAngleY = y;
		model.rotateAngleZ = z;
	}
}
