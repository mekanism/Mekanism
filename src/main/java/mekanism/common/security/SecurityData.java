package mekanism.common.security;

import io.netty.buffer.ByteBuf;
import mekanism.common.security.ISecurityTile.SecurityMode;

public class SecurityData 
{
	public SecurityMode mode = SecurityMode.PUBLIC;
	public boolean override;
	
	public SecurityData() {}
	
	public SecurityData(SecurityFrequency frequency)
	{
		mode = frequency.securityMode;
		override = frequency.override;
	}
	
	public void write(ByteBuf dataStream)
	{
		dataStream.writeInt(mode.ordinal());
		dataStream.writeBoolean(override);
	}
	
	public static SecurityData read(ByteBuf dataStream)
	{
		SecurityData data = new SecurityData();
		
		data.mode = SecurityMode.values()[dataStream.readInt()];
		data.override = dataStream.readBoolean();
		
		return data;
	}
}
