package mekanism.common.network;

import io.netty.buffer.ByteBuf;

import java.util.ArrayList;

import mekanism.api.Coord4D;
import mekanism.client.gui.GuiOredictionificator;
import mekanism.client.gui.GuiOredictionificatorFilter;
import mekanism.common.Mekanism;
import mekanism.common.PacketHandler;
import mekanism.common.inventory.container.ContainerFilter;
import mekanism.common.inventory.container.ContainerOredictionificator;
import mekanism.common.network.PacketOredictionificatorGui.OredictionificatorGuiMessage;
import mekanism.common.network.PacketTileEntity.TileEntityMessage;
import mekanism.common.tile.TileEntityContainerBlock;
import mekanism.common.tile.TileEntityOredictionificator;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.Container;
import net.minecraft.world.World;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class PacketOredictionificatorGui implements IMessageHandler<OredictionificatorGuiMessage, IMessage>
{
	@Override
	public IMessage onMessage(OredictionificatorGuiMessage message, MessageContext context) 
	{
		EntityPlayer player = PacketHandler.getPlayer(context);
		
		if(!player.worldObj.isRemote)
		{
			World worldServer = FMLCommonHandler.instance().getMinecraftServerInstance().worldServerForDimension(message.coord4D.dimensionId);

			if(worldServer != null && message.coord4D.getTileEntity(worldServer) instanceof TileEntityOredictionificator)
			{
				OredictionificatorGuiMessage.openServerGui(message.packetType, message.guiType, worldServer, (EntityPlayerMP)player, message.coord4D, message.index);
			}
		}
		else {
			if(message.coord4D.getTileEntity(player.worldObj) instanceof TileEntityOredictionificator)
			{
				try {
					if(message.packetType == OredictionificatorGuiPacket.CLIENT)
					{
						FMLCommonHandler.instance().showGuiScreen(OredictionificatorGuiMessage.getGui(message.packetType, message.guiType, player, player.worldObj, message.coord4D.xCoord, message.coord4D.yCoord, message.coord4D.zCoord, -1));
					}
					else if(message.packetType == OredictionificatorGuiPacket.CLIENT_INDEX)
					{
						FMLCommonHandler.instance().showGuiScreen(OredictionificatorGuiMessage.getGui(message.packetType, message.guiType, player, player.worldObj, message.coord4D.xCoord, message.coord4D.yCoord, message.coord4D.zCoord, message.index));
					}

					player.openContainer.windowId = message.windowId;
				} catch(Exception e) {
					e.printStackTrace();
				}
			}
		}
		
		return null;
	}
	
	public static class OredictionificatorGuiMessage implements IMessage
	{
		public Coord4D coord4D;
	
		public OredictionificatorGuiPacket packetType;
	
		public int guiType;
	
		public int windowId = -1;
	
		public int index = -1;
		
		public OredictionificatorGuiMessage() {}
	
		public OredictionificatorGuiMessage(OredictionificatorGuiPacket type, Coord4D coord, int guiID, int extra, int extra2)
		{
			packetType = type;
	
			coord4D = coord;
			guiType = guiID;
	
			if(packetType == OredictionificatorGuiPacket.CLIENT)
			{
				windowId = extra;
			}
			else if(packetType == OredictionificatorGuiPacket.SERVER_INDEX)
			{
				index = extra;
			}
			else if(packetType == OredictionificatorGuiPacket.CLIENT_INDEX)
			{
				windowId = extra;
				index = extra2;
			}
		}
	
		public static void openServerGui(OredictionificatorGuiPacket t, int guiType, World world, EntityPlayerMP playerMP, Coord4D obj, int i)
		{
			Container container = null;
	
			playerMP.closeContainer();
	
			if(guiType == 0)
			{
				container = new ContainerOredictionificator(playerMP.inventory, (TileEntityOredictionificator)obj.getTileEntity(world));
			}
			else if(guiType == 1)
			{
				container = new ContainerFilter(playerMP.inventory, (TileEntityContainerBlock)obj.getTileEntity(world));
			}
	
			playerMP.getNextWindowId();
			int window = playerMP.currentWindowId;
	
			if(t == OredictionificatorGuiPacket.SERVER)
			{
				Mekanism.packetHandler.sendTo(new OredictionificatorGuiMessage(OredictionificatorGuiPacket.CLIENT, obj, guiType, window, 0), playerMP);
			}
			else if(t == OredictionificatorGuiPacket.SERVER_INDEX)
			{
				Mekanism.packetHandler.sendTo(new OredictionificatorGuiMessage(OredictionificatorGuiPacket.CLIENT_INDEX, obj, guiType, window, i), playerMP);
			}
	
			playerMP.openContainer = container;
			playerMP.openContainer.windowId = window;
			playerMP.openContainer.addCraftingToCrafters(playerMP);
	
			if(guiType == 0)
			{
				TileEntityOredictionificator tile = (TileEntityOredictionificator)obj.getTileEntity(world);
	
				for(EntityPlayer player : tile.playersUsing)
				{
					Mekanism.packetHandler.sendTo(new TileEntityMessage(obj, tile.getFilterPacket(new ArrayList())), (EntityPlayerMP)player);
				}
			}
		}
	
		@SideOnly(Side.CLIENT)
		public static GuiScreen getGui(OredictionificatorGuiPacket packetType, int type, EntityPlayer player, World world, int x, int y, int z, int index)
		{
			if(type == 0)
			{
				return new GuiOredictionificator(player.inventory, (TileEntityOredictionificator)world.getTileEntity(x, y, z));
			}
			else {
				if(packetType == OredictionificatorGuiPacket.CLIENT)
				{
					if(type == 1)
					{
						return new GuiOredictionificatorFilter(player, (TileEntityOredictionificator)world.getTileEntity(x, y, z));
					}
				}
				else if(packetType == OredictionificatorGuiPacket.CLIENT_INDEX)
				{
					if(type == 1)
					{
						return new GuiOredictionificatorFilter(player, (TileEntityOredictionificator)world.getTileEntity(x, y, z), index);
					}
				}
			}
	
			return null;
		}
	
		@Override
		public void toBytes(ByteBuf dataStream)
		{
			dataStream.writeInt(packetType.ordinal());
	
			dataStream.writeInt(coord4D.xCoord);
			dataStream.writeInt(coord4D.yCoord);
			dataStream.writeInt(coord4D.zCoord);
	
			dataStream.writeInt(coord4D.dimensionId);
	
			dataStream.writeInt(guiType);
	
			if(packetType == OredictionificatorGuiPacket.CLIENT || packetType == OredictionificatorGuiPacket.CLIENT_INDEX)
			{
				dataStream.writeInt(windowId);
			}
	
			if(packetType == OredictionificatorGuiPacket.SERVER_INDEX || packetType == OredictionificatorGuiPacket.CLIENT_INDEX)
			{
				dataStream.writeInt(index);
			}
		}
	
		@Override
		public void fromBytes(ByteBuf dataStream)
		{
			packetType = OredictionificatorGuiPacket.values()[dataStream.readInt()];
	
			coord4D = new Coord4D(dataStream.readInt(), dataStream.readInt(), dataStream.readInt(), dataStream.readInt());
	
			guiType = dataStream.readInt();
	
			if(packetType == OredictionificatorGuiPacket.CLIENT || packetType == OredictionificatorGuiPacket.CLIENT_INDEX)
			{
				windowId = dataStream.readInt();
			}
	
			if(packetType == OredictionificatorGuiPacket.SERVER_INDEX || packetType == OredictionificatorGuiPacket.CLIENT_INDEX)
			{
				index = dataStream.readInt();
			}
		}
	}
	
	public static enum OredictionificatorGuiPacket
	{
		SERVER, CLIENT, SERVER_INDEX, CLIENT_INDEX
	}
}
