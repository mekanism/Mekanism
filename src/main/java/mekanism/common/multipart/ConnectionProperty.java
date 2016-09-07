package mekanism.common.multipart;

import java.util.Arrays;

import mekanism.common.multipart.PartSidedPipe.ConnectionType;
import net.minecraftforge.common.property.IUnlistedProperty;

public class ConnectionProperty implements IUnlistedProperty<ConnectionProperty>
{
	public static ConnectionProperty INSTANCE = new ConnectionProperty();
	
	public byte connectionByte;
	public byte transmitterConnections;
	public ConnectionType[] connectionTypes;
	public boolean renderCenter;
	
	public ConnectionProperty() {}
	
	public ConnectionProperty(byte b, byte b1, ConnectionType[] types, boolean center)
	{
		connectionByte = b;
		transmitterConnections = b1;
		connectionTypes = types;
		renderCenter = center;
	}
	
	@Override
	public String getName() 
	{
		return "connection";
	}

	@Override
	public boolean isValid(ConnectionProperty value) 
	{
		return true;
	}

	@Override
	public Class getType() 
	{
		return getClass();
	}

	@Override
	public String valueToString(ConnectionProperty value) 
	{
		return Byte.toString(value.connectionByte) + "_" + Byte.toString(value.transmitterConnections) + "_" 
				+ Arrays.toString(value.connectionTypes) + "_" + value.renderCenter;
	}
}
