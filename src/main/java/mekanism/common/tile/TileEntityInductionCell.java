package mekanism.common.tile;

import io.netty.buffer.ByteBuf;

import java.util.ArrayList;

import mekanism.api.energy.IStrictEnergyStorage;
import mekanism.common.Tier.InductionCellTier;
import mekanism.common.util.MekanismUtils;
import net.minecraft.nbt.NBTTagCompound;

public class TileEntityInductionCell extends TileEntityBasicBlock implements IStrictEnergyStorage
{
	public InductionCellTier tier = InductionCellTier.BASIC;
	
	public double electricityStored;
	
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
		electricityStored = dataStream.readDouble();

		super.handlePacketData(dataStream);

		MekanismUtils.updateBlock(worldObj, xCoord, yCoord, zCoord);
	}

	@Override
	public ArrayList getNetworkedData(ArrayList data)
	{
		data.add(tier.ordinal());
		data.add(electricityStored);

		super.getNetworkedData(data);

		return data;
	}

	@Override
	public void readFromNBT(NBTTagCompound nbtTags)
	{
		super.readFromNBT(nbtTags);

		tier = InductionCellTier.values()[nbtTags.getInteger("tier")];
		electricityStored = nbtTags.getDouble("electricityStored");
	}

	@Override
	public void writeToNBT(NBTTagCompound nbtTags)
	{
		super.writeToNBT(nbtTags);

		nbtTags.setInteger("tier", tier.ordinal());
		nbtTags.setDouble("electricityStored", electricityStored);
	}

	@Override
	public double getEnergy() 
	{
		return electricityStored;
	}

	@Override
	public void setEnergy(double energy) 
	{
		electricityStored = Math.min(energy, getMaxEnergy());
	}

	@Override
	public double getMaxEnergy() 
	{
		return tier.MAX_ELECTRICITY;
	}
}
