package mekanism.common.network;

import java.io.DataOutputStream;

import mekanism.common.item.ItemPortableTeleporter;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

import com.google.common.io.ByteArrayDataInput;

public class PacketDigitUpdate implements IMekanismPacket
{
	public int index;
	public int digit;
	
	@Override
	public String getName() 
	{
		return "DigitUpdate";
	}
	
	@Override
	public IMekanismPacket setParams(Object... data)
	{
		index = (Integer)data[0];
		digit = (Integer)data[1];
		
		return this;
	}

	@Override
	public void read(ByteArrayDataInput dataStream, EntityPlayer player, World world) throws Exception 
	{
		int index = dataStream.readInt();
		int digit = dataStream.readInt();
		
		ItemStack currentStack = player.getCurrentEquippedItem();
		
		if(currentStack != null && currentStack.getItem() instanceof ItemPortableTeleporter)
		{
			ItemPortableTeleporter item = (ItemPortableTeleporter)currentStack.getItem();
			item.setDigit(currentStack, index, digit);
		}
	}

	@Override
	public void write(DataOutputStream dataStream) throws Exception
	{
		dataStream.writeInt(index);
		dataStream.writeInt(digit);
	}
}
