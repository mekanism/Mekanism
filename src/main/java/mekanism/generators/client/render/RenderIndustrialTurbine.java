package mekanism.generators.client.render;

import java.util.HashMap;
import java.util.Map;

import mekanism.api.Coord4D;
import mekanism.client.render.MekanismRenderer;
import mekanism.client.render.MekanismRenderer.DisplayInteger;
import mekanism.client.render.MekanismRenderer.Model3D;
import mekanism.client.render.tileentity.RenderDynamicTank.RenderData;
import mekanism.common.content.tank.TankUpdateProtocol;
import mekanism.generators.common.tile.turbine.TileEntityTurbineCasing;
import mekanism.generators.common.tile.turbine.TileEntityTurbineRotor;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.init.Blocks;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;

import org.lwjgl.opengl.GL11;

public class RenderIndustrialTurbine extends TileEntitySpecialRenderer
{
	private static Map<RenderData, DisplayInteger[]> cachedFluids = new HashMap<RenderData, DisplayInteger[]>();
	
	private Fluid STEAM = FluidRegistry.getFluid("steam");
	
	@Override
	public void renderTileEntityAt(TileEntity tileEntity, double x, double y, double z, float partialTick)
	{
		renderAModelAt((TileEntityTurbineCasing)tileEntity, x, y, z, partialTick);
	}

	public void renderAModelAt(TileEntityTurbineCasing tileEntity, double x, double y, double z, float partialTick)
	{
		if(tileEntity.clientHasStructure && tileEntity.isRendering && tileEntity.structure != null)
		{
			RenderTurbineRotor.internalRender = true;
			Coord4D coord = tileEntity.structure.complex;
			
			while(true)
			{
				coord = coord.getFromSide(ForgeDirection.DOWN);
				TileEntity tile = coord.getTileEntity(tileEntity.getWorldObj());
				
				if(!(tile instanceof TileEntityTurbineRotor))
				{
					break;
				}
				
				TileEntityRendererDispatcher.instance.renderTileEntity(tile, partialTick);
			}
			
			RenderTurbineRotor.internalRender = false;
			
			if(tileEntity.structure.fluidStored != null && tileEntity.structure.fluidStored.amount != 0)
			{
				RenderData data = new RenderData();
	
				data.location = tileEntity.structure.renderLocation;
				data.height = tileEntity.structure.lowerVolume/(tileEntity.structure.volLength*tileEntity.structure.volWidth);
				data.length = tileEntity.structure.volLength;
				data.width = tileEntity.structure.volWidth;
				
				bindTexture(MekanismRenderer.getBlocksTexture());
				
				if(data.location != null && data.height >= 1 && tileEntity.structure.fluidStored.getFluid() != null)
				{
					push();
	
					GL11.glTranslated(getX(data.location.xCoord), getY(data.location.yCoord), getZ(data.location.zCoord));
					
					MekanismRenderer.glowOn(tileEntity.structure.fluidStored.getFluid().getLuminosity());
					MekanismRenderer.colorFluid(tileEntity.structure.fluidStored.getFluid());
	
					DisplayInteger[] displayList = getListAndRender(data, tileEntity.getWorldObj());
	
					GL11.glColor4f(1F, 1F, 1F, Math.min(1, ((float)tileEntity.structure.fluidStored.amount / (float)tileEntity.structure.getFluidCapacity())+0.3F));
					displayList[getStages(data.height+1)-1].render();
	
					MekanismRenderer.glowOff();
					MekanismRenderer.resetColor();
	
					pop();
				}
			}
		}
	}
	
	private void pop()
	{
		GL11.glDisable(GL11.GL_CULL_FACE);
		GL11.glDisable(GL11.GL_BLEND);
		GL11.glPopMatrix();
	}

	private void push()
	{
		GL11.glPushMatrix();
		
		GL11.glEnable(GL11.GL_CULL_FACE);
		GL11.glEnable(GL11.GL_BLEND);
	    GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
	}
	
	private int getStages(int height)
	{
		return TankUpdateProtocol.FLUID_PER_TANK/10;
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
	
	private DisplayInteger[] getListAndRender(RenderData data, World world)
	{
		if(cachedFluids.containsKey(data))
		{
			return cachedFluids.get(data);
		}

		Model3D toReturn = new Model3D();
		toReturn.baseBlock = Blocks.water;
		toReturn.setTexture(STEAM.getIcon());

		final int stages = getStages(data.height);
		DisplayInteger[] displays = new DisplayInteger[stages];

		cachedFluids.put(data, displays);

		for(int i = 0; i < stages; i++)
		{
			displays[i] = DisplayInteger.createAndStart();

			if(STEAM.getIcon() != null)
			{
				toReturn.minX = 0 + .01;
				toReturn.minY = 0 + .01;
				toReturn.minZ = 0 + .01;

				toReturn.maxX = data.length - .01;
				toReturn.maxY = ((float)i/(float)stages)*(data.height+1) - .01;
				toReturn.maxZ = data.width - .01;

				MekanismRenderer.renderObject(toReturn);
			}

			GL11.glEndList();
		}

		return displays;
	}
	
	public static void resetDisplayInts()
	{
		cachedFluids.clear();
	}
}
