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
	
	public String getInventoryName()
	{
		return MekanismUtils.localize(getBlockType().getUnlocalizedName() + ".InductionCell" + tier.getBaseTier().getName() + ".name");
	}
	
	@Override
	public void handlePacketData(ByteBuf dataStream)
	{
		tier = InductionCellTier.values()[dataStream.readInt()];

		super.handlePacketData(dataStream);
		
		electricityStored = dataStream.readDouble();

		MekanismUtils.updateBlock(worldObj, xCoord, yCoord, zCoord);
	}

	@Override
	public ArrayList getNetworkedData(ArrayList data)
	{
		data.add(tier.ordinal());

		super.getNetworkedData(data);
		
		data.add(electricityStored);

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
		return tier.maxEnergy;
	}
}
