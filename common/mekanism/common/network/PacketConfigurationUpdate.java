package mekanism.common.network;

import java.io.DataOutputStream;
import java.util.ArrayList;

import mekanism.api.IConfigurable;
import mekanism.api.Object3D;
import mekanism.common.ITileNetwork;
import mekanism.common.PacketHandler;
import mekanism.common.PacketHandler.Transmission;
import mekanism.common.tileentity.TileEntityBasicBlock;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.TransporterUtils;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeDirection;

import com.google.common.io.ByteArrayDataInput;

public class PacketConfigurationUpdate implements IMekanismPacket
{
	public Object3D object3D;
	
	public int configIndex;
	
	public int inputSide;
	
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
		
		object3D = (Object3D)data[1];
		
		if(packetType == ConfigurationPacket.SIDE_DATA)
		{
			configIndex = (Integer)data[2];
		}
		
		if(packetType == ConfigurationPacket.INPUT_COLOR)
		{
			inputSide = (Integer)data[2];
		}
		
		return this;
	}
	
	@Override
	public void read(ByteArrayDataInput dataStream, EntityPlayer player, World world) throws Exception 
	{
		packetType = ConfigurationPacket.values()[dataStream.readInt()];
		
		object3D = new Object3D(dataStream.readInt(), dataStream.readInt(), dataStream.readInt(), dataStream.readInt());
		
		TileEntity tile = object3D.getTileEntity(world);
		
		if(tile instanceof IConfigurable)
		{
			IConfigurable config = (IConfigurable)tile;
			
			if(packetType == ConfigurationPacket.EJECT)
			{
				config.getEjector().setEjecting(!config.getEjector().isEjecting());
			}
			else if(packetType == ConfigurationPacket.SIDE_DATA)
			{
				configIndex = dataStream.readInt();
				
				MekanismUtils.incrementOutput((IConfigurable)tile, configIndex);
				PacketHandler.sendPacket(Transmission.CLIENTS_RANGE, new PacketTileEntity().setParams(object3D, ((ITileNetwork)tile).getNetworkedData(new ArrayList())), object3D, 50D);
			}
			else if(packetType == ConfigurationPacket.EJECT_COLOR)
			{
				config.getEjector().setOutputColor(TransporterUtils.increment(config.getEjector().getOutputColor()));
			}
			else if(packetType == ConfigurationPacket.INPUT_COLOR)
			{
				inputSide = dataStream.readInt();
				ForgeDirection side = ForgeDirection.getOrientation(inputSide);
				config.getEjector().setInputColor(side, TransporterUtils.increment(config.getEjector().getInputColor(side)));
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
