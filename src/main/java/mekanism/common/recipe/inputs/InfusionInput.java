package mekanism.common.recipe.inputs;

import mekanism.api.infuse.InfuseRegistry;
import mekanism.api.infuse.InfuseType;
import mekanism.api.util.StackUtils;
import mekanism.common.InfuseStorage;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

/**
 * An infusion input, containing the type of and amount of infuse the operation requires, as well as the input ItemStack.
 * @author AidanBrady
 *
 */
public class InfusionInput extends MachineInput<InfusionInput>
{
	public InfuseStorage infuse;

	/** The input ItemStack */
	public ItemStack inputStack;

	public InfusionInput(InfuseStorage storage, ItemStack itemStack)
	{
		infuse = new InfuseStorage(storage.type, storage.amount);
		inputStack = itemStack;
	}

	public InfusionInput(InfuseType infusionType, int required, ItemStack itemStack)
	{
		infuse = new InfuseStorage(infusionType, required);
		inputStack = itemStack;
	}
	
	@Override
	public void load(NBTTagCompound nbtTags)
	{
		inputStack = ItemStack.loadItemStackFromNBT(nbtTags.getCompoundTag("input"));
		InfuseType type = InfuseRegistry.get(nbtTags.getString("infuseType"));
		int amount = nbtTags.getInteger("infuseAmount");
		infuse = new InfuseStorage(type, amount);
	}
	
	public InfusionInput() {}

	@Override
	public InfusionInput copy()
	{
		return new InfusionInput(infuse.type, infuse.amount, inputStack.copy());
	}

	@Override
	public boolean isValid()
	{
		return infuse.type != null && inputStack != null;
	}

	public boolean use(ItemStack[] inventory, int index, InfuseStorage infuseStorage, boolean deplete)
	{
		if(StackUtils.contains(inventory[index], inputStack) && infuseStorage.contains(infuse))
		{
			if(deplete)
			{
				inventory[index] = StackUtils.subtract(inventory[index], inputStack);
				infuseStorage.subtract(infuse);
			}
			return true;
		}
		return false;
	}

	@Override
	public int hashIngredients()
	{
		return infuse.type.unlocalizedName.hashCode() << 8 | StackUtils.hashItemStack(inputStack);
	}

	@Override
	public boolean testEquality(InfusionInput other)
	{
		if(!isValid())
		{
			return !other.isValid();
		}
		return infuse.type == other.infuse.type && StackUtils.equalsWildcardWithNBT(inputStack, other.inputStack);
	}

	@Override
	public boolean isInstance(Object other)
	{
		return other instanceof InfusionInput;
	}
}
