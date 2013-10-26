package mekanism.common.transporter;

import net.minecraft.item.ItemStack;

public final class SlotInfo
{
	public ItemStack itemStack;
	public int slotID;
	
	public SlotInfo(ItemStack stack, int i)
	{
		itemStack = stack;
		slotID = i;
	}
}
