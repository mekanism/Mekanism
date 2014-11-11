package mekanism.common.inventory.slot;

import java.util.Collection;
import java.util.Collections;

import mekanism.api.gas.Gas;
import mekanism.api.gas.IGasItem;

import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

public class SlotStorageTank extends Slot
{
	public Collection<Gas> types;
	public boolean acceptsAllGasses;

	public SlotStorageTank(IInventory inventory, int index, int x, int y)
	{
		super(inventory, index, x, y);
		types = null;
		acceptsAllGasses = true;
	}

	public SlotStorageTank(IInventory inventory, Gas gas, boolean all, int index, int x, int y)
	{
		super(inventory, index, x, y);
		types = Collections.singletonList(gas);
		acceptsAllGasses = all;
	}

	public SlotStorageTank(IInventory inventory, Collection<Gas> gases, boolean all, int index, int x, int y)
	{
		super(inventory, index, x, y);
		types = gases;
		acceptsAllGasses = all;
	}

	@Override
	public boolean isItemValid(ItemStack itemstack)
	{
		if(acceptsAllGasses)
		{
			return itemstack.getItem() instanceof IGasItem;
		}

		if(itemstack.getItem() instanceof IGasItem)
		{
			return ((IGasItem)itemstack.getItem()).getGas(itemstack) == null || types.contains(((IGasItem)itemstack.getItem()).getGas(itemstack).getGas());
		}

		return false;
	}
}
