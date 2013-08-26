package mekanism.generators.common.network;

import java.io.DataOutputStream;

import mekanism.common.network.IMekanismPacket;
import mekanism.generators.common.tileentity.TileEntityElectrolyticSeparator;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;

import com.google.common.io.ByteArrayDataInput;

public class PacketElectrolyticSeparatorParticle implements IMekanismPacket
{
	public TileEntityElectrolyticSeparator tileEntity;
	
	@Override
	public IMekanismPacket setParams(Object... data)
	{
		tileEntity = (TileEntityElectrolyticSeparator)data[0];
		
		return this;
	}
	
	@Override
	public String getName() 
	{
		return "ElectrolyticSeparatorParticle";
	}

	@Override
	public void read(ByteArrayDataInput dataStream, EntityPlayer player, World world) throws Exception
	{
		int x = dataStream.readInt();
		int y = dataStream.readInt();
		int z = dataStream.readInt();
		
		TileEntityElectrolyticSeparator tileEntity = (TileEntityElectrolyticSeparator)world.getBlockTileEntity(x, y, z);
		
		if(tileEntity != null)
		{
			tileEntity.spawnParticle();
		}
	}

	@Override
	public void write(DataOutputStream dataStream) throws Exception
	{
		dataStream.writeInt(tileEntity.xCoord);
		dataStream.writeInt(tileEntity.yCoord);
		dataStream.writeInt(tileEntity.zCoord);
	}
}
