package mekanism.common.network;

import java.io.DataOutputStream;

import mekanism.common.item.ItemElectricBow;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

import com.google.common.io.ByteArrayDataInput;

public class PacketElectricBowState implements IMekanismPacket
{
	public boolean fireMode;
	
	@Override
	public String getName() 
	{
		return "ElectricBowState";
	}
	
	@Override
	public IMekanismPacket setParams(Object... data)
	{
		fireMode = (Boolean)data[0];
		
		return this;
	}

	@Override
	public void read(ByteArrayDataInput dataStream, EntityPlayer player, World world) throws Exception 
	{
 		boolean state = dataStream.readBoolean();
		
		ItemStack itemstack = player.getCurrentEquippedItem();
		
		if(itemstack != null && itemstack.getItem() instanceof ItemElectricBow)
		{
			((ItemElectricBow)itemstack.getItem()).setFireState(itemstack, state);
		}
	}

	@Override
	public void write(DataOutputStream dataStream) throws Exception
	{
		dataStream.writeBoolean(fireMode);
	}
}
