package mekanism.client.render.tileentity;

import java.util.HashMap;
import java.util.Map;

import mekanism.api.Object3D;
import mekanism.client.render.MekanismRenderer;
import mekanism.client.render.MekanismRenderer.DisplayInteger;
import mekanism.client.render.MekanismRenderer.Model3D;
import mekanism.common.SynchronizedTankData.ValveData;
import mekanism.common.tileentity.TileEntityDynamicTank;
import net.minecraft.block.Block;
import net.minecraft.client.renderer.GLAllocation;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeDirection;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;

import org.lwjgl.opengl.GL11;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class RenderDynamicTank extends TileEntitySpecialRenderer
{
	private static Map<RenderData, HashMap<Fluid, int[]>> cachedCenterFluids = new HashMap<RenderData, HashMap<Fluid, int[]>>();
	private static Map<ValveRenderData, HashMap<Fluid, DisplayInteger>> cachedValveFluids = new HashMap<ValveRenderData, HashMap<Fluid, DisplayInteger>>();
	
	@Override
	public void renderTileEntityAt(TileEntity tileEntity, double x, double y, double z, float partialTick)
	{
		renderAModelAt((TileEntityDynamicTank)tileEntity, x, y, z, partialTick);
	}
	
	public void renderAModelAt(TileEntityDynamicTank tileEntity, double x, double y, double z, float partialTick)
	{		
		if(tileEntity.clientHasStructure && tileEntity.isRendering && tileEntity.structure != null && tileEntity.structure.fluidStored != null && tileEntity.structure.fluidStored.amount != 0)
		{
			RenderData data = new RenderData();
			
			data.location = tileEntity.structure.renderLocation;
			data.height = tileEntity.structure.volHeight;
			data.length = tileEntity.structure.volLength;
			data.width = tileEntity.structure.volWidth;
			
			func_110628_a(MekanismRenderer.getLiquidTexture());
			
			if(data.location != null && data.height > 0 && tileEntity.structure.fluidStored.getFluid() != null)
			{
				push();
				
				GL11.glTranslated(getX(data.location.xCoord), getY(data.location.yCoord), getZ(data.location.zCoord));
				
				if(tileEntity.structure.fluidStored.getFluid() == FluidRegistry.LAVA)
				{
					MekanismRenderer.glowOn();
				}
				
				boolean gas = tileEntity.structure.fluidStored.getFluid().isGaseous();
				
				int[] displayList = getListAndRender(data, tileEntity.structure.fluidStored.getFluid(), tileEntity.worldObj);
				if(gas)
				{
					GL11.glColor4f(1.F, 1.F, 1.F, (float)tileEntity.structure.fluidStored.amount / (float)tileEntity.clientCapacity);
					GL11.glCallList(displayList[getStages(data.height)-1]);
				}
				else {
					GL11.glCallList(displayList[(int)(((float)tileEntity.structure.fluidStored.amount/(float)tileEntity.clientCapacity)*((float)getStages(data.height)-1))]);
				}
				
				if(tileEntity.structure.fluidStored.getFluid() == FluidRegistry.LAVA)
				{
					MekanismRenderer.glowOff();
				}
				
				pop();
				
				for(ValveData valveData : tileEntity.valveViewing.keySet())
				{
					if(tileEntity.valveViewing.get(valveData) > 0)
					{
						push();
						
						GL11.glTranslated(getX(valveData.location.xCoord), getY(valveData.location.yCoord), getZ(valveData.location.zCoord));
						
						if(tileEntity.structure.fluidStored.getFluid() == FluidRegistry.LAVA)
						{
							MekanismRenderer.glowOn();
						}
						
						int display = getValveDisplay(ValveRenderData.get(data, valveData), tileEntity.structure.fluidStored.getFluid(), tileEntity.worldObj).display;
						GL11.glCallList(display);
						
						if(tileEntity.structure.fluidStored.getFluid() == FluidRegistry.LAVA)
						{
							MekanismRenderer.glowOff();
						}
						
						pop();
					}
				}
			}
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
	
	private int[] getListAndRender(RenderData data, Fluid fluid, World world)
	{
		if(cachedCenterFluids.containsKey(data) && cachedCenterFluids.get(data).containsKey(fluid))
		{
			return cachedCenterFluids.get(data).get(fluid);
		}
		
		Model3D toReturn = new Model3D();
		toReturn.baseBlock = Block.waterStill;
		toReturn.setTexture(fluid.getIcon());
		
		final int stages = getStages(data.height);
		int[] displays = new int[stages];
		
		if(cachedCenterFluids.containsKey(data))
		{
			cachedCenterFluids.get(data).put(fluid, displays);
		}
		else {
			HashMap<Fluid, int[]> map = new HashMap<Fluid, int[]>();
			map.put(fluid, displays);
			cachedCenterFluids.put(data, map);
		}
		
		MekanismRenderer.colorFluid(fluid);
		
		for(int i = 0; i < stages; i++)
		{
			displays[i] = GLAllocation.generateDisplayLists(1);
			GL11.glNewList(displays[i], 4864);
			
			toReturn.minX = 0 + .01;
			toReturn.minY = 0 + .01;
			toReturn.minZ = 0 + .01;
			
			toReturn.maxX = data.length-2 - .01;
			toReturn.maxY = ((float)i/(float)stages)*(data.height-2) - .01;
			toReturn.maxZ = data.width-2 - .01;
			
			MekanismRenderer.renderObject(toReturn);
			GL11.glEndList();
		}
		
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		
		return displays;
	}
	
	private DisplayInteger getValveDisplay(ValveRenderData data, Fluid fluid, World world)
	{
		if(cachedValveFluids.containsKey(data) && cachedValveFluids.get(data).containsKey(fluid))
		{
			return cachedValveFluids.get(data).get(fluid);
		}
		
		Model3D toReturn = new Model3D();
		toReturn.baseBlock = Block.waterStill;
		toReturn.setTexture(fluid.getFlowingIcon());
		
		DisplayInteger display = new DisplayInteger();
		
		if(cachedValveFluids.containsKey(data))
		{
			cachedValveFluids.get(data).put(fluid, display);
		}
		else {
			HashMap<Fluid, DisplayInteger> map = new HashMap<Fluid, DisplayInteger>();
			map.put(fluid, display);
			cachedValveFluids.put(data, map);
		}
		
		MekanismRenderer.colorFluid(fluid);
		
		display.display = GLAllocation.generateDisplayLists(1);
		GL11.glNewList(display.display, 4864);
		
		switch(data.side)
		{
			case DOWN:
			{
				toReturn.minX = .3;
				toReturn.minY = 1 + .01;
				toReturn.minZ = .3;
				
				toReturn.maxX = .7;
				toReturn.maxY = 1.4 + .1;
				toReturn.maxZ = .7;
				break;
			}
			case UP:
			{
				toReturn.minX = .3;
				toReturn.minY = -(data.height-2) - .01;
				toReturn.minZ = .3;
				
				toReturn.maxX = .7;
				toReturn.maxY = -.01;
				toReturn.maxZ = .7;
				break;
			}
			case NORTH:
			{
				toReturn.minX = .3;
				toReturn.minY = -(getValveFluidHeight(data)) + .01;
				toReturn.minZ = 1 + .02;
				
				toReturn.maxX = .7;
				toReturn.maxY = .7;
				toReturn.maxZ = 1.4;
				break;
			}
			case SOUTH:
			{
				toReturn.minX = .3;
				toReturn.minY = -(getValveFluidHeight(data)) + .01;
				toReturn.minZ = -.4;
				
				toReturn.maxX = .7;
				toReturn.maxY = .7;
				toReturn.maxZ = -.02;
				break;
			}
			case WEST:
			{
				toReturn.minX = 1 + .02;
				toReturn.minY = -(getValveFluidHeight(data)) + .01;
				toReturn.minZ = .3;
				
				toReturn.maxX = 1.4;
				toReturn.maxY = .7;
				toReturn.maxZ = .7;
				break;
			}
			case EAST:
			{
				toReturn.minX = -.4;
				toReturn.minY = -(getValveFluidHeight(data)) + .01;
				toReturn.minZ = .3;
				
				toReturn.maxX = -.02;
				toReturn.maxY = .7;
				toReturn.maxZ = .7;
				break;
			}
			default:
			{
				break;
			}
		}
		
		MekanismRenderer.renderObject(toReturn);
		GL11.glEndList();
		
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		
		return display;
	}
	
	private int getValveFluidHeight(ValveRenderData data)
	{
		return data.valveLocation.yCoord - data.location.yCoord;
	}
	
	private int getStages(int height)
	{
		return (height-2)*1600;
	}
	
	private double getX(int x)
	{
		return x - TileEntityRenderer.staticPlayerX;
	}
	
	private double getY(int y)
	{
		return y - TileEntityRenderer.staticPlayerY;
	}
	
	private double getZ(int z)
	{
		return z - TileEntityRenderer.staticPlayerZ;
	}
	
	public static class RenderData
	{
		public Object3D location;
		
		public int height;
		public int length;
		public int width;
		
		@Override
		public int hashCode() 
		{
			int code = 1;
			code = 31 * code + location.hashCode();
			code = 31 * code + height;
			code = 31 * code + length;
			code = 31 * code + width;
			return code;
		}
		
		@Override
		public boolean equals(Object data)
		{
			return data instanceof RenderData && ((RenderData)data).location.equals(location) && ((RenderData)data).height == height;
		}
	}
	
	public static class ValveRenderData extends RenderData
	{
		public ForgeDirection side;
		public Object3D valveLocation;
		
		public static ValveRenderData get(RenderData renderData, ValveData valveData)
		{
			ValveRenderData data = new ValveRenderData();
			
			data.location = renderData.location;
			data.height = renderData.height;
			data.length = renderData.length;
			data.width = renderData.width;
			
			data.side = valveData.side;
			data.valveLocation = valveData.location;
			
			return data;
		}
		
		@Override
		public boolean equals(Object data)
		{
			return data instanceof ValveRenderData && super.equals(data) && ((ValveRenderData)data).side.equals(side);
		}
		
		@Override
		public int hashCode()
		{
			int code = 1;
			code = 31 * code + super.hashCode();
			code = 31 * code + side.ordinal();
			code = 31 * code + valveLocation.hashCode();
			return code;
		}
	}
}
