package mekanism.common.network;

import io.netty.buffer.ByteBuf;

import java.util.ArrayList;
import java.util.List;

import mekanism.api.Coord4D;
import mekanism.common.Mekanism;
import mekanism.common.PacketHandler;
import mekanism.common.base.IGuiProvider;
import mekanism.common.network.PacketSimpleGui.SimpleGuiMessage;
import mekanism.common.tile.prefab.TileEntityBasicBlock;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class PacketSimpleGui implements IMessageHandler<SimpleGuiMessage, IMessage>
{
	public static List<IGuiProvider> handlers = new ArrayList<>();
	
	@Override
	public IMessage onMessage(SimpleGuiMessage message, MessageContext context) 
	{
		EntityPlayer player = PacketHandler.getPlayer(context);
		
		PacketHandler.handlePacket(() ->
        {
            if(!player.world.isRemote)
            {
                World worldServer = FMLCommonHandler.instance().getMinecraftServerInstance().getWorld(message.coord4D.dimensionId);

                if(worldServer != null && message.coord4D.getTileEntity(worldServer) instanceof TileEntityBasicBlock)
                {
                    if(message.guiId == -1)
                    {
                        return;
                    }

                    SimpleGuiMessage.openServerGui(message.guiHandler, message.guiId, (EntityPlayerMP)player, player.world, message.coord4D);
                }
            }
            else {
                FMLCommonHandler.instance().showGuiScreen(SimpleGuiMessage.getGui(message.guiHandler, message.guiId, player, player.world, message.coord4D));
                player.openContainer.windowId = message.windowId;
            }
        }, player);
		
		return null;
	}
	
	public static class SimpleGuiMessage implements IMessage
	{
		public Coord4D coord4D;
		
		public int guiHandler;
	
		public int guiId;
		
		public int windowId;
		
		public SimpleGuiMessage() {}
	
		public SimpleGuiMessage(Coord4D coord, int handler, int gui)
		{
			coord4D = coord;
			guiHandler = handler;
			guiId = gui;
		}
		
		public SimpleGuiMessage(Coord4D coord, int handler, int gui, int id)
		{
			this(coord, handler, gui);
			windowId = id;
		}
	
		public static void openServerGui(int handler, int id, EntityPlayerMP playerMP, World world, Coord4D obj)
		{
			playerMP.closeContainer();
			playerMP.getNextWindowId();
	
			int window = playerMP.currentWindowId;
	
			Mekanism.packetHandler.sendTo(new SimpleGuiMessage(obj, handler, id, window), playerMP);
	
			playerMP.openContainer = handlers.get(handler).getServerGui(id, playerMP, world, obj.getPos());
			playerMP.openContainer.windowId = window;
			playerMP.openContainer.addListener(playerMP);
		}
	
		@SideOnly(Side.CLIENT)
		public static GuiScreen getGui(int handler, int id, EntityPlayer player, World world, Coord4D obj)
		{
			return (GuiScreen)handlers.get(handler).getClientGui(id, player, world, obj.getPos());
		}
	
		@Override
		public void toBytes(ByteBuf dataStream)
		{
			coord4D.write(dataStream);
	
			dataStream.writeInt(guiHandler);
			dataStream.writeInt(guiId);
			dataStream.writeInt(windowId);
		}
	
		@Override
		public void fromBytes(ByteBuf dataStream)
		{
			coord4D = Coord4D.read(dataStream);
	
			guiHandler = dataStream.readInt();
			guiId = dataStream.readInt();
			windowId = dataStream.readInt();
		}
	}
}
