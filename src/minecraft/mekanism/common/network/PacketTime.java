package mekanism.common.network;

import java.io.DataOutputStream;

import mekanism.common.MekanismUtils;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;

import com.google.common.io.ByteArrayDataInput;

public class PacketTime implements IMekanismPacket
{
	public int hourToSet;
	
	public PacketTime(int hour)
	{
		hourToSet = hour;
	}
	
	public PacketTime() {}
	
	@Override
	public String getName() 
	{
		return "Time";
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
