package mekanism.common;

import ic2.api.Direction;
import ic2.api.energy.tile.IEnergySink;

import java.util.ArrayList;
import java.util.EnumSet;

import mekanism.api.IActiveState;
import mekanism.api.IConfigurable;
import mekanism.api.IElectricMachine;
import mekanism.api.SideData;
import mekanism.client.Sound;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.common.ForgeDirection;
import net.minecraftforge.common.MinecraftForge;
import universalelectricity.core.electricity.ElectricityConnections;
import universalelectricity.core.implement.IConductor;
import universalelectricity.core.implement.IJouleStorage;
import universalelectricity.core.implement.IVoltage;
import universalelectricity.core.vector.Vector3;
import cpw.mods.fml.client.FMLClientHandler;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import dan200.computer.api.IComputerAccess;
import dan200.computer.api.IPeripheral;

public abstract class TileEntityBasicMachine extends TileEntityElectricBlock implements IElectricMachine, IEnergySink, IJouleStorage, IVoltage, IPeripheral, IActiveState, IConfigurable
{
	/** The Sound instance for this machine. */
	@SideOnly(Side.CLIENT)
	public Sound audio;
	
	public byte[] sideConfig;
	
	public ArrayList<SideData> sideOutputs = new ArrayList<SideData>();
	
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
		
		if(powerProvider != null)
		{
			int received = (int)(powerProvider.useEnergy(0, (float)((currentMaxElectricity-electricityStored)*Mekanism.TO_BC), true)*10);
			setJoules(electricityStored + received);
		}
		
		if(!registered && worldObj != null && !worldObj.isRemote)
		{
			Mekanism.manager.register(this);
			registered = true;
		}
		
		if(!worldObj.isRemote)
		{
			for(ForgeDirection direction : ForgeDirection.values())
			{
				TileEntity tileEntity = Vector3.getTileEntityFromSide(worldObj, new Vector3(this), direction);
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
		
		if(worldObj.isRemote)
		{
			try {
				synchronized(Mekanism.audioHandler.sounds)
				{
					handleSound();
				}
			} catch(NoSuchMethodError e) {}
		}
	}

	@SideOnly(Side.CLIENT)
	public void handleSound()
	{
		synchronized(Mekanism.audioHandler.sounds)
		{
			if(audio == null && worldObj != null && worldObj.isRemote)
			{
				if(FMLClientHandler.instance().getClient().sndManager.sndSystem != null)
				{
					audio = Mekanism.audioHandler.getSound(soundURL, worldObj, xCoord, yCoord, zCoord);
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
	}
	
	@Override
    public void readFromNBT(NBTTagCompound nbtTags)
    {
        super.readFromNBT(nbtTags);

        operatingTicks = nbtTags.getInteger("operatingTicks");
        isActive = nbtTags.getBoolean("isActive");
        currentTicksRequired = nbtTags.getInteger("currentTicksRequired");
        currentMaxElectricity = nbtTags.getDouble("currentMaxElectricity");
        
        if(nbtTags.hasKey("sideDataStored"))
        {
        	for(int i = 0; i < 6; i++)
        	{
        		sideConfig[i] = nbtTags.getByte("config"+i);
        	}
        }
    }

	@Override
    public void writeToNBT(NBTTagCompound nbtTags)
    {
        super.writeToNBT(nbtTags);
        
        nbtTags.setInteger("operatingTicks", operatingTicks);
        nbtTags.setBoolean("isActive", isActive);
        nbtTags.setInteger("currentTicksRequired", currentTicksRequired);
        nbtTags.setDouble("currentMaxElectricity", currentMaxElectricity);
        
        nbtTags.setBoolean("sideDataStored", true);
        
        for(int i = 0; i < 6; i++)
        {
        	nbtTags.setByte("config"+i, sideConfig[i]);
        }
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
	public int getMaxSafeInput()
	{
		return 2048;
	}
	
	@Override
	public int demandsEnergy() 
	{
		return (int)((currentMaxElectricity - electricityStored)*Mekanism.TO_IC2);
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
	
	@Override
	public int getStartInventorySide(ForgeDirection side) 
	{
		return sideOutputs.get(sideConfig[MekanismUtils.getBaseOrientation(side.ordinal(), facing)]).slotStart;
	}

	@Override
	public int getSizeInventorySide(ForgeDirection side)
	{
		return sideOutputs.get(sideConfig[MekanismUtils.getBaseOrientation(side.ordinal(), facing)]).slotAmount;
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
	public double getVoltage(Object... data) 
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
	public void attach(IComputerAccess computer) {}

	@Override
	public void detach(IComputerAccess computer) {}
	
	@Override
	public ArrayList<SideData> getSideData()
	{
		return sideOutputs;
	}
	
	@Override
	public byte[] getConfiguration()
	{
		return sideConfig;
	}
	
	@Override
	public int getOrientation()
	{
		return facing;
	}
}
