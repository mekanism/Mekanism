package mekanism.common.recipe.outputs;

import mekanism.api.gas.GasStack;
import mekanism.api.gas.GasTank;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

public class PressurizedOutput extends MachineOutput<PressurizedOutput>
{
	private ItemStack itemOutput;
	private GasStack gasOutput;

	public PressurizedOutput(ItemStack item, GasStack gas)
	{
		itemOutput = item;
		gasOutput = gas;
	}
	
	public PressurizedOutput() {}
	
	@Override
	public void load(NBTTagCompound nbtTags)
	{
		itemOutput = ItemStack.loadItemStackFromNBT(nbtTags.getCompoundTag("itemOutput"));
		gasOutput = GasStack.readFromNBT(nbtTags.getCompoundTag("gasOutput"));
	}

	public boolean canFillTank(GasTank tank)
	{
		return tank.canReceive(gasOutput.getGas()) && tank.getNeeded() >= gasOutput.amount;
	}

	public boolean canAddProducts(ItemStack[] inventory, int index)
	{
		return inventory[index] == null || (inventory[index].isItemEqual(itemOutput) && inventory[index].stackSize + itemOutput.stackSize <= inventory[index].getMaxStackSize());
	}

	public void fillTank(GasTank tank)
	{
		tank.receive(gasOutput, true);
	}

	public void addProducts(ItemStack[] inventory, int index)
	{
		if(inventory[index] == null)
		{
			inventory[index] = itemOutput.copy();
		}
		else if(inventory[index].isItemEqual(itemOutput))
		{
			inventory[index].stackSize += itemOutput.stackSize;
		}
	}

	public boolean applyOutputs(ItemStack[] inventory, int index, GasTank tank, boolean doEmit)
	{
		if(canFillTank(tank) && canAddProducts(inventory, index))
		{
			if(doEmit)
			{
				fillTank(tank);
				addProducts(inventory, index);
			}
			return true;
		}
		return false;
	}

	public ItemStack getItemOutput()
	{
		return itemOutput;
	}

	public GasStack getGasOutput()
	{
		return gasOutput;
	}

	@Override
	public PressurizedOutput copy()
	{
		return new PressurizedOutput(itemOutput.copy(), gasOutput.copy());
	}
}
