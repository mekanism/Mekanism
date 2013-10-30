package mekanism.common.network;

import java.io.DataOutputStream;
import java.util.ArrayList;

import mekanism.api.IConfigurable;
import mekanism.api.Object3D;
import mekanism.common.ITileNetwork;
import mekanism.common.PacketHandler;
import mekanism.common.PacketHandler.Transmission;
import mekanism.common.util.MekanismUtils;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

import com.google.common.io.ByteArrayDataInput;

public class PacketConfigurationUpdate implements IMekanismPacket
{
	public Object3D object3D;
	
	public int configIndex;
	
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
		
		return this;
	}
	
	@Override
	public void read(ByteArrayDataInput dataStream, EntityPlayer player, World world) throws Exception 
	{
		packetType = ConfigurationPacket.values()[dataStream.readInt()];
		
		object3D = new Object3D(dataStream.readInt(), dataStream.readInt(), dataStream.readInt(), dataStream.readInt());
		
		if(packetType == ConfigurationPacket.EJECT)
		{
			TileEntity tile = object3D.getTileEntity(world);
			
			if(tile instanceof IConfigurable)
			{
				IConfigurable config = (IConfigurable)tile;
				config.getEjector().setEjecting(!config.getEjector().isEjecting());
				System.out.println("Yup");
			}
		}
		else if(packetType == ConfigurationPacket.SIDE_DATA)
		{
			configIndex = dataStream.readInt();
			
			TileEntity tile = object3D.getTileEntity(world);
			
			if(tile instanceof IConfigurable)
			{
				MekanismUtils.incrementOutput((IConfigurable)tile, configIndex);
				PacketHandler.sendPacket(Transmission.CLIENTS_RANGE, new PacketTileEntity().setParams(object3D, ((ITileNetwork)tile).getNetworkedData(new ArrayList())), object3D, 50D);
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
	}
	
	public static enum ConfigurationPacket
	{
		EJECT, SIDE_DATA
	}
}
