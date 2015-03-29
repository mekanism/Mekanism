package mekanism.common;

import java.util.ArrayList;
import java.util.List;

import mekanism.common.multipart.TransmitterType;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;

public final class ItemAttacher
{
	private static final List<ItemStack> attachable = new ArrayList<ItemStack>();

	static
	{
		attachable.add(new ItemStack(Blocks.lever));

		for(TransmitterType type : TransmitterType.values())
		{
			attachable.add(new ItemStack(MekanismItems.PartTransmitter, 1, type.ordinal()));
		}
	}

	public static boolean canAttach(ItemStack itemStack)
	{
		if(itemStack == null)
		{
			return false;
		}

		for(ItemStack stack : attachable)
		{
			if(stack.isItemEqual(itemStack))
			{
				return true;
			}
		}

		return false;
	}
}
