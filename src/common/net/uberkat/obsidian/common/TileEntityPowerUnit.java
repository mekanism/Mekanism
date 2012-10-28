package net.uberkat.obsidian.common;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import obsidian.api.IEnergizedItem;
import obsidian.api.ITileNetwork;
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
import universalelectricity.prefab.TileEntityDisableable;

import buildcraft.api.power.IPowerProvider;
import buildcraft.api.power.IPowerReceptor;
import buildcraft.api.power.PowerFramework;
import buildcraft.api.power.PowerProvider;
import buildcraft.api.core.Orientations;

import com.google.common.io.ByteArrayDataInput;

import dan200.computer.api.IComputerAccess;
import dan200.computer.api.IPeripheral;

import ic2.api.Direction;
import ic2.api.ElectricItem;
import ic2.api.EnergyNet;
import ic2.api.IEnergySink;
import ic2.api.IEnergySource;
import ic2.api.IEnergyStorage;
import ic2.api.IWrenchable;
import ic2.api.IElectricItem;
import net.minecraft.src.*;
import net.minecraftforge.common.ForgeDirection;
import net.minecraftforge.common.ISidedInventory;

public class TileEntityPowerUnit extends TileEntityElectricBlock implements IEnergySink, IEnergySource, IEnergyStorage, IPowerReceptor, IJouleStorage, IElectricityReceiver, IEnergyAcceptor, IPeripheral
{
	/** Output per tick this machine can transfer. */
	public int output;
	
	/** BuildCraft power provider. */
	public IPowerProvider powerProvider;
	
	/**
	 * A block used to store and transfer electricity.
	 */
	public TileEntityPowerUnit()
	{
		this("Power Unit", 500000, 256);
	}
	
	/**
	 * A block used to store and transfer electricity.
	 * @param energy - maximum energy this block can hold.
	 * @param i - output per tick this block can handle.
	 */
	public TileEntityPowerUnit(String name, int maxEnergy, int i)
	{
		super(name, maxEnergy);
		inventory = new ItemStack[2];
		output = i;
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
		
		if(inventory[0] != null && energyStored > 0)
		{
			if(inventory[0].getItem() instanceof IEnergizedItem)
			{
				IEnergizedItem item = (IEnergizedItem)inventory[0].getItem();
				int rejects = item.charge(inventory[0], item.getRate());
				setEnergy(energyStored - (item.getRate() - rejects));
			}
			else if(inventory[0].getItem() instanceof IItemElectric)
			{
				IItemElectric electricItem = (IItemElectric) inventory[0].getItem();
				double ampsToGive = Math.min(ElectricInfo.getAmps(electricItem.getMaxJoules() * 0.005, getVoltage()), (energyStored*UniversalElectricity.IC2_RATIO));
				double joules = electricItem.onReceive(ampsToGive, getVoltage(), inventory[0]);
				setJoules((energyStored*UniversalElectricity.IC2_RATIO) - (ElectricInfo.getJoules(ampsToGive, getVoltage(), 1) - joules));
			}
			else if(inventory[0].getItem() instanceof IElectricItem)
			{
				int sent = ElectricItem.charge(inventory[0], energyStored, 3, false, false);
				setEnergy(energyStored - sent);
			}
		}
		
		if(inventory[1] != null && energyStored < MAX_ENERGY)
		{
			if(inventory[1].getItem() instanceof IEnergizedItem)
			{
				IEnergizedItem item = (IEnergizedItem)inventory[1].getItem();
				int received = item.discharge(inventory[1], item.getRate());
				setEnergy(energyStored + received);
			}
			else if(inventory[1].getItem() instanceof IItemElectric)
			{
				IItemElectric electricItem = (IItemElectric) inventory[1].getItem();

				if (electricItem.canProduceElectricity())
				{
					double joulesReceived = electricItem.onUse(electricItem.getMaxJoules() * 0.005, inventory[1]);
					setJoules((energyStored*UniversalElectricity.IC2_RATIO) + joulesReceived);
				}
			}
			else if(inventory[1].getItem() instanceof IElectricItem)
			{
				IElectricItem item = (IElectricItem)inventory[1].getItem();
				if(item.canProvideEnergy())
				{
					int gain = ElectricItem.discharge(inventory[1], MAX_ENERGY - energyStored, 3, false, false);
					setEnergy(energyStored + gain);
				}
			}
			else if(inventory[1].itemID == Item.redstone.shiftedIndex)
			{
				setEnergy(energyStored + 1000);
				--inventory[1].stackSize;
				
                if (inventory[1].stackSize <= 0)
                {
                    inventory[1] = null;
                }
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
						
						int rejects = ((IEnergyAcceptor)tileEntity).transferToAcceptor(output);
						
						setEnergy(energyStored - (sendingEnergy - rejects));
					}
				}
			}
		}
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

	public void handlePacketData(INetworkManager network, Packet250CustomPayload packet, EntityPlayer player, ByteArrayDataInput dataStream) 
	{
		try {
			facing = dataStream.readInt();
			energyStored = dataStream.readInt();
			worldObj.markBlockAsNeedsUpdate(xCoord, yCoord, zCoord);
		} catch (Exception e)
		{
			System.out.println("[ObsidianIngots] Error while handling tile entity packet.");
			e.printStackTrace();
		}
	}

	public boolean acceptsEnergyFrom(TileEntity emitter, Direction direction)
	{
		return direction.toForgeDirection() != ForgeDirection.getOrientation(facing);
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

	public boolean demandsEnergy() 
	{
		return energyStored < MAX_ENERGY;
	}

    public int injectEnergy(Direction direction, int i)
    {
    	int rejects = 0;
    	int neededEnergy = MAX_ENERGY-energyStored;
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

	public boolean emitsEnergyTo(TileEntity receiver, Direction direction)
	{
		return direction.toForgeDirection() == ForgeDirection.getOrientation(facing);
	}

	public int getMaxEnergyOutput()
	{
		return output;
	}

	public double getJoules(Object... data) 
	{
		return energyStored*UniversalElectricity.IC2_RATIO;
	}

	public void setJoules(double joules, Object... data) 
	{
		setEnergy((int)(joules*UniversalElectricity.TO_IC2_RATIO));
	}

	public double getMaxJoules() 
	{
		return MAX_ENERGY*UniversalElectricity.IC2_RATIO;
	}

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

	public boolean canConnect(ForgeDirection side) 
	{
		return true;
	}

	public double getVoltage() 
	{
		return 120;
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

	public void onReceive(TileEntity sender, double amps, double voltage, ForgeDirection side) 
	{
		setEnergy(energyStored + (int)(ElectricInfo.getJoules(amps, voltage)*UniversalElectricity.TO_IC2_RATIO));
	}

	public double wattRequest() 
	{
		return ElectricInfo.getWatts(MAX_ENERGY*UniversalElectricity.IC2_RATIO) - ElectricInfo.getWatts(energyStored*UniversalElectricity.IC2_RATIO);
	}

	public boolean canReceiveFromSide(ForgeDirection side) 
	{
		return side != ForgeDirection.getOrientation(facing);
	}

	public String getType() 
	{
		return getInvName();
	}

	public String[] getMethodNames() 
	{
		return new String[] {"getStored", "getOutput", "getMaxEnergy", "getEnergyNeeded"};
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

	public int transferToAcceptor(int amount) 
	{
    	int rejects = 0;
    	int neededEnergy = MAX_ENERGY-energyStored;
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
		return side != ForgeDirection.getOrientation(facing);
	}
	
    public void sendPacket()
    {
    	PacketHandler.sendPowerUnitPacket(this);
    }
    
    public void sendPacketWithRange()
    {
    	PacketHandler.sendPowerUnitPacketWithRange(this, 50);
    }
}
