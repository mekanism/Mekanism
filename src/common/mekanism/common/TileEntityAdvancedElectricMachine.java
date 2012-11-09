package mekanism.common;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.List;

import mekanism.api.IMachineUpgrade;

import universalelectricity.core.UniversalElectricity;
import universalelectricity.electricity.ElectricInfo;
import universalelectricity.implement.IItemElectric;

import buildcraft.api.power.IPowerProvider;
import buildcraft.api.power.PowerFramework;

import com.google.common.io.ByteArrayDataInput;

import dan200.computer.api.IComputerAccess;

import ic2.api.ElectricItem;
import ic2.api.EnergyNet;
import ic2.api.IElectricItem;
import ic2.api.IWrenchable;
import mekanism.api.IEnergizedItem;
import net.minecraft.src.*;
import net.minecraftforge.common.ForgeDirection;
import net.minecraftforge.common.ISidedInventory;

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
			if(energyStored < currentMaxEnergy)
			{
				if(inventory[3].getItem() instanceof IEnergizedItem)
				{
					IEnergizedItem item = (IEnergizedItem)inventory[3].getItem();
					
					if(item.canBeDischarged())
					{
						int received = 0;
						int energyNeeded = currentMaxEnergy - energyStored;
						if(item.getRate() <= energyNeeded)
						{
							received = item.discharge(inventory[3], item.getRate());
						}
						else if(item.getRate() > energyNeeded)
						{
							received = item.discharge(inventory[3], energyNeeded);
						}
						
						setEnergy(energyStored + received);
					}
				}
				else if(inventory[3].getItem() instanceof IItemElectric)
				{
					IItemElectric electricItem = (IItemElectric)inventory[3].getItem();

					if (electricItem.canProduceElectricity())
					{
						double joulesReceived = electricItem.onUse(electricItem.getMaxJoules() * 0.005, inventory[3]);
						setEnergy(energyStored + (int)(joulesReceived*UniversalElectricity.TO_IC2_RATIO));
					}
				}
				else if(inventory[3].getItem() instanceof IElectricItem)
				{
					IElectricItem item = (IElectricItem)inventory[3].getItem();
					if(item.canProvideEnergy())
					{
						int gain = ElectricItem.discharge(inventory[3], currentMaxEnergy - energyStored, 3, false, false);
						setEnergy(energyStored + gain);
					}
				}
			}
			if(inventory[3].itemID == Item.redstone.shiftedIndex && energyStored <= (MAX_ENERGY-1000))
			{
				setEnergy(energyStored + 1000);
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
			
			if(currentMaxEnergy == MAX_ENERGY)
			{
				energyToAdd = ((IMachineUpgrade)inventory[4].getItem()).getEnergyBoost(inventory[4]);
			}
			
			if(currentTicksRequired == TICKS_REQUIRED)
			{
				ticksToRemove = ((IMachineUpgrade)inventory[4].getItem()).getTickReduction(inventory[4]);
			}
			
			currentMaxEnergy += energyToAdd;
			currentTicksRequired -= ticksToRemove;
		}
		else if(inventory[4] == null)
		{
			currentTicksRequired = TICKS_REQUIRED;
			currentMaxEnergy = MAX_ENERGY;
		}
		
		if(canOperate() && (operatingTicks+1) < currentTicksRequired && secondaryEnergyStored >= SECONDARY_ENERGY_PER_TICK)
		{
			++operatingTicks;
			secondaryEnergyStored -= SECONDARY_ENERGY_PER_TICK;
			energyStored -= ENERGY_PER_TICK;
		}
		else if((operatingTicks+1) >= currentTicksRequired)
		{
			if(!worldObj.isRemote)
			{
				operate();
			}
			operatingTicks = 0;
			secondaryEnergyStored -= SECONDARY_ENERGY_PER_TICK;
			energyStored -= ENERGY_PER_TICK;
		}
		
		if(energyStored < 0)
		{
			energyStored = 0;
		}
		
		if(secondaryEnergyStored < 0)
		{
			secondaryEnergyStored = 0;
		}
		
		if(energyStored > currentMaxEnergy)
		{
			energyStored = currentMaxEnergy;
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

    @Override
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
    public void sendPacket()
    {
    	PacketHandler.sendAdvancedElectricMachinePacket(this);
    }
    
    @Override
    public void sendPacketWithRange()
    {
    	PacketHandler.sendAdvancedElectricMachinePacketWithRange(this, 50);
    }

    @Override
	public void handlePacketData(INetworkManager network, Packet250CustomPayload packet, EntityPlayer player, ByteArrayDataInput dataStream)
	{
		try {
			facing = dataStream.readInt();
			isActive = dataStream.readByte() != 0;
			operatingTicks = dataStream.readInt();
			energyStored = dataStream.readInt();
			secondaryEnergyStored = dataStream.readInt();
			currentMaxEnergy = dataStream.readInt();
			currentTicksRequired = dataStream.readInt();
			worldObj.markBlockAsNeedsUpdate(xCoord, yCoord, zCoord);
		} catch (Exception e)
		{
			System.out.println("[Mekanism] Error while handling tile entity packet.");
			e.printStackTrace();
		}
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
				return new Object[] {energyStored};
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
				return new Object[] {currentMaxEnergy};
			case 7:
				return new Object[] {(currentMaxEnergy-energyStored)};
			default:
				System.err.println("[Mekanism] Attempted to call unknown method with computer ID " + computer.getID());
				return new Object[] {"Unknown command."};
		}
	}
}
