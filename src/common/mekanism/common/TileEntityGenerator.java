package mekanism.common;

import buildcraft.api.core.Orientations;
import buildcraft.api.power.IPowerProvider;
import buildcraft.api.power.IPowerReceptor;
import buildcraft.api.power.PowerFramework;
import buildcraft.api.power.PowerProvider;

import com.google.common.io.ByteArrayDataInput;

import dan200.computer.api.IComputerAccess;
import dan200.computer.api.IPeripheral;

import ic2.api.Direction;
import ic2.api.ElectricItem;
import ic2.api.EnergyNet;
import ic2.api.IElectricItem;
import ic2.api.IEnergySource;
import ic2.api.IEnergyStorage;
import universalelectricity.core.UniversalElectricity;
import universalelectricity.core.Vector3;
import universalelectricity.electricity.ElectricInfo;
import universalelectricity.electricity.ElectricityManager;
import universalelectricity.implement.IConductor;
import universalelectricity.implement.IElectricityReceiver;
import universalelectricity.implement.IItemElectric;
import universalelectricity.implement.IJouleStorage;
import universalelectricity.prefab.TileEntityConductor;
import mekanism.api.IEnergizedItem;
import mekanism.api.IEnergyAcceptor;
import net.minecraft.src.*;
import net.minecraftforge.common.ForgeDirection;

public abstract class TileEntityGenerator extends TileEntityElectricBlock implements IEnergySource, IEnergyStorage, IPowerReceptor, IJouleStorage, IElectricityReceiver, IPeripheral
{
	/** Output per tick this generator can transfer. */
	public int output;
	
	/** BuildCraft power provider. */
	public IPowerProvider powerProvider;
	
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
		
		if(PowerFramework.currentFramework != null)
		{
			powerProvider = PowerFramework.currentFramework.createPowerProvider();
			powerProvider.configure(0, 2, 2000, 1, MAX_ENERGY/10);
		}
	}
	
	@Override
	public void onUpdate()
	{	
		if(energyStored > 0)
		{
			TileEntity tileEntity = Vector3.getTileEntityFromSide(worldObj, Vector3.get(this), ForgeDirection.getOrientation(facing));
			
			if(Mekanism.hooks.IC2Loaded)
			{
				if(energyStored >= output)
				{
					setEnergy(energyStored - (output - EnergyNet.getForWorld(worldObj).emitEnergyFrom(this, output)));
				}
			}
			
			if(tileEntity != null)
			{
				if(isPowerReceptor(tileEntity))
				{
					IPowerReceptor receptor = (IPowerReceptor)tileEntity;
	            	int energyNeeded = Math.min(receptor.getPowerProvider().getMinEnergyReceived(), receptor.getPowerProvider().getMaxEnergyReceived())*10;
	            	float transferEnergy = Math.max(Math.min(Math.min(energyNeeded, energyStored), 54000), 0);
	            	receptor.getPowerProvider().receiveEnergy((float)(transferEnergy/10), Orientations.dirs()[ForgeDirection.getOrientation(facing).getOpposite().ordinal()]);
	            	setEnergy(energyStored - (int)transferEnergy);
				}
				else if(tileEntity instanceof TileEntityConductor)
				{
					double joulesNeeded = ElectricityManager.instance.getElectricityRequired(((IConductor) tileEntity).getNetwork());
					double transferAmps = Math.max(Math.min(Math.min(ElectricInfo.getAmps(joulesNeeded, getVoltage()), ElectricInfo.getAmps(energyStored*UniversalElectricity.IC2_RATIO, getVoltage())), 80), 0);
					if (!worldObj.isRemote)
					{
						ElectricityManager.instance.produceElectricity(this, (IConductor)tileEntity, transferAmps, getVoltage());
					}
					setEnergy(energyStored - (int)(ElectricInfo.getJoules(transferAmps, getVoltage())*UniversalElectricity.TO_IC2_RATIO));
				}
				else if(tileEntity instanceof IEnergyAcceptor)
				{
					if(((IEnergyAcceptor)tileEntity).canReceive(ForgeDirection.getOrientation(facing).getOpposite()))
					{
						int sendingEnergy = 0;
						if(energyStored >= output)
						{
							sendingEnergy = output;
						}
						else if(energyStored < output)
						{
							sendingEnergy = energyStored;
						}
						
						int rejects = ((IEnergyAcceptor)tileEntity).transferToAcceptor(sendingEnergy);
						
						setEnergy(energyStored - (sendingEnergy - rejects));
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
		return energyStored*i / MAX_ENERGY;
	}
	
	/**
	 * Set this block's energy to a new amount.
	 * @param energy - new amount of energy
	 */
	public void setEnergy(int energy)
	{
		energyStored = Math.max(Math.min(energy, MAX_ENERGY), 0);
	}
	
	/**
	 * Sets this block's active state to a new value.
	 * @param active - new active state
	 */
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
		setEnergy((int)(joules*UniversalElectricity.TO_IC2_RATIO));
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
	public void setPowerProvider(IPowerProvider provider)
	{
		powerProvider = provider;
	}

	@Override
	public IPowerProvider getPowerProvider() 
	{
		return powerProvider;
	}

	@Override
	public void doWork() {}

	@Override
	public int powerRequest() 
	{
		return getPowerProvider().getMaxEnergyReceived();
	}
	
	@Override
	public double getMaxJoules() 
	{
		return MAX_ENERGY*UniversalElectricity.IC2_RATIO;
	}
	
	@Override
	public double getJoules(Object... data) 
	{
		return energyStored*UniversalElectricity.IC2_RATIO;
	}
	
	@Override
	public int getMaxEnergyOutput()
	{
		return output;
	}
	
	@Override
	public boolean emitsEnergyTo(TileEntity receiver, Direction direction)
	{
		return direction.toForgeDirection() == ForgeDirection.getOrientation(facing);
	}
	
	@Override
	public int getStored() 
	{
		return energyStored;
	}

	@Override
	public int getCapacity() 
	{
		return MAX_ENERGY;
	}

	@Override
	public int getRate() 
	{
		return output;
	}
	
	@Override
	public boolean canConnect(ForgeDirection side) 
	{
		return side == ForgeDirection.getOrientation(facing);
	}
	
	@Override
	public void onReceive(TileEntity sender, double amps, double voltage, ForgeDirection side) {}

	@Override
	public double wattRequest() 
	{
		return 0;
	}

	@Override
	public boolean canReceiveFromSide(ForgeDirection side) 
	{
		return false;
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
