package mekanism.common.tile;

import java.util.ArrayList;

import com.google.common.io.ByteArrayDataInput;

import mekanism.api.Coord4D;
import mekanism.common.ITileNetwork;
import mekanism.common.PacketHandler;
import mekanism.common.PacketHandler.Transmission;
import mekanism.common.network.PacketDataRequest;
import mekanism.common.network.PacketTileEntity;
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

			PacketHandler.sendPacket(Transmission.ALL_CLIENTS, new PacketTileEntity().setParams(Coord4D.get(this), getNetworkedData(new ArrayList())));
		}
	}

	@Override
	public void validate()
	{
		super.validate();

		if(worldObj.isRemote)
		{
			PacketHandler.sendPacket(Transmission.SERVER, new PacketDataRequest().setParams(Coord4D.get(this)));
		}
	}

	@Override
	public boolean canUpdate()
	{
		return false;
	}

	public void onNeighborChange(int id)
	{
		TileEntity tile = worldObj.getBlockTileEntity(mainX, mainY, mainZ);

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
				PacketHandler.sendPacket(Transmission.CLIENTS_DIM, new PacketTileEntity().setParams(Coord4D.get(tileEntity), tileEntity.getNetworkedData(new ArrayList())), tileEntity.worldObj.provider.dimensionId);
			}
		}
	}

	public void onPower() {}

	public void onNoPower() {}

	@Override
	public void handlePacketData(ByteArrayDataInput dataStream)
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
