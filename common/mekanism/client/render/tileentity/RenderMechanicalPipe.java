package mekanism.client.render.tileentity;

import java.util.HashMap;

import mekanism.client.model.ModelTransmitter;
import mekanism.client.render.MekanismRenderer;
import mekanism.client.render.MekanismRenderer.DisplayInteger;
import mekanism.client.render.MekanismRenderer.Model3D;
import mekanism.common.tileentity.TileEntityMechanicalPipe;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.ResourceType;
import mekanism.common.PipeUtils;
import net.minecraft.block.Block;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.ForgeDirection;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;

import org.lwjgl.opengl.GL11;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class RenderMechanicalPipe extends TileEntitySpecialRenderer
{
	private ModelTransmitter model = new ModelTransmitter();
	
	private HashMap<ForgeDirection, HashMap<Fluid, DisplayInteger[]>> cachedLiquids = new HashMap<ForgeDirection, HashMap<Fluid, DisplayInteger[]>>();
	
	private static final int stages = 40;
	
	private static final double offset = 0.015;
	
	@Override
	public void renderTileEntityAt(TileEntity tileEntity, double x, double y, double z, float partialTick)
	{
		renderAModelAt((TileEntityMechanicalPipe)tileEntity, x, y, z, partialTick);
	}

	public void renderAModelAt(TileEntityMechanicalPipe tileEntity, double x, double y, double z, float partialTick)
	{
		func_110628_a(MekanismUtils.getResource(ResourceType.RENDER, "MechanicalPipe" + (tileEntity.isActive ? "Active" : "") + ".png"));
		GL11.glPushMatrix();
		GL11.glTranslatef((float)x + 0.5F, (float)y + 1.5F, (float)z + 0.5F);
		GL11.glScalef(1.0F, -1F, -1F);
		GL11.glDisable(GL11.GL_CULL_FACE);
		
		boolean[] connectable = PipeUtils.getConnections(tileEntity);
		
		model.renderCenter(connectable);
		
		for(int i = 0; i < 6; i++)
		{
			model.renderSide(ForgeDirection.getOrientation(i), connectable[i]);
		}
		
		GL11.glEnable(GL11.GL_CULL_FACE);
		GL11.glPopMatrix();
		
		if(tileEntity.fluidScale > 0 && tileEntity.refFluid != null)
		{
			push();
			
			if(tileEntity.refFluid.getFluid() == FluidRegistry.LAVA)
			{
				MekanismRenderer.glowOn();
			}
			
			func_110628_a(MekanismRenderer.getLiquidTexture());
			GL11.glTranslatef((float)x, (float)y, (float)z);
			
			for(int i = 0; i < 6; i++)
			{
				if(connectable[i])
				{
					DisplayInteger[] displayLists = getListAndRender(ForgeDirection.getOrientation(i), tileEntity.refFluid.getFluid());
					
					if(displayLists != null)
					{
						displayLists[Math.max(3, (int)((float)tileEntity.fluidScale*(stages-1)))].render();
					}
				}
			}
			
			DisplayInteger[] displayLists = getListAndRender(ForgeDirection.UNKNOWN, tileEntity.refFluid.getFluid());
			
			if(displayLists != null)
			{
				displayLists[Math.max(3, (int)((float)tileEntity.fluidScale*(stages-1)))].render();
			}
			
			if(tileEntity.refFluid.getFluid() == FluidRegistry.LAVA)
			{
				MekanismRenderer.glowOff();
			}
			
			pop();
		}
	}
	
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
	
	private DisplayInteger[] getListAndRender(ForgeDirection side, Fluid fluid)
	{
		if(side == null || fluid == null || fluid.getIcon() == null)
		{
			return null;
		}
		
		if(cachedLiquids.containsKey(side) && cachedLiquids.get(side).containsKey(fluid))
		{
			return cachedLiquids.get(side).get(fluid);
		}
		
		Model3D toReturn = new Model3D();
		toReturn.baseBlock = Block.waterStill;
		toReturn.setTexture(fluid.getIcon());
		
		toReturn.setSideRender(side, false);
		toReturn.setSideRender(side.getOpposite(), false);
		
		DisplayInteger[] displays = new DisplayInteger[stages];
		
		if(cachedLiquids.containsKey(side))
		{
			cachedLiquids.get(side).put(fluid, displays);
		}
		else {
			HashMap<Fluid, DisplayInteger[]> map = new HashMap<Fluid, DisplayInteger[]>();
			map.put(fluid, displays);
			cachedLiquids.put(side, map);
		}
		
		MekanismRenderer.colorFluid(fluid);
		
		for(int i = 0; i < stages; i++)
		{
			displays[i] = DisplayInteger.createAndStart();
			
			switch(side)
			{
				case UNKNOWN:
				{
					toReturn.minX = 0.3 + offset;
					toReturn.minY = 0.3 + offset;
					toReturn.minZ = 0.3 + offset;
					
					toReturn.maxX = 0.7 - offset;
					toReturn.maxY = 0.3 - offset + ((float)i / (float)100);
					toReturn.maxZ = 0.7 - offset;
					break;
				}
				case DOWN:
				{
					toReturn.minX = 0.5 + offset - ((float)i / (float)100)/2;
					toReturn.minY = 0.0;
					toReturn.minZ = 0.5 + offset - ((float)i / (float)100)/2;
					
					toReturn.maxX = 0.5 - offset + ((float)i / (float)100)/2;
					toReturn.maxY = 0.3 + offset;
					toReturn.maxZ = 0.5 - offset + ((float)i / (float)100)/2;
					break;
				}
				case UP:
				{
					toReturn.minX = 0.5 + offset - ((float)i / (float)100)/2;
					toReturn.minY = 0.3 - offset + ((float)i / (float)100);
					toReturn.minZ = 0.5 + offset - ((float)i / (float)100)/2;
					
					toReturn.maxX = 0.5 - offset + ((float)i / (float)100)/2;
					toReturn.maxY = 1.0;
					toReturn.maxZ = 0.5 - offset + ((float)i / (float)100)/2;
					break;
				}
				case NORTH:
				{
					toReturn.minX = 0.3 + offset;
					toReturn.minY = 0.3 + offset;
					toReturn.minZ = 0.0;
					
					toReturn.maxX = 0.7 - offset;
					toReturn.maxY = 0.3 - offset + ((float)i / (float)100);
					toReturn.maxZ = 0.3 + offset;
					break;
				}
				case SOUTH:
				{
					toReturn.minX = 0.3 + offset;
					toReturn.minY = 0.3 + offset;
					toReturn.minZ = 0.7 - offset;
					
					toReturn.maxX = 0.7 - offset;
					toReturn.maxY = 0.3 - offset + ((float)i / (float)100);
					toReturn.maxZ = 1.0;
					break;
				}
				case WEST:
				{
					toReturn.minX = 0.0;
					toReturn.minY = 0.3 + offset;
					toReturn.minZ = 0.3 + offset;
					
					toReturn.maxX = 0.3 + offset;
					toReturn.maxY = 0.3 - offset + ((float)i / (float)100);
					toReturn.maxZ = 0.7 - offset;
					break;
				}
				case EAST:
				{
					toReturn.minX = 0.7 - offset;
					toReturn.minY = 0.3 + offset;
					toReturn.minZ = 0.3 + offset;
					
					toReturn.maxX = 1.0;
					toReturn.maxY = 0.3 - offset + ((float)i / (float)100);
					toReturn.maxZ = 0.7 - offset;
					break;
				}
			}
			
			MekanismRenderer.renderObject(toReturn);
			DisplayInteger.endList();
		}
		
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		
		return displays;
	}
}
