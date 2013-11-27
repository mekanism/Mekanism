package mekanism.common.tileentity;

import ic2.api.energy.event.EnergyTileLoadEvent;
import ic2.api.energy.event.EnergyTileUnloadEvent;
import ic2.api.energy.tile.IEnergySink;
import ic2.api.energy.tile.IEnergySource;
import ic2.api.energy.tile.IEnergyTile;
import ic2.api.tile.IEnergyStorage;
import ic2.api.tile.IWrenchable;

import java.util.ArrayList;
import java.util.EnumSet;

import mekanism.api.Object3D;
import mekanism.api.energy.ICableOutputter;
import mekanism.api.energy.IStrictEnergyAcceptor;
import mekanism.api.energy.IStrictEnergyStorage;
import mekanism.common.ITileNetwork;
import mekanism.common.Mekanism;
import mekanism.common.util.MekanismUtils;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
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
import cofh.api.energy.IEnergyHandler;

import com.google.common.io.ByteArrayDataInput;

public abstract class TileEntityElectricBlock extends TileEntityContainerBlock implements IWrenchable, ITileNetwork, IPowerReceptor, IEnergyTile, IElectrical, IElectricalStorage, IConnector, IStrictEnergyStorage, IEnergyHandler, IEnergySink, IEnergySource, IEnergyStorage, IStrictEnergyAcceptor, ICableOutputter
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
			if(!Mekanism.ic2Registered.contains(Object3D.get(this)))
			{
				Mekanism.ic2Registered.add(Object3D.get(this));
				MinecraftForge.EVENT_BUS.post(new EnergyTileLoadEvent(this));
			}
		}
	}
	
	@Override
	public void onUpdate()
	{
		reconfigure();
	}
	
	public ForgeDirection getOutputtingSide()
	{
		return null;
	}

	protected EnumSet<ForgeDirection> getConsumingSides()
	{
		return EnumSet.allOf(ForgeDirection.class);
	}
	
	public double getMaxOutput()
	{
		return 0;
	}
	
	@Override
	public boolean canConnect(ForgeDirection direction)
	{
		return getConsumingSides().contains(direction) || getOutputtingSide() == direction;
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
	public float getVoltage()
	{
		return (float)(120*Mekanism.TO_UE);
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
			Mekanism.ic2Registered.remove(Object3D.get(this));
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
		return powerHandler.getPowerReceiver();
	}
	
	protected void reconfigure()
	{
		if(MekanismUtils.useBuildcraft())
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
	
	@Override
	public float receiveElectricity(ForgeDirection from, ElectricityPack receive, boolean doReceive) 
	{
		if(getConsumingSides().contains(from))
		{
			double toAdd = Math.min(getMaxEnergy()-getEnergy(), receive.getWatts()*Mekanism.FROM_UE);
			
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
		if(getOutputtingSide() == from)
		{
			double toSend = Math.min(getEnergy(), Math.min(getMaxOutput(), request.getWatts()*Mekanism.FROM_UE));
			
			if(doProvide)
			{
				setEnergy(getEnergy() - toSend);
			}
			
			return ElectricityPack.getFromWatts((float)(toSend*Mekanism.TO_UE), getVoltage());
		}
		
		return new ElectricityPack();
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
		return getOutputtingSide() == direction ? Math.min(getEnergyStored(), (float)(getMaxOutput()*Mekanism.TO_UE)) : 0;
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
		if(getOutputtingSide() == from)
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
		return canConnect(from);
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
		return 2048;
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
		return side == getOutputtingSide();
	}
	
	@Override
	public boolean canOutputTo(ForgeDirection side)
	{
		return side == getOutputtingSide();
	}
	
	@Override
	public boolean acceptsEnergyFrom(TileEntity emitter, ForgeDirection direction)
	{
		return direction != getOutputtingSide();
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
	public double getOfferedEnergy() 
	{
		return Math.min(getEnergy()*Mekanism.TO_IC2, getOutput());
	}
	
	@Override
	public boolean canReceiveEnergy(ForgeDirection side)
	{
		return side != getOutputtingSide();
	}

	@Override
	public boolean emitsEnergyTo(TileEntity receiver, ForgeDirection direction)
	{
		return direction == getOutputtingSide() && !(receiver instanceof TileEntityUniversalCable);
	}

	@Override
	public double getOutputEnergyUnitsPerTick()
	{
		return getMaxOutput()*Mekanism.TO_IC2;
	}

	@Override
	public void drawEnergy(double amount)
	{
		setEnergy(getEnergy()-amount*Mekanism.FROM_IC2);
	}

	@Override
    public double injectEnergyUnits(ForgeDirection direction, double i)
    {
		if(Object3D.get(this).getFromSide(direction).getTileEntity(worldObj) instanceof TileEntityUniversalCable)
		{
			return i;
		}
		
		if(!getConsumingSides().contains(direction))
		{
			return i;
		}
		
		double givenEnergy = i*Mekanism.FROM_IC2;
    	double rejects = 0;
    	double neededEnergy = getMaxEnergy()-getEnergy();
    	
    	if(givenEnergy <= neededEnergy)
    	{
    		setEnergy(getEnergy() + givenEnergy);
    	}
    	else if(givenEnergy > neededEnergy)
    	{
    		setEnergy(getEnergy() + neededEnergy);
    		rejects = givenEnergy-neededEnergy;
    	}
    	
    	return rejects*Mekanism.TO_IC2;
    }
	
	@Override
	public double transferEnergyToAcceptor(ForgeDirection side, double amount)
	{
		if(!getConsumingSides().contains(side))
		{
			return amount;
		}
		
    	double rejects = 0;
    	double neededElectricity = getMaxEnergy()-getEnergy();
    	
    	if(amount <= neededElectricity)
    	{
    		setEnergy(getEnergy() + amount);
    	}
    	else {
    		setEnergy(getEnergy() + neededElectricity);
    		rejects = amount-neededElectricity;
    	}
    	
    	return rejects;
	}
}
