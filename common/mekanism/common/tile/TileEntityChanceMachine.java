package mekanism.common.tile;

import java.util.Map;

import mekanism.api.ChanceOutput;
import mekanism.api.EnumColor;
import mekanism.common.Mekanism;
import mekanism.common.SideData;
import mekanism.common.recipe.RecipeHandler;
import mekanism.common.tile.component.TileComponentEjector;
import mekanism.common.tile.component.TileComponentUpgrade;
import mekanism.common.util.ChargeUtils;
import mekanism.common.util.InventoryUtils;
import mekanism.common.util.MekanismUtils;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import dan200.computer.api.IComputerAccess;
import dan200.computer.api.ILuaContext;

public class TileEntityChanceMachine extends TileEntityBasicMachine
{
	public TileEntityChanceMachine(String soundPath, String name, ResourceLocation location, double perTick, int ticksRequired, double maxEnergy)
	{
		super(soundPath, name, location, perTick, ticksRequired, maxEnergy);

		sideOutputs.add(new SideData(EnumColor.GREY, InventoryUtils.EMPTY));
		sideOutputs.add(new SideData(EnumColor.DARK_RED, new int[] {0}));
		sideOutputs.add(new SideData(EnumColor.DARK_GREEN, new int[] {1}));
		sideOutputs.add(new SideData(EnumColor.DARK_BLUE, new int[] {2, 4}));
		sideOutputs.add(new SideData(EnumColor.ORANGE, new int[] {3}));

		sideConfig = new byte[] {2, 1, 0, 0, 4, 3};

		inventory = new ItemStack[5];

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

			if(canOperate() && MekanismUtils.canFunction(this) && getEnergy() >= MekanismUtils.getEnergyPerTick(getSpeedMultiplier(), getEnergyMultiplier(), ENERGY_PER_TICK))
			{
				setActive(true);

				if((operatingTicks+1) < MekanismUtils.getTicks(getSpeedMultiplier(), TICKS_REQUIRED))
				{
					operatingTicks++;
					electricityStored -= MekanismUtils.getEnergyPerTick(getSpeedMultiplier(), getEnergyMultiplier(), ENERGY_PER_TICK);
				}
				else if((operatingTicks+1) >= MekanismUtils.getTicks(getSpeedMultiplier(), TICKS_REQUIRED))
				{
					operate();

					operatingTicks = 0;
					electricityStored -= MekanismUtils.getEnergyPerTick(getSpeedMultiplier(), getEnergyMultiplier(), ENERGY_PER_TICK);
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
		if(slotID == 3)
		{
			return itemstack.itemID == Mekanism.SpeedUpgrade.itemID || itemstack.itemID == Mekanism.EnergyUpgrade.itemID;
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
		ChanceOutput output = RecipeHandler.getChanceOutput(inventory[0], true, getRecipes());

		if(inventory[0].stackSize <= 0)
		{
			inventory[0] = null;
		}

		if(output.hasPrimary())
		{
			if(inventory[2] == null)
			{
				inventory[2] = output.primaryOutput;
			}
			else {
				inventory[2].stackSize += output.primaryOutput.stackSize;
			}
		}

		if(output.hasSecondary() && output.checkSecondary())
		{
			if(inventory[4] == null)
			{
				inventory[4] = output.secondaryOutput;
			}
			else {
				inventory[4].stackSize += output.secondaryOutput.stackSize;
			}
		}

		onInventoryChanged();
		ejectorComponent.onOutput();
	}

	@Override
	public boolean canOperate()
	{
		if(inventory[0] == null)
		{
			return false;
		}

		ChanceOutput output = RecipeHandler.getChanceOutput(inventory[0], false, getRecipes());

		if(output == null)
		{
			return false;
		}

		if(output.hasPrimary())
		{
			if(inventory[2] != null)
			{
				if(!inventory[2].isItemEqual(output.primaryOutput))
				{
					return false;
				}
				else {
					if(inventory[2].stackSize + output.primaryOutput.stackSize > inventory[2].getMaxStackSize())
					{
						return false;
					}
				}
			}
		}

		if(output.hasSecondary())
		{
			if(inventory[4] != null)
			{
				if(!inventory[4].isItemEqual(output.secondaryOutput))
				{
					return false;
				}
				else {
					if(inventory[4].stackSize + output.secondaryOutput.stackSize > inventory[4].getMaxStackSize())
					{
						return false;
					}
				}
			}
		}

		return true;
	}

	@Override
	public boolean canExtractItem(int slotID, ItemStack itemstack, int side)
	{
		if(slotID == 1)
		{
			return ChargeUtils.canBeOutputted(itemstack, false);
		}
		else if(slotID == 2 || slotID == 4)
		{
			return true;
		}

		return false;
	}

	@Override
	public Map getRecipes()
	{
		return null;
	}

	@Override
	public String[] getMethodNames()
	{
		return null;
	}

	@Override
	public Object[] callMethod(IComputerAccess computer, ILuaContext context, int method, Object[] arguments) throws Exception
	{
		return null;
	}
}
