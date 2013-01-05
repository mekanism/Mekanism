package mekanism.generators.common;

import ic2.api.Direction;
import ic2.api.IEnergyStorage;
import ic2.api.energy.EnergyNet;
import ic2.api.energy.event.EnergyTileSourceEvent;
import ic2.api.energy.tile.IEnergySource;

import java.util.EnumSet;

import mekanism.api.IActiveState;
import mekanism.common.Mekanism;
import mekanism.common.TileEntityElectricBlock;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.ForgeDirection;
import net.minecraftforge.common.MinecraftForge;
import universalelectricity.core.electricity.ElectricInfo;
import universalelectricity.core.electricity.ElectricityConnections;
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
		
		if(electricityStored > 0)
		{
			TileEntity tileEntity = Vector3.getTileEntityFromSide(worldObj, new Vector3(this), ForgeDirection.getOrientation(facing));
			
			if(Mekanism.hooks.IC2Loaded)
			{
				if(electricityStored >= output)
				{
					MinecraftForge.EVENT_BUS.post(new EnergyTileSourceEvent(this, output));
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
				else if(tileEntity instanceof IConductor)
				{
					double joulesNeeded = ((IConductor)tileEntity).getNetwork().getRequest().getWatts();
					double transferAmps = Math.max(Math.min(Math.min(ElectricInfo.getAmps(joulesNeeded, getVoltage()), ElectricInfo.getAmps(electricityStored, getVoltage())), 80), 0);

					if (!worldObj.isRemote && transferAmps > 0)
					{
						((IConductor)tileEntity).getNetwork().startProducing(this, transferAmps, getVoltage());
						setJoules(electricityStored - ElectricInfo.getWatts(transferAmps, getVoltage()));
					}
					else
					{
						((IConductor)tileEntity).getNetwork().stopProducing(this);
					}
				}
			}
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
	public double getVoltage()
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
	public void attach(IComputerAccess computer, String computerSide) {}

	@Override
	public void detach(IComputerAccess computer) {}
	
	@Override
	public double getMaxJoules(Object... data) 
	{
		return MAX_ELECTRICITY*Mekanism.FROM_IC2;
	}
	
	@Override
	public double getJoules(Object... data) 
	{
		return electricityStored*Mekanism.FROM_IC2;
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
