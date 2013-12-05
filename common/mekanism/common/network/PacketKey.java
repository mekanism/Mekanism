package mekanism.common.network;

import java.io.DataOutputStream;

import mekanism.common.Mekanism;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;

import com.google.common.io.ByteArrayDataInput;

public class PacketKey implements IMekanismPacket
{
	public int key;
	public boolean add;
	
	@Override
	public String getName() 
	{
		return "Key";
	}
	
	@Override
	public IMekanismPacket setParams(Object... data)
	{
		key = (Integer)data[0];
		add = (Boolean)data[1];
		
		return this;
	}

	@Override
	public void read(ByteArrayDataInput dataStream, EntityPlayer player, World world) throws Exception 
	{
 		key = dataStream.readInt();
 		add = dataStream.readBoolean();
 		
 		if(add)
 		{
 			Mekanism.keyMap.add(player, key);
 		}
 		else {
 			Mekanism.keyMap.remove(player, key);
 		}
	}

	@Override
	public void write(DataOutputStream dataStream) throws Exception
	{
		dataStream.writeInt(key);
		dataStream.writeBoolean(add);
	}
}
