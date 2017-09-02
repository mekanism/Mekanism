package mekanism.common.integration.forgeenergy;

import mekanism.common.base.IEnergyWrapper;
import mekanism.common.config.MekanismConfig.general;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.energy.IEnergyStorage;

public class ForgeEnergyIntegration implements IEnergyStorage
{
	public IEnergyWrapper tileEntity;
	
	public EnumFacing side;
	
	public ForgeEnergyIntegration(IEnergyWrapper tile, EnumFacing facing)
	{
		tileEntity = tile;
		side = facing;
	}
	
	@Override
	public int receiveEnergy(int maxReceive, boolean simulate) 
	{
		return rfToForge(tileEntity.receiveEnergy(side, forgeToRF(maxReceive), simulate));
	}

	@Override
	public int extractEnergy(int maxExtract, boolean simulate) 
	{
		return rfToForge(tileEntity.extractEnergy(side, forgeToRF(maxExtract), simulate));
	}

	@Override
	public int getEnergyStored() 
	{
		return (int)Math.round(Math.min(Integer.MAX_VALUE, tileEntity.getEnergy()*general.TO_FORGE));
	}

	@Override
	public int getMaxEnergyStored()
	{
		return (int)Math.round(Math.min(Integer.MAX_VALUE, tileEntity.getMaxEnergy()*general.TO_FORGE));
	}

	@Override
	public boolean canExtract() 
	{
		return tileEntity.sideIsOutput(side);
	}

	@Override
	public boolean canReceive() 
	{
		return tileEntity.sideIsConsumer(side);
	}
	
	public static int rfToForge(int rf)
	{
		return (int)Math.round(rf*general.FROM_RF*general.TO_FORGE);
	}
	
	public static int forgeToRF(int forge)
	{
		return (int)Math.round(forge*general.FROM_FORGE*general.TO_RF);
	}
}
