package mekanism.common.network;

import java.io.DataOutputStream;

import mekanism.api.Coord4D;
import mekanism.common.IRedstoneControl;
import mekanism.common.IRedstoneControl.RedstoneControl;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

import com.google.common.io.ByteArrayDataInput;

public class PacketRedstoneControl implements IMekanismPacket
{
	public Coord4D coord;
	public RedstoneControl value;
	
	@Override
	public String getName() 
	{
		return "RedstoneControl";
	}
	
	@Override
	public IMekanismPacket setParams(Object... data)
	{
		coord = (Coord4D)data[0];
		value = (RedstoneControl)data[1];
		
		return this;
	}

	@Override
	public void read(ByteArrayDataInput dataStream, EntityPlayer player, World world) throws Exception
	{
		Coord4D obj = Coord4D.read(dataStream);
		RedstoneControl control = RedstoneControl.values()[dataStream.readInt()];
		
		TileEntity tileEntity = obj.getTileEntity(world);
		
		if(tileEntity instanceof IRedstoneControl)
		{
			((IRedstoneControl)tileEntity).setControlType(control);
		}
	}

	@Override
	public void write(DataOutputStream dataStream) throws Exception
	{
		dataStream.writeInt(coord.xCoord);
		dataStream.writeInt(coord.yCoord);
		dataStream.writeInt(coord.zCoord);
		dataStream.writeInt(coord.dimensionId);
		
		dataStream.writeInt(value.ordinal());
	}
}
