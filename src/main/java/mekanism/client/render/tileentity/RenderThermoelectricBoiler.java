package mekanism.client.render.tileentity;

import java.util.HashMap;
import java.util.Map;

import mekanism.client.render.MekanismRenderer;
import mekanism.client.render.MekanismRenderer.DisplayInteger;
import mekanism.client.render.MekanismRenderer.Model3D;
import mekanism.client.render.tileentity.RenderDynamicTank.RenderData;
import mekanism.client.render.tileentity.RenderDynamicTank.ValveRenderData;
import mekanism.common.content.tank.SynchronizedTankData.ValveData;
import mekanism.common.content.tank.TankUpdateProtocol;
import mekanism.common.tile.TileEntityBoilerCasing;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.init.Blocks;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;

import org.lwjgl.opengl.GL11;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class RenderThermoelectricBoiler extends TileEntitySpecialRenderer
{
	private static Map<RenderData, DisplayInteger[]> cachedLowerFluids = new HashMap<RenderData, DisplayInteger[]>();
	private static Map<RenderData, DisplayInteger> cachedUpperFluids = new HashMap<RenderData, DisplayInteger>();
	private static Map<ValveRenderData, DisplayInteger> cachedValveFluids = new HashMap<ValveRenderData, DisplayInteger>();
	
	private Fluid STEAM = FluidRegistry.getFluid("steam");
	private Fluid WATER = FluidRegistry.WATER;
	
	@Override
	public void renderTileEntityAt(TileEntity tileEntity, double x, double y, double z, float partialTick)
	{
		renderAModelAt((TileEntityBoilerCasing)tileEntity, x, y, z, partialTick);
	}

	public void renderAModelAt(TileEntityBoilerCasing tileEntity, double x, double y, double z, float partialTick)
	{
		if(tileEntity.clientHasStructure && tileEntity.isRendering && tileEntity.structure != null && tileEntity.structure.renderLocation != null && tileEntity.structure.upperRenderLocation != null)
		{
			if(tileEntity.structure.waterStored != null && tileEntity.structure.waterStored.amount != 0)
			{
				RenderData data = new RenderData();

				data.location = tileEntity.structure.renderLocation;
				data.height = (tileEntity.structure.upperRenderLocation.yCoord-1)-tileEntity.structure.renderLocation.yCoord;
				data.length = tileEntity.structure.volLength;
				data.width = tileEntity.structure.volWidth;

				bindTexture(MekanismRenderer.getBlocksTexture());
				
				if(data.location != null && data.height >= 1 && tileEntity.structure.waterStored.getFluid() != null)
				{
					push();

					GL11.glTranslatef((float)getX(data.location.xCoord), (float)getY(data.location.yCoord), (float)getZ(data.location.zCoord));

					MekanismRenderer.glowOn(tileEntity.structure.waterStored.getFluid().getLuminosity());
					MekanismRenderer.colorFluid(tileEntity.structure.waterStored.getFluid());

					DisplayInteger[] displayList = getLowerDisplay(data, tileEntity.structure.waterStored.getFluid(), tileEntity.getWorldObj());

					if(tileEntity.structure.waterStored.getFluid().isGaseous())
					{
						GL11.glColor4f(1F, 1F, 1F, Math.min(1, ((float)tileEntity.structure.waterStored.amount / (float)tileEntity.clientWaterCapacity)+MekanismRenderer.GAS_RENDER_BASE));
						displayList[getStages(data.height)-1].render();
					}
					else {
						displayList[Math.min(getStages(data.height)-1, (int)(tileEntity.prevWaterScale*((float)getStages(data.height)-1)))].render();
					}

					MekanismRenderer.glowOff();
					MekanismRenderer.resetColor();

					pop();

					for(ValveData valveData : tileEntity.valveViewing)
					{
						push();

						GL11.glTranslatef((float)getX(valveData.location.xCoord), (float)getY(valveData.location.yCoord), (float)getZ(valveData.location.zCoord));

						MekanismRenderer.glowOn(tileEntity.structure.waterStored.getFluid().getLuminosity());

						getValveDisplay(ValveRenderData.get(data, valveData), tileEntity.structure.waterStored.getFluid(), tileEntity.getWorldObj()).render();

						MekanismRenderer.glowOff();
						MekanismRenderer.resetColor();

						pop();
					}
				}
			}
			
			if(tileEntity.structure.steamStored != null && tileEntity.structure.steamStored.amount != 0)
			{
				RenderData data = new RenderData();

				data.location = tileEntity.structure.upperRenderLocation;
				data.height = (tileEntity.structure.renderLocation.yCoord+tileEntity.structure.volHeight-2)-(tileEntity.structure.upperRenderLocation.yCoord);
				data.length = tileEntity.structure.volLength;
				data.width = tileEntity.structure.volWidth;

				bindTexture(MekanismRenderer.getBlocksTexture());
				
				if(data.location != null && data.height >= 1 && tileEntity.structure.steamStored.getFluid() != null)
				{
					push();
					
					GL11.glTranslatef((float)getX(data.location.xCoord), (float)getY(data.location.yCoord), (float)getZ(data.location.zCoord));
					
					MekanismRenderer.glowOn(tileEntity.structure.steamStored.getFluid().getLuminosity());
					MekanismRenderer.colorFluid(tileEntity.structure.steamStored.getFluid());
	
					DisplayInteger display = getUpperDisplay(data, tileEntity.structure.steamStored.getFluid(), tileEntity.getWorldObj());
	
					GL11.glColor4f(1F, 1F, 1F, Math.min(1, ((float)tileEntity.structure.steamStored.amount / (float)tileEntity.clientSteamCapacity)+MekanismRenderer.GAS_RENDER_BASE));
					display.render();
	
					MekanismRenderer.glowOff();
					MekanismRenderer.resetColor();
	
					pop();
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
	
	private DisplayInteger[] getLowerDisplay(RenderData data, Fluid fluid, World world)
	{
		if(cachedLowerFluids.containsKey(data))
		{
			return cachedLowerFluids.get(data);
		}

		Model3D toReturn = new Model3D();
		toReturn.baseBlock = Blocks.water;
		toReturn.setTexture(fluid.getIcon());

		final int stages = getStages(data.height);
		DisplayInteger[] displays = new DisplayInteger[stages];

		cachedLowerFluids.put(data, displays);

		for(int i = 0; i < stages; i++)
		{
			displays[i] = DisplayInteger.createAndStart();

			if(fluid.getIcon() != null)
			{
				toReturn.minX = 0 + .01;
				toReturn.minY = 0 + .01;
				toReturn.minZ = 0 + .01;

				toReturn.maxX = data.length - .01;
				toReturn.maxY = ((float)i/(float)stages)*data.height - .01;
				toReturn.maxZ = data.width - .01;

				MekanismRenderer.renderObject(toReturn);
			}

			GL11.glEndList();
		}

		return displays;
	}
	
	private DisplayInteger getUpperDisplay(RenderData data, Fluid fluid, World world)
	{
		if(cachedUpperFluids.containsKey(data))
		{
			return cachedUpperFluids.get(data);
		}

		Model3D toReturn = new Model3D();
		toReturn.baseBlock = Blocks.water;
		toReturn.setTexture(fluid.getIcon());

		final int stages = getStages(data.height);
		DisplayInteger display = DisplayInteger.createAndStart();

		cachedUpperFluids.put(data, display);
		
		if(STEAM.getIcon() != null)
		{
			toReturn.minX = 0 + .01;
			toReturn.minY = 0 + .01;
			toReturn.minZ = 0 + .01;

			toReturn.maxX = data.length - .01;
			toReturn.maxY = data.height - .01;
			toReturn.maxZ = data.width - .01;

			MekanismRenderer.renderObject(toReturn);
		}

		GL11.glEndList();

		return display;
	}

	private DisplayInteger getValveDisplay(ValveRenderData data, Fluid fluid, World world)
	{
		if(cachedValveFluids.containsKey(data))
		{
			return cachedValveFluids.get(data);
		}

		Model3D toReturn = new Model3D();
		toReturn.baseBlock = Blocks.water;
		toReturn.setTexture(fluid.getFlowingIcon());

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
		return height*(TankUpdateProtocol.FLUID_PER_TANK/10);
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
	
	public static void resetDisplayInts()
	{
		cachedLowerFluids.clear();
		cachedUpperFluids.clear();
		cachedValveFluids.clear();
	}
}
