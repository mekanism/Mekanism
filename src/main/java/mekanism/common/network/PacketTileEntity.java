package mekanism.common.network;

import java.util.ArrayList;

import mekanism.api.Coord4D;
import mekanism.common.PacketHandler;
import mekanism.common.base.ITileNetwork;
import mekanism.common.network.PacketTileEntity.TileEntityMessage;

import net.minecraft.tileentity.TileEntity;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;

import io.netty.buffer.ByteBuf;

public class PacketTileEntity implements IMessageHandler<TileEntityMessage, IMessage>
{
	@Override
	public IMessage onMessage(TileEntityMessage message, MessageContext context) 
	{
		TileEntity tileEntity = message.coord4D.getTileEntity(PacketHandler.getPlayer(context).worldObj);
		
		if(tileEntity instanceof ITileNetwork)
		{
			try {
				((ITileNetwork)tileEntity).handlePacketData(message.storedBuffer);
			} catch(Exception e) {
				e.printStackTrace();
			}
			
			message.storedBuffer.release();
		}
		
		return null;
	}
	
	public static class TileEntityMessage implements IMessage
	{
		public Coord4D coord4D;
	
		public ArrayList parameters;
		
		public ByteBuf storedBuffer = null;
		
		public TileEntityMessage() {}
	
		public TileEntityMessage(Coord4D coord, ArrayList params)
		{
			coord4D = coord;
			parameters = params;
		}
	
		@Override
		public void toBytes(ByteBuf dataStream)
		{
			dataStream.writeInt(coord4D.xCoord);
			dataStream.writeInt(coord4D.yCoord);
			dataStream.writeInt(coord4D.zCoord);
			dataStream.writeInt(coord4D.dimensionId);
	
			PacketHandler.encode(new Object[] {parameters}, dataStream);
		}
	
		@Override
		public void fromBytes(ByteBuf dataStream)
		{
			coord4D = new Coord4D(dataStream.readInt(), dataStream.readInt(), dataStream.readInt(), dataStream.readInt());
			
			storedBuffer = dataStream.copy();
		}
	}
}
