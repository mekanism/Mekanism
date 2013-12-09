package mekanism.common.network;

import java.io.DataOutputStream;

import mekanism.common.Mekanism;
import mekanism.common.PacketHandler;
import mekanism.common.PacketHandler.Transmission;
import mekanism.common.item.ItemScubaTank;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

import com.google.common.io.ByteArrayDataInput;

public class PacketScubaTankData implements IMekanismPacket
{
	public ScubaTankPacket packetType;
	
	public String username;
	public boolean value;
	
	@Override
	public String getName() 
	{
		return "ScubaTankData";
	}
	
	@Override
	public IMekanismPacket setParams(Object... data)
	{
		packetType = (ScubaTankPacket)data[0];
		
		if(packetType == ScubaTankPacket.UPDATE)
		{
			username = (String)data[1];
			value = (Boolean)data[2];
		}
	
		return this;
	}

	@Override
	public void read(ByteArrayDataInput dataStream, EntityPlayer player, World world) throws Exception
	{
		packetType = ScubaTankPacket.values()[dataStream.readInt()];
		
        if(packetType == ScubaTankPacket.FULL)
        {
			Mekanism.gasmaskOn.clear();

			int amount = dataStream.readInt();

			for(int i = 0; i < amount; i++)
			{
				Mekanism.gasmaskOn.add(dataStream.readUTF());
			}
        }
        else if(packetType == ScubaTankPacket.UPDATE)
		{
			String username = dataStream.readUTF();
			boolean value = dataStream.readBoolean();
			
			if(value)
			{
				Mekanism.gasmaskOn.add(username);
			}
			else {
				Mekanism.gasmaskOn.remove(username);
			}
			
			if(!world.isRemote)
			{
				PacketHandler.sendPacket(Transmission.CLIENTS_DIM, new PacketScubaTankData().setParams(ScubaTankPacket.UPDATE, username, value), world.provider.dimensionId);
			}
		}
		else if(packetType == ScubaTankPacket.MODE)
		{
			ItemStack stack = player.getCurrentItemOrArmor(3);
			
			if(stack != null && stack.getItem() instanceof ItemScubaTank)
			{
				((ItemScubaTank)stack.getItem()).toggleFlowing(stack);
			}
		}
	}

	@Override
	public void write(DataOutputStream dataStream) throws Exception
	{
		dataStream.writeInt(packetType.ordinal());
		
		if(packetType == ScubaTankPacket.UPDATE)
		{
			dataStream.writeUTF(username);
			dataStream.writeBoolean(value);
		}
		else if(packetType == ScubaTankPacket.FULL)
		{
			dataStream.writeInt(Mekanism.gasmaskOn.size());

			for(String username : Mekanism.gasmaskOn)
			{
				dataStream.writeUTF(username);
			}
		}
	}
	
	public static enum ScubaTankPacket
	{
		UPDATE,
		FULL,
		MODE;
	}
}
