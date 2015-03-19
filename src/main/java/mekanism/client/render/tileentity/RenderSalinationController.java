package mekanism.client.render.tileentity;

import java.util.HashMap;
import java.util.Map;

import mekanism.api.Coord4D;
import mekanism.client.render.MekanismRenderer;
import mekanism.client.render.MekanismRenderer.DisplayInteger;
import mekanism.client.render.MekanismRenderer.Model3D;
import mekanism.common.content.tank.TankUpdateProtocol;
import mekanism.common.tile.TileEntitySolarEvaporationController;

import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.init.Blocks;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.fluids.Fluid;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

import org.lwjgl.opengl.GL11;

@SideOnly(Side.CLIENT)
public class RenderSalinationController extends TileEntitySpecialRenderer
{
	private static Map<SalinationRenderData, HashMap<Fluid, DisplayInteger[]>> cachedCenterFluids = new HashMap<SalinationRenderData, HashMap<Fluid, DisplayInteger[]>>();

	@Override
	public void renderTileEntityAt(TileEntity tileEntity, double x, double y, double z, float partialTick)
	{
		renderAModelAt((TileEntitySolarEvaporationController)tileEntity, x, y, z, partialTick);
	}

	public void renderAModelAt(TileEntitySolarEvaporationController tileEntity, double x, double y, double z, float partialTick)
	{
		if(tileEntity.structured && tileEntity.inputTank.getFluid() != null)
		{
			SalinationRenderData data = new SalinationRenderData();

			data.height = tileEntity.height-2;
			data.side = ForgeDirection.getOrientation(tileEntity.facing);

			bindTexture(MekanismRenderer.getBlocksTexture());
			
			if(data.height >= 1 && tileEntity.inputTank.getCapacity() > 0)
			{
				Coord4D renderLoc = tileEntity.getRenderLocation();

				push();

				GL11.glTranslated(getX(renderLoc.xCoord), getY(renderLoc.yCoord), getZ(renderLoc.zCoord));

				MekanismRenderer.glowOn(tileEntity.inputTank.getFluid().getFluid().getLuminosity());

				DisplayInteger[] displayList = getListAndRender(data, tileEntity.inputTank.getFluid().getFluid());
				displayList[(int)(((float)tileEntity.inputTank.getFluidAmount()/tileEntity.inputTank.getCapacity())*((float)getStages(data.height)-1))].render();

				MekanismRenderer.glowOff();

				pop();
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

	@SuppressWarnings("incomplete-switch")
	private DisplayInteger[] getListAndRender(SalinationRenderData data, Fluid fluid)
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

		MekanismRenderer.colorFluid(fluid);

		for(int i = 0; i < stages; i++)
		{
			displays[i] = DisplayInteger.createAndStart();

			if(fluid.getIcon() != null)
			{
				switch(data.side)
				{
					case NORTH:
						toReturn.minX = 0 + .01;
						toReturn.minY = 0 + .01;
						toReturn.minZ = 0 + .01;

						toReturn.maxX = 2 - .01;
						toReturn.maxY = ((float)i/(float)stages)*data.height - .01;
						toReturn.maxZ = 2 - .01;
						break;
					case SOUTH:
						toReturn.minX = -1 + .01;
						toReturn.minY = 0 + .01;
						toReturn.minZ = -1 + .01;

						toReturn.maxX = 1 - .01;
						toReturn.maxY = ((float)i/(float)stages)*data.height - .01;
						toReturn.maxZ = 1 - .01;
						break;
					case WEST:
						toReturn.minX = 0 + .01;
						toReturn.minY = 0 + .01;
						toReturn.minZ = -1 + .01;

						toReturn.maxX = 2 - .01;
						toReturn.maxY = ((float)i/(float)stages)*data.height - .01;
						toReturn.maxZ = 1 - .01;
						break;
					case EAST:
						toReturn.minX = -1 + .01;
						toReturn.minY = 0 + .01;
						toReturn.minZ = 0 + .01;

						toReturn.maxX = 1 - .01;
						toReturn.maxY = ((float)i/(float)stages)*data.height - .01;
						toReturn.maxZ = 2 - .01;
						break;
				}

				MekanismRenderer.renderObject(toReturn);
			}

			displays[i].endList();
		}

		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);

		return displays;
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

	public static class SalinationRenderData
	{
		public int height;
		public ForgeDirection side;

		@Override
		public int hashCode()
		{
			int code = 1;
			code = 31 * code + height;
			return code;
		}

		@Override
		public boolean equals(Object data)
		{
			return data instanceof SalinationRenderData && ((SalinationRenderData)data).height == height &&
					((SalinationRenderData)data).side == side;
		}
	}

	public static void resetDisplayInts()
	{
		cachedCenterFluids.clear();
	}
}
