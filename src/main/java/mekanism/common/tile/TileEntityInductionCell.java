package mekanism.common.tile;

import io.netty.buffer.ByteBuf;

import java.util.ArrayList;

import mekanism.common.Tier.InductionCellTier;
import mekanism.common.util.MekanismUtils;
import net.minecraft.nbt.NBTTagCompound;

public class TileEntityInductionCell extends TileEntityBasicBlock
{
	public InductionCellTier tier = InductionCellTier.BASIC;
	
	@Override
	public void onUpdate() {}
	
	@Override
	public boolean canUpdate()
	{
		return false;
	}
	
	@Override
	public void handlePacketData(ByteBuf dataStream)
	{
		tier = InductionCellTier.values()[dataStream.readInt()];

		super.handlePacketData(dataStream);

		MekanismUtils.updateBlock(worldObj, xCoord, yCoord, zCoord);
	}

	@Override
	public ArrayList getNetworkedData(ArrayList data)
	{
		data.add(tier.ordinal());

		super.getNetworkedData(data);

		return data;
	}

	@Override
	public void readFromNBT(NBTTagCompound nbtTags)
	{
		super.readFromNBT(nbtTags);

		tier = InductionCellTier.values()[nbtTags.getInteger("tier")];
	}

	@Override
	public void writeToNBT(NBTTagCompound nbtTags)
	{
		super.writeToNBT(nbtTags);

		nbtTags.setInteger("tier", tier.ordinal());
	}
}
