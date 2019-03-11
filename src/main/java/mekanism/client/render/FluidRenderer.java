package mekanism.client.render;

import java.util.HashMap;
import java.util.Map;

import mekanism.api.Coord4D;
import mekanism.client.render.MekanismRenderer.DisplayInteger;
import mekanism.client.render.MekanismRenderer.FluidType;
import mekanism.client.render.MekanismRenderer.Model3D;
import mekanism.common.content.tank.SynchronizedTankData.ValveData;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.init.Blocks;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.fluids.Fluid;

import net.minecraftforge.fluids.FluidStack;
import org.lwjgl.opengl.GL11;

public final class FluidRenderer 
{
	private static final int BLOCK_STAGES = 1000;
	
	private static Map<RenderData, DisplayInteger[]> cachedCenterFluids = new HashMap<>();
	private static Map<ValveRenderData, DisplayInteger> cachedValveFluids = new HashMap<>();
	
	public static void translateToOrigin(Coord4D origin)
	{
		GL11.glTranslated(getX(origin.x), getY(origin.y), getZ(origin.z));
	}
	
	public static int getStages(RenderData data)
	{
		return data.height*BLOCK_STAGES;
	}
	
	public static void pop()
	{
		GL11.glPopAttrib();
		GlStateManager.popMatrix();
	}

	public static void push()
	{
		GlStateManager.pushMatrix();
		GL11.glPushAttrib(GL11.GL_ENABLE_BIT);
		GL11.glEnable(GL11.GL_CULL_FACE);
		GL11.glEnable(GL11.GL_BLEND);
		GL11.glDisable(GL11.GL_LIGHTING);
		GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
	}
	
	public static DisplayInteger getTankDisplay(RenderData data)
	{
		return getTankDisplay(data, 1);
	}
	
	public static DisplayInteger getTankDisplay(RenderData data, double scale)
	{
		int maxStages = getStages(data);
		int stage = Math.min(maxStages, (int)(scale*(float)maxStages));
				
		if(cachedCenterFluids.containsKey(data))
		{
			DisplayInteger[] ret = cachedCenterFluids.get(data);
			
			if(ret[stage] != null)
			{
				return ret[stage];
			}
		}

		Model3D toReturn = new Model3D();
		toReturn.baseBlock = Blocks.WATER;
		toReturn.setTexture(MekanismRenderer.getFluidTexture(data.fluidType, FluidType.STILL));

		DisplayInteger display = DisplayInteger.createAndStart();
		
		if(!cachedCenterFluids.containsKey(data))
		{
			cachedCenterFluids.put(data, new DisplayInteger[maxStages+1]);
		}
		
		cachedCenterFluids.get(data)[stage] = display;
		
		if(maxStages == 0)
		{
			maxStages = stage = 1;
		}

		if(data.fluidType.getFluid().getStill(data.fluidType) != null)
		{
			toReturn.minX = 0 + .01;
			toReturn.minY = 0 + .01;
			toReturn.minZ = 0 + .01;

			toReturn.maxX = data.length - .01;
			toReturn.maxY = ((float)stage/(float)maxStages)*data.height - .01;
			toReturn.maxZ = data.width - .01;

			MekanismRenderer.renderObject(toReturn);
		}

		DisplayInteger.endList();

		return display;
	}
	
	public static class RenderData
	{
		public Coord4D location;

		public int height;
		public int length;
		public int width;
		
		public FluidStack fluidType;

		@Override
		public int hashCode()
		{
			int code = 1;
			code = 31 * code + location.hashCode();
			code = 31 * code + height;
			code = 31 * code + length;
			code = 31 * code + width;
			code = 31 * code + fluidType.getFluid().getName().hashCode();
			code = 31 * code + (fluidType.tag != null ? fluidType.tag.hashCode() : 0);
			return code;
		}

		@Override
		public boolean equals(Object data)
		{
			return data instanceof RenderData && ((RenderData)data).height == height &&
					((RenderData)data).length == length && ((RenderData)data).width == width && ((RenderData)data).fluidType.isFluidEqual(fluidType);
		}
	}
	
	public static DisplayInteger getValveDisplay(ValveRenderData data)
	{
		if(cachedValveFluids.containsKey(data))
		{
			return cachedValveFluids.get(data);
		}

		Model3D toReturn = new Model3D();
		toReturn.baseBlock = Blocks.WATER;
		MekanismRenderer.prepFlowing(toReturn, data.fluidType);

		DisplayInteger display = DisplayInteger.createAndStart();
		cachedValveFluids.put(data, display);

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
				toReturn.minY = -data.height - .01;
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

		if(data.fluidType.getFluid().getFlowing(data.fluidType) != null)
		{
			MekanismRenderer.renderObject(toReturn);
		}
		
		DisplayInteger.endList();

		return display;
	}
	
	private static int getValveFluidHeight(ValveRenderData data)
	{
		return data.valveLocation.y - data.location.y;
	}
	
	private static double getX(int x)
	{
		return x - TileEntityRendererDispatcher.staticPlayerX;
	}

	private static double getY(int y)
	{
		return y - TileEntityRendererDispatcher.staticPlayerY;
	}

	private static double getZ(int z)
	{
		return z - TileEntityRendererDispatcher.staticPlayerZ;
	}

	public static class ValveRenderData extends RenderData
	{
		public EnumFacing side;
		public Coord4D valveLocation;

		public static ValveRenderData get(RenderData renderData, ValveData valveData)
		{
			ValveRenderData data = new ValveRenderData();

			data.location = renderData.location;
			data.height = renderData.height;
			data.length = renderData.length;
			data.width = renderData.width;
			data.fluidType = renderData.fluidType;

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
	
	public static void resetDisplayInts()
	{
		cachedCenterFluids.clear();
		cachedValveFluids.clear();
	}
}
