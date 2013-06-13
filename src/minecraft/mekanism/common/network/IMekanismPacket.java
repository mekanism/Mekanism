package mekanism.common.network;

import java.io.DataOutputStream;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;

import com.google.common.io.ByteArrayDataInput;

public interface IMekanismPacket
{
	public String getName();
	
	public void read(ByteArrayDataInput dataStream, EntityPlayer player, World world) throws Exception;
	
	public void write(DataOutputStream dataStream) throws Exception;
}
