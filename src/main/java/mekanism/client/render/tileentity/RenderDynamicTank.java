package mekanism.client.render.tileentity;

import java.util.HashMap;
import java.util.Map;

import mekanism.api.Coord4D;
import mekanism.client.render.MekanismRenderer;
import mekanism.client.render.MekanismRenderer.DisplayInteger;
import mekanism.client.render.MekanismRenderer.Model3D;
import mekanism.common.content.tank.SynchronizedTankData.ValveData;
import mekanism.common.content.tank.TankUpdateProtocol;
import mekanism.common.tile.TileEntityDynamicTank;

import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.init.Blocks;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.fluids.Fluid;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

import org.lwjgl.opengl.GL11;

@SideOnly(Side.CLIENT)
public class RenderDynamicTank extends TileEntitySpecialRenderer
{
	private static Map<RenderData, HashMap<Fluid, DisplayInteger[]>> cachedCenterFluids = new HashMap<RenderData, HashMap<Fluid, DisplayInteger[]>>();
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

			bindTexture(MekanismRenderer.getBlocksTexture());

			if(data.location != null && data.height >= 3 && tileEntity.structure.fluidStored.getFluid() != null)
			{
				push();

				GL11.glTranslated(getX(data.location.xCoord), getY(data.location.yCoord), getZ(data.location.zCoord));

				MekanismRenderer.glowOn(tileEntity.structure.fluidStored.getFluid().getLuminosity());
				MekanismRenderer.colorFluid(tileEntity.structure.fluidStored.getFluid());

				DisplayInteger[] displayList = getListAndRender(data, tileEntity.structure.fluidStored.getFluid(), tileEntity.getWorldObj());

				if(tileEntity.structure.fluidStored.getFluid().isGaseous())
				{
					GL11.glColor4f(1F, 1F, 1F, Math.min(1, ((float)tileEntity.structure.fluidStored.amount / (float)tileEntity.clientCapacity)+0.3F));
					displayList[getStages(data.height)-1].render();
				}
				else {
					displayList[Math.min(getStages(data.height)-1, (int)(tileEntity.prevScale*((float)getStages(data.height)-1)))].render();
				}

				MekanismRenderer.glowOff();
				MekanismRenderer.resetColor();

				pop();

				for(ValveData valveData : tileEntity.valveViewing.keySet())
				{
					if(tileEntity.valveViewing.get(valveData) > 0)
					{
						push();

						GL11.glTranslated(getX(valveData.location.xCoord), getY(valveData.location.yCoord), getZ(valveData.location.zCoord));

						MekanismRenderer.glowOn(tileEntity.structure.fluidStored.getFluid().getLuminosity());

						getValveDisplay(ValveRenderData.get(data, valveData), tileEntity.structure.fluidStored.getFluid(), tileEntity.getWorldObj()).render();

						MekanismRenderer.glowOff();
						MekanismRenderer.resetColor();

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

	private DisplayInteger[] getListAndRender(RenderData data, Fluid fluid, World world)
	{
		if(cachedCenterFluids.containsKey(data) && cachedCenterFluids.get(data).containsKey(fluid))
		{
			return cachedCenterFluids.get(data).get(fluid);
		}

		Model3D toReturn = new Model3D();
		toReturn.baseBlock = Blocks.water;
		toReturn.setTexture(fluid.getIcon());

		final int stages = getStages(data.height);
		DisplayInteger[] displays = new DisplayInteger[stages];

		if(cachedCenterFluids.containsKey(data))
		{
			cachedCenterFluids.get(data).put(fluid, displays);
		}
		else {
			HashMap<Fluid, DisplayInteger[]> map = new HashMap<Fluid, DisplayInteger[]>();
			map.put(fluid, displays);
			cachedCenterFluids.put(data, map);
		}

		for(int i = 0; i < stages; i++)
		{
			displays[i] = DisplayInteger.createAndStart();

			if(fluid.getIcon() != null)
			{
				toReturn.minX = 0 + .01;
				toReturn.minY = 0 + .01;
				toReturn.minZ = 0 + .01;

				toReturn.maxX = data.length - .01;
				toReturn.maxY = ((float)i/(float)stages)*(data.height-2) - .01;
				toReturn.maxZ = data.width - .01;

				MekanismRenderer.renderObject(toReturn);
			}

			GL11.glEndList();
		}

		return displays;
	}

	private DisplayInteger getValveDisplay(ValveRenderData data, Fluid fluid, World world)
	{
		if(cachedValveFluids.containsKey(data) && cachedValveFluids.get(data).containsKey(fluid))
		{
			return cachedValveFluids.get(data).get(fluid);
		}

		Model3D toReturn = new Model3D();
		toReturn.baseBlock = Blocks.water;
		toReturn.setTexture(fluid.getFlowingIcon());

		DisplayInteger display = DisplayInteger.createAndStart();

		if(cachedValveFluids.containsKey(data))
		{
			cachedValveFluids.get(data).put(fluid, display);
		}
		else {
			HashMap<Fluid, DisplayInteger> map = new HashMap<Fluid, DisplayInteger>();
			map.put(fluid, display);
			cachedValveFluids.put(data, map);
		}

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

		if(fluid.getFlowingIcon() != null)
		{
			MekanismRenderer.renderObject(toReturn);
		}
		
		display.endList();

		return display;
	}

	private int getValveFluidHeight(ValveRenderData data)
	{
		return data.valveLocation.yCoord - data.location.yCoord;
	}

	private int getStages(int height)
	{
		return (height-2)*(TankUpdateProtocol.FLUID_PER_TANK/10);
	}

	private double getX(int x)
	{
		return x - TileEntityRendererDispatcher.staticPlayerX;
	}

	private double getY(int y)
	{
		return y - TileEntityRendererDispatcher.staticPlayerY;
	}

	private double getZ(int z)
	{
		return z - TileEntityRendererDispatcher.staticPlayerZ;
	}

	public static class RenderData
	{
		public Coord4D location;

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
			return data instanceof RenderData && ((RenderData)data).location.equals(location) && ((RenderData)data).height == height &&
					((RenderData)data).length == length && ((RenderData)data).width == width;
		}
	}

	public static class ValveRenderData extends RenderData
	{
		public ForgeDirection side;
		public Coord4D valveLocation;

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

	public static void resetDisplayInts()
	{
		cachedCenterFluids.clear();
		cachedValveFluids.clear();
	}
}
