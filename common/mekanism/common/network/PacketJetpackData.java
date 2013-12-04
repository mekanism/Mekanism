package mekanism.common.network;

import java.io.DataOutputStream;

import mekanism.common.Mekanism;
import mekanism.common.PacketHandler;
import mekanism.common.PacketHandler.Transmission;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;

import com.google.common.io.ByteArrayDataInput;

public class PacketJetpackData implements IMekanismPacket
{
	public EntityPlayer updatePlayer;
	public boolean value;
	
	@Override
	public String getName() 
	{
		return "RedstoneControl";
	}
	
	@Override
	public IMekanismPacket setParams(Object... data)
	{
		updatePlayer = (EntityPlayer)data[0];
		value = (Boolean)data[1];
	
		return this;
	}

	@Override
	public void read(ByteArrayDataInput dataStream, EntityPlayer player, World world) throws Exception
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
				PacketHandler.sendPacket(Transmission.CLIENTS_DIM, new PacketJetpackData().setParams(p, value), world.provider.dimensionId);
			}
		}
	}

	@Override
	public void write(DataOutputStream dataStream) throws Exception
	{
		dataStream.writeUTF(updatePlayer.username);
		dataStream.writeBoolean(value);
	}
}
