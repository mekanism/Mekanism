package mekanism.common.tileentity;

import ic2.api.energy.event.EnergyTileLoadEvent;
import ic2.api.energy.event.EnergyTileUnloadEvent;
import ic2.api.energy.tile.IEnergySink;
import ic2.api.energy.tile.IEnergyTile;
import ic2.api.tile.IEnergyStorage;

import java.util.ArrayList;
import java.util.EnumSet;

import mekanism.api.Coord4D;
import mekanism.api.energy.ICableOutputter;
import mekanism.api.energy.IStrictEnergyAcceptor;
import mekanism.api.energy.IStrictEnergyStorage;
import mekanism.api.transmitters.IGridTransmitter;
import mekanism.common.ITileNetwork;
import mekanism.common.Mekanism;
import mekanism.common.util.MekanismUtils;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeDirection;
import net.minecraftforge.common.MinecraftForge;
import buildcraft.api.power.IPowerEmitter;
import buildcraft.api.power.IPowerReceptor;
import buildcraft.api.power.PowerHandler;
import buildcraft.api.power.PowerHandler.PowerReceiver;
import cofh.api.energy.IEnergyHandler;

import com.google.common.io.ByteArrayDataInput;

public abstract class TileEntityElectricBlock extends TileEntityContainerBlock implements ITileNetwork, IPowerEmitter, IPowerReceptor, IEnergyTile, IStrictEnergyStorage, IEnergyHandler, IEnergySink, IEnergyStorage, IStrictEnergyAcceptor, ICableOutputter
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
		
		powerHandler = new PowerHandler(this, PowerHandler.Type.STORAGE);
		powerHandler.configurePowerPerdition(0, 0);
		powerHandler.configure(0, 0, 0, 0);
	}
	
	public void register()
	{
		if(!worldObj.isRemote)
		{
			if(!Mekanism.ic2Registered.contains(Coord4D.get(this)))
			{
				Mekanism.ic2Registered.add(Coord4D.get(this));
				MinecraftForge.EVENT_BUS.post(new EnergyTileLoadEvent(this));
			}
		}
	}
	
	@Override
	public void onUpdate()
	{
		reconfigure();
	}
	
	public EnumSet<ForgeDirection> getOutputtingSides()
	{
		return EnumSet.noneOf(ForgeDirection.class);
	}

	protected EnumSet<ForgeDirection> getConsumingSides()
	{
		EnumSet set = EnumSet.allOf(ForgeDirection.class);
		set.remove(ForgeDirection.UNKNOWN);
		return set;
	}
	
	public double getMaxOutput()
	{
		return 0;
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
		MekanismUtils.saveChunk(this);
	}
	
	@Override
	public double getMaxEnergy()
	{
		return MAX_ELECTRICITY;
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
	public void onChunkUnload()
	{
		if(!worldObj.isRemote)
		{
			Mekanism.ic2Registered.remove(Coord4D.get(this));
			MinecraftForge.EVENT_BUS.post(new EnergyTileUnloadEvent(this));
		}
		
		super.onChunkUnload();
	}
    
	@Override
    public void readFromNBT(NBTTagCompound nbtTags)
    {
        super.readFromNBT(nbtTags);

        electricityStored = nbtTags.getDouble("electricityStored");
        reconfigure();
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
		if(getConsumingSides().contains(side))
		{
			return powerHandler.getPowerReceiver();
		}
		
		return null;
	}
	
	protected void reconfigure()
	{
		if(MekanismUtils.useBuildCraft())
		{
			powerHandler.configure(1, (float)((getMaxEnergy()-getEnergy())*Mekanism.TO_BC), 0, (float)(getMaxEnergy()*Mekanism.TO_BC));
		}
	}
	
	@Override
	public void doWork(PowerHandler workProvider) 
	{
		if(powerHandler.getEnergyStored() > 0)
		{
			if(getEnergy() < getMaxEnergy())
			{
				setEnergy(getEnergy() + powerHandler.useEnergy(0, (float)((getMaxEnergy()-getEnergy())*Mekanism.TO_BC), true)*Mekanism.FROM_BC);
			}
			
			powerHandler.setEnergy(0);
		}
		
		reconfigure();
	}
	
	@Override
	public World getWorld()
	{
		return worldObj;
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
	
	@Override
	public int receiveEnergy(ForgeDirection from, int maxReceive, boolean simulate)
	{
		if(getConsumingSides().contains(from))
		{
			double toAdd = (int)Math.min(getMaxEnergy()-getEnergy(), maxReceive*Mekanism.FROM_TE);
			
			if(!simulate)
			{
				setEnergy(getEnergy() + toAdd);
			}
			
			return (int)Math.round(toAdd*Mekanism.TO_TE);
		}
		
		return 0;
	}

	@Override
	public int extractEnergy(ForgeDirection from, int maxExtract, boolean simulate)
	{
		if(getOutputtingSides().contains(from))
		{
			double toSend = Math.min(getEnergy(), Math.min(getMaxOutput(), maxExtract*Mekanism.FROM_TE));
			
			if(!simulate)
			{
				setEnergy(getEnergy() - toSend);
			}
			
			return (int)Math.round(toSend*Mekanism.TO_TE);
		}
		
		return 0;
	}

	@Override
	public boolean canInterface(ForgeDirection from) 
	{
		return getConsumingSides().contains(from) || getOutputtingSides().contains(from);
	}

	@Override
	public int getEnergyStored(ForgeDirection from)
	{
		return (int)Math.round(getEnergy()*Mekanism.TO_TE);
	}

	@Override
	public int getMaxEnergyStored(ForgeDirection from) 
	{
		return (int)Math.round(getMaxEnergy()*Mekanism.TO_TE);
	}
	
	@Override
	public int getMaxSafeInput()
	{
		return Integer.MAX_VALUE;
	}

	@Override
	public void setStored(int energy)
	{
		setEnergy(energy*Mekanism.FROM_IC2);
	}

	@Override
	public int addEnergy(int amount)
	{
		setEnergy(getEnergy() + amount*Mekanism.FROM_IC2);
		return (int)Math.round(getEnergy()*Mekanism.TO_IC2);
	}

	@Override
	public boolean isTeleporterCompatible(ForgeDirection side) 
	{
		return getOutputtingSides().contains(side);
	}
	
	@Override
	public boolean canOutputTo(ForgeDirection side)
	{
		return getOutputtingSides().contains(side);
	}
	
	@Override
	public boolean acceptsEnergyFrom(TileEntity emitter, ForgeDirection direction)
	{
		return !getOutputtingSides().contains(direction);
	}

	@Override
	public int getStored() 
	{
		return (int)Math.round(getEnergy()*Mekanism.TO_IC2);
	}

	@Override
	public int getCapacity() 
	{
		return (int)Math.round(getMaxEnergy()*Mekanism.TO_IC2);
	}

	@Override
	public int getOutput() 
	{
		return (int)Math.round(getMaxOutput()*Mekanism.TO_IC2);
	}

	@Override
	public double demandedEnergyUnits() 
	{
		return (getMaxEnergy() - getEnergy())*Mekanism.TO_IC2;
	}
	
	@Override
	public boolean canReceiveEnergy(ForgeDirection side)
	{
		return getConsumingSides().contains(side);
	}

	@Override
	public double getOutputEnergyUnitsPerTick()
	{
		return getMaxOutput()*Mekanism.TO_IC2;
	}

	@Override
    public double injectEnergyUnits(ForgeDirection direction, double i)
    {
		if(Coord4D.get(this).getFromSide(direction).getTileEntity(worldObj) instanceof IGridTransmitter)
		{
			return i;
		}
    	
    	return transferEnergyToAcceptor(direction, i*Mekanism.FROM_IC2)*Mekanism.TO_IC2;
    }
	
	@Override
	public double transferEnergyToAcceptor(ForgeDirection side, double amount)
	{
		if(!getConsumingSides().contains(side))
		{
			return amount;
		}
		
    	double toUse = Math.min(getMaxEnergy()-getEnergy(), amount);
    	setEnergy(getEnergy() + toUse);
    	
    	return amount-toUse;
	}
	
	@Override
	public boolean canEmitPowerFrom(ForgeDirection side) 
	{
		return getOutputtingSides().contains(side);
	}
}
