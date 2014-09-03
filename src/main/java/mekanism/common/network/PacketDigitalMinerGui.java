package mekanism.common.network;

import java.util.ArrayList;

import mekanism.api.Coord4D;
import mekanism.client.gui.GuiDigitalMiner;
import mekanism.client.gui.GuiDigitalMinerConfig;
import mekanism.client.gui.GuiMFilterSelect;
import mekanism.client.gui.GuiMItemStackFilter;
import mekanism.client.gui.GuiMMaterialFilter;
import mekanism.client.gui.GuiMModIDFilter;
import mekanism.client.gui.GuiMOreDictFilter;
import mekanism.common.Mekanism;
import mekanism.common.PacketHandler;
import mekanism.common.inventory.container.ContainerDigitalMiner;
import mekanism.common.inventory.container.ContainerFilter;
import mekanism.common.inventory.container.ContainerNull;
import mekanism.common.network.PacketDigitalMinerGui.DigitalMinerGuiMessage;
import mekanism.common.network.PacketTileEntity.TileEntityMessage;
import mekanism.common.tile.TileEntityContainerBlock;
import mekanism.common.tile.TileEntityDigitalMiner;

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

public class PacketDigitalMinerGui implements IMessageHandler<DigitalMinerGuiMessage, IMessage>
{
	@Override
	public IMessage onMessage(DigitalMinerGuiMessage message, MessageContext context) 
	{
		EntityPlayer player = PacketHandler.getPlayer(context);
		
		if(!player.worldObj.isRemote)
		{
			World worldServer = FMLCommonHandler.instance().getMinecraftServerInstance().worldServerForDimension(message.coord4D.dimensionId);

			if(worldServer != null && message.coord4D.getTileEntity(worldServer) instanceof TileEntityDigitalMiner)
			{
				DigitalMinerGuiMessage.openServerGui(message.packetType, message.guiType, worldServer, (EntityPlayerMP)player, message.coord4D, message.index);
			}
		}
		else {
			if(message.coord4D.getTileEntity(player.worldObj) instanceof TileEntityDigitalMiner)
			{
				try {
					if(message.packetType == MinerGuiPacket.CLIENT)
					{
						FMLCommonHandler.instance().showGuiScreen(DigitalMinerGuiMessage.getGui(message.packetType, message.guiType, player, player.worldObj, message.coord4D.xCoord, message.coord4D.yCoord, message.coord4D.zCoord, -1));
					}
					else if(message.packetType == MinerGuiPacket.CLIENT_INDEX)
					{
						FMLCommonHandler.instance().showGuiScreen(DigitalMinerGuiMessage.getGui(message.packetType, message.guiType, player, player.worldObj, message.coord4D.xCoord, message.coord4D.yCoord, message.coord4D.zCoord, message.index));
					}

					player.openContainer.windowId = message.windowId;
				} catch(Exception e) {
					e.printStackTrace();
				}
			}
		}
		
		return null;
	}
	
	public static class DigitalMinerGuiMessage implements IMessage
	{
		public Coord4D coord4D;
	
		public MinerGuiPacket packetType;
	
		public int guiType;
	
		public int windowId = -1;
	
		public int index = -1;
		
		public DigitalMinerGuiMessage() {}
	
		public DigitalMinerGuiMessage(MinerGuiPacket type, Coord4D coord, int guiID, int extra, int extra2)
		{
			packetType = type;
	
			coord4D = coord;
			guiType = guiID;
	
			if(packetType == MinerGuiPacket.CLIENT)
			{
				windowId = extra;
			}
			else if(packetType == MinerGuiPacket.SERVER_INDEX)
			{
				index = extra;
			}
			else if(packetType == MinerGuiPacket.CLIENT_INDEX)
			{
				windowId = extra;
				index = extra2;
			}
		}
	
		public static void openServerGui(MinerGuiPacket t, int guiType, World world, EntityPlayerMP playerMP, Coord4D obj, int i)
		{
			Container container = null;
	
			playerMP.closeContainer();
	
			if(guiType == 0)
			{
				container = new ContainerNull(playerMP, (TileEntityContainerBlock)obj.getTileEntity(world));
			}
			else if(guiType == 4)
			{
				container = new ContainerDigitalMiner(playerMP.inventory, (TileEntityDigitalMiner)obj.getTileEntity(world));
			}
			else if(guiType == 5)
			{
				container = new ContainerNull(playerMP, (TileEntityContainerBlock)obj.getTileEntity(world));
			}
			else if(guiType == 1 || guiType == 2 || guiType == 3 || guiType == 6)
			{
				container = new ContainerFilter(playerMP.inventory, (TileEntityContainerBlock)obj.getTileEntity(world));
			}
	
			playerMP.getNextWindowId();
			int window = playerMP.currentWindowId;
	
			if(t == MinerGuiPacket.SERVER)
			{
				Mekanism.packetHandler.sendTo(new DigitalMinerGuiMessage(MinerGuiPacket.CLIENT, obj, guiType, window, 0), playerMP);
			}
			else if(t == MinerGuiPacket.SERVER_INDEX)
			{
				Mekanism.packetHandler.sendTo(new DigitalMinerGuiMessage(MinerGuiPacket.CLIENT_INDEX, obj, guiType, window, i), playerMP);
			}
	
			playerMP.openContainer = container;
			playerMP.openContainer.windowId = window;
			playerMP.openContainer.addCraftingToCrafters(playerMP);
	
			if(guiType == 0)
			{
				TileEntityDigitalMiner tile = (TileEntityDigitalMiner)obj.getTileEntity(world);
	
				for(EntityPlayer player : tile.playersUsing)
				{
					Mekanism.packetHandler.sendTo(new TileEntityMessage(obj, tile.getFilterPacket(new ArrayList())), (EntityPlayerMP)player);
				}
			}
		}
	
		@SideOnly(Side.CLIENT)
		public static GuiScreen getGui(MinerGuiPacket packetType, int type, EntityPlayer player, World world, int x, int y, int z, int index)
		{
			if(type == 0)
			{
				return new GuiDigitalMinerConfig(player, (TileEntityDigitalMiner)world.getTileEntity(x, y, z));
			}
			else if(type == 4)
			{
				return new GuiDigitalMiner(player.inventory, (TileEntityDigitalMiner)world.getTileEntity(x, y, z));
			}
			else if(type == 5)
			{
				return new GuiMFilterSelect(player, (TileEntityDigitalMiner)world.getTileEntity(x, y, z));
			}
			else {
				if(packetType == MinerGuiPacket.CLIENT)
				{
					if(type == 1)
					{
						return new GuiMItemStackFilter(player, (TileEntityDigitalMiner)world.getTileEntity(x, y, z));
					}
					else if(type == 2)
					{
						return new GuiMOreDictFilter(player, (TileEntityDigitalMiner)world.getTileEntity(x, y, z));
					}
					else if(type == 3)
					{
						return new GuiMMaterialFilter(player, (TileEntityDigitalMiner)world.getTileEntity(x, y, z));
					}
					else if(type == 6)
					{
						return new GuiMModIDFilter(player, (TileEntityDigitalMiner)world.getTileEntity(x, y, z));
					}
				}
				else if(packetType == MinerGuiPacket.CLIENT_INDEX)
				{
					if(type == 1)
					{
						return new GuiMItemStackFilter(player, (TileEntityDigitalMiner)world.getTileEntity(x, y, z), index);
					}
					else if(type == 2)
					{
						return new GuiMOreDictFilter(player, (TileEntityDigitalMiner)world.getTileEntity(x, y, z), index);
					}
					else if(type == 3)
					{
						return new GuiMMaterialFilter(player, (TileEntityDigitalMiner)world.getTileEntity(x, y, z), index);
					}
					else if(type == 6)
					{
						return new GuiMModIDFilter(player, (TileEntityDigitalMiner)world.getTileEntity(x, y, z), index);
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
	
			if(packetType == MinerGuiPacket.CLIENT || packetType == MinerGuiPacket.CLIENT_INDEX)
			{
				dataStream.writeInt(windowId);
			}
	
			if(packetType == MinerGuiPacket.SERVER_INDEX || packetType == MinerGuiPacket.CLIENT_INDEX)
			{
				dataStream.writeInt(index);
			}
		}
	
		@Override
		public void fromBytes(ByteBuf dataStream)
		{
			packetType = MinerGuiPacket.values()[dataStream.readInt()];
	
			coord4D = new Coord4D(dataStream.readInt(), dataStream.readInt(), dataStream.readInt(), dataStream.readInt());
	
			guiType = dataStream.readInt();
	
			if(packetType == MinerGuiPacket.CLIENT || packetType == MinerGuiPacket.CLIENT_INDEX)
			{
				windowId = dataStream.readInt();
			}
	
			if(packetType == MinerGuiPacket.SERVER_INDEX || packetType == MinerGuiPacket.CLIENT_INDEX)
			{
				index = dataStream.readInt();
			}
		}
	}
	
	public static enum MinerGuiPacket
	{
		SERVER, CLIENT, SERVER_INDEX, CLIENT_INDEX
	}
}
