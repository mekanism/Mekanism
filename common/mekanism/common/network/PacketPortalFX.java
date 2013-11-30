package mekanism.common.network;

import java.io.DataOutputStream;
import java.util.Random;

import mekanism.common.Object3D;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;

import com.google.common.io.ByteArrayDataInput;

public class PacketPortalFX implements IMekanismPacket
{
	public Object3D object3D;
	
	@Override
	public String getName() 
	{
		return "PortalFX";
	}
	
	@Override
	public IMekanismPacket setParams(Object... data)
	{
		object3D = (Object3D)data[0];
		
		return this;
	}

	@Override
	public void read(ByteArrayDataInput dataStream, EntityPlayer player, World world) throws Exception
	{
		Random random = new Random();
		
		int x = dataStream.readInt();
		int y = dataStream.readInt();
		int z = dataStream.readInt();
		
		for(int i = 0; i < 50; i++)
		{
			world.spawnParticle("portal", x + random.nextFloat(), y + random.nextFloat(), z + random.nextFloat(), 0.0F, 0.0F, 0.0F);
			world.spawnParticle("portal", x + random.nextFloat(), y + 1 + random.nextFloat(), z + random.nextFloat(), 0.0F, 0.0F, 0.0F);
		}
	}

	@Override
	public void write(DataOutputStream dataStream) throws Exception
	{
		dataStream.writeInt(object3D.xCoord);
		dataStream.writeInt(object3D.yCoord);
		dataStream.writeInt(object3D.zCoord);
	}
}
