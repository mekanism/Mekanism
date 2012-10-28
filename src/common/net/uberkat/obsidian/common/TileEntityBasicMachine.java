package net.uberkat.obsidian.common;

import obsidian.api.IElectricMachine;
import universalelectricity.core.UniversalElectricity;
import universalelectricity.electricity.ElectricInfo;
import universalelectricity.electricity.ElectricityManager;
import universalelectricity.prefab.TileEntityDisableable;

import buildcraft.api.power.IPowerProvider;
import buildcraft.api.power.PowerFramework;

import com.google.common.io.ByteArrayDataInput;

import cpw.mods.fml.client.FMLClientHandler;
import cpw.mods.fml.common.Loader;
import cpw.mods.fml.common.Side;
import cpw.mods.fml.common.asm.SideOnly;
import dan200.computer.api.IComputerAccess;

import ic2.api.Direction;
import ic2.api.EnergyNet;
import ic2.api.IWrenchable;
import net.minecraft.src.*;
import net.minecraftforge.common.ForgeDirection;
import net.minecraftforge.common.ISidedInventory;
import net.uberkat.obsidian.client.Sound;

public abstract class TileEntityBasicMachine extends TileEntityElectricBlock implements IElectricMachine
{
	/** The Sound instance for this machine. */
	public Sound audio;
	
	/** The bundled URL of this machine's sound effect */
	public String soundURL;
	
	/** How much energy this machine uses per tick. */
	public int ENERGY_PER_TICK;
	
	/** How many ticks this machine has operated for. */
	public int operatingTicks = 0;
	
	/** Ticks required to operate -- or smelt an item. */
	public int TICKS_REQUIRED;
	
	/** The current tick requirement for this machine. */
	public int currentTicksRequired;
	
	/** The current energy capacity for this machine. */
	public int currentMaxEnergy;
	
	/** Whether or not this block is in it's active state. */
	public boolean isActive;
	
	/** The previous active state for this block. */
	public boolean prevActive;
	
	/** The GUI texture path for this machine. */
	public String guiTexturePath;
	
	/** BuildCraft power provider. */
	public IPowerProvider powerProvider;
	
	/**
	 * The foundation of all machines - a simple tile entity with a facing, active state, initialized state, sound effect, and animated texture.
	 * @param soundPath - location of the sound effect
	 * @param name - full name of this machine
	 * @param path - GUI texture path of this machine
	 * @param perTick - the energy this machine consumes every tick in it's active state
	 * @param ticksRequired - how many ticks it takes to run a cycle
	 * @param maxEnergy - how much energy this machine can store
	 */
	public TileEntityBasicMachine(String soundPath, String name, String path, int perTick, int ticksRequired, int maxEnergy)
	{
		super(name, maxEnergy);
		ENERGY_PER_TICK = perTick;
		TICKS_REQUIRED = currentTicksRequired = ticksRequired;
		soundURL = soundPath;
		guiTexturePath = path;
		isActive = false;
		if(PowerFramework.currentFramework != null)
		{
			powerProvider = PowerFramework.currentFramework.createPowerProvider();
			powerProvider.configure(20, 25, 25, 25, maxEnergy/10);
		}
	}
	
	public void onUpdate()
	{
		if(audio == null && worldObj.isRemote)
		{
			audio = ObsidianIngots.audioHandler.getSound(fullName.replace(" ", ""), soundURL, worldObj, xCoord, yCoord, zCoord);
		}
		
		if(worldObj.isRemote)
		{
			audio.updateVolume(FMLClientHandler.instance().getClient().thePlayer);
			if(!audio.isPlaying && isActive == true)
			{
				audio.play();
			}
			else if(audio.isPlaying && isActive == false)
			{
				audio.stop();
			}
		}
	}
	
	public void invalidate()
	{
		super.invalidate();
		if(worldObj.isRemote && audio != null)
		{
			audio.remove();
		}
	}
	
	public boolean demandsEnergy() 
	{
		return energyStored < currentMaxEnergy;
	}

    public int injectEnergy(Direction direction, int i)
    {
    	int rejects = 0;
    	int neededEnergy = currentMaxEnergy-energyStored;
    	if(i <= neededEnergy)
    	{
    		energyStored += i;
    	}
    	else if(i > neededEnergy)
    	{
    		energyStored += neededEnergy;
    		rejects = i-neededEnergy;
    	}
    	
    	return rejects;
    }
	
	public void setPowerProvider(IPowerProvider provider)
	{
		powerProvider = provider;
	}
	
	public IPowerProvider getPowerProvider() 
	{
		return powerProvider;
	}
	
	public int powerRequest() 
	{
		return getPowerProvider().getMaxEnergyReceived();
	}
	
	public void doWork() {}
	
	public boolean acceptsEnergyFrom(TileEntity emitter, Direction direction)
	{
		return true;
	}
	
	/**
	 * Gets the scaled energy level for the GUI.
	 * @param i - multiplier
	 * @return scaled energy
	 */
	public int getScaledEnergyLevel(int i)
	{
		return energyStored*i / currentMaxEnergy;
	}

	/**
	 * Gets the scaled progress level for the GUI.
	 * @param i - multiplier
	 * @return
	 */
	public int getScaledProgress(int i)
	{
		return operatingTicks*i / currentTicksRequired;
	}
	
	public double getMaxJoules() 
	{
		return currentMaxEnergy*UniversalElectricity.IC2_RATIO;
	}
	
	public double getJoules(Object... data) 
	{
		return energyStored*UniversalElectricity.IC2_RATIO;
	}

	public void setJoules(double joules, Object... data) 
	{
		setEnergy((int)(joules*UniversalElectricity.TO_IC2_RATIO));
	}
	
	public boolean canConnect(ForgeDirection side) 
	{
		return true;
	}
	
	public boolean canReceiveFromSide(ForgeDirection side) 
	{
		return true;
	}
	
	public double getVoltage() 
	{
		return 120;
	}
	
	public double wattRequest() 
	{
		return ElectricInfo.getWatts(currentMaxEnergy*UniversalElectricity.IC2_RATIO) - ElectricInfo.getWatts(energyStored*UniversalElectricity.IC2_RATIO);
	}
	
	public void onReceive(TileEntity sender, double amps, double voltage, ForgeDirection side) 
	{
		setEnergy(energyStored + (int)(ElectricInfo.getJoules(amps, voltage)*UniversalElectricity.TO_IC2_RATIO));
	}
	
	/**
	 * Sets the energy to a new amount.
	 * @param energy - amount to store
	 */
	public void setEnergy(int energy)
	{
		energyStored = Math.max(Math.min(energy, currentMaxEnergy), 0);
	}

    public void setActive(boolean active)
    {
    	isActive = active;
    	
    	if(prevActive != active)
    	{
    		sendPacket();
    	}
    	
    	prevActive = active;
    }
    
    public String getType()
    {
    	return getInvName();
    }

	public boolean canAttachToSide(int side) 
	{
		return true;
	}

	public void attach(IComputerAccess computer, String computerSide) {}

	public void detach(IComputerAccess computer) {}
    
	public int transferToAcceptor(int amount)
	{
    	int rejects = 0;
    	int neededEnergy = currentMaxEnergy-energyStored;
    	if(amount <= neededEnergy)
    	{
    		energyStored += amount;
    	}
    	else if(amount > neededEnergy)
    	{
    		energyStored += neededEnergy;
    		rejects = amount-neededEnergy;
    	}
    	
    	return rejects;
	}
	
	public boolean canReceive(ForgeDirection side)
	{
		return true;
	}
}
