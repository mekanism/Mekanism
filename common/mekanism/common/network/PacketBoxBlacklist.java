package mekanism.common.network;

import java.io.DataOutputStream;

import mekanism.api.MekanismAPI;
import mekanism.api.MekanismAPI.BlockInfo;
import mekanism.common.IModule;
import mekanism.common.Mekanism;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;

import com.google.common.io.ByteArrayDataInput;

public class PacketBoxBlacklist implements IMekanismPacket
{
	@Override
	public String getName() 
	{
		return "BoxBlacklist";
	}
	
	@Override
	public IMekanismPacket setParams(Object... data)
	{
		return this;
	}

	@Override
	public void read(ByteArrayDataInput dataStream, EntityPlayer player, World world) throws Exception 
	{
		MekanismAPI.getBoxIgnore().clear();
		
		int amount = dataStream.readInt();
		
		for(int i = 0; i < amount; i++)
		{
			MekanismAPI.addBoxBlacklist(dataStream.readInt(), dataStream.readInt());
		}
		
		System.out.println("[Mekanism] Received Cardboard Box blacklist entries from server (" + amount + " total)");
	}

	@Override
	public void write(DataOutputStream dataStream) throws Exception
	{
		dataStream.writeInt(MekanismAPI.getBoxIgnore().size());
		
		for(BlockInfo info : MekanismAPI.getBoxIgnore())
		{
			dataStream.writeInt(info.id);
			dataStream.writeInt(info.meta);
		}
	}
}
