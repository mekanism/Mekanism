package net.uberkat.obsidian.common;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import obsidian.api.IEnergizedItem;
import obsidian.api.ITileNetwork;

import universalelectricity.UniversalElectricity;
import universalelectricity.electricity.ElectricInfo;
import universalelectricity.electricity.ElectricityManager;
import universalelectricity.implement.IConductor;
import universalelectricity.implement.IElectricityReceiver;
import universalelectricity.implement.IItemElectric;
import universalelectricity.implement.IJouleStorage;
import universalelectricity.network.ConnectionHandler;
import universalelectricity.network.ConnectionHandler.ConnectionType;
import universalelectricity.network.ISimpleConnectionHandler;
import universalelectricity.prefab.TileEntityConductor;
import universalelectricity.prefab.TileEntityDisableable;
import universalelectricity.prefab.Vector3;

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

public class TileEntityPowerUnit extends TileEntityDisableable implements IInventory, ISidedInventory, ITileNetwork, IWrenchable, IEnergySink, IEnergySource, IEnergyStorage, IPowerReceptor, IJouleStorage, IElectricityReceiver, IPeripheral
{
	/** The inventory slot itemstacks used by this power unit. */
	public ItemStack[] inventory = new ItemStack[2];
	
	/** Maximum amount of energy this unit can hold. */
	public int maxEnergy;
	
	/** Output per tick this machine can transfer. */
	public int output;
	
	/** The amount of energy this unit is storing. */
	public int energyStored = 0;
	
	/** Direction this block is facing. */
	public int facing;
	
	/** A timer used to send packets to clients. */
	public int packetTick = 0;
	
	/** BuildCraft power provider. */
	public IPowerProvider powerProvider;
	
	/** Whether or not this machine has initialized and registered with other mods. */
	public boolean initialized = false;
	
	/**
	 * A block used to store and transfer electricity.
	 */
	public TileEntityPowerUnit()
	{
		this(500000, 256);
	}
	
	/**
	 * A block used to store and transfer electricity.
	 * @param energy - maximum energy this block can hold.
	 * @param i - output per tick this block can handle.
	 */
	public TileEntityPowerUnit(int energy, int i)
	{
		if(ObsidianIngots.hooks.UELoaded)
		{
			ElectricityManager.instance.registerElectricUnit(this);
		}
		maxEnergy = energy;
		output = i;
		if(PowerFramework.currentFramework != null)
		{
			powerProvider = PowerFramework.currentFramework.createPowerProvider();
			powerProvider.configure(5, 25, 25, 25, maxEnergy/10);
		}
	}
	
	public void updateEntity()
	{
		if(!worldObj.isRemote)
		{
			if(packetTick == 5)
			{
				PacketHandler.sendPowerUnitPacket(this);
			}
			
			packetTick++;
		}
		
		if(!initialized)
		{
			if(ObsidianIngots.hooks.IC2Loaded)
			{
				EnergyNet.getForWorld(worldObj).addTileEntity(this);
			}
			
			initialized = true;
		}
		
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
		
		if(inventory[1] != null && energyStored < maxEnergy)
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
					int gain = ElectricItem.discharge(inventory[1], maxEnergy - energyStored, 3, false, false);
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
			
			if(isPowerReceptor(tileEntity))
			{
				IPowerReceptor receptor = (IPowerReceptor)tileEntity;
            	int energyNeeded = Math.min(receptor.getPowerProvider().getMinEnergyReceived(), receptor.getPowerProvider().getMaxEnergyReceived())*10;
            	float transferEnergy = Math.max(Math.min(Math.min(energyNeeded, energyStored), 54000), 0);
            	receptor.getPowerProvider().receiveEnergy((float)(transferEnergy/10), Orientations.dirs()[ForgeDirection.getOrientation(facing).getOpposite().ordinal()]);
            	setEnergy(energyStored - (int)transferEnergy);
			}
			
			TileEntity connector = Vector3.getConnectorFromSide(worldObj, Vector3.get(this), ForgeDirection.getOrientation(facing));
			
			if(connector != null && connector instanceof TileEntityConductor)
			{
				double joulesNeeded = ElectricityManager.instance.getElectricityRequired(((IConductor) connector).getConnectionID());
				double transferAmps = Math.max(Math.min(Math.min(ElectricInfo.getAmps(joulesNeeded, getVoltage()), ElectricInfo.getAmps(energyStored*UniversalElectricity.IC2_RATIO, getVoltage())), 80), 0);
				if (!worldObj.isRemote)
				{
					ElectricityManager.instance.produceElectricity(this, (IConductor) connector, transferAmps, getVoltage());
				}
				setEnergy(energyStored - (int)(ElectricInfo.getJoules(transferAmps, getVoltage())*UniversalElectricity.TO_IC2_RATIO));
			}
		}
		
		if(!worldObj.isRemote)
		{
			PacketHandler.sendPowerUnitPacketWithRange(this, 50);
		}
	}
	
	public void setEnergy(int energy)
	{
		energyStored = Math.max(Math.min(energy, maxEnergy), 0);
	}

	public int getStartInventorySide(ForgeDirection side) 
	{
		return 1;
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

	public String getInvName()
	{
		return output == 1024 ? "Advanced Power Unit" : "Power Unit";
	}

	public int getInventoryStackLimit() 
	{
		return 64;
	}

	public void openChest() {}

	public void closeChest() {}
	
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

	public void handlePacketData(NetworkManager network, Packet250CustomPayload packet, EntityPlayer player, ByteArrayDataInput dataStream) 
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

	public boolean wrenchCanSetFacing(EntityPlayer entityPlayer, int side)
	{
		return facing != side;
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
		PacketHandler.sendPowerUnitPacket(this);
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

	public boolean acceptsEnergyFrom(TileEntity emitter, Direction direction)
	{
		return direction.toForgeDirection() != ForgeDirection.getOrientation(facing);
	}

	public boolean isAddedToEnergyNet() 
	{
		return initialized;
	}

	public int getStored() 
	{
		return energyStored;
	}

	public int getCapacity() 
	{
		return maxEnergy;
	}

	public int getOutput() 
	{
		return output;
	}

	public boolean demandsEnergy() 
	{
		return energyStored < maxEnergy;
	}

    public int injectEnergy(Direction direction, int i)
    {
    	int rejects = 0;
    	int neededEnergy = maxEnergy-energyStored;
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
		return maxEnergy*UniversalElectricity.IC2_RATIO;
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
	 * @param tileEntity
	 * @return
	 */
	public boolean isPowerReceptor(TileEntity tileEntity)
	{
		if(tileEntity instanceof IPowerReceptor) 
		{
			IPowerReceptor receptor = (IPowerReceptor) tileEntity;
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
		return ElectricInfo.getWatts(maxEnergy*UniversalElectricity.IC2_RATIO) - ElectricInfo.getWatts(energyStored*UniversalElectricity.IC2_RATIO);
	}

	public boolean canReceiveFromSide(ForgeDirection side) 
	{
		return side != ForgeDirection.getOrientation(facing);
	}

	public boolean isUseableByPlayer(EntityPlayer entityplayer)
	{
		return worldObj.getBlockTileEntity(xCoord, yCoord, zCoord) != this ? false : entityplayer.getDistanceSq((double)xCoord + 0.5D, (double)yCoord + 0.5D, (double)zCoord + 0.5D) <= 64.0D;
	}

	public String getType() 
	{
		return getInvName();
	}

	public String[] getMethodNames() 
	{
		return new String[] {"getStored", "getOutput"};
	}

	public Object[] callMethod(IComputerAccess computer, int method, Object[] arguments) throws Exception 
	{
		switch(method)
		{
			case 0:
				return new Object[] {energyStored};
			case 1:
				return new Object[] {output};
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
}
