package mekanism.common.network;

import java.io.DataOutputStream;
import java.util.ArrayList;

import mekanism.api.Coord4D;
import mekanism.api.transmitters.DynamicNetwork;
import mekanism.api.transmitters.IGridTransmitter;
import mekanism.common.ITileNetwork;
import mekanism.common.Mekanism;
import mekanism.common.tile.TileEntityDynamicTank;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

import com.google.common.io.ByteArrayDataInput;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;

import cpw.mods.fml.common.FMLCommonHandler;

public class PacketDataRequest extends MekanismPacket
{
	public Coord4D coord4D;

	public PacketDataRequest(Coord4D coord)
	{
		coord4D = coord;
	}

	public void read(ByteArrayDataInput dataStream, EntityPlayer player, World world) throws Exception
	{
		int x = dataStream.readInt();
		int y = dataStream.readInt();
		int z = dataStream.readInt();

		int id = dataStream.readInt();

		World worldServer = FMLCommonHandler.instance().getMinecraftServerInstance().worldServerForDimension(id);

		if(worldServer != null && worldServer.getTileEntity(x, y, z) instanceof ITileNetwork)
		{
			TileEntity tileEntity = worldServer.getTileEntity(x, y, z);

			if(tileEntity instanceof TileEntityDynamicTank)
			{
				((TileEntityDynamicTank)tileEntity).sendStructure = true;
			}

			if(tileEntity instanceof IGridTransmitter)
			{
				IGridTransmitter transmitter = (IGridTransmitter)tileEntity;

				if(transmitter.getTransmitterNetwork() instanceof DynamicNetwork)
				{
					((DynamicNetwork)transmitter.getTransmitterNetwork()).addUpdate(player);
				}
			}

			Mekanism.packetPipeline.sendToAll(new PacketTileEntity(Coord4D.get(worldServer.getTileEntity(x, y, z)), ((ITileNetwork) worldServer.getTileEntity(x, y, z)).getNetworkedData(new ArrayList())));
		}
	}

	public void write(DataOutputStream dataStream) throws Exception
	{
		dataStream.writeInt(coord4D.xCoord);
		dataStream.writeInt(coord4D.yCoord);
		dataStream.writeInt(coord4D.zCoord);

		dataStream.writeInt(coord4D.dimensionId);
	}

	@Override
	public void write(ChannelHandlerContext ctx, ByteBuf buffer)
	{

	}

	@Override
	public void read(ChannelHandlerContext ctx, ByteBuf buffer)
	{

	}

	@Override
	public void handleClientSide(EntityPlayer player)
	{

	}

	@Override
	public void handleServerSide(EntityPlayer player)
	{

	}
}
