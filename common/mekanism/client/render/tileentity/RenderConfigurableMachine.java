package mekanism.client.render.tileentity;

import java.util.HashMap;

import mekanism.api.EnumColor;
import mekanism.api.IConfigurable;
import mekanism.api.Object3D;
import mekanism.client.render.MekanismRenderer;
import mekanism.client.render.MekanismRenderer.DisplayInteger;
import mekanism.client.render.MekanismRenderer.Model3D;
import mekanism.common.item.ItemConfigurator;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.ResourceType;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GLAllocation;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Icon;
import net.minecraft.util.MathHelper;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeDirection;

import org.lwjgl.opengl.GL11;

import cpw.mods.fml.client.FMLClientHandler;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class RenderConfigurableMachine extends TileEntitySpecialRenderer
{
	private static Icon[] coloredOverlays;
	
	private Minecraft mc = FMLClientHandler.instance().getClient();
	
	private HashMap<ForgeDirection, HashMap<EnumColor, DisplayInteger>> cachedOverlays = new HashMap<ForgeDirection, HashMap<EnumColor, DisplayInteger>>();
	
	public RenderConfigurableMachine()
	{
		if(coloredOverlays == null)
		{
			coloredOverlays = new Icon[16];
			
			TextureMap registrar = MekanismRenderer.getTextureMap(0);
			
			coloredOverlays[0] = registrar.registerIcon("mekanism:OverlayBlack");
			coloredOverlays[1] = registrar.registerIcon("mekanism:OverlayDarkBlue");
			coloredOverlays[2] = registrar.registerIcon("mekanism:OverlayDarkGreen");
			coloredOverlays[3] = registrar.registerIcon("mekanism:OverlayDarkAqua");
			coloredOverlays[4] = registrar.registerIcon("mekanism:OverlayDarkRed");
			coloredOverlays[5] = registrar.registerIcon("mekanism:OverlayPurple");
			coloredOverlays[6] = registrar.registerIcon("mekanism:OverlayOrange");
			coloredOverlays[7] = registrar.registerIcon("mekanism:OverlayGrey");
			coloredOverlays[8] = registrar.registerIcon("mekanism:OverlayDarkGrey");
			coloredOverlays[9] = registrar.registerIcon("mekanism:OverlayIndigo");
			coloredOverlays[10] = registrar.registerIcon("mekanism:OverlayBrightGreen");
			coloredOverlays[11] = registrar.registerIcon("mekanism:OverlayAqua");
			coloredOverlays[12] = registrar.registerIcon("mekanism:OverlayRed");
			coloredOverlays[13] = registrar.registerIcon("mekanism:OverlayPink");
			coloredOverlays[14] = registrar.registerIcon("mekanism:OverlayYellow");
			coloredOverlays[15] = registrar.registerIcon("mekanism:OverlayWhite");
		}
	}
	
	@Override
	public void renderTileEntityAt(TileEntity tileEntity, double x, double y, double z, float partialTick)
	{
		renderAModelAt((IConfigurable)tileEntity, x, y, z, partialTick);
	}

	public void renderAModelAt(IConfigurable configurable, double x, double y, double z, float partialTick)
	{
		TileEntity tileEntity = (TileEntity)configurable; 
		EntityPlayer player = mc.thePlayer;
		World world = mc.thePlayer.worldObj;
		ItemStack itemStack = player.getCurrentEquippedItem();
		MovingObjectPosition pos = player.rayTrace(8.0D, 1.0F);
		
		if(pos != null && itemStack != null && itemStack.getItem() instanceof ItemConfigurator && ((ItemConfigurator)itemStack.getItem()).getState(itemStack) == 0)
		{
			int xPos = MathHelper.floor_double(pos.blockX);
			int yPos = MathHelper.floor_double(pos.blockY);
			int zPos = MathHelper.floor_double(pos.blockZ);
			
			Object3D obj = new Object3D(xPos, yPos, zPos);
			
			if(xPos == tileEntity.xCoord && yPos == tileEntity.yCoord && zPos == tileEntity.zCoord)
			{
				EnumColor color = configurable.getSideData().get(configurable.getConfiguration()[MekanismUtils.getBaseOrientation(pos.sideHit, configurable.getOrientation())]).color;
				
				push();
				
				GL11.glColor4f(1.0F, 1.0F, 1.0F, 0.4F);
				
				func_110628_a(MekanismUtils.getResource(ResourceType.TEXTURE_BLOCKS, "Overlay" + color.friendlyName.replace(" ", "") + ".png"));
				GL11.glTranslatef((float)x, (float)y, (float)z);
				
				int display = getOverlayDisplay(world, ForgeDirection.getOrientation(pos.sideHit), color).display;
				GL11.glCallList(display);
				
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
	
	private DisplayInteger getOverlayDisplay(World world, ForgeDirection side, EnumColor color)
	{
		if(cachedOverlays.containsKey(side) && cachedOverlays.get(side).containsKey(color))
		{
			return cachedOverlays.get(side).get(color);
		}
		
		Model3D toReturn = new Model3D();
		toReturn.baseBlock = Block.stone;
		toReturn.setTexture(coloredOverlays[color.ordinal()]);
		
		DisplayInteger display = new DisplayInteger();
		
		if(cachedOverlays.containsKey(side))
		{
			cachedOverlays.get(side).put(color, display);
		}
		else {
			HashMap<EnumColor, DisplayInteger> map = new HashMap<EnumColor, DisplayInteger>();
			map.put(color, display);
			cachedOverlays.put(side, map);
		}
		
		display.display = GLAllocation.generateDisplayLists(1);
		GL11.glNewList(display.display, 4864);
		
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
		GL11.glEndList();
		
		return display;
	}
}