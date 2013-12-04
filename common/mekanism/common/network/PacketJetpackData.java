package mekanism.common.network;

import java.io.DataOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import mekanism.common.Mekanism;
import mekanism.common.PacketHandler;
import mekanism.common.PacketHandler.Transmission;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;

import com.google.common.io.ByteArrayDataInput;

import cpw.mods.fml.common.FMLCommonHandler;

public class PacketJetpackData implements IMekanismPacket
{
	public PacketType packetType;
	
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
		packetType = (PacketType)data[0];
		
		if(packetType == PacketType.UPDATE)
		{
			updatePlayer = (EntityPlayer)data[1];
			value = (Boolean)data[2];
		}
		
		return this;
	}

	@Override
	public void read(ByteArrayDataInput dataStream, EntityPlayer player, World world) throws Exception
	{
		packetType = PacketType.values()[dataStream.readInt()];
		
		if(packetType == PacketType.INITIAL)
		{
			Mekanism.jetpackOn.clear();
			
			int amount = dataStream.readInt();
			
			for(int i = 0; i < amount; i++)
			{
				EntityPlayer p = world.getPlayerEntityByName(dataStream.readUTF());
				
				if(p != null)
				{
					Mekanism.jetpackOn.put(p, true);
				}
			}
		}
		else if(packetType == PacketType.UPDATE)
		{
			String username = dataStream.readUTF();
			boolean value = dataStream.readBoolean();
			
			EntityPlayer p = FMLCommonHandler.instance().getMinecraftServerInstance().getConfigurationManager().getPlayerForUsername(username);
			
			if(p != null)
			{
				Mekanism.jetpackOn.put(p, value);
				
				PacketHandler.sendPacket(Transmission.CLIENTS_DIM, new PacketJetpackData().setParams(PacketType.UPDATE, p, value), world.provider.dimensionId);
			}
		}
	}

	@Override
	public void write(DataOutputStream dataStream) throws Exception
	{
		dataStream.writeInt(packetType.ordinal());
		
		if(packetType == PacketType.UPDATE)
		{
			dataStream.writeUTF(updatePlayer.username);
			dataStream.writeBoolean(value);
		}
		else if(packetType == PacketType.INITIAL)
		{
			List<EntityPlayer> toSend = new ArrayList<EntityPlayer>();
			
			for(Map.Entry<EntityPlayer, Boolean> entry : Mekanism.jetpackOn.entrySet())
			{
				if(entry.getValue())
				{
					toSend.add(entry.getKey());
				}
			}
			
			dataStream.writeInt(toSend.size());
			
			for(EntityPlayer player : toSend)
			{
				dataStream.writeUTF(player.username);
			}
		}
	}
	
	public static enum PacketType
	{
		INITIAL, UPDATE
	}
}
