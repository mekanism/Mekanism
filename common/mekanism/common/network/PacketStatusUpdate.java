package mekanism.common.network;

import java.io.DataOutputStream;

import mekanism.common.item.ItemPortableTeleporter;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

import com.google.common.io.ByteArrayDataInput;

public class PacketStatusUpdate implements IMekanismPacket
{
	public int status;
	
	@Override
	public String getName() 
	{
		return "StatusUpdate";
	}
	
	@Override
	public IMekanismPacket setParams(Object... data)
	{
		status = (Integer)data[0];
		
		return this;
	}

	@Override
	public void read(ByteArrayDataInput dataStream, EntityPlayer player, World world) throws Exception 
	{
		ItemStack currentStack = player.getCurrentEquippedItem();
		
		if(currentStack != null && currentStack.getItem() instanceof ItemPortableTeleporter)
		{
			ItemPortableTeleporter item = (ItemPortableTeleporter)currentStack.getItem();
			item.setStatus(currentStack, dataStream.readInt());
		}
	}

	@Override
	public void write(DataOutputStream dataStream) throws Exception 
	{
		dataStream.writeInt(status);
	}
}
