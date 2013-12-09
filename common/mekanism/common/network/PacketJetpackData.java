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
	
	public EntityPlayer updatePlayer;
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
			updatePlayer = (EntityPlayer)data[1];
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
				EntityPlayer p = world.getPlayerEntityByName(dataStream.readUTF());

				if(p != null)
				{
					Mekanism.jetpackOn.add(p);
				}
			}
        }
        else if(packetType == JetpackPacket.UPDATE)
		{
			String username = dataStream.readUTF();
			boolean value = dataStream.readBoolean();
			
			EntityPlayer p = world.getPlayerEntityByName(username);
			
			if(p != null)
			{
				if(value)
				{
					Mekanism.jetpackOn.add(p);
				}
				else {
					Mekanism.jetpackOn.remove(p);
				}
				
				if(!world.isRemote)
				{
					PacketHandler.sendPacket(Transmission.CLIENTS_DIM, new PacketJetpackData().setParams(JetpackPacket.UPDATE, p, value), world.provider.dimensionId);
				}
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
			dataStream.writeUTF(updatePlayer.username);
			dataStream.writeBoolean(value);
		}
		else if(packetType == JetpackPacket.FULL)
		{
			dataStream.writeInt(Mekanism.jetpackOn.size());

			for(EntityPlayer player : Mekanism.jetpackOn)
			{
				dataStream.writeUTF(player.username);
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
