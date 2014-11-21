package mekanism.client.render.tileentity;

import java.util.HashMap;
import java.util.Map;

import mekanism.client.model.ModelPortableTank;
import mekanism.client.render.MekanismRenderer;
import mekanism.client.render.MekanismRenderer.DisplayInteger;
import mekanism.client.render.MekanismRenderer.Model3D;
import mekanism.common.tile.TileEntityPortableTank;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.ResourceType;

import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.init.Blocks;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.fluids.Fluid;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

import org.lwjgl.opengl.GL11;

@SideOnly(Side.CLIENT)
public class RenderPortableTank extends TileEntitySpecialRenderer
{
	private static Map<Fluid, DisplayInteger[]> cachedCenterFluids = new HashMap<Fluid, DisplayInteger[]>();
	private static Map<Fluid, DisplayInteger[]> cachedValveFluids = new HashMap<Fluid, DisplayInteger[]>();
	
	private static int stages = 1400;
	
	private ModelPortableTank model = new ModelPortableTank();

	@Override
	public void renderTileEntityAt(TileEntity tileEntity, double x, double y, double z, float partialTick)
	{
		renderAModelAt((TileEntityPortableTank)tileEntity, x, y, z, partialTick);
	}

	private void renderAModelAt(TileEntityPortableTank tileEntity, double x, double y, double z, float partialTick)
	{
		Fluid fluid = tileEntity.fluidTank.getFluid() != null ? tileEntity.fluidTank.getFluid().getFluid() : null;
		render(fluid, tileEntity.prevScale, tileEntity.isActive, tileEntity.valve > 0 ? tileEntity.valveFluid : null, x, y, z);
	}
	
	public void render(Fluid fluid, float fluidScale, boolean active, Fluid valveFluid, double x, double y, double z)
	{
		if(fluid != null && fluidScale > 0)
		{
			push();
			
			bindTexture(MekanismRenderer.getBlocksTexture());
			GL11.glTranslated(x, y, z);
	
			MekanismRenderer.glowOn(fluid.getLuminosity());
			MekanismRenderer.colorFluid(fluid);
	
			DisplayInteger[] displayList = getListAndRender(fluid);
	
			if(fluid.isGaseous())
			{
				GL11.glColor4f(1F, 1F, 1F, Math.min(1, fluidScale+0.3F));
				displayList[stages-1].render();
			}
			else {
				displayList[Math.min(stages-1, (int)(fluidScale*((float)stages-1)))].render();
			}
	
			MekanismRenderer.glowOff();
			MekanismRenderer.resetColor();
			
			pop();
		}
		
		if(valveFluid != null && !valveFluid.isGaseous())
		{
			push();
			
			bindTexture(MekanismRenderer.getBlocksTexture());
			GL11.glTranslated(x, y, z);
			
			MekanismRenderer.glowOn(valveFluid.getLuminosity());
			MekanismRenderer.colorFluid(valveFluid);
			
			DisplayInteger[] valveList = getValveRender(valveFluid);
			
			valveList[Math.min(stages-1, (int)(fluidScale*((float)stages-1)))].render();
			
			MekanismRenderer.glowOff();
			MekanismRenderer.resetColor();
			
			pop();
		}
		
		GL11.glPushMatrix();
		GL11.glTranslatef((float)x + 0.5F, (float)y + 1.5F, (float)z + 0.5F);
		bindTexture(MekanismUtils.getResource(ResourceType.RENDER, "PortableTank" + (active ? "On" : "") + ".png"));

		GL11.glRotatef(180F, 0.0F, 0.0F, 1.0F);
		model.render(0.0625F);
		GL11.glPopMatrix();
	}
	
	private void pop()
	{
		GL11.glPopAttrib();
		MekanismRenderer.blendOff();
		GL11.glPopMatrix();
	}

	private void push()
	{
		GL11.glPushMatrix();
		GL11.glPushAttrib(GL11.GL_ENABLE_BIT);
		GL11.glEnable(GL11.GL_CULL_FACE);
		GL11.glDisable(GL11.GL_LIGHTING);
		MekanismRenderer.blendOn();
	}
	
	private DisplayInteger[] getValveRender(Fluid fluid)
	{
		if(cachedValveFluids.containsKey(fluid))
		{
			return cachedValveFluids.get(fluid);
		}

		Model3D toReturn = new Model3D();
		toReturn.baseBlock = Blocks.water;
		toReturn.setTexture(fluid.getFlowingIcon());
		
		DisplayInteger[] displays = new DisplayInteger[stages];
		cachedValveFluids.put(fluid, displays);

		for(int i = 0; i < stages; i++)
		{
			displays[i] = DisplayInteger.createAndStart();

			if(fluid.getIcon() != null)
			{
				toReturn.minX = 0.3125 + .01;
				toReturn.minY = 0.0625 + ((float)i/(float)stages)*0.875;
				toReturn.minZ = 0.3125 + .01;

				toReturn.maxX = 0.6875 - .01;
				toReturn.maxY = 0.9375 - .01;
				toReturn.maxZ = 0.6875 - .01;

				MekanismRenderer.renderObject(toReturn);
			}

			GL11.glEndList();
		}

		return displays;
	}
	
	private DisplayInteger[] getListAndRender(Fluid fluid)
	{
		if(cachedCenterFluids.containsKey(fluid))
		{
			return cachedCenterFluids.get(fluid);
		}

		Model3D toReturn = new Model3D();
		toReturn.baseBlock = Blocks.water;
		toReturn.setTexture(fluid.getIcon());
		
		DisplayInteger[] displays = new DisplayInteger[stages];
		cachedCenterFluids.put(fluid, displays);

		for(int i = 0; i < stages; i++)
		{
			displays[i] = DisplayInteger.createAndStart();

			if(fluid.getIcon() != null)
			{
				toReturn.minX = 0.125 + .01;
				toReturn.minY = 0.0625 + .01;
				toReturn.minZ = 0.125 + .01;

				toReturn.maxX = 0.875 - .01;
				toReturn.maxY = 0.0625 + ((float)i/(float)stages)*0.875 - .01;
				toReturn.maxZ = 0.875 - .01;

				MekanismRenderer.renderObject(toReturn);
			}

			GL11.glEndList();
		}

		return displays;
	}

	public static void resetDisplayInts()
	{
		cachedCenterFluids.clear();
		cachedValveFluids.clear();
	}
}
