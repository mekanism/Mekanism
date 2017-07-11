package mekanism.common.util;

import java.util.ArrayList;
import java.util.List;

import mekanism.api.Coord4D;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

public final class MinerUtils
{
	public static List<Block> specialSilkIDs = ListUtils.asList(Blocks.ICE);

	public static List<ItemStack> getDrops(World world, Coord4D obj, boolean silk)
	{
		IBlockState state = obj.getBlockState(world);
		Block block = state.getBlock();

		if(block == null || block.isAir(state, world, obj.getPos()))
		{
			return new ArrayList<ItemStack>();
		}

		if(!silk)
		{
			return block.getDrops(world, obj.getPos(), state, 0);
		}
		else {
			List<ItemStack> ret = new ArrayList<ItemStack>();
			Item item = Item.getItemFromBlock(block);
			int meta = item.getHasSubtypes() ? block.getMetaFromState(state) : 0;
			ret.add(new ItemStack(item, 1, meta));

			if(specialSilkIDs.contains(block) || (block.getDrops(world, obj.getPos(), state, 0) != null && block.getDrops(world, obj.getPos(), state, 0).size() > 0))
			{
				return ret;
			}
		}

		return new ArrayList<ItemStack>();
	}
}
