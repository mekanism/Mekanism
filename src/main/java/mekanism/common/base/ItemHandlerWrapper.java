package mekanism.common.base;

import net.minecraft.inventory.ISidedInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.items.wrapper.SidedInvWrapper;

import javax.annotation.Nonnull;

public class ItemHandlerWrapper extends SidedInvWrapper
{
	public ItemHandlerWrapper(ISidedInventory inv, EnumFacing side)
	{
		super(inv, side);
	}

	@Override
	public boolean isItemValid(int slot, @Nonnull ItemStack stack)
	{
		int slot1 = getSlot(inv, slot, side);
		return slot1 == -1 ? false : inv.isItemValidForSlot(slot1, stack);
	}
}
