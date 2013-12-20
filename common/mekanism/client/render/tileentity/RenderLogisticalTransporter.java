package mekanism.client.render.tileentity;

import java.util.HashMap;

import mekanism.api.Coord4D;
import mekanism.client.model.ModelTransmitter;
import mekanism.client.model.ModelTransmitter.Size;
import mekanism.client.model.ModelTransporterBox;
import mekanism.client.render.MekanismRenderer;
import mekanism.client.render.MekanismRenderer.DisplayInteger;
import mekanism.client.render.MekanismRenderer.Model3D;
import mekanism.common.item.ItemConfigurator;
import mekanism.common.tileentity.TileEntityDiversionTransporter;
import mekanism.common.tileentity.TileEntityLogisticalTransporter;
import mekanism.common.transporter.TransporterStack;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.ResourceType;
import mekanism.common.util.TransporterUtils;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.RenderItem;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Icon;
import net.minecraft.util.MathHelper;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeDirection;

import org.lwjgl.opengl.GL11;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class RenderLogisticalTransporter extends TileEntitySpecialRenderer
{
	private ModelTransmitter model = new ModelTransmitter(Size.LARGE);
	private ModelTransporterBox modelBox = new ModelTransporterBox();
	
	private HashMap<ForgeDirection, HashMap<Integer, DisplayInteger>> cachedOverlays = new HashMap<ForgeDirection, HashMap<Integer, DisplayInteger>>();
	
	private Minecraft mc = Minecraft.getMinecraft();
	
	private EntityItem entityItem = new EntityItem(null);
	private RenderItem renderer = (RenderItem)RenderManager.instance.getEntityClassRenderObject(EntityItem.class);
	
	@Override
	public void renderTileEntityAt(TileEntity tileEntity, double x, double y, double z, float partialTick)
	{
		renderAModelAt((TileEntityLogisticalTransporter)tileEntity, x, y, z, partialTick);
	}

	public void renderAModelAt(TileEntityLogisticalTransporter tileEntity, double x, double y, double z, float partialTick)
	{
		int meta = Coord4D.get(tileEntity).getMetadata(tileEntity.worldObj);
		
		if(meta == 3)
		{
			bindTexture(MekanismUtils.getResource(ResourceType.RENDER, "LogisticalTransporter.png"));
		}
		else if(meta == 4)
		{
			bindTexture(MekanismUtils.getResource(ResourceType.RENDER, "RestrictiveTransporter.png"));
		}
		else if(meta == 5)
		{
			bindTexture(MekanismUtils.getResource(ResourceType.RENDER, "DiversionTransporter.png"));
		}
		
		GL11.glPushMatrix();
		GL11.glTranslatef((float)x + 0.5F, (float)y + 1.5F, (float)z + 0.5F);
		GL11.glScalef(1.0F, -1F, -1F);
		GL11.glDisable(GL11.GL_CULL_FACE);
		
		if(tileEntity.color != null)
		{
			GL11.glColor4f(tileEntity.color.getColor(0), tileEntity.color.getColor(1), tileEntity.color.getColor(2), 1.0F);
		}
		
		boolean[] connectable = TransporterUtils.getConnections(tileEntity);
		
		model.renderCenter(connectable);
		
		for(int i = 0; i < 6; i++)
		{
			model.renderSide(ForgeDirection.getOrientation(i), connectable[i]);
		}
		
		GL11.glEnable(GL11.GL_CULL_FACE);
		GL11.glPopMatrix();
		
		entityItem.age = 0;
		entityItem.hoverStart = 0;
		
		entityItem.setPosition(tileEntity.xCoord + 0.5, tileEntity.yCoord + 0.5, tileEntity.zCoord + 0.5);
		entityItem.worldObj = tileEntity.worldObj;
		
		for(TransporterStack stack : tileEntity.transit)
		{
			if(stack != null)
			{
				GL11.glPushMatrix();
				entityItem.setEntityItemStack(stack.itemStack);
				
				float[] pos = TransporterUtils.getStackPosition(tileEntity, stack, partialTick*TileEntityLogisticalTransporter.SPEED);
				
				GL11.glTranslated(x + pos[0], y + pos[1] - entityItem.yOffset, z + pos[2]);
				GL11.glScalef(0.75F, 0.75F, 0.75F);
				
				renderer.doRenderItem(entityItem, 0, 0, 0, 0, 0);
				GL11.glPopMatrix();
				
				if(stack.color != null)
				{
					bindTexture(MekanismUtils.getResource(ResourceType.RENDER, "TransporterBox.png"));
					GL11.glPushMatrix();
					MekanismRenderer.glowOn();
					GL11.glDisable(GL11.GL_CULL_FACE);
					GL11.glColor4f(stack.color.getColor(0), stack.color.getColor(1), stack.color.getColor(2), 1.0F);
					GL11.glTranslatef((float)(x + pos[0]), (float)(y + pos[1] - entityItem.yOffset - ((stack.itemStack.getItem() instanceof ItemBlock) ? 0.1 : 0)), (float)(z + pos[2]));
					modelBox.render(0.0625F);
					MekanismRenderer.glowOff();
					GL11.glPopMatrix();
				}
			}
		}
		
		if(meta == 5)
		{
			EntityPlayer player = mc.thePlayer;
			World world = mc.thePlayer.worldObj;
			ItemStack itemStack = player.getCurrentEquippedItem();
			MovingObjectPosition pos = player.rayTrace(8.0D, 1.0F);
			
			if(pos != null && itemStack != null && itemStack.getItem() instanceof ItemConfigurator)
			{
				int xPos = MathHelper.floor_double(pos.blockX);
				int yPos = MathHelper.floor_double(pos.blockY);
				int zPos = MathHelper.floor_double(pos.blockZ);
				
				Coord4D obj = new Coord4D(xPos, yPos, zPos);
				
				if(obj.equals(Coord4D.get(tileEntity)))
				{
					int mode = ((TileEntityDiversionTransporter)tileEntity).modes[pos.sideHit];
					ForgeDirection side = ForgeDirection.getOrientation(pos.sideHit);
					
					pushTransporter();
					
					GL11.glColor4f(1.0F, 1.0F, 1.0F, 0.8F);
					
					bindTexture(mode == 0 ? MekanismRenderer.getItemsTexture() : MekanismRenderer.getBlocksTexture());
					GL11.glTranslatef((float)x, (float)y, (float)z);
					GL11.glScalef(0.5F, 0.5F, 0.5F);
					GL11.glTranslatef(0.5F, 0.5F, 0.5F);
					
					int display = getOverlayDisplay(world, side, mode).display;
					GL11.glCallList(display);
					
					popTransporter();
				}
			}
		}
	}

	private void popTransporter()
	{
		GL11.glPopAttrib();
		MekanismRenderer.glowOff();
		MekanismRenderer.blendOff();
		GL11.glPopMatrix();
	}
	
	private void pushTransporter()
	{
		GL11.glPushMatrix();
		GL11.glPushAttrib(GL11.GL_ENABLE_BIT);
		GL11.glEnable(GL11.GL_CULL_FACE);
		GL11.glDisable(GL11.GL_LIGHTING);
		MekanismRenderer.glowOn();
		MekanismRenderer.blendOn();
	}
	
	private DisplayInteger getOverlayDisplay(World world, ForgeDirection side, int mode)
	{
		if(cachedOverlays.containsKey(side) && cachedOverlays.get(side).containsKey(mode))
		{
			return cachedOverlays.get(side).get(mode);
		}
		
		Icon icon = null;
		
		switch(mode)
		{
			case 0:
				icon = Item.gunpowder.getIcon(new ItemStack(Item.gunpowder), 0);
				break;
			case 1:
				icon = Block.torchRedstoneActive.getIcon(0, 0);
				break;
			case 2:
				icon = Block.torchRedstoneIdle.getIcon(0, 0);
				break;
		}
		
		Model3D toReturn = new Model3D();
		toReturn.baseBlock = Block.stone;
		toReturn.setTexture(icon);
		
		DisplayInteger display = DisplayInteger.createAndStart();
		
		if(cachedOverlays.containsKey(side))
		{
			cachedOverlays.get(side).put(mode, display);
		}
		else {
			HashMap<Integer, DisplayInteger> map = new HashMap<Integer, DisplayInteger>();
			map.put(mode, display);
			cachedOverlays.put(side, map);
		}
		
		switch(side)
		{
			case DOWN:
			{
				toReturn.minY = -0.01;
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
				toReturn.minZ = -0.01;
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
				toReturn.minX = -0.01;
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
