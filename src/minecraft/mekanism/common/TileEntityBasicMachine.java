package mekanism.common;

import ic2.api.Direction;
import ic2.api.energy.tile.IEnergySink;

import java.util.ArrayList;

import mekanism.api.IConfigurable;
import mekanism.api.IStrictEnergyAcceptor;
import mekanism.api.IUpgradeManagement;
import mekanism.api.SideData;
import mekanism.client.IHasSound;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.ForgeDirection;

import com.google.common.io.ByteArrayDataInput;

import dan200.computer.api.IComputerAccess;
import dan200.computer.api.IPeripheral;

public abstract class TileEntityBasicMachine extends TileEntityElectricBlock implements IElectricMachine, IEnergySink, IPeripheral, IActiveState, IConfigurable, IUpgradeManagement, IHasSound, IStrictEnergyAcceptor
{
	/** This machine's side configuration. */
	public byte[] sideConfig;
	
	/** An arraylist of SideData for this machine. */
	public ArrayList<SideData> sideOutputs = new ArrayList<SideData>();
	
	/** The bundled URL of this machine's sound effect */
	public String soundURL;
	
	/** How much energy this machine uses per tick. */
	public double ENERGY_PER_TICK;
	
	/** How many ticks this machine has operated for. */
	public int operatingTicks = 0;
	
	/** Ticks required to operate -- or smelt an item. */
	public int TICKS_REQUIRED;
	
	/** This machine's speed multiplier. */
	public int speedMultiplier;
	
	/** This machine's energy multiplier. */
	public int energyMultiplier;
	
	/** How long it takes this machine to install an upgrade. */
	public int UPGRADE_TICKS_REQUIRED = 40;
	
	/** How many upgrade ticks have progressed. */
	public int upgradeTicks;
	
	/** Whether or not this block is in it's active state. */
	public boolean isActive;
	
	/** The previous active state for this block. */
	public boolean prevActive;
	
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
		TICKS_REQUIRED = ticksRequired;
		soundURL = soundPath;
		guiTexturePath = path;
		isActive = false;
	}
	
	@Override
	public void onUpdate()
	{
		super.onUpdate();
		
		if(worldObj.isRemote)
		{
			Mekanism.proxy.registerSound(this);
		}
	}
	
	@Override
    public void readFromNBT(NBTTagCompound nbtTags)
    {
        super.readFromNBT(nbtTags);

        operatingTicks = nbtTags.getInteger("operatingTicks");
        isActive = nbtTags.getBoolean("isActive");
        speedMultiplier = nbtTags.getInteger("speedMultiplier");
        energyMultiplier = nbtTags.getInteger("energyMultiplier");
        upgradeTicks = nbtTags.getInteger("upgradeTicks");
        
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
        nbtTags.setInteger("speedMultiplier", speedMultiplier);
        nbtTags.setInteger("energyMultiplier", energyMultiplier);
        nbtTags.setInteger("upgradeTicks", upgradeTicks);
        
        nbtTags.setBoolean("sideDataStored", true);
        
        for(int i = 0; i < 6; i++)
        {
        	nbtTags.setByte("config"+i, sideConfig[i]);
        }
    }
	
	@Override
	public void handlePacketData(ByteArrayDataInput dataStream)
	{
		super.handlePacketData(dataStream);
		operatingTicks = dataStream.readInt();
		isActive = dataStream.readBoolean();
		speedMultiplier = dataStream.readInt();
		energyMultiplier = dataStream.readInt();
		upgradeTicks = dataStream.readInt();
		MekanismUtils.updateBlock(worldObj, xCoord, yCoord, zCoord);
	}
	
	@Override
	public ArrayList getNetworkedData(ArrayList data)
	{
		super.getNetworkedData(data);
		data.add(operatingTicks);
		data.add(isActive);
		data.add(speedMultiplier);
		data.add(energyMultiplier);
		data.add(upgradeTicks);
		return data;
	}
	
	@Override
	public void invalidate()
	{
		super.invalidate();
		
		if(worldObj.isRemote)
		{
			Mekanism.proxy.unregisterSound(this);
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
		return (int)((MekanismUtils.getEnergy(energyMultiplier, MAX_ELECTRICITY) - electricityStored)*Mekanism.TO_IC2);
	}

	@Override
    public int injectEnergy(Direction direction, int i)
    {
		double givenEnergy = i*Mekanism.FROM_IC2;
    	double rejects = 0;
    	double neededEnergy = MekanismUtils.getEnergy(energyMultiplier, MAX_ELECTRICITY)-electricityStored;
    	
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
		return (int)(electricityStored*i / MekanismUtils.getEnergy(energyMultiplier, MAX_ELECTRICITY));
	}

	/**
	 * Gets the scaled progress level for the GUI.
	 * @param i - multiplier
	 * @return
	 */
	public int getScaledProgress(int i)
	{
		return operatingTicks*i / MekanismUtils.getTicks(speedMultiplier, TICKS_REQUIRED);
	}
	
	public int getScaledUpgradeProgress(int i)
	{
		return upgradeTicks*i / UPGRADE_TICKS_REQUIRED;
	}
	
	@Override
	public double getMaxEnergy() 
	{
		return MekanismUtils.getEnergy(energyMultiplier, MAX_ELECTRICITY);
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
    		PacketHandler.sendTileEntityPacketToClients(this, 0, getNetworkedData(new ArrayList()));
    	}
    	
    	prevActive = active;
    }
	
	@Override
	public double transferEnergyToAcceptor(double amount)
	{
    	double rejects = 0;
    	double neededElectricity = MekanismUtils.getEnergy(energyMultiplier, MAX_ELECTRICITY)-electricityStored;
    	
    	if(amount <= neededElectricity)
    	{
    		electricityStored += amount;
    	}
    	else {
    		electricityStored += neededElectricity;
    		rejects = amount-neededElectricity;
    	}
    	
    	return rejects;
	}
	
	@Override
	public boolean canReceiveEnergy(ForgeDirection side)
	{
		return true;
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
	public int[] getSizeInventorySide(int side)
	{
		return sideOutputs.get(sideConfig[MekanismUtils.getBaseOrientation(side, facing)]).availableSlots;
	}
	
	@Override
	public int powerRequest(ForgeDirection side) 
	{
		return (int)Math.min(((MekanismUtils.getEnergy(energyMultiplier, MAX_ELECTRICITY)-electricityStored)*Mekanism.TO_BC), 100);
	}
	
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
	
	@Override
	public int getEnergyMultiplier(Object... data) 
	{
		return energyMultiplier;
	}

	@Override
	public void setEnergyMultiplier(int multiplier, Object... data) 
	{
		energyMultiplier = multiplier;
	}

	@Override
	public int getSpeedMultiplier(Object... data) 
	{
		return speedMultiplier;
	}

	@Override
	public void setSpeedMultiplier(int multiplier, Object... data) 
	{
		speedMultiplier = multiplier;
	}
	
	@Override
	public boolean supportsUpgrades(Object... data)
	{
		return true;
	}
	
	@Override
	public String getSoundPath()
	{
		return soundURL;
	}
}
