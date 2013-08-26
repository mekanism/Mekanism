package mekanism.common.network;

import java.io.DataOutputStream;

import mekanism.common.util.MekanismUtils;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;

import com.google.common.io.ByteArrayDataInput;

public class PacketTime implements IMekanismPacket
{
	public int hourToSet;
	
	@Override
	public String getName() 
	{
		return "Time";
	}
	
	@Override
	public IMekanismPacket setParams(Object... data)
	{
		hourToSet = (Integer)data[0];
		
		return this;
	}

	@Override
	public void read(ByteArrayDataInput dataStream, EntityPlayer player, World world) throws Exception 
	{
        player.getCurrentEquippedItem().damageItem(4999, player);
        MekanismUtils.setHourForward(world, dataStream.readInt());
	}

	@Override
	public void write(DataOutputStream dataStream) throws Exception
	{
		dataStream.writeInt(hourToSet);
	}
}
