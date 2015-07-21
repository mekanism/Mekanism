/**
 * Copyright (c) 2011-2015, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * The BuildCraft API is distributed under the terms of the MIT License.
 * Please check the contents of the license, which should be located
 * as "LICENSE.API" in the BuildCraft source code distribution.
 */
package buildcraft.api.core;

import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

/**
 * This class is a comparable container for integer block positions.
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

		return false;
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
