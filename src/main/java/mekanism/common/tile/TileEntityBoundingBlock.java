package mekanism.common.tile;

import io.netty.buffer.ByteBuf;

import java.util.ArrayList;

import mekanism.api.Coord4D;
import mekanism.common.ITileNetwork;
import mekanism.common.Mekanism;
import mekanism.common.network.PacketDataRequest;
import mekanism.common.network.PacketTileEntity;
import net.minecraft.block.Block;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;

public class TileEntityBoundingBlock extends TileEntity implements ITileNetwork
{
	public int mainX;
	public int mainY;
	public int mainZ;

	public boolean prevPower;

	public void setMainLocation(int x, int y, int z)
	{
		if(!worldObj.isRemote)
		{
			mainX = x;
			mainY = y;
			mainZ = z;

			Mekanism.packetPipeline.sendToAll(new PacketTileEntity(Coord4D.get(this), getNetworkedData(new ArrayList())));
		}
	}

	@Override
	public void validate()
	{
		super.validate();

		if(worldObj.isRemote)
		{
			Mekanism.packetPipeline.sendToServer(new PacketDataRequest(Coord4D.get(this)));
		}
	}

	@Override
	public boolean canUpdate()
	{
		return false;
	}

	public void onNeighborChange(Block block)
	{
		TileEntity tile = worldObj.getTileEntity(mainX, mainY, mainZ);

		if(tile instanceof TileEntityBasicBlock)
		{
			TileEntityBasicBlock tileEntity = (TileEntityBasicBlock)tile;

			boolean power = worldObj.isBlockIndirectlyGettingPowered(xCoord, yCoord, zCoord);

			if(prevPower != power)
			{
				if(power)
				{
					onPower();
				}
				else {
					onNoPower();
				}

				prevPower = power;
				Mekanism.packetPipeline.sendToDimension(new PacketTileEntity(Coord4D.get(tileEntity), tileEntity.getNetworkedData(new ArrayList())), tileEntity.getWorldObj().provider.dimensionId);
			}
		}
	}

	public void onPower() {}

	public void onNoPower() {}

	@Override
	public void handlePacketData(ByteBuf dataStream)
	{
		mainX = dataStream.readInt();
		mainY = dataStream.readInt();
		mainZ = dataStream.readInt();
		prevPower = dataStream.readBoolean();
	}

	@Override
	public void readFromNBT(NBTTagCompound nbtTags)
	{
		super.readFromNBT(nbtTags);

		mainX = nbtTags.getInteger("mainX");
		mainY = nbtTags.getInteger("mainY");
		mainZ = nbtTags.getInteger("mainZ");
		prevPower = nbtTags.getBoolean("prevPower");
	}

	@Override
	public void writeToNBT(NBTTagCompound nbtTags)
	{
		super.writeToNBT(nbtTags);

		nbtTags.setInteger("mainX", mainX);
		nbtTags.setInteger("mainY", mainY);
		nbtTags.setInteger("mainZ", mainZ);
		nbtTags.setBoolean("prevPower", prevPower);
	}

	@Override
	public ArrayList getNetworkedData(ArrayList data)
	{
		data.add(mainX);
		data.add(mainY);
		data.add(mainZ);
		data.add(prevPower);

		return data;
	}
}
