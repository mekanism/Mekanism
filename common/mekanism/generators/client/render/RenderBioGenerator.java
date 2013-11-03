package mekanism.generators.client.render;

import mekanism.client.render.MekanismRenderer;
import mekanism.client.render.MekanismRenderer.DisplayInteger;
import mekanism.client.render.MekanismRenderer.Model3D;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.ResourceType;
import mekanism.generators.client.model.ModelBioGenerator;
import mekanism.generators.common.tileentity.TileEntityBioGenerator;
import net.minecraft.block.Block;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Icon;
import net.minecraftforge.common.ForgeDirection;

import org.lwjgl.opengl.GL11;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class RenderBioGenerator extends TileEntitySpecialRenderer
{
	private ModelBioGenerator model = new ModelBioGenerator();
	
	private static Icon renderIcon = MekanismRenderer.getTextureMap(1).registerIcon("mekanism:LiquidEnergy");
	
	@Override
	public void renderTileEntityAt(TileEntity tileEntity, double x, double y, double z, float partialTick)
	{
		renderAModelAt((TileEntityBioGenerator)tileEntity, x, y, z, partialTick);
	}
	
	private void renderAModelAt(TileEntityBioGenerator tileEntity, double x, double y, double z, float partialTick)
	{
		GL11.glPushMatrix();
		GL11.glTranslatef((float)x + 0.5F, (float)y + 1.5F, (float)z + 0.5F);
		bindTexture(MekanismUtils.getResource(ResourceType.RENDER, "BioGenerator.png"));
		
	    switch(tileEntity.facing)
	    {
		    case 2: GL11.glRotatef(0, 0.0F, 1.0F, 0.0F); break;
			case 3: GL11.glRotatef(180, 0.0F, 1.0F, 0.0F); break;
			case 4: GL11.glRotatef(90, 0.0F, 1.0F, 0.0F); break;
			case 5: GL11.glRotatef(270, 0.0F, 1.0F, 0.0F); break;
	    }
		
		GL11.glRotatef(180, 0f, 0f, 1f);
		model.render(0.0625F);
		GL11.glPopMatrix();
	}
	
	/*
	@SuppressWarnings("incomplete-switch")
	private DisplayInteger getDisplayList(TileEntityBioGenerator tileEntity)
	{
		Model3D model3D = new Model3D();
		model3D.baseBlock = Block.waterStill;
		model3D.setTexture(renderIcon);
		
		switch(ForgeDirection.getOrientation(tileEntity.facing))
		{
			case NORTH:
			{
				model3D.minZ = -.01;
				model3D.maxZ = 0;
				
				model3D.minX = 0;
				model3D.minY = .0625;
				model3D.maxX = 1;
				model3D.maxY = 1;
				break;
			}
			case SOUTH:
			{
				model3D.minZ = 1;
				model3D.maxZ = 1.01;
				
				model3D.minX = 0;
				model3D.minY = .0625;
				model3D.maxX = 1;
				model3D.maxY = 1;
				break;
			}
			case WEST:
			{
				model3D.minX = -.01;
				model3D.maxX = 0;
				
				model3D.minY = .0625;
				model3D.minZ = 0;
				model3D.maxY = 1;
				model3D.maxZ = 1;
				break;
			}
			case EAST:
			{
				model3D.minX = 1;
				model3D.maxX = 1.01;
				
				model3D.minY = .0625;
				model3D.minZ = 0;
				model3D.maxY = 1;
				model3D.maxZ = 1;
				break;
			}
		}
		
		MekanismRenderer.renderObject(model3D);
	}
	*/
	
	private void pop()
	{
		GL11.glPopAttrib();
		GL11.glPopMatrix();
	}
	
	private void push()
	{
		GL11.glPushMatrix();
		GL11.glPushAttrib(GL11.GL_ENABLE_BIT);
		GL11.glEnable(GL11.GL_CULL_FACE);
		GL11.glEnable(GL11.GL_BLEND);
		GL11.glDisable(GL11.GL_LIGHTING);
		GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
	}
}
