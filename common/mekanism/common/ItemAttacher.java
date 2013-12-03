package mekanism.common;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

public final class ItemAttacher 
{
	private static final List<ItemStack> attachable = new ArrayList<ItemStack>();
	
	static {
		attachable.add(new ItemStack(Block.lever));
		attachable.add(new ItemStack(Block.torchRedstoneIdle));
		attachable.add(new ItemStack(Block.torchWood));
		
		attachable.add(new ItemStack(Mekanism.Transmitter, 1, 0));
		attachable.add(new ItemStack(Mekanism.Transmitter, 1, 1));
		attachable.add(new ItemStack(Mekanism.Transmitter, 1, 2));
		attachable.add(new ItemStack(Mekanism.Transmitter, 1, 3));
		attachable.add(new ItemStack(Mekanism.Transmitter, 1, 4));
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
