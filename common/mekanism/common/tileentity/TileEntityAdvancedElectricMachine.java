package mekanism.common.tileentity;

import java.util.ArrayList;

import mekanism.api.EnumColor;
import mekanism.api.IStorageTank;
import mekanism.api.SideData;
import mekanism.api.gas.EnumGas;
import mekanism.common.Mekanism;
import mekanism.common.RecipeHandler;
import mekanism.common.TileComponentUpgrade;
import mekanism.common.util.ChargeUtils;
import mekanism.common.util.MekanismUtils;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;

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
	 * @param location - GUI texture path of this machine
	 * @param perTick - how much energy this machine uses per tick.
	 * @param secondaryPerTick - how much secondary energy (fuel) this machine uses per tick.
	 * @param ticksRequired - how many ticks it takes to smelt an item.
	 * @param maxEnergy - maximum amount of energy this machine can hold.
	 * @param maxSecondaryEnergy - maximum amount of secondary energy (fuel) this machine can hold.
	 */
	public TileEntityAdvancedElectricMachine(String soundPath, String name, ResourceLocation location, double perTick, int secondaryPerTick, int ticksRequired, double maxEnergy, int maxSecondaryEnergy)
	{
		super(soundPath, name, location, perTick, ticksRequired, maxEnergy);
		
		sideOutputs.add(new SideData(EnumColor.GREY, new int[0]));
		sideOutputs.add(new SideData(EnumColor.DARK_RED, new int[] {0}));
		sideOutputs.add(new SideData(EnumColor.PURPLE, new int[] {1}));
		sideOutputs.add(new SideData(EnumColor.DARK_BLUE, new int[] {2}));
		sideOutputs.add(new SideData(EnumColor.DARK_GREEN, new int[] {3}));
		sideOutputs.add(new SideData(EnumColor.ORANGE, new int[] {4}));
		
		sideConfig = new byte[] {2, 1, 0, 4, 5, 3};
		
		inventory = new ItemStack[5];
		
		SECONDARY_ENERGY_PER_TICK = secondaryPerTick;
		MAX_SECONDARY_ENERGY = maxSecondaryEnergy;
		
		upgradeComponent = new TileComponentUpgrade(this, 4);
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
		
		if(!worldObj.isRemote)
		{
			ChargeUtils.discharge(3, this);
			
			handleSecondaryFuel();
			
			if(canOperate() && MekanismUtils.canFunction(this) && electricityStored >= MekanismUtils.getEnergyPerTick(getSpeedMultiplier(), getEnergyMultiplier(), ENERGY_PER_TICK) && secondaryEnergyStored >= MekanismUtils.getEnergyPerTick(getSpeedMultiplier(), getEnergyMultiplier(), SECONDARY_ENERGY_PER_TICK))
			{
			    setActive(true);
			    
				operatingTicks++;
				
				secondaryEnergyStored -= MekanismUtils.getEnergyPerTick(getSpeedMultiplier(), getEnergyMultiplier(), SECONDARY_ENERGY_PER_TICK);
				electricityStored -= MekanismUtils.getEnergyPerTick(getSpeedMultiplier(), getEnergyMultiplier(), ENERGY_PER_TICK);
				
				if((operatingTicks) >= MekanismUtils.getTicks(getSpeedMultiplier(), TICKS_REQUIRED))
				{
					operate();
					
					operatingTicks = 0;
				}
			}
			else {
			    setActive(false);
			}
			
			if(!canOperate())
			{
				operatingTicks = 0;
			}
		}
	}
    
    public void handleSecondaryFuel()
    {
		if(inventory[1] != null)
		{
			int fuelTicks = getFuelTicks(inventory[1]);
			int energyNeeded = MAX_SECONDARY_ENERGY - secondaryEnergyStored;
			
			if(fuelTicks > 0 && fuelTicks <= energyNeeded)
			{
				if(fuelTicks <= energyNeeded)
				{
					setSecondaryEnergy(secondaryEnergyStored + fuelTicks);
				}
				else if(fuelTicks > energyNeeded)
				{
					setSecondaryEnergy(secondaryEnergyStored + energyNeeded);
				}
				
				inventory[1].stackSize--;
				
				if(inventory[1].stackSize == 0)
				{
					inventory[1] = null;
				}
			}
		}
    }
    
	@Override
	public boolean isItemValidForSlot(int slotID, ItemStack itemstack)
	{
		if(slotID == 2)
		{
			return false;
		}
		else if(slotID == 4)
		{
			return itemstack.itemID == Mekanism.SpeedUpgrade.itemID || itemstack.itemID == Mekanism.EnergyUpgrade.itemID;
		}
		else if(slotID == 0)
		{
			return RecipeHandler.getOutput(itemstack, false, getRecipes()) != null;
		}
		else if(slotID == 3)
		{
			return ChargeUtils.canBeDischarged(itemstack);
		}
		else if(slotID == 1)
		{
			return getFuelTicks(itemstack) > 0 || 
					(this instanceof TileEntityPurificationChamber && itemstack.getItem() instanceof IStorageTank && 
							((IStorageTank)itemstack.getItem()).getGasType(itemstack) == EnumGas.OXYGEN);
		}
		
		return true;
	}

    @Override
    public void operate()
    {
        ItemStack itemstack = RecipeHandler.getOutput(inventory[0], true, getRecipes());

        if(inventory[0].stackSize <= 0)
        {
            inventory[0] = null;
        }

        if(inventory[2] == null)
        {
            inventory[2] = itemstack;
        }
        else {
            inventory[2].stackSize += itemstack.stackSize;
        }
    }

    @Override
    public boolean canOperate()
    {
        if(inventory[0] == null)
        {
            return false;
        }

        ItemStack itemstack = RecipeHandler.getOutput(inventory[0], false, getRecipes());

        if(itemstack == null)
        {
            return false;
        }

        if(inventory[2] == null)
        {
            return true;
        }

        if(!inventory[2].isItemEqual(itemstack))
        {
            return false;
        }
        else {
            return inventory[2].stackSize + itemstack.stackSize <= inventory[2].getMaxStackSize();
        }
    }
    
	@Override
	public void handlePacketData(ByteArrayDataInput dataStream)
	{
		super.handlePacketData(dataStream);
		secondaryEnergyStored = dataStream.readInt();
	}
	
	@Override
	public ArrayList getNetworkedData(ArrayList data)
	{
		super.getNetworkedData(data);
		data.add(secondaryEnergyStored);
		return data;
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
		secondaryEnergyStored = Math.max(Math.min(energy, MAX_SECONDARY_ENERGY), 0);
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
	public boolean canExtractItem(int slotID, ItemStack itemstack, int side)
	{
		if(slotID == 3)
		{
			return ChargeUtils.canBeOutputted(itemstack, false);
		}
		else if(slotID == 2)
		{
			return true;
		}
		
		return false;
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
				return new Object[] {getEnergy()};
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
				return new Object[] {MekanismUtils.getEnergy(getEnergyMultiplier(), getMaxEnergy())};
			case 7:
				return new Object[] {(MekanismUtils.getEnergy(getEnergyMultiplier(), getMaxEnergy())-getEnergy())};
			default:
				System.err.println("[Mekanism] Attempted to call unknown method with computer ID " + computer.getID());
				return new Object[] {"Unknown command."};
		}
	}
}
