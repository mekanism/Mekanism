package mekanism.common.network;

import io.netty.buffer.ByteBuf;

import java.util.ArrayList;

import mekanism.api.Coord4D;
import mekanism.common.PacketHandler;
import mekanism.common.base.ITileNetwork;
import mekanism.common.network.PacketTileEntity.TileEntityMessage;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

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
			
			MinecraftServer server = FMLCommonHandler.instance().getMinecraftServerInstance();
			
			if(server != null)
			{
				World world = server.worldServerForDimension(coord4D.dimensionId);
				PacketHandler.log("Sending TileEntity packet from coordinate " + coord4D + " (" + coord4D.getTileEntity(world) + ")");
			}
			
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
