/** 
 * Copyright (c) SpaceToad, 2011
 * http://www.mod-buildcraft.com
 * 
 * BuildCraft is distributed under the terms of the Minecraft Mod Public 
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */

package buildcraft.api.core;

import net.minecraft.src.Block;
import net.minecraft.src.World;

public class BuildCraftAPI {
	
	@Deprecated
	// To be removed, see LiquidManager
	public static final int BUCKET_VOLUME = 1000;
	public static final int LAST_ORIGINAL_BLOCK = 122;
	public static final int LAST_ORIGINAL_ITEM = 126;

	public static boolean[] softBlocks = new boolean[Block.blocksList.length];
	/**
	 * Return true if the block given in parameter is pass through (e.g. air,
	 * water...)
	 */
	public static boolean softBlock(int blockId) {
		return blockId == 0 || softBlocks[blockId] || Block.blocksList[blockId] == null;
	}

	/**
	 * Return true if the block cannot be broken, typically bedrock and lava
	 */
	public static boolean unbreakableBlock(int blockId) {
		return blockId == Block.bedrock.blockID || blockId == Block.lavaStill.blockID || blockId == Block.lavaMoving.blockID;
	}

	@Deprecated
	// To be removed
	public static void breakBlock(World world, int x, int y, int z) {
		int blockId = world.getBlockId(x, y, z);

		if (blockId != 0) {
			Block.blocksList[blockId].dropBlockAsItem(world, x, y, z, world.getBlockMetadata(x, y, z), 0);
		}

		world.setBlockWithNotify(x, y, z, 0);
	}

	static {
		for (int i = 0; i < softBlocks.length; ++i) {
			softBlocks[i] = false;
		}
	}
}
