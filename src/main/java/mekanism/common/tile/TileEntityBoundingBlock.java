package mekanism.common.tile;

import java.util.ArrayList;

import mekanism.api.Coord4D;
import mekanism.api.Range4D;
import mekanism.common.Mekanism;
import mekanism.common.base.ITileNetwork;
import mekanism.common.network.PacketDataRequest.DataRequestMessage;
import mekanism.common.network.PacketTileEntity.TileEntityMessage;

import net.minecraft.block.Block;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockPos;

import io.netty.buffer.ByteBuf;

public class TileEntityBoundingBlock extends TileEntity implements ITileNetwork
{
	public Coord4D mainPos;
	
	public boolean receivedCoords;

	public boolean prevPower;

	public void setMainLocation(Coord4D mainLocation)
	{
		receivedCoords = true;
		
		if(!worldObj.isRemote)
		{
			mainPos = mainLocation;

			Mekanism.packetHandler.sendToReceivers(new TileEntityMessage(Coord4D.get(this), getNetworkedData(new ArrayList())), new Range4D(Coord4D.get(this)));
		}
	}

	@Override
	public void validate()
	{
		super.validate();

		if(worldObj.isRemote)
		{
			Mekanism.packetHandler.sendToServer(new DataRequestMessage(Coord4D.get(this)));
		}
	}

	public Coord4D getMainPos()
	{
		return mainPos;
	}

	public void onNeighborChange(Block block)
	{
		TileEntity tile = worldObj.getTileEntity(getMainPos());

		if(tile instanceof TileEntityBasicBlock)
		{
			TileEntityBasicBlock tileEntity = (TileEntityBasicBlock)tile;

			boolean power = worldObj.isBlockIndirectlyGettingPowered(getPos()) > 0;

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
				Mekanism.packetHandler.sendToReceivers(new TileEntityMessage(Coord4D.get(tileEntity), tileEntity.getNetworkedData(new ArrayList())), new Range4D(Coord4D.get(this)));
			}
		}
	}

	public void onPower() {}

	public void onNoPower() {}

	@Override
	public void handlePacketData(ByteBuf dataStream)
	{
		mainPos = Coord4D.read(dataStream);
		prevPower = dataStream.readBoolean();
	}

	@Override
	public void readFromNBT(NBTTagCompound nbtTags)
	{
		super.readFromNBT(nbtTags);

		mainPos = Coord4D.read(nbtTags);
		prevPower = nbtTags.getBoolean("prevPower");
		receivedCoords = nbtTags.getBoolean("receivedCoords");
	}

	@Override
	public void writeToNBT(NBTTagCompound nbtTags)
	{
		super.writeToNBT(nbtTags);

		mainPos.write(nbtTags);
		nbtTags.setBoolean("prevPower", prevPower);
		nbtTags.setBoolean("receivedCoords", receivedCoords);
	}

	@Override
	public ArrayList getNetworkedData(ArrayList data)
	{
		mainPos.write(data);
		data.add(prevPower);

		return data;
	}
}
