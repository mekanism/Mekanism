/**
 * Copyright (c) 2011-2014, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.api.core;

import java.util.HashSet;
import java.util.Set;

import net.minecraft.block.Block;
import net.minecraft.world.IBlockAccess;

public final class BuildCraftAPI {

	public static ICoreProxy proxy;

	public static final Set<Block> softBlocks = new HashSet<Block>();

	/**
	 * Deactivate constructor
	 */
	private BuildCraftAPI() {
	}

	public static boolean isSoftBlock(IBlockAccess world, int x, int y, int z) {
		return isSoftBlock(world.getBlock(x, y, z), world, x, y, z);
	}

	public static boolean isSoftBlock(Block block, IBlockAccess world, int x, int y, int z) {
		return block == null || BuildCraftAPI.softBlocks.contains(block) || block.isReplaceable(world, x, y, z) || block.isAir(world, x, y, z);
	}

}
