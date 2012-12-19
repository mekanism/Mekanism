package mekanism.common;

import java.util.EnumSet;

import universalelectricity.core.electricity.ElectricInfo;
import universalelectricity.core.electricity.ElectricityConnections;
import universalelectricity.core.implement.IConductor;
import universalelectricity.core.implement.IJouleStorage;
import universalelectricity.core.implement.IVoltage;
import universalelectricity.core.vector.Vector3;

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
import ic2.api.energy.EnergyTileSinkEvent;

import mekanism.api.IActiveState;
import mekanism.api.IElectricMachine;
import mekanism.client.Sound;

import net.minecraft.src.*;
import net.minecraftforge.common.ForgeDirection;
import net.minecraftforge.common.ISidedInventory;
import net.minecraftforge.common.MinecraftForge;

public abstract class TileEntityBasicMachine extends TileEntityElectricBlock implements IElectricMachine, IEnergySink, IJouleStorage, IVoltage, IPeripheral, IActiveState
{
	/** The Sound instance for this machine. */
	@SideOnly(Side.CLIENT)
	public Sound audio;
	
	/** The bundled URL of this machine's sound effect */
	public String soundURL;
	
	/** How much energy this machine uses per tick. */
	public double ENERGY_PER_TICK;
	
	/** How many ticks this machine has operated for. */
	public int operatingTicks = 0;
	
	/** Ticks required to operate -- or smelt an item. */
	public int TICKS_REQUIRED;
	
	/** The current tick requirement for this machine. */
	public int currentTicksRequired;
	
	/** The current energy capacity for this machine. */
	public double currentMaxElectricity;
	
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
		ElectricityConnections.registerConnector(this, EnumSet.allOf(ForgeDirection.class));
		currentMaxElectricity = MAX_ELECTRICITY;
		ENERGY_PER_TICK = perTick;
		TICKS_REQUIRED = currentTicksRequired = ticksRequired;
		soundURL = soundPath;
		guiTexturePath = path;
		isActive = false;
	}
	
	@Override
	public void onUpdate()
	{
		super.onUpdate();
		
		if(!registered && worldObj != null && !worldObj.isRemote)
		{
			Mekanism.manager.register(this);
			registered = true;
		}
		
		if(demandsEnergy())
		{
			MinecraftForge.EVENT_BUS.post(new EnergyTileSinkEvent(this, (int)((MAX_ELECTRICITY-electricityStored)*Mekanism.TO_IC2)));
		}
		
		if(!worldObj.isRemote)
		{
			for(ForgeDirection direction : ForgeDirection.values())
			{
				if(direction != ForgeDirection.getOrientation(facing))
				{
					TileEntity tileEntity = Vector3.getTileEntityFromSide(worldObj, Vector3.get(this), direction);
					if(tileEntity != null)
					{
						if(tileEntity instanceof IConductor)
						{
							if(electricityStored < currentMaxElectricity)
							{
								double electricityNeeded = currentMaxElectricity - electricityStored;
								((IConductor)tileEntity).getNetwork().startRequesting(this, electricityNeeded, electricityNeeded >= getVoltage() ? getVoltage() : electricityNeeded);
								setJoules(electricityStored + ((IConductor)tileEntity).getNetwork().consumeElectricity(this).getWatts());
							}
							else if(electricityStored >= currentMaxElectricity)
							{
								((IConductor)tileEntity).getNetwork().stopRequesting(this);
							}
						}
					}
				}
			}
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
        currentTicksRequired = nbtTags.getInteger("currentTicksRequired");
        currentMaxElectricity = nbtTags.getDouble("currentMaxElectricity");
    }

	@Override
    public void writeToNBT(NBTTagCompound nbtTags)
    {
        super.writeToNBT(nbtTags);
        
        nbtTags.setInteger("operatingTicks", operatingTicks);
        nbtTags.setBoolean("isActive", isActive);
        nbtTags.setInteger("currentTicksRequired", currentTicksRequired);
        nbtTags.setDouble("currentMaxElectricity", currentMaxElectricity);
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
		return electricityStored < currentMaxElectricity;
	}

	@Override
    public int injectEnergy(Direction direction, int i)
    {
		double givenEnergy = i*Mekanism.FROM_IC2;
    	double rejects = 0;
    	double neededEnergy = currentMaxElectricity-electricityStored;
    	
    	if(givenEnergy <= neededEnergy)
    	{
    		electricityStored += givenEnergy;
    	}
    	else if(givenEnergy > neededEnergy)
    	{
    		electricityStored += neededEnergy;
    		rejects = givenEnergy-neededEnergy;
    	}
    	
    	return (int)(rejects*Mekanism.TO_IC2);
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
		return (int)(electricityStored*i / currentMaxElectricity);
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
		return currentMaxElectricity;
	}
	
	@Override
	public double getJoules(Object... data) 
	{
		return electricityStored;
	}

	@Override
	public void setJoules(double joules, Object... data)
	{
		electricityStored = Math.max(Math.min(joules, getMaxJoules()), 0);
	}
	
	@Override
	public double getVoltage() 
	{
		return 120;
	}
	
	@Override
	public boolean getActive()
	{
		return isActive;
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
	public boolean canSetFacing(int facing)
	{
		return facing != 0 && facing != 1;
	}

	@Override
	public void attach(IComputerAccess computer, String computerSide) {}

	@Override
	public void detach(IComputerAccess computer) {}
}
