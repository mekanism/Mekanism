package mekanism.common.network;

import io.netty.buffer.ByteBuf;
import mekanism.api.Coord4D;
import mekanism.common.PacketHandler;
import mekanism.common.base.ITankManager;
import mekanism.common.base.ITankManager.DropperHandler;
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
		
		if(tileEntity instanceof ITankManager)
		{
			try {
				Object tank = ((ITankManager)tileEntity).getTanks()[message.tankId];
				
				if(tank != null)
				{
					DropperHandler.useDropper(PacketHandler.getPlayer(context), tank, message.mouseButton);
				}
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
