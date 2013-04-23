package mekanism.common;

import ic2.api.IWrenchable;
import ic2.api.energy.event.EnergyTileLoadEvent;
import ic2.api.energy.event.EnergyTileUnloadEvent;
import ic2.api.energy.tile.IEnergyTile;

import java.util.ArrayList;
import java.util.EnumSet;

import mekanism.api.IStrictEnergyStorage;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.ForgeDirection;
import net.minecraftforge.common.MinecraftForge;
import universalelectricity.core.block.IConnector;
import universalelectricity.core.block.IElectricityStorage;
import universalelectricity.core.block.IVoltage;
import universalelectricity.core.electricity.ElectricityNetworkHelper;
import universalelectricity.core.electricity.ElectricityPack;
import buildcraft.api.power.IPowerProvider;
import buildcraft.api.power.IPowerReceptor;
import buildcraft.api.power.PowerFramework;

import com.google.common.io.ByteArrayDataInput;

public abstract class TileEntityElectricBlock extends TileEntityContainerBlock implements IWrenchable, ITileNetwork, IPowerReceptor, IEnergyTile, IElectricityStorage, IVoltage, IConnector, IStrictEnergyStorage
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
	public TileEntityElectricBlock(String name, double maxEnergy)
	{
		super(name);
		MAX_ELECTRICITY = maxEnergy;
		
		if(PowerFramework.currentFramework != null)
		{
			powerProvider = new LinkedPowerProvider(this);
			powerProvider.configure(0, 0, 100, 0, (int)(maxEnergy*Mekanism.TO_BC));
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
		
		if(!worldObj.isRemote)
		{
			ElectricityPack electricityPack = ElectricityNetworkHelper.consumeFromMultipleSides(this, getConsumingSides(), getRequest());
			setEnergy(getEnergy()+electricityPack.getWatts());
		}
	}

	protected EnumSet<ForgeDirection> getConsumingSides()
	{
		return EnumSet.allOf(ForgeDirection.class);
	}
	
	@Override
	public boolean canConnect(ForgeDirection direction)
	{
		return true;
	}
	
	public ElectricityPack getRequest()
	{
		return new ElectricityPack((getMaxEnergy() - getEnergy()) / getVoltage(), getVoltage());
	}
	
	@Override
	public double getEnergy()
	{
		return electricityStored;
	}
	
	@Override
	public void setEnergy(double energy)
	{
		electricityStored = Math.max(Math.min(energy, getMaxEnergy()), 0);
	}
	
	@Override
	public double getMaxEnergy()
	{
		return MAX_ELECTRICITY;
	}
	
	@Override
	public double getMaxJoules() 
	{
		return getMaxEnergy();
	}
	
	@Override
	public double getJoules() 
	{
		return getEnergy();
	}

	@Override
	public void setJoules(double joules)
	{
		setEnergy(joules);
	}
	
	@Override
	public double getVoltage()
	{
		return 120;
	}
	
	@Override
	public void handlePacketData(ByteArrayDataInput dataStream)
	{
		super.handlePacketData(dataStream);
		electricityStored = dataStream.readDouble();
	}
	
	@Override
	public ArrayList getNetworkedData(ArrayList data)
	{
		super.getNetworkedData(data);
		data.add(electricityStored);
		return data;
	}
	
	@Override
	public void invalidate()
	{
		ElectricityNetworkHelper.invalidate(this);
		
		if(initialized)
		{
			if(Mekanism.hooks.IC2Loaded)
			{
				MinecraftForge.EVENT_BUS.post(new EnergyTileUnloadEvent(this));
			}
		}
		
		super.invalidate();
	}
    
	@Override
    public void readFromNBT(NBTTagCompound nbtTags)
    {
        super.readFromNBT(nbtTags);

        electricityStored = nbtTags.getDouble("electricityStored");
    }

	@Override
    public void writeToNBT(NBTTagCompound nbtTags)
    {
        super.writeToNBT(nbtTags);
        
        nbtTags.setDouble("electricityStored", electricityStored);
    }
	
	@Override
	public boolean isAddedToEnergyNet()
	{
		return initialized;
	}
	
	@Override
	public void setPowerProvider(IPowerProvider provider) {}
	
	@Override
	public IPowerProvider getPowerProvider() 
	{
		return powerProvider;
	}
	
	@Override
	public int powerRequest(ForgeDirection side) 
	{
		return (int)Math.min(((MAX_ELECTRICITY-electricityStored)*Mekanism.TO_BC), 100);
	}
	
	@Override
	public void doWork() {}
}
