package mekanism.common.network;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;

import java.io.DataOutputStream;
import java.util.ArrayList;

import mekanism.api.Coord4D;
import mekanism.common.IInvConfiguration;
import mekanism.common.ITileNetwork;
import mekanism.common.Mekanism;
import mekanism.common.tile.TileEntityBasicBlock;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.TransporterUtils;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

import com.google.common.io.ByteArrayDataInput;

public class PacketConfigurationUpdate extends MekanismPacket
{
	public Coord4D coord4D;

	public int configIndex;

	public int inputSide;

	public int clickType;

	public ConfigurationPacket packetType;

	public PacketConfigurationUpdate(ConfigurationPacket type, Coord4D coord, int click, int extra)
	{
		packetType = type;

		coord4D = coord;

		if(packetType == ConfigurationPacket.EJECT_COLOR)
		{
			clickType = click;
		}

		if(packetType == ConfigurationPacket.SIDE_DATA)
		{
			clickType = click;
			configIndex = extra;
		}

		if(packetType == ConfigurationPacket.INPUT_COLOR)
		{
			clickType = click;
			inputSide = extra;
		}
	}

	public void read(ByteArrayDataInput dataStream, EntityPlayer player, World world) throws Exception
	{
		packetType = ConfigurationPacket.values()[dataStream.readInt()];

		coord4D = new Coord4D(dataStream.readInt(), dataStream.readInt(), dataStream.readInt(), dataStream.readInt());

		TileEntity tile = coord4D.getTileEntity(world);

		if(tile instanceof IInvConfiguration)
		{
			IInvConfiguration config = (IInvConfiguration)tile;

			if(packetType == ConfigurationPacket.EJECT)
			{
				config.getEjector().setEjecting(!config.getEjector().isEjecting());
			}
			else if(packetType == ConfigurationPacket.SIDE_DATA)
			{
				clickType = dataStream.readInt();
				configIndex = dataStream.readInt();

				if(clickType == 0)
				{
					MekanismUtils.incrementOutput((IInvConfiguration)tile, configIndex);
				}
				else if(clickType == 1)
				{
					MekanismUtils.decrementOutput((IInvConfiguration)tile, configIndex);
				}
				else if(clickType == 2)
				{
					((IInvConfiguration)tile).getConfiguration()[configIndex] = 0;
				}

				Mekanism.packetPipeline.sendToAllAround(new PacketTileEntity(coord4D, ((ITileNetwork) tile).getNetworkedData(new ArrayList())), coord4D.getTargetPoint(50D));
			}
			else if(packetType == ConfigurationPacket.EJECT_COLOR)
			{
				clickType = dataStream.readInt();

				if(clickType == 0)
				{
					config.getEjector().setOutputColor(TransporterUtils.increment(config.getEjector().getOutputColor()));
				}
				else if(clickType == 1)
				{
					config.getEjector().setOutputColor(TransporterUtils.decrement(config.getEjector().getOutputColor()));
				}
				else if(clickType == 2)
				{
					config.getEjector().setOutputColor(null);
				}
			}
			else if(packetType == ConfigurationPacket.INPUT_COLOR)
			{
				clickType = dataStream.readInt();
				inputSide = dataStream.readInt();

				ForgeDirection side = ForgeDirection.getOrientation(inputSide);

				if(clickType == 0)
				{
					config.getEjector().setInputColor(side, TransporterUtils.increment(config.getEjector().getInputColor(side)));
				}
				else if(clickType == 1)
				{
					config.getEjector().setInputColor(side, TransporterUtils.decrement(config.getEjector().getInputColor(side)));
				}
				else if(clickType == 2)
				{
					config.getEjector().setInputColor(side, null);
				}
			}
			else if(packetType == ConfigurationPacket.STRICT_INPUT)
			{
				config.getEjector().setStrictInput(!config.getEjector().hasStrictInput());
			}

			for(EntityPlayer p : ((TileEntityBasicBlock)config).playersUsing)
			{
				Mekanism.packetPipeline.sendTo(new PacketTileEntity(coord4D, ((ITileNetwork) tile).getNetworkedData(new ArrayList())), (EntityPlayerMP)p);
			}
		}
	}

	public void write(DataOutputStream dataStream) throws Exception
	{
		dataStream.writeInt(packetType.ordinal());

		dataStream.writeInt(coord4D.xCoord);
		dataStream.writeInt(coord4D.yCoord);
		dataStream.writeInt(coord4D.zCoord);

		dataStream.writeInt(coord4D.dimensionId);

		if(packetType != ConfigurationPacket.EJECT && packetType != ConfigurationPacket.STRICT_INPUT)
		{
			dataStream.writeInt(clickType);
		}

		if(packetType == ConfigurationPacket.SIDE_DATA)
		{
			dataStream.writeInt(configIndex);
		}

		if(packetType == ConfigurationPacket.INPUT_COLOR)
		{
			dataStream.writeInt(inputSide);
		}
	}

	@Override
	public void encodeInto(ChannelHandlerContext ctx, ByteBuf buffer)
	{	
	}

	@Override
	public void decodeInto(ChannelHandlerContext ctx, ByteBuf buffer)
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

	public static enum ConfigurationPacket
	{
		EJECT, SIDE_DATA, EJECT_COLOR, INPUT_COLOR, STRICT_INPUT
	}
}
