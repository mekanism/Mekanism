package net.uberkat.obsidian.common;

import java.util.List;

import obsidian.api.IEnergizedItem;

import universalelectricity.UniversalElectricity;
import universalelectricity.implement.IItemElectric;

import com.google.common.io.ByteArrayDataInput;

import dan200.computer.api.IComputerAccess;

import ic2.api.ElectricItem;
import ic2.api.EnergyNet;
import ic2.api.IElectricItem;
import ic2.api.IWrenchable;
import net.minecraft.src.*;
import net.minecraftforge.common.ForgeDirection;
import net.minecraftforge.common.ISidedInventory;

public abstract class TileEntityElectricMachine extends TileEntityBasicMachine
{
	/** The inventory slot itemstacks used by this machine. */
	public ItemStack[] inventory = new ItemStack[4];
	
	/** How much energy this machine uses per tick. */
	public int ENERGY_PER_TICK;
	
	/** Ticks required to operate -- or smelt an item. */
	public int TICKS_REQUIRED;
	
	/** The current tick requirement for this machine. */
	public int currentTicksRequired;
	
	/** Maximum amount of energy this machine can hold. */
	public int MAX_ENERGY;
	
	/** The current energy capacity for this machine. */
	public int currentMaxEnergy;
	
	/** How many ticks this machine has operated for. */
	public int operatingTicks = 0;
	
	/** How much energy is stored in this machine. */
	public int energyStored = 0;
	
	/**
	 * A simple electrical machine. This has 3 slots - the input slot (0), the energy slot (1), 
	 * output slot (2), and the upgrade slot (3). It will not run if it does not have enough energy.
	 * 
	 * @param soundPath - location of the sound effect
	 * @param name - full name of this machine
	 * @param path - GUI texture path of this machine
	 * @param perTick - energy used per tick.
	 * @param ticksRequired - ticks required to operate -- or smelt an item.
	 * @param maxEnergy - maximum energy this machine can hold.
	 */
	public TileEntityElectricMachine(String soundPath, String name, String path, int perTick, int ticksRequired, int maxEnergy)
	{
		super(soundPath, name, path);
		ENERGY_PER_TICK = perTick;
		TICKS_REQUIRED = currentTicksRequired = ticksRequired;
		MAX_ENERGY = currentMaxEnergy = maxEnergy;
	}
	
	public void onUpdate()
	{
		boolean testActive = operatingTicks > 0;
		
		if(inventory[1] != null)
		{
			if(energyStored < currentMaxEnergy)
			{
				if(inventory[1].getItem() instanceof IEnergizedItem)
				{
					int received = 0;
					int energyNeeded = currentMaxEnergy - energyStored;
					IEnergizedItem item = (IEnergizedItem)inventory[1].getItem();
					if(item.getRate() <= energyNeeded)
					{
						received = item.discharge(inventory[1], item.getRate());
					}
					else if(item.getRate() > energyNeeded)
					{
						item.setEnergy(inventory[1], (item.getEnergy(inventory[1]) - energyNeeded));
						received = energyNeeded;
					}
					
					setEnergy(energyStored + received);
				}
				else if(inventory[1].getItem() instanceof IItemElectric)
				{
					int received = 0;
					int energyNeeded = currentMaxEnergy - energyStored;
					IItemElectric item = (IItemElectric)inventory[1].getItem();
					if((item.getTransferRate()*UniversalElectricity.Wh_IC2_RATIO) <= energyNeeded)
					{
						received = (int)(item.onUseElectricity(item.getTransferRate(), inventory[1])*UniversalElectricity.Wh_IC2_RATIO);
					}
					else if(item.getTransferRate() > energyNeeded)
					{
						item.setWattHours((item.getWattHours(inventory[1]) - (energyNeeded*UniversalElectricity.IC2_RATIO)), inventory[1]);
					}
				}
				else if(inventory[1].getItem() instanceof IElectricItem)
				{
					IElectricItem item = (IElectricItem)inventory[1].getItem();
					if(item.canProvideEnergy())
					{
						int gain = ElectricItem.discharge(inventory[1], energyStored, 3, false, false);
						setEnergy(energyStored + gain);
					}
				}
			}
			if(inventory[1].itemID == Item.redstone.shiftedIndex && energyStored <= (currentMaxEnergy-1000))
			{
				setEnergy(energyStored + 1000);
				--inventory[1].stackSize;
				
	            if (inventory[1].stackSize <= 0)
	            {
	                inventory[1] = null;
	            }
			}
		}
		
		if(inventory[3] != null)
		{
			int energyToAdd = 0;
			int ticksToRemove = 0;
			
			if(inventory[3].isItemEqual(new ItemStack(ObsidianIngots.SpeedUpgrade)))
			{
				if(currentTicksRequired == TICKS_REQUIRED)
				{
					ticksToRemove = 150;
				}
			}
			else if(inventory[3].isItemEqual(new ItemStack(ObsidianIngots.EnergyUpgrade)))
			{
				if(currentMaxEnergy == MAX_ENERGY)
				{
					energyToAdd = 600;
				}
			}
			else if(inventory[3].isItemEqual(new ItemStack(ObsidianIngots.UltimateUpgrade)))
			{
				if(currentTicksRequired == TICKS_REQUIRED)
				{
					ticksToRemove = 150;
				}
				if(currentMaxEnergy == MAX_ENERGY)
				{
					energyToAdd = 600;
				}
			}
			
			currentMaxEnergy += energyToAdd;
			currentTicksRequired -= ticksToRemove;
		}
		else if(inventory[3] == null)
		{
			currentTicksRequired = TICKS_REQUIRED;
			currentMaxEnergy = MAX_ENERGY;
		}
		
		if(canOperate() && (operatingTicks+1) < currentTicksRequired)
		{
			++operatingTicks;
			energyStored -= ENERGY_PER_TICK;
		}
		else if(canOperate() && (operatingTicks+1) >= currentTicksRequired)
		{
			if(!worldObj.isRemote)
			{
				operate();
			}
			operatingTicks = 0;
			energyStored -= ENERGY_PER_TICK;
		}
		
		if(energyStored < 0)
		{
			energyStored = 0;
		}
		
		if(energyStored > currentMaxEnergy)
		{
			energyStored = currentMaxEnergy;
		}
		
		if(!canOperate())
		{
			operatingTicks = 0;
		}
		
		if(!worldObj.isRemote)
		{
			if(testActive != operatingTicks > 0)
			{
				if(operatingTicks > 0)
				{
					setActive(true);
				}
				else if(!canOperate())
				{
					setActive(false);
				}
			}
		}
	}

    public void operate()
    {
        if (!canOperate())
        {
            return;
        }

        ItemStack itemstack;

        if (inventory[0].getItem().hasContainerItem())
        {
            itemstack = RecipeHandler.getOutput(inventory[0], false, getRecipes()).copy();
            inventory[0] = new ItemStack(inventory[0].getItem().getContainerItem());
        }
        else
        {
            itemstack = RecipeHandler.getOutput(inventory[0], true, getRecipes()).copy();
        }

        if (inventory[0].stackSize <= 0)
        {
            inventory[0] = null;
        }

        if (inventory[2] == null)
        {
            inventory[2] = itemstack;
        }
        else
        {
            inventory[2].stackSize += itemstack.stackSize;
        }
    }

    public boolean canOperate()
    {
        if (inventory[0] == null)
        {
            return false;
        }
        
        if(energyStored < ENERGY_PER_TICK)
        {
        	return false;
        }

        ItemStack itemstack = RecipeHandler.getOutput(inventory[0], false, getRecipes());

        if (itemstack == null)
        {
            return false;
        }

        if (inventory[2] == null)
        {
            return true;
        }

        if (!inventory[2].isItemEqual(itemstack))
        {
            return false;
        }
        else
        {
            return inventory[2].stackSize + itemstack.stackSize <= inventory[2].getMaxStackSize();
        }
    }
    
    public void sendPacket()
    {
    	PacketHandler.sendElectricMachinePacket(this);
    }
    
    public void sendPacketWithRange()
    {
    	PacketHandler.sendElectricMachinePacketWithRange(this, 50);
    }

	public void handlePacketData(NetworkManager network, Packet250CustomPayload packet, EntityPlayer player, ByteArrayDataInput dataStream)
	{
		try {
			facing = dataStream.readInt();
			isActive = dataStream.readByte() != 0;
			operatingTicks = dataStream.readInt();
			energyStored = dataStream.readInt();
			currentMaxEnergy = dataStream.readInt();
			currentTicksRequired = dataStream.readInt();
			worldObj.markBlockAsNeedsUpdate(xCoord, yCoord, zCoord);
		} catch (Exception e)
		{
			System.out.println("[ObsidianIngots] Error while handling tile entity packet.");
			e.printStackTrace();
		}
	}
	
    public void readFromNBT(NBTTagCompound nbtTags)
    {
        super.readFromNBT(nbtTags);
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

        operatingTicks = nbtTags.getInteger("operatingTicks");
        energyStored = nbtTags.getInteger("energyStored");
        isActive = nbtTags.getBoolean("isActive");
        facing = nbtTags.getInteger("facing");
    }

    public void writeToNBT(NBTTagCompound nbtTags)
    {
        super.writeToNBT(nbtTags);
        nbtTags.setInteger("operatingTicks", operatingTicks);
        nbtTags.setInteger("energyStored", energyStored);
        nbtTags.setBoolean("isActive", isActive);
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

	public ItemStack getStackInSlot(int var1) 
	{
		return inventory[var1];
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
	
	/**
	 * Sets the energy to a new amount.
	 * @param energy - amount to store
	 */
	public void setEnergy(int energy)
	{
		energyStored = Math.max(Math.min(energy, currentMaxEnergy), 0);
	}

	public int getScaledEnergyLevel(int i)
	{
		return energyStored*i / currentMaxEnergy;
	}

	public int getScaledProgress(int i)
	{
		return operatingTicks*i / currentTicksRequired;
	}
	
	public String getType() 
	{
		return "Electric Machine";
	}

	public String[] getMethodNames() 
	{
		return new String[] {"getStored", "getProgress", "isActive", "facing", "canOperate"};
	}

	public Object[] callMethod(IComputerAccess computer, int method, Object[] arguments) throws Exception 
	{
		switch(method)
		{
			case 0:
				return new Object[] {energyStored};
			case 1:
				return new Object[] {operatingTicks};
			case 2:
				return new Object[] {isActive};
			case 3:
				return new Object[] {facing};
			case 4:
				return new Object[] {canOperate()};
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
