package mekanism.common.network;

import io.netty.buffer.ByteBuf;

import java.util.ArrayList;

import mekanism.api.Coord4D;
import mekanism.api.Range4D;
import mekanism.api.transmitters.TransmissionType;
import mekanism.common.Mekanism;
import mekanism.common.PacketHandler;
import mekanism.common.base.ISideConfiguration;
import mekanism.common.base.ITileNetwork;
import mekanism.common.network.PacketConfigurationUpdate.ConfigurationUpdateMessage;
import mekanism.common.network.PacketTileEntity.TileEntityMessage;
import mekanism.common.tile.TileEntityBasicBlock;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.TransporterUtils;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.util.ForgeDirection;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;

public class PacketConfigurationUpdate implements IMessageHandler<ConfigurationUpdateMessage, IMessage>
{
	@Override
	public IMessage onMessage(ConfigurationUpdateMessage message, MessageContext context) 
	{
		TileEntity tile = message.coord4D.getTileEntity(PacketHandler.getPlayer(context).worldObj);
		
		if(tile instanceof ISideConfiguration)
		{
			ISideConfiguration config = (ISideConfiguration)tile;

			if(message.packetType == ConfigurationPacket.EJECT)
			{
				config.getConfig().setEjecting(message.transmission, !config.getConfig().isEjecting(message.transmission));
			}
			else if(message.packetType == ConfigurationPacket.SIDE_DATA)
			{
				if(message.clickType == 0)
				{
					MekanismUtils.incrementOutput((ISideConfiguration)tile, message.transmission, message.configIndex);
				}
				else if(message.clickType == 1)
				{
					MekanismUtils.decrementOutput((ISideConfiguration)tile, message.transmission, message.configIndex);
				}
				else if(message.clickType == 2)
				{
					((ISideConfiguration)tile).getConfig().getConfig(message.transmission)[message.configIndex] = 0;
				}

				tile.markDirty();
				Mekanism.packetHandler.sendToReceivers(new TileEntityMessage(message.coord4D, ((ITileNetwork)tile).getNetworkedData(new ArrayList())), new Range4D(message.coord4D));
			}
			else if(message.packetType == ConfigurationPacket.EJECT_COLOR)
			{
				if(message.clickType == 0)
				{
					config.getEjector().setOutputColor(TransporterUtils.increment(config.getEjector().getOutputColor()));
				}
				else if(message.clickType == 1)
				{
					config.getEjector().setOutputColor(TransporterUtils.decrement(config.getEjector().getOutputColor()));
				}
				else if(message.clickType == 2)
				{
					config.getEjector().setOutputColor(null);
				}
			}
			else if(message.packetType == ConfigurationPacket.INPUT_COLOR)
			{
				ForgeDirection side = ForgeDirection.getOrientation(message.inputSide);

				if(message.clickType == 0)
				{
					config.getEjector().setInputColor(side, TransporterUtils.increment(config.getEjector().getInputColor(side)));
				}
				else if(message.clickType == 1)
				{
					config.getEjector().setInputColor(side, TransporterUtils.decrement(config.getEjector().getInputColor(side)));
				}
				else if(message.clickType == 2)
				{
					config.getEjector().setInputColor(side, null);
				}
			}
			else if(message.packetType == ConfigurationPacket.STRICT_INPUT)
			{
				config.getEjector().setStrictInput(!config.getEjector().hasStrictInput());
			}

			for(EntityPlayer p : ((TileEntityBasicBlock)config).playersUsing)
			{
				Mekanism.packetHandler.sendTo(new TileEntityMessage(message.coord4D, ((ITileNetwork)tile).getNetworkedData(new ArrayList())), (EntityPlayerMP)p);
			}
		}
		
		return null;
	}
	
	public static class ConfigurationUpdateMessage implements IMessage
	{
		public Coord4D coord4D;
	
		public int configIndex;
	
		public int inputSide;
		
		public TransmissionType transmission;
	
		public int clickType;
	
		public ConfigurationPacket packetType;
		
		public ConfigurationUpdateMessage() {}
	
		public ConfigurationUpdateMessage(ConfigurationPacket type, Coord4D coord, int click, int extra, TransmissionType trans)
		{
			packetType = type;
	
			coord4D = coord;
			
			if(packetType == ConfigurationPacket.EJECT)
			{
				transmission = trans;
			}
	
			if(packetType == ConfigurationPacket.EJECT_COLOR)
			{
				clickType = click;
			}
	
			if(packetType == ConfigurationPacket.SIDE_DATA)
			{
				clickType = click;
				configIndex = extra;
				transmission = trans;
			}
	
			if(packetType == ConfigurationPacket.INPUT_COLOR)
			{
				clickType = click;
				inputSide = extra;
			}
		}
	
		@Override
		public void toBytes(ByteBuf dataStream)
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
			
			if(packetType == ConfigurationPacket.EJECT)
			{
				dataStream.writeInt(transmission.ordinal());
			}
	
			if(packetType == ConfigurationPacket.SIDE_DATA)
			{
				dataStream.writeInt(configIndex);
				dataStream.writeInt(transmission.ordinal());
			}
	
			if(packetType == ConfigurationPacket.INPUT_COLOR)
			{
				dataStream.writeInt(inputSide);
			}
		}
	
		@Override
		public void fromBytes(ByteBuf dataStream)
		{
			packetType = ConfigurationPacket.values()[dataStream.readInt()];
	
			coord4D = new Coord4D(dataStream.readInt(), dataStream.readInt(), dataStream.readInt(), dataStream.readInt());
	
			if(packetType == ConfigurationPacket.EJECT)
			{
				transmission = TransmissionType.values()[dataStream.readInt()];
			}
			else if(packetType == ConfigurationPacket.SIDE_DATA)
			{
				clickType = dataStream.readInt();
				configIndex = dataStream.readInt();
				transmission = TransmissionType.values()[dataStream.readInt()];
			}
			else if(packetType == ConfigurationPacket.EJECT_COLOR)
			{
				clickType = dataStream.readInt();
			}
			else if(packetType == ConfigurationPacket.INPUT_COLOR)
			{
				clickType = dataStream.readInt();
				inputSide = dataStream.readInt();
			}
		}
	}
	
	public static enum ConfigurationPacket
	{
		EJECT, SIDE_DATA, EJECT_COLOR, INPUT_COLOR, STRICT_INPUT
	}
}
