package mekanism.common;

import java.util.ArrayList;
import java.util.List;

import mekanism.common.multipart.TransmitterType;
import net.minecraft.block.Block;
import net.minecraft.item.ItemStack;

public final class ItemAttacher
{
	private static final List<ItemStack> attachable = new ArrayList<ItemStack>();

	static
	{
		attachable.add(new ItemStack(Blocks.lever));
		attachable.add(new ItemStack(Blocks.torchRedstoneIdle));
		attachable.add(new ItemStack(Blocks.torchWood));

		for(TransmitterType type : TransmitterType.values())
		{
			attachable.add(new ItemStack(Mekanism.PartTransmitter, 1, type.ordinal()));
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
