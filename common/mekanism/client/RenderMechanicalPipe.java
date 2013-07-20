package mekanism.client;

import java.util.HashMap;

import mekanism.client.MekanismRenderer.Model3D;
import mekanism.common.MekanismUtils;
import mekanism.common.MekanismUtils.ResourceType;
import mekanism.common.PipeUtils;
import mekanism.common.TileEntityMechanicalPipe;
import net.minecraft.block.Block;
import net.minecraft.client.renderer.GLAllocation;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
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
	
	private HashMap<ForgeDirection, HashMap<Fluid, int[]>> cachedLiquids = new HashMap<ForgeDirection, HashMap<Fluid, int[]>>();
	
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
		
		boolean[] connectable = PipeUtils.getConnections(tileEntity);
		
		for(int i = 0; i < 6; i++)
		{
			model.renderSide(ForgeDirection.getOrientation(i), connectable[i]);
		}
		
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
					int[] displayList = getListAndRender(ForgeDirection.getOrientation(i), tileEntity.refFluid.getFluid());
					
					if(displayList != null)
					{
						GL11.glCallList(displayList[Math.max(3, (int)((float)tileEntity.fluidScale*(stages-1)))]);
					}
				}
			}
			
			int[] displayList = getListAndRender(ForgeDirection.UNKNOWN, tileEntity.refFluid.getFluid());
			
			if(displayList != null)
			{
				GL11.glCallList(displayList[Math.max(3, (int)((float)tileEntity.fluidScale*(stages-1)))]);
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
	
	private int[] getListAndRender(ForgeDirection side, Fluid fluid)
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
		
		int[] displays = new int[stages];
		
		if(cachedLiquids.containsKey(side))
		{
			cachedLiquids.get(side).put(fluid, displays);
		}
		else {
			HashMap<Fluid, int[]> map = new HashMap<Fluid, int[]>();
			map.put(fluid, displays);
			cachedLiquids.put(side, map);
		}
		
		MekanismRenderer.colorFluid(fluid);
		
		for(int i = 0; i < stages; i++)
		{
			displays[i] = GLAllocation.generateDisplayLists(1);
			GL11.glNewList(displays[i], 4864);
			
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
			GL11.glEndList();
		}
		
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		
		return displays;
	}
}
