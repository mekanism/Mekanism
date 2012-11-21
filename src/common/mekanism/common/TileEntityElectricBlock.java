package mekanism.common;

import com.google.common.io.ByteArrayDataInput;

import buildcraft.api.power.IPowerProvider;
import buildcraft.api.power.IPowerReceptor;
import buildcraft.api.power.PowerFramework;
import universalelectricity.prefab.TileEntityDisableable;
import ic2.api.EnergyNet;
import ic2.api.IWrenchable;
import mekanism.api.ITileNetwork;
import net.minecraft.src.*;
import net.minecraftforge.common.ForgeDirection;
import net.minecraftforge.common.ISidedInventory;

public abstract class TileEntityElectricBlock extends TileEntityContainerBlock implements IWrenchable, ISidedInventory, IInventory, ITileNetwork, IPowerReceptor
{
	/** How much energy is stored in this block. */
	public int energyStored;
	
	/** Maximum amount of energy this machine can hold. */
	public int MAX_ENERGY;
	
	/** BuildCraft power provider. */
	public IPowerProvider powerProvider;
	
	/**
	 * The base of all blocks that deal with electricity. It has a facing state, initialized state,
	 * and a current amount of stored energy.
	 * @param name - full name of this block
	 * @param maxEnergy - how much energy this block can store
	 */
	public TileEntityElectricBlock(String name, int maxEnergy)
	{
		super(name);
		MAX_ENERGY = maxEnergy;
		if(PowerFramework.currentFramework != null)
		{
			powerProvider = PowerFramework.currentFramework.createPowerProvider();
			powerProvider.configure(5, 2, 10, 1, maxEnergy/10);
		}
	}
    
	@Override
    public void readFromNBT(NBTTagCompound nbtTags)
    {
        super.readFromNBT(nbtTags);
        
        if(PowerFramework.currentFramework != null)
        {
        	PowerFramework.currentFramework.loadPowerProvider(this, nbtTags);
        }

        energyStored = nbtTags.getInteger("energyStored");
    }

	@Override
    public void writeToNBT(NBTTagCompound nbtTags)
    {
        super.writeToNBT(nbtTags);
        
        if(PowerFramework.currentFramework != null)
        {
        	PowerFramework.currentFramework.savePowerProvider(this, nbtTags);
        }
        
        nbtTags.setInteger("energyStored", energyStored);
    }
	
	public boolean isAddedToEnergyNet()
	{
		return initialized;
	}
	
	@Override
	public void setPowerProvider(IPowerProvider provider)
	{
		powerProvider = provider;
	}
	
	@Override
	public IPowerProvider getPowerProvider() 
	{
		return powerProvider;
	}
	
	@Override
	public int powerRequest() 
	{
		return getPowerProvider().getMaxEnergyReceived();
	}
	
	@Override
	public void doWork() {}
}
