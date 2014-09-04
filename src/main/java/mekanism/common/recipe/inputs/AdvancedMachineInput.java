package mekanism.common.recipe.inputs;

import mekanism.api.gas.Gas;
import mekanism.api.util.StackUtils;

import net.minecraft.item.ItemStack;

public class AdvancedMachineInput extends MachineInput
{
	public ItemStack itemStack;

	public Gas gasType;

	public AdvancedMachineInput(ItemStack item, Gas gas)
	{
		itemStack = item;
		gasType = gas;
	}

	public boolean isValid()
	{
		return itemStack != null && gasType != null;
	}

	public boolean matches(AdvancedMachineInput input)
	{
		return StackUtils.equalsWildcard(itemStack, input.itemStack) && input.itemStack.stackSize >= itemStack.stackSize;
	}

	@Override
	public int hashIngredients()
	{
		return StackUtils.hashItemStack(itemStack) << 8 | gasType.getID();
	}

	@Override
	public boolean testEquality(MachineInput other)
	{
		return other instanceof AdvancedMachineInput && StackUtils.equalsWildcardWithNBT(itemStack, ((AdvancedMachineInput)other).itemStack) && gasType.getID() == ((AdvancedMachineInput)other).gasType.getID();
	}
}
