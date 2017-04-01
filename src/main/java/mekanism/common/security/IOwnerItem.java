package mekanism.common.security;

import mekanism.common.util.ItemDataUtils;
import net.minecraft.inventory.ItemStackHelper;
import net.minecraft.item.ItemStack;

import java.util.UUID;

public interface IOwnerItem
{
	default UUID getOwner(ItemStack stack)
	{
		if (ItemDataUtils.hasID(stack, "ownerUUID"))
		{
			return ItemDataUtils.getUUID(stack, "ownerUUID");
		}
		else if (ItemDataUtils.hasData(stack, "owner")) {
			//TODO Fallback to old data and convert it
		}

		return null;
	}

	default void setOwner(ItemStack stack, UUID owner)
	{
		if(owner == null) {
			ItemDataUtils.removeData(stack, "ownerUUID");
			return;
		}

		ItemDataUtils.setUUID(stack, "ownerUUID", owner);
	}

	boolean hasOwner(ItemStack stack);
}
