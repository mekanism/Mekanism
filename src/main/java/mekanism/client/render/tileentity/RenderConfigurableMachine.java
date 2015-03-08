package mekanism.client.render.tileentity;

import java.util.HashMap;

import mekanism.api.Coord4D;
import mekanism.api.EnumColor;
import mekanism.api.transmitters.TransmissionType;
import mekanism.client.render.MekanismRenderer;
import mekanism.client.render.MekanismRenderer.DisplayInteger;
import mekanism.client.render.MekanismRenderer.Model3D;
import mekanism.common.base.ISideConfiguration;
import mekanism.common.item.ItemConfigurator;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

import org.lwjgl.opengl.GL11;

import codechicken.lib.math.MathHelper;
import cpw.mods.fml.client.FMLClientHandler;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class RenderConfigurableMachine extends TileEntitySpecialRenderer
{
	private Minecraft mc = FMLClientHandler.instance().getClient();

	private HashMap<ForgeDirection, HashMap<TransmissionType, DisplayInteger>> cachedOverlays = new HashMap<ForgeDirection, HashMap<TransmissionType, DisplayInteger>>();

	public RenderConfigurableMachine()
	{
		field_147501_a = TileEntityRendererDispatcher.instance;
	}

	@Override
	public void renderTileEntityAt(TileEntity tileEntity, double x, double y, double z, float partialTick)
	{
		renderAModelAt((ISideConfiguration)tileEntity, x, y, z, partialTick);
	}

	public void renderAModelAt(ISideConfiguration configurable, double x, double y, double z, float partialTick)
	{
		GL11.glPushMatrix();

		TileEntity tileEntity = (TileEntity)configurable;
		EntityPlayer player = mc.thePlayer;
		World world = mc.thePlayer.worldObj;
		ItemStack itemStack = player.getCurrentEquippedItem();
		MovingObjectPosition pos = player.rayTrace(8.0D, 1.0F);

		if(pos != null && itemStack != null && itemStack.getItem() instanceof ItemConfigurator && ((ItemConfigurator)itemStack.getItem()).getState(itemStack).isConfigurating())
		{
			int xPos = MathHelper.floor_double(pos.blockX);
			int yPos = MathHelper.floor_double(pos.blockY);
			int zPos = MathHelper.floor_double(pos.blockZ);

			Coord4D obj = new Coord4D(xPos, yPos, zPos, tileEntity.getWorldObj().provider.dimensionId);
			TransmissionType type = ((ItemConfigurator)itemStack.getItem()).getState(itemStack).getTransmission();

			if(configurable.getConfig().supports(type))
			{
				if(xPos == tileEntity.xCoord && yPos == tileEntity.yCoord && zPos == tileEntity.zCoord)
				{
					EnumColor color = configurable.getConfig().getOutput(type, pos.sideHit, configurable.getOrientation()).color;
	
					push();
	
					MekanismRenderer.color(color, 0.6F);
	
					bindTexture(MekanismRenderer.getBlocksTexture());
					GL11.glTranslatef((float)x, (float)y, (float)z);
	
					int display = getOverlayDisplay(world, ForgeDirection.getOrientation(pos.sideHit), type).display;
					GL11.glCallList(display);
	
					pop();
				}
			}
		}

		GL11.glPopMatrix();
	}

	private void pop()
	{
		GL11.glPopAttrib();
		MekanismRenderer.glowOff();
		MekanismRenderer.blendOff();
		GL11.glPopMatrix();
	}

	private void push()
	{
		GL11.glPushMatrix();
		GL11.glPushAttrib(GL11.GL_ENABLE_BIT);
		GL11.glEnable(GL11.GL_CULL_FACE);
		GL11.glDisable(GL11.GL_LIGHTING);
		MekanismRenderer.glowOn();
		MekanismRenderer.blendOn();
	}

	private DisplayInteger getOverlayDisplay(World world, ForgeDirection side, TransmissionType type)
	{
		if(cachedOverlays.containsKey(side) && cachedOverlays.get(side).containsKey(type))
		{
			return cachedOverlays.get(side).get(type);
		}

		Model3D toReturn = new Model3D();
		toReturn.baseBlock = Blocks.stone;
		toReturn.setTexture(MekanismRenderer.overlays.get(type));

		DisplayInteger display = DisplayInteger.createAndStart();

		if(cachedOverlays.containsKey(side))
		{
			cachedOverlays.get(side).put(type, display);
		}
		else {
			HashMap<TransmissionType, DisplayInteger> map = new HashMap<TransmissionType, DisplayInteger>();
			map.put(type, display);
			cachedOverlays.put(side, map);
		}

		switch(side)
		{
			case DOWN:
			{
				toReturn.minY = -.01;
				toReturn.maxY = 0;

				toReturn.minX = 0;
				toReturn.minZ = 0;
				toReturn.maxX = 1;
				toReturn.maxZ = 1;
				break;
			}
			case UP:
			{
				toReturn.minY = 1;
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
				toReturn.maxZ = 0;

				toReturn.minX = 0;
				toReturn.minY = 0;
				toReturn.maxX = 1;
				toReturn.maxY = 1;
				break;
			}
			case SOUTH:
			{
				toReturn.minZ = 1;
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
				toReturn.maxX = 0;

				toReturn.minY = 0;
				toReturn.minZ = 0;
				toReturn.maxY = 1;
				toReturn.maxZ = 1;
				break;
			}
			case EAST:
			{
				toReturn.minX = 1;
				toReturn.maxX = 1.01;

				toReturn.minY = 0;
				toReturn.minZ = 0;
				toReturn.maxY = 1;
				toReturn.maxZ = 1;
				break;
			}
			default:
			{
				break;
			}
		}

		MekanismRenderer.renderObject(toReturn);
		display.endList();

		return display;
	}
}