package mekanism.common;

import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.ForgeDirection;
import buildcraft.api.power.IPowerReceptor;
import buildcraft.api.power.PowerProvider;

public class LinkedPowerProvider extends PowerProvider
{
	public TileEntityElectricBlock tileEntity;
	
	public LinkedPowerProvider(TileEntityElectricBlock tile)
	{
		tileEntity = tile;
	}

	@Override
	public boolean update(IPowerReceptor receptor)
	{
		return true;
	}
	
	@Override
	public float useEnergy(float min, float max, boolean doUse) 
	{
		float result = 0;

		if(tileEntity.electricityStored*Mekanism.TO_BC >= min) 
		{
			if(tileEntity.electricityStored*Mekanism.TO_BC <= max) 
			{
				result = (float)(tileEntity.electricityStored*Mekanism.TO_BC);
				
				if(doUse)
				{
					tileEntity.electricityStored = 0;
				}
			} 
			else {
				result = max;
				
				if(doUse) 
				{
					tileEntity.electricityStored -= max*Mekanism.FROM_BC;
				}
			}
		}

		return result;
	}
	
	@Override
	public void receiveEnergy(float quantity, ForgeDirection from) 
	{
		tileEntity.setEnergy(tileEntity.electricityStored+(quantity*Mekanism.FROM_BC));
	}
}
