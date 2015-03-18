package mekanism.client.render;

import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import net.minecraft.block.Block;
import net.minecraft.world.IBlockAccess;

/**
 * CTM Logic adapted from Chisel. Code licensed under GPLv2
 * 
 * @author AUTOMATIC_MAIDEN, asie, pokefenn, unpairedbracket
 */
public class CTM
{
	static int submaps[][] = { {16, 17, 18, 19}, {16, 9, 18, 13}, {8, 9, 12, 13}, {8, 17, 12, 19}, {16, 9, 6, 15}, {8, 17, 14, 7}, {2, 11, 6, 15},
			{8, 9, 14, 15}, {10, 1, 14, 15}, {10, 11, 14, 5}, {0, 11, 4, 15}, {0, 1, 14, 15}, {}, {}, {}, {}, {16, 17, 6, 7}, {16, 9, 6, 5}, {8, 9, 4, 5},
			{8, 17, 4, 7}, {2, 11, 18, 13}, {10, 3, 12, 19}, {10, 11, 12, 13}, {10, 3, 14, 7}, {0, 11, 14, 15}, {10, 11, 4, 15}, {10, 11, 4, 5},
			{10, 1, 14, 5}, {}, {}, {}, {}, {2, 3, 6, 7}, {2, 1, 6, 5}, {0, 1, 4, 5}, {0, 3, 4, 7}, {2, 11, 6, 5}, {8, 9, 4, 15}, {2, 1, 6, 15}, {8, 9, 14, 5},
			{0, 1, 4, 15}, {0, 1, 14, 5}, {10, 1, 4, 15}, {0, 11, 14, 5}, {}, {}, {}, {}, {2, 3, 18, 19}, {2, 1, 18, 13}, {0, 1, 12, 13}, {0, 3, 12, 19},
			{10, 1, 12, 13}, {0, 3, 14, 7}, {0, 11, 12, 13}, {10, 3, 4, 7}, {0, 11, 4, 5}, {10, 1, 4, 5}, {10, 11, 14, 15}, {0, 1, 4, 5}, {}, {}, {}, {},};

	public static int[] getSubmapIndices(IBlockAccess world, int x, int y, int z, int side, HashMap<Block, List<Integer>> blockMetas, boolean convexConnections)
	{
		int index = getTexture(world, x, y, z, side, blockMetas, convexConnections);

		return submaps[index];
	}

	public static int getTexture(IBlockAccess world, int x, int y, int z, int side, HashMap<Block, List<Integer>> blockMetas, boolean convexConnections)
	{
		if(world == null)
		{
			return 0;
		}

		int texture = 0;

		boolean b[] = new boolean[6];

		if(side <= 1)
		{
			b[0] = isConnected(world, x - 1, y, z, side, blockMetas, convexConnections);
			b[1] = isConnected(world, x + 1, y, z, side, blockMetas, convexConnections);
			b[2] = isConnected(world, x, y, z + 1, side, blockMetas, convexConnections);
			b[3] = isConnected(world, x, y, z - 1, side, blockMetas, convexConnections);
		}
		else if(side == 2)
		{
			b[0] = isConnected(world, x + 1, y, z, side, blockMetas, convexConnections);
			b[1] = isConnected(world, x - 1, y, z, side, blockMetas, convexConnections);
			b[2] = isConnected(world, x, y - 1, z, side, blockMetas, convexConnections);
			b[3] = isConnected(world, x, y + 1, z, side, blockMetas, convexConnections);
		}
		else if(side == 3)
		{
			b[0] = isConnected(world, x - 1, y, z, side, blockMetas, convexConnections);
			b[1] = isConnected(world, x + 1, y, z, side, blockMetas, convexConnections);
			b[2] = isConnected(world, x, y - 1, z, side, blockMetas, convexConnections);
			b[3] = isConnected(world, x, y + 1, z, side, blockMetas, convexConnections);
		}
		else if(side == 4)
		{
			b[0] = isConnected(world, x, y, z - 1, side, blockMetas, convexConnections);
			b[1] = isConnected(world, x, y, z + 1, side, blockMetas, convexConnections);
			b[2] = isConnected(world, x, y - 1, z, side, blockMetas, convexConnections);
			b[3] = isConnected(world, x, y + 1, z, side, blockMetas, convexConnections);
		}
		else if(side == 5)
		{
			b[0] = isConnected(world, x, y, z + 1, side, blockMetas, convexConnections);
			b[1] = isConnected(world, x, y, z - 1, side, blockMetas, convexConnections);
			b[2] = isConnected(world, x, y - 1, z, side, blockMetas, convexConnections);
			b[3] = isConnected(world, x, y + 1, z, side, blockMetas, convexConnections);
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

		if(side <= 1)
		{
			b2[0] = !isConnected(world, x + 1, y, z + 1, side, blockMetas, convexConnections);
			b2[1] = !isConnected(world, x - 1, y, z + 1, side, blockMetas, convexConnections);
			b2[2] = !isConnected(world, x + 1, y, z - 1, side, blockMetas, convexConnections);
			b2[3] = !isConnected(world, x - 1, y, z - 1, side, blockMetas, convexConnections);
		}
		else if(side == 2)
		{
			b2[0] = !isConnected(world, x - 1, y - 1, z, side, blockMetas, convexConnections);
			b2[1] = !isConnected(world, x + 1, y - 1, z, side, blockMetas, convexConnections);
			b2[2] = !isConnected(world, x - 1, y + 1, z, side, blockMetas, convexConnections);
			b2[3] = !isConnected(world, x + 1, y + 1, z, side, blockMetas, convexConnections);
		}
		else if(side == 3)
		{
			b2[0] = !isConnected(world, x + 1, y - 1, z, side, blockMetas, convexConnections);
			b2[1] = !isConnected(world, x - 1, y - 1, z, side, blockMetas, convexConnections);
			b2[2] = !isConnected(world, x + 1, y + 1, z, side, blockMetas, convexConnections);
			b2[3] = !isConnected(world, x - 1, y + 1, z, side, blockMetas, convexConnections);
		}
		else if(side == 4)
		{
			b2[0] = !isConnected(world, x, y - 1, z + 1, side, blockMetas, convexConnections);
			b2[1] = !isConnected(world, x, y - 1, z - 1, side, blockMetas, convexConnections);
			b2[2] = !isConnected(world, x, y + 1, z + 1, side, blockMetas, convexConnections);
			b2[3] = !isConnected(world, x, y + 1, z - 1, side, blockMetas, convexConnections);
		}
		else if(side == 5)
		{
			b2[0] = !isConnected(world, x, y - 1, z - 1, side, blockMetas, convexConnections);
			b2[1] = !isConnected(world, x, y - 1, z + 1, side, blockMetas, convexConnections);
			b2[2] = !isConnected(world, x, y + 1, z - 1, side, blockMetas, convexConnections);
			b2[3] = !isConnected(world, x, y + 1, z + 1, side, blockMetas, convexConnections);
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

	public static boolean isConnected(IBlockAccess world, int x, int y, int z, int side, HashMap<Block, List<Integer>> blockMetas, boolean convexConnections)
	{
		int x2 = x, y2 = y, z2 = z;

		switch(side)
		{
			case 0:
				y2--;
				break;
			case 1:
				y2++;
				break;
			case 2:
				z2--;
				break;
			case 3:
				z2++;
				break;
			case 4:
				x2--;
				break;
			case 5:
				x2++;
				break;
		}

		Block block1 = world.getBlock(x, y, z);
		Block block2 = world.getBlock(x2, y2, z2);

		int meta1 = world.getBlockMetadata(x, y, z);
		int meta2 = world.getBlockMetadata(x2, y2, z2);

		boolean validBlockMeta1 = false;
		boolean invalidBlockMeta2 = true;

		for(Entry<Block, List<Integer>> entry : blockMetas.entrySet())
		{
			validBlockMeta1 |= block1.equals(entry.getKey()) && entry.getValue().contains(meta1);

			invalidBlockMeta2 &= convexConnections || !(block2.equals(entry.getKey()) && entry.getValue().contains(meta2));
		}

		return validBlockMeta1 && invalidBlockMeta2;
	}
}