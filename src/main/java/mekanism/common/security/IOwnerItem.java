package mekanism.common.security;

import java.util.UUID;

import net.minecraft.item.ItemStack;

public interface IOwnerItem
{
	public UUID getOwnerUUID(ItemStack stack);
	
	public void setOwnerUUID(ItemStack stack, UUID owner);
	
	public boolean hasOwner(ItemStack stack);
}
