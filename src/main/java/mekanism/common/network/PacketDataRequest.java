package mekanism.common.network;

import java.util.ArrayList;

import mekanism.api.Coord4D;
import mekanism.api.transmitters.DynamicNetwork;
import mekanism.api.transmitters.IGridTransmitter;
import mekanism.api.transmitters.ITransmitterTile;
import mekanism.common.Mekanism;
import mekanism.common.PacketHandler;
import mekanism.common.base.ITileNetwork;
import mekanism.common.network.PacketDataRequest.DataRequestMessage;
import mekanism.common.network.PacketTileEntity.TileEntityMessage;
import mekanism.common.tile.TileEntityDynamicTank;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;

import io.netty.buffer.ByteBuf;

public class PacketDataRequest implements IMessageHandler<DataRequestMessage, IMessage>
{
	@Override
	public IMessage onMessage(DataRequestMessage message, MessageContext context) 
	{
		EntityPlayer player = PacketHandler.getPlayer(context);
		World worldServer = FMLCommonHandler.instance().getMinecraftServerInstance().worldServerForDimension(message.coord4D.dimensionId);
		
		if(worldServer != null && message.coord4D.getTileEntity(worldServer) instanceof ITileNetwork)
		{
			TileEntity tileEntity = message.coord4D.getTileEntity(worldServer);

			if(tileEntity instanceof TileEntityDynamicTank)
			{
				((TileEntityDynamicTank)tileEntity).sendStructure = true;
			}

			if(tileEntity instanceof ITransmitterTile)
			{
				IGridTransmitter transmitter = ((ITransmitterTile)tileEntity).getTransmitter();

				if(transmitter.hasTransmitterNetwork())
				{
					transmitter.getTransmitterNetwork().addUpdate(player);
				}
			}

			Mekanism.packetHandler.sendTo(new TileEntityMessage(Coord4D.get(tileEntity), ((ITileNetwork)tileEntity).getNetworkedData(new ArrayList())), (EntityPlayerMP)player);
		}
		
		return null;
	}
	
	public static class DataRequestMessage implements IMessage
	{
		public Coord4D coord4D;
		
		public DataRequestMessage() {}
	
		public DataRequestMessage(Coord4D coord)
		{
			coord4D = coord;
		}
		
		@Override
		public void toBytes(ByteBuf dataStream)
		{
			dataStream.writeInt(coord4D.xCoord);
			dataStream.writeInt(coord4D.yCoord);
			dataStream.writeInt(coord4D.zCoord);
	
			dataStream.writeInt(coord4D.dimensionId);
		}
	
		@Override
		public void fromBytes(ByteBuf dataStream)
		{
			coord4D = new Coord4D(dataStream.readInt(), dataStream.readInt(), dataStream.readInt(), dataStream.readInt());
		}
	}
}
