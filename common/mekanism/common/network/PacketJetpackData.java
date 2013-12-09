package mekanism.common.network;

import java.io.DataOutputStream;

import mekanism.common.Mekanism;
import mekanism.common.PacketHandler;
import mekanism.common.PacketHandler.Transmission;
import mekanism.common.item.ItemJetpack;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

import com.google.common.io.ByteArrayDataInput;

public class PacketJetpackData implements IMekanismPacket
{
	public JetpackPacket packetType;
	
	public String username;
	public boolean value;
	
	@Override
	public String getName() 
	{
		return "JetpackData";
	}
	
	@Override
	public IMekanismPacket setParams(Object... data)
	{
		packetType = (JetpackPacket)data[0];
		
		if(packetType == JetpackPacket.UPDATE)
		{
			username = (String)data[1];
			value = (Boolean)data[2];
		}
	
		return this;
	}

	@Override
	public void read(ByteArrayDataInput dataStream, EntityPlayer player, World world) throws Exception
	{
		packetType = JetpackPacket.values()[dataStream.readInt()];
		
        if(packetType == JetpackPacket.FULL)
        {
			Mekanism.jetpackOn.clear();

			int amount = dataStream.readInt();

			for(int i = 0; i < amount; i++)
			{
				Mekanism.jetpackOn.add(dataStream.readUTF());
			}
        }
        else if(packetType == JetpackPacket.UPDATE)
		{
			String username = dataStream.readUTF();
			boolean value = dataStream.readBoolean();
			
			if(value)
			{
				Mekanism.jetpackOn.add(username);
			}
			else {
				Mekanism.jetpackOn.remove(username);
			}
			
			if(!world.isRemote)
			{
				PacketHandler.sendPacket(Transmission.CLIENTS_DIM, new PacketJetpackData().setParams(JetpackPacket.UPDATE, username, value), world.provider.dimensionId);
			}
		}
		else if(packetType == JetpackPacket.MODE)
		{
			ItemStack stack = player.getCurrentItemOrArmor(3);
			
			if(stack != null && stack.getItem() instanceof ItemJetpack)
			{
				((ItemJetpack)stack.getItem()).incrementMode(stack);
			}
		}
	}

	@Override
	public void write(DataOutputStream dataStream) throws Exception
	{
		dataStream.writeInt(packetType.ordinal());
		
		if(packetType == JetpackPacket.UPDATE)
		{
			dataStream.writeUTF(username);
			dataStream.writeBoolean(value);
		}
		else if(packetType == JetpackPacket.FULL)
		{
			dataStream.writeInt(Mekanism.jetpackOn.size());

			for(String username : Mekanism.jetpackOn)
			{
				dataStream.writeUTF(username);
			}
		}
	}
	
	public static enum JetpackPacket
	{
		UPDATE,
		FULL,
		MODE;
	}
}
