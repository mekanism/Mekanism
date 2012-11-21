package mekanism.common;

import universalelectricity.core.UniversalElectricity;
import universalelectricity.core.electricity.ElectricInfo;
import universalelectricity.core.implement.IElectricityReceiver;
import universalelectricity.core.implement.IJouleStorage;
import buildcraft.api.power.IPowerProvider;
import buildcraft.api.power.IPowerReceptor;
import buildcraft.api.power.PowerFramework;

import com.google.common.io.ByteArrayDataInput;

import cpw.mods.fml.client.FMLClientHandler;
import cpw.mods.fml.common.Loader;
import cpw.mods.fml.common.Side;
import cpw.mods.fml.common.asm.SideOnly;
import dan200.computer.api.IComputerAccess;
import dan200.computer.api.IPeripheral;

import ic2.api.Direction;
import ic2.api.EnergyNet;
import ic2.api.IEnergySink;
import ic2.api.IWrenchable;
import mekanism.api.IElectricMachine;
import mekanism.api.IEnergyAcceptor;
import mekanism.client.Sound;
import net.minecraft.src.*;
import net.minecraftforge.common.ForgeDirection;
import net.minecraftforge.common.ISidedInventory;

public abstract class TileEntityBasicMachine extends TileEntityElectricBlock implements IElectricMachine, IEnergySink, IJouleStorage, IElectricityReceiver, IEnergyAcceptor, IPeripheral
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
	
	/** Whether or not this machine has been registered with the MachineryManager. */
	public boolean registered;
	
	/** The GUI texture path for this machine. */
	public String guiTexturePath;
	
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
	}
	
	@Override
	public void onUpdate()
	{
		if(!registered && worldObj != null && !worldObj.isRemote)
		{
			Mekanism.manager.register(this);
			registered = true;
		}
		
		if(worldObj.isRemote)
		{
			handleSound();
		}
	}
	
	@SideOnly(Side.CLIENT)
	public void handleSound()
	{
		if(audio == null && worldObj != null && worldObj.isRemote)
		{
			if(FMLClientHandler.instance().getClient().sndManager.sndSystem != null)
			{
				audio = Mekanism.audioHandler.getSound(fullName.replace(" ", ""), soundURL, worldObj, xCoord, yCoord, zCoord);
			}
		}
		
		if(worldObj != null && worldObj.isRemote && audio != null)
		{
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
	
	@Override
    public void readFromNBT(NBTTagCompound nbtTags)
    {
        super.readFromNBT(nbtTags);

        operatingTicks = nbtTags.getInteger("operatingTicks");
        isActive = nbtTags.getBoolean("isActive");
    }

	@Override
    public void writeToNBT(NBTTagCompound nbtTags)
    {
        super.writeToNBT(nbtTags);
        
        nbtTags.setInteger("operatingTicks", operatingTicks);
        nbtTags.setBoolean("isActive", isActive);
    }
	
	@Override
	public void invalidate()
	{
		super.invalidate();
		if(!worldObj.isRemote && registered)
		{
			Mekanism.manager.remove(this);
			registered = false;
		}
		
		if(worldObj.isRemote && audio != null)
		{
			audio.remove();
		}
	}
	
	@Override
	public boolean demandsEnergy() 
	{
		return energyStored < currentMaxEnergy;
	}

	@Override
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
	
	@Override
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
	
	@Override
	public double getMaxJoules(Object... data) 
	{
		return currentMaxEnergy*UniversalElectricity.IC2_RATIO;
	}
	
	@Override
	public double getJoules(Object... data) 
	{
		return energyStored*UniversalElectricity.IC2_RATIO;
	}

	@Override
	public void setJoules(double joules, Object... data) 
	{
		setEnergy((int)(joules*UniversalElectricity.TO_IC2_RATIO));
	}
	
	@Override
	public boolean canConnect(ForgeDirection side) 
	{
		return true;
	}
	
	@Override
	public boolean canReceiveFromSide(ForgeDirection side) 
	{
		return true;
	}
	
	@Override
	public double getVoltage() 
	{
		return 120;
	}
	
	@Override
	public double wattRequest() 
	{
		return isActive ? ElectricInfo.getWatts((ENERGY_PER_TICK*4)*UniversalElectricity.IC2_RATIO) : 0;
	}
	
	@Override
	public void onReceive(TileEntity sender, double amps, double voltage, ForgeDirection side) 
	{
		int energyToReceive = (int)(ElectricInfo.getJoules(amps, voltage)*UniversalElectricity.TO_IC2_RATIO);
		int energyNeeded = currentMaxEnergy - energyStored;
		int energyToStore = 0;
		
		if(energyToReceive <= energyNeeded)
		{
			energyToStore = energyToReceive;
		}
		else if(energyToReceive > energyNeeded)
		{
			energyToStore = energyNeeded;
		}
		setEnergy(energyStored + energyToStore);
	}
	
	/**
	 * Sets the energy to a new amount.
	 * @param energy - amount to store
	 */
	public void setEnergy(int energy)
	{
		energyStored = Math.max(Math.min(energy, currentMaxEnergy), 0);
	}

	@Override
    public void setActive(boolean active)
    {
    	isActive = active;
    	
    	if(prevActive != active)
    	{
    		sendPacket();
    	}
    	
    	prevActive = active;
    }
    
	@Override
    public String getType()
    {
    	return getInvName();
    }

	@Override
	public boolean canAttachToSide(int side) 
	{
		return true;
	}

	@Override
	public void attach(IComputerAccess computer, String computerSide) {}

	@Override
	public void detach(IComputerAccess computer) {}
    
	@Override
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
	
	@Override
	public boolean canReceive(ForgeDirection side)
	{
		return true;
	}
}
