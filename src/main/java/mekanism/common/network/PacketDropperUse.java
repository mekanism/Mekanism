package mekanism.common.network;

import io.netty.buffer.ByteBuf;
import mekanism.api.Coord4D;
import mekanism.common.PacketHandler;
import mekanism.common.base.IDropperHandler;
import mekanism.common.network.PacketDropperUse.DropperUseMessage;
import net.minecraft.tileentity.TileEntity;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;

public class PacketDropperUse implements IMessageHandler<DropperUseMessage, IMessage>
{
	@Override
	public IMessage onMessage(DropperUseMessage message, MessageContext context) 
	{
		TileEntity tileEntity = message.coord4D.getTileEntity(PacketHandler.getPlayer(context).worldObj);
		
		if(tileEntity instanceof IDropperHandler)
		{
			try {
				((IDropperHandler)tileEntity).useDropper(PacketHandler.getPlayer(context), message.tankId);
			} catch(Exception e) {
				e.printStackTrace();
			}
		}
		
		return null;
	}
	
	public static class DropperUseMessage implements IMessage
	{
		public Coord4D coord4D;
		
		public int mouseButton;
		public int tankId;
		
		public DropperUseMessage() {}
	
		public DropperUseMessage(Coord4D coord, int button, int id)
		{
			coord4D = coord;
			mouseButton = button;
			tankId = id;
		}
	
		@Override
		public void toBytes(ByteBuf dataStream)
		{
			dataStream.writeInt(coord4D.xCoord);
			dataStream.writeInt(coord4D.yCoord);
			dataStream.writeInt(coord4D.zCoord);
			dataStream.writeInt(coord4D.dimensionId);
			
			dataStream.writeInt(mouseButton);
			dataStream.writeInt(tankId);
		}
	
		@Override
		public void fromBytes(ByteBuf dataStream)
		{
			coord4D = new Coord4D(dataStream.readInt(), dataStream.readInt(), dataStream.readInt(), dataStream.readInt());
			
			mouseButton = dataStream.readInt();
			tankId = dataStream.readInt();
		}
	}
}
