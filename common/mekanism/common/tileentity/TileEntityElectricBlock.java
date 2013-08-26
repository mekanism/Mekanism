package mekanism.common.tileentity;

import ic2.api.energy.event.EnergyTileLoadEvent;
import ic2.api.energy.event.EnergyTileUnloadEvent;
import ic2.api.energy.tile.IEnergyTile;
import ic2.api.tile.IWrenchable;

import java.util.ArrayList;
import java.util.EnumSet;

import mekanism.api.energy.IStrictEnergyStorage;
import mekanism.common.ITileNetwork;
import mekanism.common.Mekanism;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeDirection;
import net.minecraftforge.common.MinecraftForge;
import universalelectricity.core.block.IConnector;
import universalelectricity.core.block.IElectrical;
import universalelectricity.core.block.IElectricalStorage;
import universalelectricity.core.electricity.ElectricityPack;
import buildcraft.api.power.IPowerReceptor;
import buildcraft.api.power.PowerHandler;
import buildcraft.api.power.PowerHandler.PowerReceiver;

import com.google.common.io.ByteArrayDataInput;

public abstract class TileEntityElectricBlock extends TileEntityContainerBlock implements IWrenchable, ITileNetwork, IPowerReceptor, IEnergyTile, IElectrical, IElectricalStorage, IConnector, IStrictEnergyStorage
{
	/** How much energy is stored in this block. */
	public double electricityStored;
	
	/** Maximum amount of energy this machine can hold. */
	public double MAX_ELECTRICITY;
	
	/** BuildCraft power handler. */
	public PowerHandler powerHandler;
	
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
		
		powerHandler = new PowerHandler(this, PowerHandler.Type.MACHINE);
		powerHandler.configure(0, 100, 0, (int)(maxEnergy*Mekanism.TO_BC));
	}
	
	@Override
	public void onUpdate()
	{
		if(!worldObj.isRemote)
		{
			if(!initialized)
			{
				if(Mekanism.hooks.IC2Loaded)
				{
					MinecraftForge.EVENT_BUS.post(new EnergyTileLoadEvent(this));
				}
				
				initialized = true;
			}
			
			if(getEnergy() < getMaxEnergy() && powerHandler.getEnergyStored() > 0)
			{
				setEnergy(getEnergy() + powerHandler.useEnergy(0, (float)((getMaxEnergy()-getEnergy())*Mekanism.TO_BC), true)*Mekanism.FROM_BC);
			}
		}
	}

	protected EnumSet<ForgeDirection> getConsumingSides()
	{
		return EnumSet.allOf(ForgeDirection.class);
	}
	
	protected EnumSet<ForgeDirection> getOutputtingSides()
	{
		return EnumSet.noneOf(ForgeDirection.class);
	}
	
	@Override
	public boolean canConnect(ForgeDirection direction)
	{
		return getConsumingSides().contains(direction) || getOutputtingSides().contains(direction);
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
	public float getVoltage()
	{
		return 120;
	}
	
	@Override
	public void handlePacketData(ByteArrayDataInput dataStream)
	{
		super.handlePacketData(dataStream);
		setEnergy(dataStream.readDouble());
	}
	
	@Override
	public ArrayList getNetworkedData(ArrayList data)
	{
		super.getNetworkedData(data);
		data.add(getEnergy());
		return data;
	}
	
	@Override
	public void invalidate()
	{
		if(initialized && !worldObj.isRemote)
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
        
        nbtTags.setDouble("electricityStored", getEnergy());
    }
	
	@Override
	public PowerReceiver getPowerReceiver(ForgeDirection side) 
	{
		return powerHandler.getPowerReceiver();
	}
	
	@Override
	public void doWork(PowerHandler workProvider) {}
	
	@Override
	public World getWorld()
	{
		return worldObj;
	}
	
	@Override
	public float receiveElectricity(ForgeDirection from, ElectricityPack receive, boolean doReceive) 
	{
		if(getConsumingSides().contains(from))
		{
			double toAdd = (float)Math.min(getMaxEnergy()-getEnergy(), receive.getWatts()*Mekanism.FROM_UE);
			
			if(doReceive)
			{
				setEnergy(getEnergy() + toAdd);
			}
			
			return (float)(toAdd*Mekanism.TO_UE);
		}
		
		return 0;
	}

	@Override
	public ElectricityPack provideElectricity(ForgeDirection from, ElectricityPack request, boolean doProvide) 
	{
		return null;
	}

	@Override
	public float getRequest(ForgeDirection direction) 
	{
		if(getConsumingSides().contains(direction))
		{
			return getMaxEnergyStored()-getEnergyStored();
		}
		
		return 0;
	}

	@Override
	public float getProvide(ForgeDirection direction) 
	{
		return 0;
	}
	
	@Override
	public void setEnergyStored(float energy)
	{
		setEnergy(energy*Mekanism.FROM_UE);
	}

	@Override
	public float getEnergyStored() 
	{
		return (float)(getEnergy()*Mekanism.TO_UE);
	}

	@Override
	public float getMaxEnergyStored() 
	{
		return (float)(getMaxEnergy()*Mekanism.TO_UE);
	}
	
	/**
	 * Gets the scaled energy level for the GUI.
	 * @param i - multiplier
	 * @return scaled energy
	 */
	public int getScaledEnergyLevel(int i)
	{
		return (int)(getEnergy()*i / getMaxEnergy());
	}
}
