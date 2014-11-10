/**
 * Copyright (c) 2011-2014, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.api.boards;

import java.util.Collection;

import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.nbt.NBTTagCompound;

public abstract class RedstoneBoardRegistry {

	public static RedstoneBoardRegistry instance;

	public abstract void registerBoardClass(RedstoneBoardNBT<?> redstoneBoardNBT, float probability);

	public abstract void createRandomBoard(NBTTagCompound nbt);

	public abstract RedstoneBoardNBT getRedstoneBoard(NBTTagCompound nbt);

	public abstract RedstoneBoardNBT<?> getRedstoneBoard(String id);

	public abstract void registerIcons(IIconRegister par1IconRegister);

	public abstract Collection<RedstoneBoardNBT<?>> getAllBoardNBTs();
}
