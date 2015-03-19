package mekanism.common.network;

import mekanism.api.Coord4D;
import mekanism.common.Mekanism;
import mekanism.common.PacketHandler;
import mekanism.common.network.PacketSimpleGui.SimpleGuiMessage;
import mekanism.common.tile.TileEntityBasicBlock;

import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.world.World;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

import io.netty.buffer.ByteBuf;

public class PacketSimpleGui implements IMessageHandler<SimpleGuiMessage, IMessage>
{
	@Override
	public IMessage onMessage(SimpleGuiMessage message, MessageContext context) 
	{
		EntityPlayer player = PacketHandler.getPlayer(context);
		
		if(!player.worldObj.isRemote)
		{
			World worldServer = FMLCommonHandler.instance().getMinecraftServerInstance().worldServerForDimension(message.coord4D.dimensionId);

			if(worldServer != null && message.coord4D.getTileEntity(worldServer) instanceof TileEntityBasicBlock)
			{
				if(message.guiId == -1)
				{
					return null;
				}

				SimpleGuiMessage.openServerGui(message.guiId, (EntityPlayerMP)player, player.worldObj, message.coord4D);
			}
		}
		else {
			FMLCommonHandler.instance().showGuiScreen(SimpleGuiMessage.getGui(message.guiId, player, player.worldObj, message.coord4D));
			player.openContainer.windowId = message.windowId;
		}
		
		return null;
	}
	
	public static class SimpleGuiMessage implements IMessage
	{
		public Coord4D coord4D;
	
		public int guiId;
		
		public int windowId;
		
		public SimpleGuiMessage() {}
	
		public SimpleGuiMessage(Coord4D coord, int gui)
		{
			coord4D = coord;
			guiId = gui;
		}
		
		public SimpleGuiMessage(Coord4D coord, int gui, int id)
		{
			this(coord, gui);
			windowId = id;
		}
	
		public static void openServerGui(int id, EntityPlayerMP playerMP, World world, Coord4D obj)
		{
			playerMP.closeContainer();
			playerMP.getNextWindowId();
	
			int window = playerMP.currentWindowId;
	
			Mekanism.packetHandler.sendTo(new SimpleGuiMessage(obj, id, window), playerMP);
	
			playerMP.openContainer = Mekanism.proxy.getServerGui(id, playerMP, world, obj.xCoord, obj.yCoord, obj.zCoord);
			playerMP.openContainer.windowId = window;
			playerMP.openContainer.addCraftingToCrafters(playerMP);
		}
	
		@SideOnly(Side.CLIENT)
		public static GuiScreen getGui(int id, EntityPlayer player, World world, Coord4D obj)
		{
			return (GuiScreen)Mekanism.proxy.getClientGui(id, player, world, obj.xCoord, obj.yCoord, obj.zCoord);
		}
	
		@Override
		public void toBytes(ByteBuf dataStream)
		{
			dataStream.writeInt(coord4D.xCoord);
			dataStream.writeInt(coord4D.yCoord);
			dataStream.writeInt(coord4D.zCoord);
	
			dataStream.writeInt(coord4D.dimensionId);
	
			dataStream.writeInt(guiId);
			dataStream.writeInt(windowId);
		}
	
		@Override
		public void fromBytes(ByteBuf dataStream)
		{
			coord4D = new Coord4D(dataStream.readInt(), dataStream.readInt(), dataStream.readInt(), dataStream.readInt());
	
			guiId = dataStream.readInt();
			windowId = dataStream.readInt();
		}
	}
}
