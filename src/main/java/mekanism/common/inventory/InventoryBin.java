package mekanism.common.inventory;

import mekanism.api.util.StackUtils;
import mekanism.common.Tier.BinTier;
import mekanism.common.base.ITierItem;
import mekanism.common.block.states.BlockStateBasic.BasicBlockType;
import mekanism.common.util.ItemDataUtils;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

public class InventoryBin
{
	public ItemStack bin;

	public InventoryBin(ItemStack stack)
	{
		bin = stack;
	}

	public ItemStack getStack()
	{
		if(getItemCount() > 0 && getItemType() != null)
		{
			ItemStack ret = getItemType().copy();
			ret.stackSize = Math.min(getItemType().getMaxStackSize(), getItemCount());

			return ret;
		}

		return null;
	}

	public ItemStack removeStack()
	{
		ItemStack stack = getStack();

		if(stack == null)
		{
			return null;
		}

		if(getTier() != BinTier.CREATIVE)
		{
			setItemCount(getItemCount() - stack.stackSize);
		}
		
		return stack.copy();
	}

	public ItemStack add(ItemStack stack)
	{
		if(isValid(stack) && (getTier() == BinTier.CREATIVE || getItemCount() != getMaxStorage()))
		{
			if(getItemType() == null)
			{
				setItemType(stack);
			}

			if(getTier() != BinTier.CREATIVE)
			{
				if(getItemCount() + stack.stackSize <= getMaxStorage())
				{
					setItemCount(getItemCount() + stack.stackSize);
					return null;
				}
				else {
					ItemStack rejects = getItemType().copy();
					rejects.stackSize = (getItemCount()+stack.stackSize) - getMaxStorage();
	
					setItemCount(getMaxStorage());
	
					return rejects;
				}
			}
			else {
				setItemCount(Integer.MAX_VALUE);
			}
		}

		return stack;
	}

	public boolean isValid(ItemStack stack)
	{
		if(stack == null || stack.stackSize <= 0)
		{
			return false;
		}

		if(BasicBlockType.get(stack) == BasicBlockType.BIN)
		{
			return false;
		}

		if(getItemType() == null)
		{
			return true;
		}

		if(!stack.isItemEqual(getItemType()) || !ItemStack.areItemStackTagsEqual(stack, getItemType()))
		{
			return false;
		}

		return true;
	}
	
	public int getMaxStorage()
	{
		return getTier().storage;
	}
	
	public BinTier getTier()
	{
		return BinTier.values()[((ITierItem)bin.getItem()).getBaseTier(bin).ordinal()];
	}

	public int getItemCount()
	{
		return ItemDataUtils.getInt(bin, "itemCount");
	}

	public void setItemCount(int count)
	{
		ItemDataUtils.setInt(bin, "itemCount", Math.max(0, count));

		if(getItemCount() == 0)
		{
			setItemType(null);
		}
	}

	public ItemStack getItemType()
	{
		if(getItemCount() == 0)
		{
			return null;
		}

		return ItemStack.loadItemStackFromNBT(ItemDataUtils.getCompound(bin, "storedItem"));
	}

	public void setItemType(ItemStack stack)
	{
		if(stack == null)
		{
			ItemDataUtils.removeData(bin, "storedItem");
			return;
		}

		ItemDataUtils.setCompound(bin, "storedItem", StackUtils.size(stack, 1).writeToNBT(new NBTTagCompound()));
	}
}
