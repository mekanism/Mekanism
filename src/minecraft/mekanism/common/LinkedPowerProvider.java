package mekanism.common;

import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.ForgeDirection;
import buildcraft.api.power.IPowerReceptor;
import buildcraft.api.power.PowerProvider;

public class LinkedPowerProvider extends PowerProvider
{
	public TileEntity tileEntity;
	
	public LinkedPowerProvider(TileEntity tile)
	{
		tileEntity = tile;
	}

	@Override
	public boolean update(IPowerReceptor receptor)
	{
		TileEntityElectricBlock electricBlock = (TileEntityElectricBlock)tileEntity;
		maxEnergyStored = (int)(electricBlock.getMaxJoules()*Mekanism.TO_BC);
		energyStored = (int)(electricBlock.electricityStored);
		return true;
	}
	
	@Override
	public void receiveEnergy(float quantity, ForgeDirection from) 
	{
		TileEntityElectricBlock electricBlock = (TileEntityElectricBlock)tileEntity;
		electricBlock.setJoules(electricBlock.electricityStored+(quantity*Mekanism.FROM_BC));
	}
}
