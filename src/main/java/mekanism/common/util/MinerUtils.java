package mekanism.common.util;

import java.util.ArrayList;
import java.util.List;

import mekanism.api.Coord4D;
import mekanism.api.util.ListUtils;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

public final class MinerUtils
{
	public static List<Block> specialSilkIDs = ListUtils.asList(Blocks.ice);

	public static List<ItemStack> getDrops(World world, Coord4D obj, boolean silk)
	{
		IBlockState state = obj.getBlockState(world);
		Block block = state.getBlock();

		if(block == null)
		{
			return new ArrayList<ItemStack>();
		}

		if(block.isAir(world, obj))
		{
			return new ArrayList<ItemStack>();
		}


		if(!silk)
		{
			return block.getDrops(world, obj, state, 0);
		}
		else {
			List<ItemStack> ret = new ArrayList<ItemStack>();
			ret.add(new ItemStack(block, 1, block.getMetaFromState(state)));

			if(specialSilkIDs.contains(block) || (block.getDrops(world, obj, state, 0) != null && block.getDrops(world, obj, state, 0).size() > 0))
			{
				return ret;
			}
		}

		return new ArrayList<ItemStack>();
	}
}
