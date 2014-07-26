package mekanism.common.tile;

import mekanism.api.EnumColor;
import mekanism.common.Mekanism;
import mekanism.common.SideData;
import mekanism.common.recipe.RecipeHandler;
import mekanism.common.tile.component.TileComponentEjector;
import mekanism.common.tile.component.TileComponentUpgrade;
import mekanism.common.util.ChargeUtils;
import mekanism.common.util.InventoryUtils;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.ResourceType;
import net.minecraft.item.ItemStack;
import cpw.mods.fml.common.Optional.Method;
import dan200.computercraft.api.lua.ILuaContext;
import dan200.computercraft.api.peripheral.IComputerAccess;

public abstract class TileEntityElectricMachine extends TileEntityBasicMachine
{
	/**
	 * A simple electrical machine. This has 3 slots - the input slot (0), the energy slot (1),
	 * output slot (2), and the upgrade slot (3). It will not run if it does not have enough energy.
	 *
	 * @param soundPath - location of the sound effect
	 * @param name - full name of this machine
	 * @param perTick - energy used per tick.
	 * @param ticksRequired - ticks required to operate -- or smelt an item.
	 * @param maxEnergy - maximum energy this machine can hold.
	 */
	public TileEntityElectricMachine(String soundPath, String name, double perTick, int ticksRequired, double maxEnergy)
	{
		super(soundPath, name, MekanismUtils.getResource(ResourceType.GUI, "GuiBasicMachine.png"), perTick, ticksRequired, maxEnergy);

		sideOutputs.add(new SideData(EnumColor.GREY, InventoryUtils.EMPTY));
		sideOutputs.add(new SideData(EnumColor.DARK_RED, new int[] {0}));
		sideOutputs.add(new SideData(EnumColor.DARK_GREEN, new int[] {1}));
		sideOutputs.add(new SideData(EnumColor.DARK_BLUE, new int[] {2}));
		sideOutputs.add(new SideData(EnumColor.ORANGE, new int[] {3}));

		sideConfig = new byte[] {2, 1, 0, 0, 4, 3};

		inventory = new ItemStack[4];

		upgradeComponent = new TileComponentUpgrade(this, 3);
		ejectorComponent = new TileComponentEjector(this, sideOutputs.get(3));
	}

	@Override
	public void onUpdate()
	{
		super.onUpdate();

		if(!worldObj.isRemote)
		{
			ChargeUtils.discharge(1, this);

			if(canOperate() && MekanismUtils.canFunction(this) && getEnergy() >= MekanismUtils.getEnergyPerTick(this, ENERGY_PER_TICK))
			{
				setActive(true);

				if((operatingTicks+1) < MekanismUtils.getTicks(this, TICKS_REQUIRED))
				{
					operatingTicks++;
					electricityStored -= MekanismUtils.getEnergyPerTick(this, ENERGY_PER_TICK);
				}
				else if((operatingTicks+1) >= MekanismUtils.getTicks(this, TICKS_REQUIRED))
				{
					operate();

					operatingTicks = 0;
					electricityStored -= MekanismUtils.getEnergyPerTick(this, ENERGY_PER_TICK);
				}
			}
			else {
				if(prevEnergy >= getEnergy())
				{
					setActive(false);
				}
			}

			if(!canOperate())
			{
				operatingTicks = 0;
			}

			prevEnergy = getEnergy();
		}
	}

	@Override
	public boolean isItemValidForSlot(int slotID, ItemStack itemstack)
	{
		if(slotID == 2)
		{
			return false;
		}
		else if(slotID == 3)
		{
			return itemstack.getItem() == Mekanism.SpeedUpgrade || itemstack.getItem() == Mekanism.EnergyUpgrade;
		}
		else if(slotID == 0)
		{
			return RecipeHandler.isInRecipe(itemstack, getRecipes());
		}
		else if(slotID == 1)
		{
			return ChargeUtils.canBeDischarged(itemstack);
		}

		return false;
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

		markDirty();
		ejectorComponent.onOutput();
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
	public boolean canExtractItem(int slotID, ItemStack itemstack, int side)
	{
		if(slotID == 1)
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
	@Method(modid = "ComputerCraft")
	public String[] getMethodNames()
	{
		return new String[] {"getStored", "getProgress", "isActive", "facing", "canOperate", "getMaxEnergy", "getEnergyNeeded"};
	}

	@Override
	@Method(modid = "ComputerCraft")
	public Object[] callMethod(IComputerAccess computer, ILuaContext context, int method, Object[] arguments) throws Exception
	{
		switch(method)
		{
			case 0:
				return new Object[] {getEnergy()};
			case 1:
				return new Object[] {operatingTicks};
			case 2:
				return new Object[] {isActive};
			case 3:
				return new Object[] {facing};
			case 4:
				return new Object[] {canOperate()};
			case 5:
				return new Object[] {getMaxEnergy()};
			case 6:
				return new Object[] {getMaxEnergy()-getEnergy()};
			default:
				Mekanism.logger.error("Attempted to call unknown method with computer ID " + computer.getID());
				return new Object[] {"Unknown command."};
		}
	}
}
