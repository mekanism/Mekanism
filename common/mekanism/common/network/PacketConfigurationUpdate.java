package mekanism.common.network;

import java.io.DataOutputStream;
import java.util.ArrayList;

import mekanism.api.Coord4D;
import mekanism.common.IInvConfiguration;
import mekanism.common.ITileNetwork;
import mekanism.common.PacketHandler;
import mekanism.common.PacketHandler.Transmission;
import mekanism.common.tile.TileEntityBasicBlock;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.TransporterUtils;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeDirection;

import com.google.common.io.ByteArrayDataInput;

public class PacketConfigurationUpdate implements IMekanismPacket
{
	public Coord4D object3D;
	
	public int configIndex;
	
	public int inputSide;
	
	public int clickType;
	
	public ConfigurationPacket packetType;
	
	@Override
	public String getName()
	{
		return "ConfigurationUpdate";
	}
	
	@Override
	public IMekanismPacket setParams(Object... data)
	{
		packetType = (ConfigurationPacket)data[0];
		
		object3D = (Coord4D)data[1];
		
		if(packetType == ConfigurationPacket.EJECT_COLOR)
		{
			clickType = (Integer)data[2];
		}
		
		if(packetType == ConfigurationPacket.SIDE_DATA)
		{
			clickType = (Integer)data[2];
			configIndex = (Integer)data[3];
		}
		
		if(packetType == ConfigurationPacket.INPUT_COLOR)
		{
			clickType = (Integer)data[2];
			inputSide = (Integer)data[3];
		}
		
		return this;
	}
	
	@Override
	public void read(ByteArrayDataInput dataStream, EntityPlayer player, World world) throws Exception 
	{
		packetType = ConfigurationPacket.values()[dataStream.readInt()];
		
		object3D = new Coord4D(dataStream.readInt(), dataStream.readInt(), dataStream.readInt(), dataStream.readInt());
		
		TileEntity tile = object3D.getTileEntity(world);
		
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
				
				PacketHandler.sendPacket(Transmission.CLIENTS_RANGE, new PacketTileEntity().setParams(object3D, ((ITileNetwork)tile).getNetworkedData(new ArrayList())), object3D, 50D);
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
				PacketHandler.sendPacket(Transmission.SINGLE_CLIENT, new PacketTileEntity().setParams(object3D, ((ITileNetwork)tile).getNetworkedData(new ArrayList())), p);
			}
		}
	}
	
	@Override
	public void write(DataOutputStream dataStream) throws Exception
	{
		dataStream.writeInt(packetType.ordinal());
		
		dataStream.writeInt(object3D.xCoord);
		dataStream.writeInt(object3D.yCoord);
		dataStream.writeInt(object3D.zCoord);
		
		dataStream.writeInt(object3D.dimensionId);
		
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
	
	public static enum ConfigurationPacket
	{
		EJECT, SIDE_DATA, EJECT_COLOR, INPUT_COLOR, STRICT_INPUT
	}
}
