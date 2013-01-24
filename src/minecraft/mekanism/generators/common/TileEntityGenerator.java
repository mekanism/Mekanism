package mekanism.generators.common;

import ic2.api.Direction;
import ic2.api.IEnergyStorage;
import ic2.api.energy.EnergyNet;
import ic2.api.energy.event.EnergyTileSourceEvent;
import ic2.api.energy.tile.IEnergySource;

import java.util.EnumSet;

import cpw.mods.fml.client.FMLClientHandler;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

import mekanism.api.IActiveState;
import mekanism.client.Sound;
import mekanism.common.Mekanism;
import mekanism.common.TileEntityElectricBlock;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.ForgeDirection;
import net.minecraftforge.common.MinecraftForge;
import universalelectricity.core.electricity.ElectricInfo;
import universalelectricity.core.electricity.ElectricityConnections;
import universalelectricity.core.electricity.ElectricityNetwork;
import universalelectricity.core.implement.IConductor;
import universalelectricity.core.implement.IJouleStorage;
import universalelectricity.core.implement.IVoltage;
import universalelectricity.core.vector.Vector3;
import buildcraft.api.power.IPowerProvider;
import buildcraft.api.power.IPowerReceptor;
import buildcraft.api.power.PowerProvider;
import dan200.computer.api.IComputerAccess;
import dan200.computer.api.IPeripheral;

public abstract class TileEntityGenerator extends TileEntityElectricBlock implements IEnergySource, IEnergyStorage, IPowerReceptor, IJouleStorage, IVoltage, IPeripheral, IActiveState
{
	/** The Sound instance for this generator. */
	@SideOnly(Side.CLIENT)
	public Sound audio;
	
	/** Output per tick this generator can transfer. */
	public int output;
	
	/** Whether or not this block is in it's active state. */
	public boolean isActive;
	
	/** The previous active state for this block. */
	public boolean prevActive;
	
	/**
	 * Generator -- a block that produces energy. It has a certain amount of fuel it can store as well as an output rate.
	 * @param name - full name of this generator
	 * @param maxEnergy - how much energy this generator can store
	 * @param maxFuel - how much fuel this generator can store
	 */
	public TileEntityGenerator(String name, int maxEnergy, int out)
	{
		super(name, maxEnergy);
		
		output = out;
		isActive = false;
	}
	
	@Override
	public void onUpdate()
	{	
		super.onUpdate();
		
		if(worldObj.isRemote)
		{
			try {
				synchronized(Mekanism.audioHandler.sounds)
				{
					handleSound();
				}
			} catch(NoSuchMethodError e) {}
		}
		
		if(packetTick == 20)
		{
			if(ElectricityConnections.isConnector(this))
			{
				ElectricityConnections.unregisterConnector(this);
			}
			
			ElectricityConnections.registerConnector(this, EnumSet.of(ForgeDirection.getOrientation(facing)));
			worldObj.notifyBlocksOfNeighborChange(xCoord, yCoord, zCoord, MekanismGenerators.generatorID);
		}
		
		if(electricityStored > 0)
		{
			TileEntity tileEntity = Vector3.getTileEntityFromSide(worldObj, new Vector3(this), ForgeDirection.getOrientation(facing));
			
			if(Mekanism.hooks.IC2Loaded)
			{
				if(electricityStored >= output)
				{
					EnergyTileSourceEvent event = new EnergyTileSourceEvent(this, output);
					MinecraftForge.EVENT_BUS.post(event);
					setJoules(electricityStored - (output - event.amount));
				}
			}
			
			if(tileEntity != null)
			{
				if(isPowerReceptor(tileEntity))
				{
					IPowerReceptor receptor = (IPowerReceptor)tileEntity;
	            	double electricityNeeded = Math.min(receptor.getPowerProvider().getMinEnergyReceived(), receptor.getPowerProvider().getMaxEnergyReceived())*Mekanism.FROM_BC;
	            	float transferEnergy = (float)Math.max(Math.min(Math.min(electricityNeeded, electricityStored), 80000), 0);
	            	receptor.getPowerProvider().receiveEnergy((float)(transferEnergy*Mekanism.TO_BC), ForgeDirection.getOrientation(facing).getOpposite());
	            	setJoules(electricityStored - (int)transferEnergy);
				}
			}
		}
		
		if(!worldObj.isRemote)
		{
			ForgeDirection outputDirection = ForgeDirection.getOrientation(facing);
			TileEntity outputTile = Vector3.getTileEntityFromSide(worldObj, new Vector3(this), outputDirection);

			ElectricityNetwork outputNetwork = ElectricityNetwork.getNetworkFromTileEntity(outputTile, outputDirection);

			if(outputNetwork != null)
			{
				double outputWatts = Math.min(outputNetwork.getRequest().getWatts(), getJoules());

				if(getJoules() > 0 && outputWatts > 0)
				{
					outputNetwork.startProducing(this, outputWatts / getVoltage(), getVoltage());
					setJoules(electricityStored - outputWatts);
				}
				else {
					outputNetwork.stopProducing(this);
				}
			}
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
					audio = Mekanism.audioHandler.getSound(fullName.replace(" ", "").replace("-","") + ".ogg", worldObj, xCoord, yCoord, zCoord);
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
	public void invalidate()
	{
		super.invalidate();
		
		if(worldObj.isRemote && audio != null)
		{
			audio.remove();
		}
	}
	
	/**
	 * Gets the boost this generator can receive in it's current location.
	 * @return environmental boost
	 */
	public abstract int getEnvironmentBoost();
	
	/**
	 * Whether or not this generator can operate.
	 * @return if the generator can operate
	 */
	public abstract boolean canOperate();
	
	/**
	 * Whether or not the declared Tile Entity is an instance of a BuildCraft power receptor.
	 * @param tileEntity - tile entity to check
	 * @return if the tile entity is a power receptor
	 */
	public boolean isPowerReceptor(TileEntity tileEntity)
	{
		if(tileEntity instanceof IPowerReceptor) 
		{
			IPowerReceptor receptor = (IPowerReceptor)tileEntity;
			IPowerProvider provider = receptor.getPowerProvider();
			return provider != null && provider.getClass().getSuperclass().equals(PowerProvider.class);
		}
		return false;
	}
	
	/**
	 * Gets the scaled energy level for the GUI.
	 * @param i - multiplier
	 * @return
	 */
	public int getScaledEnergyLevel(int i)
	{
		return (int)(electricityStored*i / MAX_ELECTRICITY);
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
	public double getVoltage(Object... data)
	{
		return 120;
	}
	
	@Override
	public void setJoules(double joules, Object... data)
	{
		electricityStored = Math.max(Math.min(joules, getMaxJoules()), 0);
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
	public void attach(IComputerAccess computer) {}

	@Override
	public void detach(IComputerAccess computer) {}
	
	@Override
	public double getMaxJoules(Object... data) 
	{
		return MAX_ELECTRICITY;
	}
	
	@Override
	public double getJoules(Object... data) 
	{
		return electricityStored;
	}
	
	@Override
	public int getMaxEnergyOutput()
	{
		return output;
	}
	
	@Override
	public void setFacing(short orientation)
	{
		super.setFacing(orientation);
		
		if(ElectricityConnections.isConnector(this))
		{
			ElectricityConnections.unregisterConnector(this);
		}
		
		ElectricityConnections.registerConnector(this, EnumSet.of(ForgeDirection.getOrientation(orientation)));
		worldObj.notifyBlocksOfNeighborChange(xCoord, yCoord, zCoord, MekanismGenerators.generatorID);
	}
	
	@Override
	public boolean emitsEnergyTo(TileEntity receiver, Direction direction)
	{
		return direction.toForgeDirection() == ForgeDirection.getOrientation(facing);
	}
	
	@Override
	public int getStored() 
	{
		return (int)(electricityStored*Mekanism.TO_IC2);
	}

	@Override
	public int getCapacity() 
	{
		return (int)(MAX_ELECTRICITY*Mekanism.TO_IC2);
	}

	@Override
	public int getOutput() 
	{
		return output;
	}
	
	@Override
	public boolean isTeleporterCompatible(Direction side) 
	{
		return side.toForgeDirection() == ForgeDirection.getOrientation(facing);
	}
	
	@Override
	public int addEnergy(int amount)
	{
		setJoules(electricityStored + amount*Mekanism.FROM_IC2);
		return (int)electricityStored;
	}
	
	@Override
	public void setStored(int energy)
	{
		setJoules(energy*Mekanism.FROM_IC2);
	}
	
	@Override
    public void readFromNBT(NBTTagCompound nbtTags)
    {
        super.readFromNBT(nbtTags);

        isActive = nbtTags.getBoolean("isActive");
    }

	@Override
    public void writeToNBT(NBTTagCompound nbtTags)
    {
        super.writeToNBT(nbtTags);
        
        nbtTags.setBoolean("isActive", isActive);
    }
}
