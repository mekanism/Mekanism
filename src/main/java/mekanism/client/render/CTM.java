package mekanism.client.render;

import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.IBlockAccess;

import com.google.common.base.Predicate;

/**
 * CTM Logic adapted from Chisel.
 * Code licensed under GPLv2
 * @author AUTOMATIC_MAIDEN, asie, pokefenn, unpairedbracket
 */
public class CTM
{
	static int submaps[][] = {
			{16, 17, 18, 19},
			{16, 9, 18, 13},
			{8, 9, 12, 13},
			{8, 17, 12, 19},
			{16, 9, 6, 15},
			{8, 17, 14, 7},
			{2, 11, 6, 15},
			{8, 9, 14, 15},
			{10, 1, 14, 15},
			{10, 11, 14, 5},
			{0, 11, 4, 15},
			{0, 1, 14, 15},
			{},
			{},
			{},
			{},
			{16, 17, 6, 7},
			{16, 9, 6, 5},
			{8, 9, 4, 5},
			{8, 17, 4, 7},
			{2, 11, 18, 13},
			{10, 3, 12, 19},
			{10, 11, 12, 13},
			{10, 3, 14, 7},
			{0, 11, 14, 15},
			{10, 11, 4, 15},
			{10, 11, 4, 5},
			{10, 1, 14, 5},
			{},
			{},
			{},
			{},
			{2, 3, 6, 7},
			{2, 1, 6, 5},
			{0, 1, 4, 5},
			{0, 3, 4, 7},
			{2, 11, 6, 5},
			{8, 9, 4, 15},
			{2, 1, 6, 15},
			{8, 9, 14, 5},
			{0, 1, 4, 15},
			{0, 1, 14, 5},
			{10, 1, 4, 15},
			{0, 11, 14, 5},
			{},
			{},
			{},
			{},
			{2, 3, 18, 19},
			{2, 1, 18, 13},
			{0, 1, 12, 13},
			{0, 3, 12, 19},
			{10, 1, 12, 13},
			{0, 3, 14, 7},
			{0, 11, 12, 13},
			{10, 3, 4, 7},
			{0, 11, 4, 5},
			{10, 1, 4, 5},
			{10, 11, 14, 15},
			{0, 1, 4, 5},
			{},
			{},
			{},
			{},
	};

	public static int[] getSubmapIndices(IBlockAccess world, BlockPos pos, EnumFacing side, HashMap<Block, Predicate<IBlockState>> blockMetas)
	{
		int index = getTexture(world, pos, side, blockMetas);

		return submaps[index];
	}

	public static int getTexture(IBlockAccess world, BlockPos pos, EnumFacing side, HashMap<Block, Predicate<IBlockState>> blockMetas)
	{
		if(world == null)
		{
			return 0;
		}

		int texture = 0;

		boolean b[] = new boolean[6];
		
		if(side == EnumFacing.DOWN || side == EnumFacing.UP)
		{
			b[0] = isConnected(world, pos.west(), side, blockMetas);
			b[1] = isConnected(world, pos.east(), side, blockMetas);
			b[2] = isConnected(world, pos.north(), side, blockMetas);
			b[3] = isConnected(world, pos.south(), side, blockMetas);
		} 
		else if(side == EnumFacing.NORTH)
		{
			b[0] = isConnected(world, pos.east(), side, blockMetas);
			b[1] = isConnected(world, pos.west(), side, blockMetas);
			b[2] = isConnected(world, pos.down(), side, blockMetas);
			b[3] = isConnected(world, pos.up(), side, blockMetas);
		} 
		else if(side == EnumFacing.SOUTH)
		{
			b[0] = isConnected(world, pos.west(), side, blockMetas);
			b[1] = isConnected(world, pos.east(), side, blockMetas);
			b[2] = isConnected(world, pos.down(), side, blockMetas);
			b[3] = isConnected(world, pos.up(), side, blockMetas);
		} 
		else if(side == EnumFacing.WEST)
		{
			b[0] = isConnected(world, pos.north(), side, blockMetas);
			b[1] = isConnected(world, pos.south(), side, blockMetas);
			b[2] = isConnected(world, pos.down(), side, blockMetas);
			b[3] = isConnected(world, pos.up(), side, blockMetas);
		} 
		else if(side == EnumFacing.EAST)
		{
			b[0] = isConnected(world, pos.south(), side, blockMetas);
			b[1] = isConnected(world, pos.north(), side, blockMetas);
			b[2] = isConnected(world, pos.down(), side, blockMetas);
			b[3] = isConnected(world, pos.up(), side, blockMetas);
		}
		
		if(b[0] & !b[1] & !b[2] & !b[3])
		{
			texture = 3;
		}
		else if(!b[0] & b[1] & !b[2] & !b[3])
		{
			texture = 1;
		}
		else if(!b[0] & !b[1] & b[2] & !b[3])
		{
			texture = 16;
		}
		else if(!b[0] & !b[1] & !b[2] & b[3])
		{
			texture = 48;
		}
		else if(b[0] & b[1] & !b[2] & !b[3])
		{
			texture = 2;
		}
		else if(!b[0] & !b[1] & b[2] & b[3])
		{
			texture = 32;
		}
		else if(b[0] & !b[1] & b[2] & !b[3])
		{
			texture = 19;
		}
		else if(b[0] & !b[1] & !b[2] & b[3])
		{
			texture = 51;
		}
		else if(!b[0] & b[1] & b[2] & !b[3])
		{
			texture = 17;
		}
		else if(!b[0] & b[1] & !b[2] & b[3])
		{
			texture = 49;
		}
		else if(!b[0] & b[1] & b[2] & b[3])
		{
			texture = 33;
		}
		else if(b[0] & !b[1] & b[2] & b[3])
		{
			texture = 35;
		}
		else if(b[0] & b[1] & !b[2] & b[3])
		{
			texture = 50;
		}
		else if(b[0] & b[1] & b[2] & !b[3])
		{
			texture = 18;
		}
		else if(b[0] & b[1] & b[2] & b[3])
		{
			texture = 34;
		}

		boolean b2[] = new boolean[6];
		
		if(side == EnumFacing.DOWN || side == EnumFacing.UP)
		{
			b2[0] = !isConnected(world, pos.south().east(), side, blockMetas);
			b2[1] = !isConnected(world, pos.south().west(), side, blockMetas);
			b2[2] = !isConnected(world, pos.north().east(), side, blockMetas);
			b2[3] = !isConnected(world, pos.north().west(), side, blockMetas);
		} 
		else if(side == EnumFacing.NORTH)
		{
			b2[0] = !isConnected(world, pos.down().west(), side, blockMetas);
			b2[1] = !isConnected(world, pos.down().east(), side, blockMetas);
			b2[2] = !isConnected(world, pos.up().west(), side, blockMetas);
			b2[3] = !isConnected(world, pos.up().east(), side, blockMetas);
		} 
		else if(side == EnumFacing.SOUTH)
		{
			b2[0] = !isConnected(world, pos.down().east(), side, blockMetas);
			b2[1] = !isConnected(world, pos.down().west(), side, blockMetas);
			b2[2] = !isConnected(world, pos.up().east(), side, blockMetas);
			b2[3] = !isConnected(world, pos.up().west(), side, blockMetas);
		} 
		else if(side == EnumFacing.WEST)
		{
			b2[0] = !isConnected(world, pos.down().south(), side, blockMetas);
			b2[1] = !isConnected(world, pos.down().north(), side, blockMetas);
			b2[2] = !isConnected(world, pos.up().south(), side, blockMetas);
			b2[3] = !isConnected(world, pos.up().north(), side, blockMetas);
		} 
		else if(side == EnumFacing.EAST)
		{
			b2[0] = !isConnected(world, pos.down().north(), side, blockMetas);
			b2[1] = !isConnected(world, pos.down().south(), side, blockMetas);
			b2[2] = !isConnected(world, pos.up().north(), side, blockMetas);
			b2[3] = !isConnected(world, pos.up().south(), side, blockMetas);
		}

		if(texture == 17 && b2[0])
		{
			texture = 4;
		}
		
		if(texture == 19 && b2[1])
		{
			texture = 5;
		}
		
		if(texture == 49 && b2[2])
		{
			texture = 20;
		}
		
		if(texture == 51 && b2[3])
		{
			texture = 21;
		}

		if(texture == 18 && b2[0] && b2[1])
		{
			texture = 7;
		}
		
		if(texture == 33 && b2[0] && b2[2])
		{
			texture = 6;
		}
		
		if(texture == 35 && b2[3] && b2[1])
		{
			texture = 23;
		}
		
		if(texture == 50 && b2[3] && b2[2])
		{
			texture = 22;
		}

		if(texture == 18 && !b2[0] && b2[1])
		{
			texture = 39;
		}
		
		if(texture == 33 && b2[0] && !b2[2])
		{
			texture = 38;
		}
		
		if(texture == 35 && !b2[3] && b2[1])
		{
			texture = 53;
		}
		
		if(texture == 50 && b2[3] && !b2[2])
		{
			texture = 52;
		}

		if(texture == 18 && b2[0] && !b2[1])
		{
			texture = 37;
		}
		
		if(texture == 33 && !b2[0] && b2[2])
		{
			texture = 36;
		}
		
		if(texture == 35 && b2[3] && !b2[1])
		{
			texture = 55;
		}
		
		if(texture == 50 && !b2[3] && b2[2])
		{
			texture = 54;
		}

		if(texture == 34 && b2[0] && b2[1] && b2[2] && b2[3])
		{
			texture = 58;
		}

		if(texture == 34 && !b2[0] && b2[1] && b2[2] && b2[3])
		{
			texture = 9;
		}
		
		if(texture == 34 && b2[0] && !b2[1] && b2[2] && b2[3])
		{
			texture = 25;
		}
		
		if(texture == 34 && b2[0] && b2[1] && !b2[2] && b2[3])
		{
			texture = 8;
		}
		
		if(texture == 34 && b2[0] && b2[1] && b2[2] && !b2[3])
		{
			texture = 24;
		}

		if(texture == 34 && b2[0] && b2[1] && !b2[2] && !b2[3])
		{
			texture = 11;
		}
		
		if(texture == 34 && !b2[0] && !b2[1] && b2[2] && b2[3])
		{
			texture = 26;
		}
		
		if(texture == 34 && !b2[0] && b2[1] && !b2[2] && b2[3])
		{
			texture = 27;
		}
		
		if(texture == 34 && b2[0] && !b2[1] && b2[2] && !b2[3])
		{
			texture = 10;
		}

		if(texture == 34 && b2[0] && !b2[1] && !b2[2] && b2[3])
		{
			texture = 42;
		}
		if(texture == 34 && !b2[0] && b2[1] && b2[2] && !b2[3])
		{
			texture = 43;
		}

		if(texture == 34 && b2[0] && !b2[1] && !b2[2] && !b2[3])
		{
			texture = 40;
		}
		
		if(texture == 34 && !b2[0] && b2[1] && !b2[2] && !b2[3])
		{
			texture = 41;
		}
		
		if(texture == 34 && !b2[0] && !b2[1] && b2[2] && !b2[3])
		{
			texture = 56;
		}
		
		if(texture == 34 && !b2[0] && !b2[1] && !b2[2] && b2[3])
		{
			texture = 57;
		}
		
		return texture;
	}

	public static boolean isConnected(IBlockAccess world, BlockPos pos1, EnumFacing side, HashMap<Block, Predicate<IBlockState>> blockPredicate)
	{
		BlockPos pos2 = pos1.offset(side);

		IBlockState state1 = world.getBlockState(pos1);
		IBlockState state2 = world.getBlockState(pos2);

		Block block1 = state1.getBlock();
		Block block2 = state2.getBlock();

		boolean validBlockPredicate1 = false;
		boolean invalidBlockPredicate2 = true;

		for(Entry<Block, Predicate<IBlockState>> entry : blockPredicate.entrySet())
		{
			if(!validBlockPredicate1)
				validBlockPredicate1 = block1.equals(entry.getKey()) && entry.getValue().apply(state1);

			if(invalidBlockPredicate2)
				invalidBlockPredicate2 = !(block2.equals(entry.getKey()) && entry.getValue().apply(state2));

			if(!invalidBlockPredicate2)
				return false;
		}

		return validBlockPredicate1 && invalidBlockPredicate2;
	}
}