package mekanism.client.render.tileentity;

import java.util.HashMap;

import mekanism.api.transmitters.TransmissionType;
import mekanism.client.render.MekanismRenderer;
import mekanism.client.render.MekanismRenderer.DisplayInteger;
import mekanism.client.render.MekanismRenderer.Model3D;
import mekanism.common.SideData;
import mekanism.common.base.ISideConfiguration;
import mekanism.common.item.ItemConfigurator;
import mekanism.common.tile.component.TileComponentConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraftforge.fml.client.FMLClientHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import org.lwjgl.opengl.GL11;

@SideOnly(Side.CLIENT)
public class RenderConfigurableMachine<S extends TileEntity & ISideConfiguration> extends TileEntitySpecialRenderer<S>
{
	private Minecraft mc = FMLClientHandler.instance().getClient();

	private HashMap<EnumFacing, HashMap<TransmissionType, DisplayInteger>> cachedOverlays = new HashMap<>();

	public RenderConfigurableMachine()
	{
		rendererDispatcher = TileEntityRendererDispatcher.instance;
	}

	@Override
	public void render(S configurable, double x, double y, double z, float partialTick, int destroyStage, float alpha)
	{
		GlStateManager.pushMatrix();

		EntityPlayer player = mc.player;
		ItemStack itemStack = player.inventory.getCurrentItem();
		RayTraceResult pos = player.rayTrace(8.0D, 1.0F);

		if(pos != null && !itemStack.isEmpty() && itemStack.getItem() instanceof ItemConfigurator && ((ItemConfigurator)itemStack.getItem()).getState(itemStack).isConfigurating())
		{
			BlockPos bp = pos.getBlockPos();

			TransmissionType type = ((ItemConfigurator)itemStack.getItem()).getState(itemStack).getTransmission();

			if(configurable.getConfig().supports(type))
			{
				if(bp.equals(configurable.getPos()))
				{
					SideData data = configurable.getConfig().getOutput(type, pos.sideHit, configurable.getOrientation());
					
					if(data != TileComponentConfig.EMPTY)
					{
						push();
		
						MekanismRenderer.color(data.color, 0.6F);
		
						bindTexture(MekanismRenderer.getBlocksTexture());
						GlStateManager.translate((float)x, (float)y, (float)z);
		
						int display = getOverlayDisplay(pos.sideHit, type).display;
						GL11.glCallList(display);
						
						MekanismRenderer.resetColor();
		
						pop();
					}
				}
			}
		}

		GlStateManager.popMatrix();
	}

	private void pop()
	{
		GL11.glPopAttrib();
		MekanismRenderer.glowOff();
		MekanismRenderer.blendOff();
		GlStateManager.popMatrix();
	}

	private void push()
	{
		GlStateManager.pushMatrix();
		GL11.glPushAttrib(GL11.GL_ENABLE_BIT);
		GL11.glEnable(GL11.GL_CULL_FACE);
		GL11.glDisable(GL11.GL_LIGHTING);
		MekanismRenderer.glowOn();
		MekanismRenderer.blendOn();
	}

	private DisplayInteger getOverlayDisplay(EnumFacing side, TransmissionType type)
	{
		if(cachedOverlays.containsKey(side) && cachedOverlays.get(side).containsKey(type))
		{
			return cachedOverlays.get(side).get(type);
		}

		Model3D toReturn = new Model3D();
		toReturn.baseBlock = Blocks.STONE;
		toReturn.setTexture(MekanismRenderer.overlays.get(type));

		DisplayInteger display = DisplayInteger.createAndStart();

		if(cachedOverlays.containsKey(side))
		{
			cachedOverlays.get(side).put(type, display);
		}
		else {
			HashMap<TransmissionType, DisplayInteger> map = new HashMap<>();
			map.put(type, display);
			cachedOverlays.put(side, map);
		}

		switch(side)
		{
			case DOWN:
			{
				toReturn.minY = -.01;
				toReturn.maxY = -.001;

				toReturn.minX = 0;
				toReturn.minZ = 0;
				toReturn.maxX = 1;
				toReturn.maxZ = 1;
				break;
			}
			case UP:
			{
				toReturn.minY = 1.001;
				toReturn.maxY = 1.01;

				toReturn.minX = 0;
				toReturn.minZ = 0;
				toReturn.maxX = 1;
				toReturn.maxZ = 1;
				break;
			}
			case NORTH:
			{
				toReturn.minZ = -.01;
				toReturn.maxZ = -.001;

				toReturn.minX = 0;
				toReturn.minY = 0;
				toReturn.maxX = 1;
				toReturn.maxY = 1;
				break;
			}
			case SOUTH:
			{
				toReturn.minZ = 1.001;
				toReturn.maxZ = 1.01;

				toReturn.minX = 0;
				toReturn.minY = 0;
				toReturn.maxX = 1;
				toReturn.maxY = 1;
				break;
			}
			case WEST:
			{
				toReturn.minX = -.01;
				toReturn.maxX = -.001;

				toReturn.minY = 0;
				toReturn.minZ = 0;
				toReturn.maxY = 1;
				toReturn.maxZ = 1;
				break;
			}
			case EAST:
			{
				toReturn.minX = 1.001;
				toReturn.maxX = 1.01;

				toReturn.minY = 0;
				toReturn.minZ = 0;
				toReturn.maxY = 1;
				toReturn.maxZ = 1;
				break;
			}
		}

		MekanismRenderer.renderObject(toReturn);
		DisplayInteger.endList();

		return display;
	}
}
