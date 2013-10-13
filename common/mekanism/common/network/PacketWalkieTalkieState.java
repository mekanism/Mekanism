package mekanism.common.network;

import java.io.DataOutputStream;

import mekanism.common.item.ItemWalkieTalkie;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

import com.google.common.io.ByteArrayDataInput;

public class PacketWalkieTalkieState implements IMekanismPacket
{
	public int channel;
	
	@Override
	public String getName() 
	{
		return "WalkieTalkieState";
	}
	
	@Override
	public IMekanismPacket setParams(Object... data)
	{
		channel = (Integer)data[0];
		
		return this;
	}

	@Override
	public void read(ByteArrayDataInput dataStream, EntityPlayer player, World world) throws Exception 
	{
 		int chan = dataStream.readInt();
		
		ItemStack itemstack = player.getCurrentEquippedItem();
		
		if(itemstack != null && itemstack.getItem() instanceof ItemWalkieTalkie)
		{
			((ItemWalkieTalkie)itemstack.getItem()).setChannel(itemstack, chan);
		}
	}

	@Override
	public void write(DataOutputStream dataStream) throws Exception
	{
		dataStream.writeInt(channel);
	}
}
