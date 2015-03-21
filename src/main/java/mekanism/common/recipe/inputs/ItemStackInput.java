package mekanism.common.recipe.inputs;

import mekanism.api.util.StackUtils;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.oredict.OreDictionary;

public class ItemStackInput extends MachineInput<ItemStackInput>
{
	public ItemStack ingredient;

	public ItemStackInput(ItemStack stack)
	{
		ingredient = stack;
	}
	
	public ItemStackInput() {}
	
	@Override
	public void load(NBTTagCompound nbtTags)
	{
		ingredient = ItemStack.loadItemStackFromNBT(nbtTags.getCompoundTag("input"));
	}

	@Override
	public ItemStackInput copy()
	{
		return new ItemStackInput(ingredient.copy());
	}

	@Override
	public boolean isValid()
	{
		return ingredient != null;
	}

	public ItemStackInput wildCopy()
	{
		return new ItemStackInput(new ItemStack(ingredient.getItem(), ingredient.stackSize, OreDictionary.WILDCARD_VALUE));
	}

	public boolean useItemStackFromInventory(ItemStack[] inventory, int index, boolean deplete)
	{
		if(StackUtils.contains(inventory[index], ingredient))
		{
			if(deplete)
			{
				inventory[index] = StackUtils.subtract(inventory[index], ingredient);
			}
			
			return true;
		}
		
		return false;
	}

	@Override
	public int hashIngredients()
	{
		return StackUtils.hashItemStack(ingredient);
	}

	@Override
	public boolean testEquality(ItemStackInput other)
	{
		return StackUtils.equalsWildcardWithNBT(ingredient, other.ingredient);
	}

	@Override
	public boolean isInstance(Object other)
	{
		return other instanceof ItemStackInput;
	}
}
