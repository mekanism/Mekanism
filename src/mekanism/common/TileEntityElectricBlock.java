package mekanism.common;

import ic2.api.IWrenchable;
import ic2.api.energy.event.EnergyTileLoadEvent;
import ic2.api.energy.event.EnergyTileUnloadEvent;
import ic2.api.energy.tile.IEnergyTile;
import mekanism.api.ITileNetwork;
import net.minecraft.inventory.IInventory;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.ISidedInventory;
import net.minecraftforge.common.MinecraftForge;
import buildcraft.api.power.IPowerProvider;
import buildcraft.api.power.IPowerReceptor;
import buildcraft.api.power.PowerFramework;

public abstract class TileEntityElectricBlock extends TileEntityContainerBlock implements IWrenchable, ISidedInventory, IInventory, ITileNetwork, IPowerReceptor, IEnergyTile
{
	/** How much energy is stored in this block. */
	public double electricityStored;
	
	/** Maximum amount of energy this machine can hold. */
	public double MAX_ELECTRICITY;
	
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
		MAX_ELECTRICITY = maxEnergy;
		
		if(PowerFramework.currentFramework != null)
		{
			powerProvider = PowerFramework.currentFramework.createPowerProvider();
			powerProvider.configure(5, 2, 10, 1, maxEnergy/10);
		}
	}
	
	@Override
	public void onUpdate()
	{
		if(!initialized && worldObj != null)
		{
			if(Mekanism.hooks.IC2Loaded)
			{
				MinecraftForge.EVENT_BUS.post(new EnergyTileLoadEvent(this));
			}
			
			initialized = true;
		}
	}
	
	@Override
	public void invalidate()
	{
		super.invalidate();
		
		if(initialized)
		{
			if(Mekanism.hooks.IC2Loaded)
			{
				MinecraftForge.EVENT_BUS.post(new EnergyTileUnloadEvent(this));
			}
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

        electricityStored = nbtTags.getDouble("electricityStored");
    }

	@Override
    public void writeToNBT(NBTTagCompound nbtTags)
    {
        super.writeToNBT(nbtTags);
        
        if(PowerFramework.currentFramework != null)
        {
        	PowerFramework.currentFramework.savePowerProvider(this, nbtTags);
        }
        
        nbtTags.setDouble("electricityStored", electricityStored);
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
