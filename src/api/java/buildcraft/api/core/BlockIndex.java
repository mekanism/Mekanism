/**
 * Copyright (c) 2011-2014, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.api.core;

import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

/**
 * This class is a comparable container for block positions. TODO: should this be merged with position?
 */
public class BlockIndex implements Comparable<BlockIndex> {

	public int x;
	public int y;
	public int z;

	public BlockIndex() {

	}

	/**
	 * Creates an index for a block located on x, y. z
	 */
	public BlockIndex(int x, int y, int z) {

		this.x = x;
		this.y = y;
		this.z = z;
	}

	public BlockIndex(NBTTagCompound c) {
		this.x = c.getInteger("i");
		this.y = c.getInteger("j");
		this.z = c.getInteger("k");
	}

	public BlockIndex(Entity entity) {
		x = (int) Math.floor(entity.posX);
		y = (int) Math.floor(entity.posY);
		z = (int) Math.floor(entity.posZ);
	}

	public BlockIndex(TileEntity entity) {
		this(entity.xCoord, entity.yCoord, entity.zCoord);
	}

	/**
	 * Provides a deterministic and complete ordering of block positions.
	 */
	@Override
	public int compareTo(BlockIndex o) {

		if (o.x < x) {
			return 1;
		} else if (o.x > x) {
			return -1;
		} else if (o.z < z) {
			return 1;
		} else if (o.z > z) {
			return -1;
		} else if (o.y < y) {
			return 1;
		} else if (o.y > y) {
			return -1;
		} else {
			return 0;
		}
	}

	public void writeTo(NBTTagCompound c) {
		c.setInteger("i", x);
		c.setInteger("j", y);
		c.setInteger("k", z);
	}

	public Block getBlock(World world) {
		return world.getBlock(x, y, z);
	}

	@Override
	public String toString() {
		return "{" + x + ", " + y + ", " + z + "}";
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof BlockIndex) {
			BlockIndex b = (BlockIndex) obj;

			return b.x == x && b.y == y && b.z == z;
		}

		return super.equals(obj);
	}

	@Override
	public int hashCode() {
		return (x * 37 + y) * 37 + z;
	}

	public boolean nextTo(BlockIndex blockIndex) {
		return (Math.abs(blockIndex.x - x) <= 1 && blockIndex.y == y && blockIndex.z == z)
				|| (blockIndex.x == x && Math.abs(blockIndex.y - y) <= 1 && blockIndex.z == z)
				|| (blockIndex.x == x && blockIndex.y == y && Math.abs(blockIndex.z - z) <= 1);
	}
}
