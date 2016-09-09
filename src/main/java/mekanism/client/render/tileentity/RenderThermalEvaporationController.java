package mekanism.client.render.tileentity;

import java.util.HashMap;
import java.util.Map;

import mekanism.client.render.FluidRenderer;
import mekanism.client.render.MekanismRenderer;
import mekanism.client.render.MekanismRenderer.DisplayInteger;
import mekanism.client.render.MekanismRenderer.FluidType;
import mekanism.client.render.MekanismRenderer.Model3D;
import mekanism.common.content.tank.TankUpdateProtocol;
import mekanism.common.tile.TileEntityThermalEvaporationController;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.init.Blocks;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import org.lwjgl.opengl.GL11;

@SideOnly(Side.CLIENT)
public class RenderThermalEvaporationController extends TileEntitySpecialRenderer<TileEntityThermalEvaporationController>
{
	private static Map<SalinationRenderData, HashMap<Fluid, DisplayInteger[]>> cachedCenterFluids = new HashMap<SalinationRenderData, HashMap<Fluid, DisplayInteger[]>>();

	@Override
	public void renderTileEntityAt(TileEntityThermalEvaporationController tileEntity, double x, double y, double z, float partialTick, int destroyStage)
	{
		if(tileEntity.structured && tileEntity.inputTank.getFluid() != null)
		{
			SalinationRenderData data = new SalinationRenderData();

			data.height = tileEntity.height-2;
			data.side = tileEntity.facing;

			bindTexture(MekanismRenderer.getBlocksTexture());
			
			if(data.height >= 1 && tileEntity.inputTank.getCapacity() > 0)
			{
				push();

				FluidRenderer.translateToOrigin(tileEntity.getRenderLocation());

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
		GlStateManager.popMatrix();
	}

	private void push()
	{
		GlStateManager.pushMatrix();
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
		toReturn.baseBlock = Blocks.WATER;
		toReturn.setTexture(MekanismRenderer.getFluidTexture(fluid, FluidType.STILL));

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

			if(fluid.getStill() != null)
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

		MekanismRenderer.resetColor();

		return displays;
	}

	private int getStages(int height)
	{
		return height*(TankUpdateProtocol.FLUID_PER_TANK/10);
	}

	public static class SalinationRenderData
	{
		public int height;
		public EnumFacing side;

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
