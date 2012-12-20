package mekanism.common;

import ic2.api.ElectricItem;
import ic2.api.IElectricItem;
import mekanism.api.IMachineUpgrade;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.INetworkManager;
import net.minecraft.network.packet.Packet250CustomPayload;
import universalelectricity.core.implement.IItemElectric;

import com.google.common.io.ByteArrayDataInput;

import dan200.computer.api.IComputerAccess;

public abstract class TileEntityAdvancedElectricMachine extends TileEntityBasicMachine
{
	/** How much secondary energy (fuel) this machine uses per tick. */
	public int SECONDARY_ENERGY_PER_TICK;
	
	/** Maximum amount of secondary energy (fuel) this machine can hold. */
	public int MAX_SECONDARY_ENERGY;
	
	/** How much secondary energy (fuel) is stored in this machine. */
	public int secondaryEnergyStored = 0;
	
	/**
	 * Advanced Electric Machine -- a machine like this has a total of 4 slots. Input slot (0), fuel slot (1), output slot (2), 
	 * energy slot (3), and the upgrade slot (4). The machine will not run if it does not have enough electricity, or if it doesn't have enough
	 * fuel ticks.
	 * 
	 * @param soundPath - location of the sound effect
	 * @param name - full name of this machine
	 * @param path - GUI texture path of this machine
	 * @param perTick - how much energy this machine uses per tick.
	 * @param secondaryPerTick - how much secondary energy (fuel) this machine uses per tick.
	 * @param ticksRequired - how many ticks it takes to smelt an item.
	 * @param maxEnergy - maximum amount of energy this machine can hold.
	 * @param maxSecondaryEnergy - maximum amount of secondary energy (fuel) this machine can hold.
	 */
	public TileEntityAdvancedElectricMachine(String soundPath, String name, String path, int perTick, int secondaryPerTick, int ticksRequired, int maxEnergy, int maxSecondaryEnergy)
	{
		super(soundPath, name, path, perTick, ticksRequired, maxEnergy);
		inventory = new ItemStack[5];
		SECONDARY_ENERGY_PER_TICK = secondaryPerTick;
		MAX_SECONDARY_ENERGY = maxSecondaryEnergy;
	}
    
    /**
     * Gets the amount of ticks the declared itemstack can fuel this machine.
     * @param itemstack - itemstack to check with
     * @return fuel ticks
     */
    public abstract int getFuelTicks(ItemStack itemstack);
	
    @Override
	public void onUpdate()
	{
		super.onUpdate();
		
		boolean testActive = operatingTicks > 0;
		
		if(inventory[3] != null)
		{
			if(electricityStored < currentMaxElectricity)
			{
				if(inventory[3].getItem() instanceof IItemElectric)
				{
					IItemElectric electricItem = (IItemElectric)inventory[3].getItem();

					if (electricItem.canProduceElectricity())
					{
						double joulesNeeded = currentMaxElectricity-electricityStored;
						double joulesReceived = 0;
						
						if(electricItem.getVoltage() <= joulesNeeded)
						{
							joulesReceived = electricItem.onUse(electricItem.getVoltage(), inventory[3]);
						}
						else if(electricItem.getVoltage() > joulesNeeded)
						{
							joulesReceived = electricItem.onUse(joulesNeeded, inventory[3]);
						}
						
						setJoules(electricityStored + joulesReceived);
					}
				}
				else if(inventory[3].getItem() instanceof IElectricItem)
				{
					IElectricItem item = (IElectricItem)inventory[3].getItem();
					if(item.canProvideEnergy())
					{
						double gain = ElectricItem.discharge(inventory[3], (int)((MAX_ELECTRICITY - electricityStored)*Mekanism.TO_IC2), 3, false, false)*Mekanism.FROM_IC2;
						setJoules(electricityStored + gain);
					}
				}
			}
			if(inventory[3].itemID == Item.redstone.shiftedIndex && electricityStored <= (MAX_ELECTRICITY-1000))
			{
				setJoules(electricityStored + 1000);
				--inventory[3].stackSize;
				
	            if (inventory[3].stackSize <= 0)
	            {
	                inventory[3] = null;
	            }
			}
		}
		
		if(inventory[1] != null && secondaryEnergyStored == 0)
		{
			int fuelTicks = getFuelTicks(inventory[1]);
			if(fuelTicks > 0)
			{
				int energyNeeded = MAX_SECONDARY_ENERGY - secondaryEnergyStored;
				if(fuelTicks <= energyNeeded)
				{
					setSecondaryEnergy(secondaryEnergyStored + fuelTicks);
				}
				else if(fuelTicks > energyNeeded)
				{
					setSecondaryEnergy(secondaryEnergyStored + energyNeeded);
				}
				--inventory[1].stackSize;
				
				if(inventory[1].stackSize == 0)
				{
					inventory[1] = null;
				}
			}
		}
		
		if(inventory[4] != null && inventory[4].getItem() instanceof IMachineUpgrade)
		{
			int energyToAdd = 0;
			int ticksToRemove = 0;
			
			if(currentMaxElectricity == MAX_ELECTRICITY)
			{
				energyToAdd = ((IMachineUpgrade)inventory[4].getItem()).getEnergyBoost(inventory[4]);
			}
			
			if(currentTicksRequired == TICKS_REQUIRED)
			{
				ticksToRemove = ((IMachineUpgrade)inventory[4].getItem()).getTickReduction(inventory[4]);
			}
			
			currentMaxElectricity += energyToAdd;
			currentTicksRequired -= ticksToRemove;
		}
		else if(inventory[4] == null)
		{
			currentTicksRequired = TICKS_REQUIRED;
			currentMaxElectricity = MAX_ELECTRICITY;
		}
		
		if(canOperate() && (operatingTicks+1) < currentTicksRequired && secondaryEnergyStored >= SECONDARY_ENERGY_PER_TICK)
		{
			++operatingTicks;
			secondaryEnergyStored -= SECONDARY_ENERGY_PER_TICK;
			electricityStored -= ENERGY_PER_TICK;
		}
		else if((operatingTicks+1) >= currentTicksRequired)
		{
			if(!worldObj.isRemote)
			{
				operate();
			}
			operatingTicks = 0;
			secondaryEnergyStored -= SECONDARY_ENERGY_PER_TICK;
			electricityStored -= ENERGY_PER_TICK;
		}
		
		if(electricityStored < 0)
		{
			electricityStored = 0;
		}
		
		if(secondaryEnergyStored < 0)
		{
			secondaryEnergyStored = 0;
		}
		
		if(electricityStored > currentMaxElectricity)
		{
			electricityStored = currentMaxElectricity;
		}
		
		if(secondaryEnergyStored > MAX_SECONDARY_ENERGY)
		{
			secondaryEnergyStored = MAX_SECONDARY_ENERGY;
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

    @Override
    public void operate()
    {
        if (!canOperate())
        {
            return;
        }

        ItemStack itemstack = RecipeHandler.getOutput(inventory[0], true, getRecipes());

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

    @Override
    public boolean canOperate()
    {
        if (inventory[0] == null)
        {
            return false;
        }
        
        if(electricityStored < ENERGY_PER_TICK)
        {
        	return false;
        }
        
        if(secondaryEnergyStored < SECONDARY_ENERGY_PER_TICK)
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
    
	@Override
	public void handlePacketData(INetworkManager network, Packet250CustomPayload packet, EntityPlayer player, ByteArrayDataInput dataStream)
	{
		try {
			facing = dataStream.readInt();
			isActive = dataStream.readBoolean();
			operatingTicks = dataStream.readInt();
			electricityStored = dataStream.readDouble();
			secondaryEnergyStored = dataStream.readInt();
			currentMaxElectricity = dataStream.readDouble();
			currentTicksRequired = dataStream.readInt();
			worldObj.markBlockForRenderUpdate(xCoord, yCoord, zCoord);
		} catch (Exception e)
		{
			System.out.println("[Mekanism] Error while handling tile entity packet.");
			e.printStackTrace();
		}
	}
    
    @Override
    public void sendPacket()
    {
    	PacketHandler.sendTileEntityPacketToClients(this, 0, facing, isActive, operatingTicks, electricityStored, secondaryEnergyStored, currentMaxElectricity, currentTicksRequired);
    }
    
    @Override
    public void sendPacketWithRange()
    {
    	PacketHandler.sendTileEntityPacketToClients(this, 50, facing, isActive, operatingTicks, electricityStored, secondaryEnergyStored, currentMaxElectricity, currentTicksRequired);
    }
	
    @Override
    public void readFromNBT(NBTTagCompound nbtTags)
    {
    	super.readFromNBT(nbtTags);
    	
        secondaryEnergyStored = nbtTags.getInteger("secondaryEnergyStored");
    }

    @Override
    public void writeToNBT(NBTTagCompound nbtTags)
    {
        super.writeToNBT(nbtTags);
        
        nbtTags.setInteger("secondaryEnergyStored", secondaryEnergyStored);
    }
	
	/**
	 * Sets the secondary energy to a new amount
	 * @param energy - amount to store
	 */
	public void setSecondaryEnergy(int energy)
	{
		secondaryEnergyStored = Math.max(Math.min(energy, getFuelTicks(inventory[1])), 0);
	}
	
	/**
	 * Gets the scaled secondary energy level for the GUI.
	 * @param i - multiplier
	 * @return scaled secondary energy
	 */
	public int getScaledSecondaryEnergyLevel(int i)
	{
		return secondaryEnergyStored*i / MAX_SECONDARY_ENERGY;
	}

	@Override
	public String[] getMethodNames() 
	{
		return new String[] {"getStored", "getSecondaryStored", "getProgress", "isActive", "facing", "canOperate", "getMaxEnergy", "getEnergyNeeded"};
	}

	@Override
	public Object[] callMethod(IComputerAccess computer, int method, Object[] arguments) throws Exception 
	{
		switch(method)
		{
			case 0:
				return new Object[] {electricityStored};
			case 1:
				return new Object[] {secondaryEnergyStored};
			case 2:
				return new Object[] {operatingTicks};
			case 3:
				return new Object[] {isActive};
			case 4:
				return new Object[] {facing};
			case 5:
				return new Object[] {canOperate()};
			case 6:
				return new Object[] {currentMaxElectricity};
			case 7:
				return new Object[] {(currentMaxElectricity-electricityStored)};
			default:
				System.err.println("[Mekanism] Attempted to call unknown method with computer ID " + computer.getID());
				return new Object[] {"Unknown command."};
		}
	}
}
