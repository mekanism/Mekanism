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
	public BlockPos mainPos = BlockPos.ORIGIN;
	
	public boolean receivedCoords;

	public int prevPower;

	public void setMainLocation(BlockPos pos)
	{
		receivedCoords = true;
		
		if(!worldObj.isRemote)
		{
			mainPos = pos;

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

	public void onNeighborChange(Block block)
	{
		TileEntity tile = worldObj.getTileEntity(mainPos);

		if(tile instanceof TileEntityBasicBlock)
		{
			TileEntityBasicBlock tileEntity = (TileEntityBasicBlock)tile;

			int power = worldObj.isBlockIndirectlyGettingPowered(getPos());

			if(prevPower != power)
			{
				if(power > 0)
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
		mainPos = new BlockPos(dataStream.readInt(), dataStream.readInt(), dataStream.readInt());
		prevPower = dataStream.readInt();
	}

	@Override
	public void readFromNBT(NBTTagCompound nbtTags)
	{
		super.readFromNBT(nbtTags);

		mainPos = new BlockPos(nbtTags.getInteger("mainX"), nbtTags.getInteger("mainY"), nbtTags.getInteger("mainZ"));
		prevPower = nbtTags.getInteger("prevPower");
		receivedCoords = nbtTags.getBoolean("receivedCoords");
	}

	@Override
	public void writeToNBT(NBTTagCompound nbtTags)
	{
		super.writeToNBT(nbtTags);

		nbtTags.setInteger("mainX", mainPos.getX());
		nbtTags.setInteger("mainY", mainPos.getY());
		nbtTags.setInteger("mainZ", mainPos.getZ());
		nbtTags.setInteger("prevPower", prevPower);
		nbtTags.setBoolean("receivedCoords", receivedCoords);
	}

	@Override
	public ArrayList getNetworkedData(ArrayList data)
	{
		data.add(mainPos.getX());
		data.add(mainPos.getY());
		data.add(mainPos.getZ());
		data.add(prevPower);

		return data;
	}
}
