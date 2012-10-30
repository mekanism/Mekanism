package net.uberkat.obsidian.common;

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
import obsidian.api.IEnergizedItem;
import obsidian.api.IEnergyAcceptor;
import universalelectricity.core.UniversalElectricity;
import universalelectricity.core.Vector3;
import universalelectricity.electricity.ElectricInfo;
import universalelectricity.electricity.ElectricityManager;
import universalelectricity.implement.IConductor;
import universalelectricity.implement.IElectricityReceiver;
import universalelectricity.implement.IItemElectric;
import universalelectricity.implement.IJouleStorage;
import universalelectricity.prefab.TileEntityConductor;
import net.minecraft.src.*;
import net.minecraftforge.common.ForgeDirection;

public abstract class TileEntityGenerator extends TileEntityElectricBlock implements IEnergySource, IEnergyStorage, IPowerReceptor, IJouleStorage, IElectricityReceiver, IPeripheral
{
	/** The amount of fuel stored in this generator. */
	public int fuelStored;
	
	/** The maximum amount of fuel this generator can store. */
	public int MAX_FUEL;
	
	/** Output per tick this generator can transfer. */
	public int output = 128;
	
	/** BuildCraft power provider. */
	public IPowerProvider powerProvider;
	
	/**
	 * Generator -- a block that produces energy. It has a certain amount of fuel it can store as well as an output rate.
	 * @param name - full name of this generator
	 * @param maxEnergy - how much energy this generator can store
	 * @param maxFuel - how much fuel this generator can store
	 */
	public TileEntityGenerator(String name, int maxEnergy, int maxFuel)
	{
		super(name, maxEnergy);
		MAX_FUEL = maxFuel;
		inventory = new ItemStack[2];
		if(PowerFramework.currentFramework != null)
		{
			powerProvider = PowerFramework.currentFramework.createPowerProvider();
			powerProvider.configure(0, 2, 2000, 1, MAX_ENERGY/10);
		}
	}
	
	public void onUpdate()
	{
		if(powerProvider != null)
		{
			int received = (int)(powerProvider.useEnergy(25, 25, true)*10);
			setEnergy(energyStored + received);
		}
		
		if(inventory[1] != null && energyStored > 0)
		{
			if(inventory[1].getItem() instanceof IEnergizedItem)
			{
				IEnergizedItem item = (IEnergizedItem)inventory[1].getItem();
				int sendingEnergy = 0;
				
				if(item.getRate() <= energyStored)
				{
					sendingEnergy = item.getRate();
				}
				else if(item.getRate() > energyStored)
				{
					sendingEnergy = energyStored;
				}
				
				int rejects = item.charge(inventory[1], sendingEnergy);
				setEnergy(energyStored - (sendingEnergy - rejects));
			}
			else if(inventory[1].getItem() instanceof IItemElectric)
			{
				IItemElectric electricItem = (IItemElectric) inventory[1].getItem();
				double ampsToGive = Math.min(ElectricInfo.getAmps(electricItem.getMaxJoules() * 0.005, getVoltage()), (energyStored*UniversalElectricity.IC2_RATIO));
				double joules = electricItem.onReceive(ampsToGive, getVoltage(), inventory[1]);
				setJoules((energyStored*UniversalElectricity.IC2_RATIO) - (ElectricInfo.getJoules(ampsToGive, getVoltage(), 1) - joules));
			}
			else if(inventory[1].getItem() instanceof IElectricItem)
			{
				int sent = ElectricItem.charge(inventory[1], energyStored, 3, false, false);
				setEnergy(energyStored - sent);
			}
		}
		
		if(inventory[0] != null && fuelStored < MAX_ENERGY)
		{
			int fuel = getFuel(inventory[0]);
			if(fuel > 0)
			{
				int fuelNeeded = MAX_FUEL - fuelStored;
				if(fuel <= fuelNeeded)
				{
					fuelStored += fuel;
					--inventory[0].stackSize;
				}
				
				if(inventory[0].stackSize == 0)
				{
					inventory[0] = null;
				}
			}
		}
		
		if(energyStored < MAX_ENERGY)
		{
			setEnergy(energyStored + getEnvironmentBoost());
			
			if(fuelStored > 0)
			{
				fuelStored--;
				setEnergy(energyStored + 4);
			}
		}
		
		if(energyStored > 0)
		{
			TileEntity tileEntity = Vector3.getTileEntityFromSide(worldObj, Vector3.get(this), ForgeDirection.getOrientation(facing));
			
			if(ObsidianIngots.hooks.IC2Loaded)
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
	 * Gets the amount of fuel a certain ItemStack contains.
	 * @param itemstack - slot stack to check
	 * @return amount of fuel the stack contains
	 */
	public abstract int getFuel(ItemStack itemstack);
	
	/**
	 * Gets the boost this generator can receive in it's current location.
	 * @return environmental boost
	 */
	public int getEnvironmentBoost()
	{
		int boost = 0;
		
		if(worldObj.getBlockId(xCoord+1, yCoord, zCoord) == 10 || worldObj.getBlockId(xCoord+1, yCoord, zCoord) == 11)
			boost+=1;
		if(worldObj.getBlockId(xCoord-1, yCoord, zCoord) == 10 || worldObj.getBlockId(xCoord-1, yCoord, zCoord) == 11)
			boost+=1;
		if(worldObj.getBlockId(xCoord, yCoord+1, zCoord) == 10 || worldObj.getBlockId(xCoord, yCoord+1, zCoord) == 11)
			boost+=1;
		if(worldObj.getBlockId(xCoord, yCoord-1, zCoord) == 10 || worldObj.getBlockId(xCoord, yCoord-1, zCoord) == 11)
			boost+=1;
		if(worldObj.getBlockId(xCoord, yCoord, zCoord+1) == 10 || worldObj.getBlockId(xCoord, yCoord, zCoord+1) == 11)
			boost+=1;
		if(worldObj.getBlockId(xCoord, yCoord, zCoord-1) == 10 || worldObj.getBlockId(xCoord, yCoord, zCoord-1) == 11)
			boost+=1;
		
		return boost;
	}
	
	/**
	 * Whether or not this machine can operate and generate power.
	 * @return if the machine can generate power
	 */
	public boolean canPower()
	{
		if(fuelStored <= 0)
		{
			return false;
		}
		
		if(energyStored >= MAX_ENERGY)
		{
			return false;
		}
		return true;
	}
	
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
	 * Gets the scaled fuel level for the GUI.
	 * @param i - multiplier
	 * @return
	 */
	public int getScaledFuelLevel(int i)
	{
		return fuelStored*i / MAX_FUEL;
	}
	
	/**
	 * Set this block's energy to a new amount.
	 * @param energy - new amount of energy
	 */
	public void setEnergy(int energy)
	{
		energyStored = Math.max(Math.min(energy, MAX_ENERGY), 0);
	}
	
    public void readFromNBT(NBTTagCompound nbtTags)
    {
        super.readFromNBT(nbtTags);
        
        if(PowerFramework.currentFramework != null)
        {
        	PowerFramework.currentFramework.loadPowerProvider(this, nbtTags);
        }
        
        NBTTagList tagList = nbtTags.getTagList("Items");
        inventory = new ItemStack[getSizeInventory()];

        for (int slots = 0; slots < tagList.tagCount(); ++slots)
        {
            NBTTagCompound tagCompound = (NBTTagCompound)tagList.tagAt(slots);
            byte slotID = tagCompound.getByte("Slot");

            if (slotID >= 0 && slotID < inventory.length)
            {
                inventory[slotID] = ItemStack.loadItemStackFromNBT(tagCompound);
            }
        }

        energyStored = nbtTags.getInteger("energyStored");
        fuelStored = nbtTags.getInteger("fuelStored");
        facing = nbtTags.getInteger("facing");
    }

    public void writeToNBT(NBTTagCompound nbtTags)
    {
        super.writeToNBT(nbtTags);
        
        if(PowerFramework.currentFramework != null)
        {
        	PowerFramework.currentFramework.savePowerProvider(this, nbtTags);
        }
        
        nbtTags.setInteger("energyStored", energyStored);
        nbtTags.setInteger("fuelStored", fuelStored);
        nbtTags.setInteger("facing", facing);
        NBTTagList tagList = new NBTTagList();

        for (int slots = 0; slots < inventory.length; ++slots)
        {
            if (inventory[slots] != null)
            {
                NBTTagCompound tagCompound = new NBTTagCompound();
                tagCompound.setByte("Slot", (byte)slots);
                inventory[slots].writeToNBT(tagCompound);
                tagList.appendTag(tagCompound);
            }
        }

        nbtTags.setTag("Items", tagList);
    }
	
	public double getVoltage()
	{
		return 120;
	}
	
	public void setJoules(double joules, Object... data) 
	{
		setEnergy((int)(joules*UniversalElectricity.TO_IC2_RATIO));
	}

	public void handlePacketData(INetworkManager network, Packet250CustomPayload packet, EntityPlayer player, ByteArrayDataInput dataStream)
	{
		try {
			facing = dataStream.readInt();
			energyStored = dataStream.readInt();
			fuelStored = dataStream.readInt();
			worldObj.markBlockAsNeedsUpdate(xCoord, yCoord, zCoord);
		} catch (Exception e)
		{
			System.out.println("[ObsidianIngots] Error while handling tile entity packet.");
			e.printStackTrace();
		}
	}
	
	public void sendPacket()
	{
		PacketHandler.sendGeneratorPacket(this);
	}
	
	public void sendPacketWithRange()
	{
		PacketHandler.sendGeneratorPacketWithRange(this, 50);
	}
	
	public String getType() 
	{
		return getInvName();
	}

	public String[] getMethodNames() 
	{
		return new String[] {"getStored", "getOutput", "getMaxEnergy", "getEnergyNeeded", "getFuel", "getFuelNeeded"};
	}

	public Object[] callMethod(IComputerAccess computer, int method, Object[] arguments) throws Exception 
	{
		switch(method)
		{
			case 0:
				return new Object[] {energyStored};
			case 1:
				return new Object[] {output};
			case 2:
				return new Object[] {MAX_ENERGY};
			case 3:
				return new Object[] {(MAX_ENERGY-energyStored)};
			case 4:
				return new Object[] {fuelStored};
			case 5:
				return new Object[] {MAX_FUEL-fuelStored};
			default:
				System.err.println("[ObsidianIngots] Attempted to call unknown method with computer ID " + computer.getID());
				return null;
		}
	}

	public boolean canAttachToSide(int side) 
	{
		return true;
	}

	public void attach(IComputerAccess computer, String computerSide) {}

	public void detach(IComputerAccess computer) {}
	
	public void setPowerProvider(IPowerProvider provider)
	{
		powerProvider = provider;
	}

	public IPowerProvider getPowerProvider() 
	{
		return powerProvider;
	}

	public void doWork() {}

	public int powerRequest() 
	{
		return getPowerProvider().getMaxEnergyReceived();
	}
	
	public double getMaxJoules() 
	{
		return MAX_ENERGY*UniversalElectricity.IC2_RATIO;
	}
	
	public double getJoules(Object... data) 
	{
		return energyStored*UniversalElectricity.IC2_RATIO;
	}
	
	public int getMaxEnergyOutput()
	{
		return output;
	}
	
	public boolean emitsEnergyTo(TileEntity receiver, Direction direction)
	{
		return direction.toForgeDirection() == ForgeDirection.getOrientation(facing);
	}
	
	public int getStored() 
	{
		return energyStored;
	}

	public int getCapacity() 
	{
		return MAX_ENERGY;
	}

	public int getRate() 
	{
		return output;
	}
	
	public boolean canConnect(ForgeDirection side) 
	{
		return side == ForgeDirection.getOrientation(facing);
	}
	
	public void onReceive(TileEntity sender, double amps, double voltage, ForgeDirection side) {}

	public double wattRequest() 
	{
		return 0;
	}

	public boolean canReceiveFromSide(ForgeDirection side) 
	{
		return false;
	}
}
