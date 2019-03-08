package mekanism.client.render.tileentity;

import java.util.HashMap;

import mekanism.client.render.FluidRenderer;
import mekanism.client.render.MekanismRenderer;
import mekanism.client.render.MekanismRenderer.DisplayInteger;
import mekanism.client.render.MekanismRenderer.FluidType;
import mekanism.client.render.MekanismRenderer.Model3D;
import mekanism.common.tile.TileEntityThermalEvaporationController;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import org.lwjgl.opengl.GL11;
import java.util.Arrays;

@SideOnly(Side.CLIENT)
public class RenderThermalEvaporationController extends TileEntitySpecialRenderer<TileEntityThermalEvaporationController>
{
	private static HashMap<Fluid, DisplayInteger[]> cachedCenterFluids = new HashMap<>();
	private static final int LEVELS = 16;
	private static final int ALL_LEVELS = LEVELS + 2;
	private static final int RING_INDEX = ALL_LEVELS-2;
	private static final int CONCAVE_INDEX = ALL_LEVELS-1;

	@Override
	public void render(TileEntityThermalEvaporationController tileEntity, double x, double y, double z, float partialTick, int destroyStage, float alpha)
	{
		if(tileEntity.structured && tileEntity.inputTank.getFluid() != null)
		{

			bindTexture(MekanismRenderer.getBlocksTexture());
			
			if(tileEntity.height-2 >= 1 && tileEntity.inputTank.getCapacity() > 0)
			{
				push();

				FluidRenderer.translateToOrigin(tileEntity.getRenderLocation());
				MekanismRenderer.glowOn(tileEntity.inputTank.getFluid().getFluid().getLuminosity());
				MekanismRenderer.colorFluid(tileEntity.inputTank.getFluid().getFluid());
				DisplayInteger[] displayList = getListAndRender(tileEntity.inputTank.getFluid().getFluid());

				float levels = Math.min(((float)tileEntity.inputTank.getFluidAmount()/tileEntity.inputTank.getCapacity()), 1);
				levels *= (tileEntity.height-2);

				int partialLevels = (int)((levels-(int)levels)*16);

				switch(tileEntity.facing)
				{
					case SOUTH:
						GlStateManager.translate(-1, 0, -1);
						break;
					case EAST:
						GlStateManager.translate(-1, 0, 0);
						break;
					case WEST:
						GlStateManager.translate(0, 0, -1);
						break;
					default:
						break;
				}

				GlStateManager.translate(0, 0.01, 0);

				if((int)levels>0)
				{
					displayList[CONCAVE_INDEX].render();
					GlStateManager.translate(0, 1, 0);

					for(int i = 1; i < (int)levels; i++)
					{
						displayList[RING_INDEX].render();
						GlStateManager.translate(0, 1, 0);
					}
				}
				displayList[partialLevels].render();

				MekanismRenderer.resetColor();
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


	private DisplayInteger[] getListAndRender(Fluid fluid)
	{
		if(cachedCenterFluids.containsKey(fluid))
		{
			return cachedCenterFluids.get(fluid);
		}

		DisplayInteger[] displays = new DisplayInteger[ALL_LEVELS];

		Model3D model = new Model3D();
		model.baseBlock = fluid.getBlock();
		model.setTexture(MekanismRenderer.getFluidTexture(fluid, FluidType.STILL));

		MekanismRenderer.colorFluid(fluid);

		if(fluid.getStill() == null)
		{
			DisplayInteger empty = DisplayInteger.createAndStart();
			DisplayInteger.endList();
			Arrays.fill(displays, 0, LEVELS, empty);
		}
		else {
			model.setSideRender(EnumFacing.DOWN, false);
			
			for(int i = 0; i < LEVELS; i++)
			{
				displays[i] = generateLevel(i, model);
			}
			
			model.setSideRender(EnumFacing.UP, false);
			displays[RING_INDEX] = generateLevel(LEVELS-1, model);
			model.setSideRender(EnumFacing.DOWN, true);
			displays[CONCAVE_INDEX] = generateLevel(LEVELS-1, model);
		}

		MekanismRenderer.resetColor();
		cachedCenterFluids.put(fluid, displays);
		
		return displays;
	}

	private DisplayInteger generateLevel(int height, Model3D model)
	{
		DisplayInteger displayInteger = DisplayInteger.createAndStart();

		model.minX = 0 + .01;
		model.minY = 0;
		model.minZ = 0 + .01;
		model.maxX = 2 - .01;
		model.maxY = (float)height/(float)(LEVELS-1) + (height==0?.02:0);
		model.maxZ = 2 - .01;

		MekanismRenderer.renderObject(model);
		DisplayInteger.endList();

		return displayInteger;
	}

	public static void resetDisplayInts()
	{
		cachedCenterFluids.clear();
	}
}
