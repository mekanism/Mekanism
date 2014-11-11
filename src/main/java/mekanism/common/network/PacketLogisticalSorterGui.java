package mekanism.common.network;

import mekanism.api.Coord4D;
import mekanism.client.gui.GuiLogisticalSorter;
import mekanism.client.gui.GuiTFilterSelect;
import mekanism.client.gui.GuiTItemStackFilter;
import mekanism.client.gui.GuiTMaterialFilter;
import mekanism.client.gui.GuiTModIDFilter;
import mekanism.client.gui.GuiTOreDictFilter;
import mekanism.common.Mekanism;
import mekanism.common.PacketHandler;
import mekanism.common.inventory.container.ContainerFilter;
import mekanism.common.inventory.container.ContainerNull;
import mekanism.common.network.PacketLogisticalSorterGui.LogisticalSorterGuiMessage;
import mekanism.common.tile.TileEntityContainerBlock;
import mekanism.common.tile.TileEntityLogisticalSorter;

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

import io.netty.buffer.ByteBuf;

public class PacketLogisticalSorterGui implements IMessageHandler<LogisticalSorterGuiMessage, IMessage>
{
	@Override
	public IMessage onMessage(LogisticalSorterGuiMessage message, MessageContext context) 
	{
		EntityPlayer player = PacketHandler.getPlayer(context);
		
		if(!player.worldObj.isRemote)
		{
			World worldServer = FMLCommonHandler.instance().getMinecraftServerInstance().worldServerForDimension(message.object3D.dimensionId);

			if(worldServer != null && message.object3D.getTileEntity(worldServer) instanceof TileEntityLogisticalSorter)
			{
				LogisticalSorterGuiMessage.openServerGui(message.packetType, message.guiType, worldServer, (EntityPlayerMP)player, message.object3D, message.index);
			}
		}
		else {
			if(message.object3D.getTileEntity(player.worldObj) instanceof TileEntityLogisticalSorter)
			{
				try {
					if(message.packetType == SorterGuiPacket.CLIENT)
					{
						FMLCommonHandler.instance().showGuiScreen(LogisticalSorterGuiMessage.getGui(message.packetType, message.guiType, player, player.worldObj, message.object3D.xCoord, message.object3D.yCoord, message.object3D.zCoord, -1));
					}
					else if(message.packetType == SorterGuiPacket.CLIENT_INDEX)
					{
						FMLCommonHandler.instance().showGuiScreen(LogisticalSorterGuiMessage.getGui(message.packetType, message.guiType, player, player.worldObj, message.object3D.xCoord, message.object3D.yCoord, message.object3D.zCoord, message.index));
					}

					player.openContainer.windowId = message.windowId;
				} catch(Exception e) {
					e.printStackTrace();
				}
			}
		}
		
		return null;
	}
	
	public static class LogisticalSorterGuiMessage implements IMessage
	{
		public Coord4D object3D;
	
		public SorterGuiPacket packetType;
	
		public int guiType;
	
		public int windowId = -1;
	
		public int index = -1;
		
		public LogisticalSorterGuiMessage() {}
	
		public LogisticalSorterGuiMessage(SorterGuiPacket type, Coord4D coord, int guiId, int extra, int extra2)
		{
			packetType = type;
	
			object3D = coord;
			guiType = guiId;
	
			if(packetType == SorterGuiPacket.CLIENT)
			{
				windowId = extra;
			}
			else if(packetType == SorterGuiPacket.SERVER_INDEX)
			{
				index = extra;
			}
			else if(packetType == SorterGuiPacket.CLIENT_INDEX)
			{
				windowId = extra2;
				index = extra2;
			}
		}
	
		public static void openServerGui(SorterGuiPacket t, int guiType, World world, EntityPlayerMP playerMP, Coord4D obj, int i)
		{
			Container container = null;
	
			playerMP.closeContainer();
	
			if(guiType == 0)
			{
				container = new ContainerNull(playerMP, (TileEntityContainerBlock)obj.getTileEntity(world));
			}
			else if(guiType == 4)
			{
				container = new ContainerNull(playerMP, (TileEntityContainerBlock)obj.getTileEntity(world));
			}
			else if(guiType == 1 || guiType == 2 || guiType == 3 || guiType == 5)
			{
				container = new ContainerFilter(playerMP.inventory, (TileEntityContainerBlock)obj.getTileEntity(world));
			}
	
			playerMP.getNextWindowId();
			int window = playerMP.currentWindowId;
	
			if(t == SorterGuiPacket.SERVER)
			{
				Mekanism.packetHandler.sendTo(new LogisticalSorterGuiMessage(SorterGuiPacket.CLIENT, obj, guiType, window, 0), playerMP);
			}
			else if(t == SorterGuiPacket.SERVER_INDEX)
			{
				Mekanism.packetHandler.sendTo(new LogisticalSorterGuiMessage(SorterGuiPacket.CLIENT_INDEX, obj, guiType, window, i), playerMP);
			}
	
			playerMP.openContainer = container;
			playerMP.openContainer.windowId = window;
			playerMP.openContainer.addCraftingToCrafters(playerMP);
		}
	
		@SideOnly(Side.CLIENT)
		public static GuiScreen getGui(SorterGuiPacket packetType, int type, EntityPlayer player, World world, int x, int y, int z, int index)
		{
			if(type == 0)
			{
				return new GuiLogisticalSorter(player, (TileEntityLogisticalSorter)world.getTileEntity(x, y, z));
			}
			else if(type == 4)
			{
				return new GuiTFilterSelect(player, (TileEntityLogisticalSorter)world.getTileEntity(x, y, z));
			}
			else {
				if(packetType == SorterGuiPacket.CLIENT)
				{
					if(type == 1)
					{
						return new GuiTItemStackFilter(player, (TileEntityLogisticalSorter)world.getTileEntity(x, y, z));
					}
					else if(type == 2)
					{
						return new GuiTOreDictFilter(player, (TileEntityLogisticalSorter)world.getTileEntity(x, y, z));
					}
					else if(type == 3)
					{
						return new GuiTMaterialFilter(player, (TileEntityLogisticalSorter)world.getTileEntity(x, y, z));
					}
					else if(type == 5)
					{
						return new GuiTModIDFilter(player, (TileEntityLogisticalSorter)world.getTileEntity(x, y, z));
					}
				}
				else if(packetType == SorterGuiPacket.CLIENT_INDEX)
				{
					if(type == 1)
					{
						return new GuiTItemStackFilter(player, (TileEntityLogisticalSorter)world.getTileEntity(x, y, z), index);
					}
					else if(type == 2)
					{
						return new GuiTOreDictFilter(player, (TileEntityLogisticalSorter)world.getTileEntity(x, y, z), index);
					}
					else if(type == 3)
					{
						return new GuiTMaterialFilter(player, (TileEntityLogisticalSorter)world.getTileEntity(x, y, z), index);
					}
					else if(type == 5)
					{
						return new GuiTModIDFilter(player, (TileEntityLogisticalSorter)world.getTileEntity(x, y, z), index);
					}
				}
			}
	
			return null;
		}
	
		@Override
		public void toBytes(ByteBuf dataStream)
		{
			dataStream.writeInt(packetType.ordinal());
	
			dataStream.writeInt(object3D.xCoord);
			dataStream.writeInt(object3D.yCoord);
			dataStream.writeInt(object3D.zCoord);
	
			dataStream.writeInt(object3D.dimensionId);
	
			dataStream.writeInt(guiType);
	
			if(packetType == SorterGuiPacket.CLIENT || packetType == SorterGuiPacket.CLIENT_INDEX)
			{
				dataStream.writeInt(windowId);
			}
	
			if(packetType == SorterGuiPacket.SERVER_INDEX || packetType == SorterGuiPacket.CLIENT_INDEX)
			{
				dataStream.writeInt(index);
			}
		}
	
		@Override
		public void fromBytes(ByteBuf dataStream)
		{
			packetType = SorterGuiPacket.values()[dataStream.readInt()];
	
			object3D = new Coord4D(dataStream.readInt(), dataStream.readInt(), dataStream.readInt(), dataStream.readInt());
	
			guiType = dataStream.readInt();
	
			if(packetType == SorterGuiPacket.CLIENT || packetType == SorterGuiPacket.CLIENT_INDEX)
			{
				windowId = dataStream.readInt();
			}
	
			if(packetType == SorterGuiPacket.SERVER_INDEX || packetType == SorterGuiPacket.CLIENT_INDEX)
			{
				index = dataStream.readInt();
			}
		}
	}
	
	public static enum SorterGuiPacket
	{
		SERVER, CLIENT, SERVER_INDEX, CLIENT_INDEX
	}
}
