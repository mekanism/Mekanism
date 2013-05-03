package mekanism.common;

import ic2.api.item.IElectricItem;
import mekanism.api.EnumColor;
import mekanism.api.SideData;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import universalelectricity.core.item.IItemElectric;
import dan200.computer.api.IComputerAccess;

public abstract class TileEntityElectricMachine extends TileEntityBasicMachine
{
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
		super(soundPath, name, path, perTick, ticksRequired, maxEnergy);
		
		sideOutputs.add(new SideData(EnumColor.GREY, 0, 0, new int[0]));
		sideOutputs.add(new SideData(EnumColor.DARK_RED, 0, 1, new int[] {0}));
		sideOutputs.add(new SideData(EnumColor.DARK_GREEN, 1, 1, new int[] {1}));
		sideOutputs.add(new SideData(EnumColor.DARK_BLUE, 2, 1, new int[] {2}));
		sideOutputs.add(new SideData(EnumColor.ORANGE, 3, 1, new int[] {3}));
		
		sideConfig = new byte[] {2, 1, 0, 0, 4, 3};
		
		inventory = new ItemStack[4];
	}
	
	@Override
	public void onUpdate()
	{
		super.onUpdate();
		
		if(!worldObj.isRemote)
		{
			ChargeUtils.discharge(1, this);
			
			if(inventory[3] != null)
			{
				if(inventory[3].isItemEqual(new ItemStack(Mekanism.EnergyUpgrade)) && energyMultiplier < 8)
				{
					if(upgradeTicks < UPGRADE_TICKS_REQUIRED)
					{
						upgradeTicks++;
					}
					else if(upgradeTicks == UPGRADE_TICKS_REQUIRED)
					{
						upgradeTicks = 0;
						energyMultiplier++;
						
						inventory[3].stackSize--;
						
						if(inventory[3].stackSize == 0)
						{
							inventory[3] = null;
						}
					}
				}
				else if(inventory[3].isItemEqual(new ItemStack(Mekanism.SpeedUpgrade)) && speedMultiplier < 8)
				{
					if(upgradeTicks < UPGRADE_TICKS_REQUIRED)
					{
						upgradeTicks++;
					}
					else if(upgradeTicks == UPGRADE_TICKS_REQUIRED)
					{
						upgradeTicks = 0;
						speedMultiplier++;
						
						inventory[3].stackSize--;
						
						if(inventory[3].stackSize == 0)
						{
							inventory[3] = null;
						}
					}
				}
				else {
					upgradeTicks = 0;
				}
			}
			else {
				upgradeTicks = 0;
			}
			
			if(electricityStored >= ENERGY_PER_TICK)
			{
				if(canOperate() && (operatingTicks+1) < MekanismUtils.getTicks(speedMultiplier, TICKS_REQUIRED))
				{
					operatingTicks++;
					electricityStored -= ENERGY_PER_TICK;
				}
				else if(canOperate() && (operatingTicks+1) >= MekanismUtils.getTicks(speedMultiplier, TICKS_REQUIRED))
				{
					operate();
					
					operatingTicks = 0;
					electricityStored -= ENERGY_PER_TICK;
				}
			}
			
			if(!canOperate())
			{
				operatingTicks = 0;
			}
			
			if(canOperate() && electricityStored >= ENERGY_PER_TICK)
			{
				setActive(true);
			}
			else {
				setActive(false);
			}
		}
	}
	
	@Override
	public boolean isStackValidForSlot(int slotID, ItemStack itemstack)
	{
		if(slotID == 2)
		{
			return false;
		}
		else if(slotID == 3)
		{
			return itemstack.itemID == Mekanism.SpeedUpgrade.itemID || itemstack.itemID == Mekanism.EnergyUpgrade.itemID;
		}
		else if(slotID == 0)
		{
			return RecipeHandler.getOutput(itemstack, false, getRecipes()) != null;
		}
		else if(slotID == 1)
		{
			return (itemstack.getItem() instanceof IElectricItem && ((IElectricItem)itemstack.getItem()).canProvideEnergy(itemstack)) || 
					(itemstack.getItem() instanceof IItemElectric && ((IItemElectric)itemstack.getItem()).getProvideRequest(itemstack).amperes != 0) || 
					itemstack.itemID == Item.redstone.itemID;
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
	public boolean func_102008_b(int slotID, ItemStack itemstack, int side)
	{
		if(slotID == 1)
		{
			return (itemstack.getItem() instanceof IItemElectric && ((IItemElectric)itemstack.getItem()).getProvideRequest(itemstack).getWatts() == 0) ||
					(itemstack.getItem() instanceof IElectricItem && ((IElectricItem)itemstack.getItem()).canProvideEnergy(itemstack) && 
							(!(itemstack.getItem() instanceof IItemElectric) || 
							((IItemElectric)itemstack.getItem()).getProvideRequest(itemstack).getWatts() == 0));
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
		return new String[] {"getStored", "getProgress", "isActive", "facing", "canOperate", "getMaxEnergy", "getEnergyNeeded"};
	}

	@Override
	public Object[] callMethod(IComputerAccess computer, int method, Object[] arguments) throws Exception 
	{
		switch(method)
		{
			case 0:
				return new Object[] {electricityStored};
			case 1:
				return new Object[] {operatingTicks};
			case 2:
				return new Object[] {isActive};
			case 3:
				return new Object[] {facing};
			case 4:
				return new Object[] {canOperate()};
			case 5:
				return new Object[] {MekanismUtils.getEnergy(energyMultiplier, MAX_ELECTRICITY)};
			case 6:
				return new Object[] {(MekanismUtils.getEnergy(energyMultiplier, MAX_ELECTRICITY)-electricityStored)};
			default:
				System.err.println("[Mekanism] Attempted to call unknown method with computer ID " + computer.getID());
				return new Object[] {"Unknown command."};
		}
	}
}
