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

public abstract class TileEntityBasicMachine extends TileEntityDisableable implements IElectricMachine
{
	/** The inventory slot itemstacks used by this machine. */
	public ItemStack[] inventory;
	
	/** The Sound instance for this machine. */
	public Sound audio;
	
	/** The bundled URL of this machine's sound effect */
	public String soundURL;
	
	/** How much energy this machine uses per tick. */
	public int ENERGY_PER_TICK;
	
	/** How many ticks this machine has operated for. */
	public int operatingTicks = 0;
	
	/** How much energy is stored in this machine. */
	public int energyStored = 0;
	
	/** Ticks required to operate -- or smelt an item. */
	public int TICKS_REQUIRED;
	
	/** The current tick requirement for this machine. */
	public int currentTicksRequired;
	
	/** Maximum amount of energy this machine can hold. */
	public int MAX_ENERGY;
	
	/** The current energy capacity for this machine. */
	public int currentMaxEnergy;
	
	/** The direction this block is facing. */
	public int facing;
	
	/** A timer used to send packets to clients. */
	public int packetTick = 0;
	
	/** Whether or not this block is in it's active state. */
	public boolean isActive;
	
	/** The previous active state for this block. */
	public boolean prevActive;
	
	/** Whether or not this machine has initialized and registered with other mods. */
	public boolean initialized;
	
	/** The full name of this machine. */
	public String fullName;
	
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
		ENERGY_PER_TICK = perTick;
		TICKS_REQUIRED = currentTicksRequired = ticksRequired;
		MAX_ENERGY = currentMaxEnergy = maxEnergy;
		soundURL = soundPath;
		fullName = name;
		guiTexturePath = path;
		isActive = false;
		if(PowerFramework.currentFramework != null)
		{
			powerProvider = PowerFramework.currentFramework.createPowerProvider();
			powerProvider.configure(5, 25, 25, 25, maxEnergy/10);
		}
	}
	
	public void updateEntity()
	{
		if(!initialized && worldObj != null)
		{
			if(ObsidianIngots.hooks.IC2Loaded)
			{
				EnergyNet.getForWorld(worldObj).addTileEntity(this);
			}
			
			initialized = true;
		}
		
		if(audio == null && worldObj.isRemote)
		{
			audio = ObsidianIngots.audioHandler.getSound(fullName.replace(" ", ""), soundURL, worldObj, xCoord, yCoord, zCoord);
		}
		
		onUpdate();
		
		if(!worldObj.isRemote)
		{
			if(packetTick == 5)
			{
				sendPacket();
			}
			
			packetTick++;
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
		
		if(!worldObj.isRemote)
		{
			sendPacketWithRange();
		}
	}
	
	public boolean isUseableByPlayer(EntityPlayer entityplayer)
	{
		return worldObj.getBlockTileEntity(xCoord, yCoord, zCoord) != this ? false : entityplayer.getDistanceSq((double)xCoord + 0.5D, (double)yCoord + 0.5D, (double)zCoord + 0.5D) <= 64.0D;
	}
	
	public void openChest() {}

	public void closeChest() {}
	
	public String getInvName() 
	{
		return fullName;
	}
	
	public int getInventoryStackLimit() 
	{
		return 64;
	}
	
	public void invalidate()
	{
		super.invalidate();
		if(worldObj.isRemote)
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

	public boolean wrenchCanSetFacing(EntityPlayer entityPlayer, int side)
	{
		return true;
	}

	public short getFacing() 
	{
		return (short)facing;
	}

	public void setFacing(short direction) 
	{
		if(initialized)
		{
			if(ObsidianIngots.hooks.IC2Loaded)
			{
				EnergyNet.getForWorld(worldObj).removeTileEntity(this);
			}
		}
		
		initialized = false;
		facing = direction;
		sendPacket();
		if(ObsidianIngots.hooks.IC2Loaded)
		{
			EnergyNet.getForWorld(worldObj).addTileEntity(this);
		}
		initialized = true;
	}

	public boolean wrenchCanRemove(EntityPlayer entityPlayer) 
	{
		return true;
	}

	public float getWrenchDropRate() 
	{
		return 1.0F;
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
	
	public boolean isAddedToEnergyNet()
	{
		return initialized;
	}
	
	/**
	 * Gets the scaled energy level for the GUI.
	 * @param i - multiplier
	 * @return
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
	
	public int getStartInventorySide(ForgeDirection side) 
	{
        if (side == ForgeDirection.DOWN) return 1;
        if (side == ForgeDirection.UP) return 0; 
        return 2;
	}

	public int getSizeInventorySide(ForgeDirection side)
	{
		return 1;
	}

	public int getSizeInventory() 
	{
		return inventory.length;
	}

	public ItemStack getStackInSlot(int par1) 
	{
		return inventory[par1];
	}

    public ItemStack decrStackSize(int par1, int par2)
    {
        if (inventory[par1] != null)
        {
            ItemStack var3;

            if (inventory[par1].stackSize <= par2)
            {
                var3 = inventory[par1];
                inventory[par1] = null;
                return var3;
            }
            else
            {
                var3 = inventory[par1].splitStack(par2);

                if (inventory[par1].stackSize == 0)
                {
                    inventory[par1] = null;
                }

                return var3;
            }
        }
        else
        {
            return null;
        }
    }

    public ItemStack getStackInSlotOnClosing(int par1)
    {
        if (inventory[par1] != null)
        {
            ItemStack var2 = inventory[par1];
            inventory[par1] = null;
            return var2;
        }
        else
        {
            return null;
        }
    }

    public void setInventorySlotContents(int par1, ItemStack par2ItemStack)
    {
        inventory[par1] = par2ItemStack;

        if (par2ItemStack != null && par2ItemStack.stackSize > getInventoryStackLimit())
        {
            par2ItemStack.stackSize = getInventoryStackLimit();
        }
    }
    
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
