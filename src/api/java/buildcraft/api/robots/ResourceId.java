/**
 * Copyright (c) 2011-2014, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.api.robots;

import net.minecraft.nbt.NBTTagCompound;

import net.minecraftforge.common.util.ForgeDirection;

import buildcraft.api.core.BlockIndex;

public abstract class ResourceId {

	public BlockIndex index = new BlockIndex();
	public ForgeDirection side = ForgeDirection.UNKNOWN;
	public int localId = 0;

	protected ResourceId() {
	}

	@Override
	public boolean equals(Object obj) {
		if (obj.getClass() != getClass()) {
			return false;
		}

		ResourceId compareId = (ResourceId) obj;

		return index.equals(compareId.index)
				&& side == compareId.side
				&& localId == compareId.localId;
	}

	@Override
	public int hashCode() {
		return ((index.hashCode() * 37) + side.ordinal() * 37) + localId;
	}

	public void writeToNBT(NBTTagCompound nbt) {
		NBTTagCompound indexNBT = new NBTTagCompound();
		index.writeTo(indexNBT);
		nbt.setTag("index", indexNBT);
		nbt.setByte("side", (byte) side.ordinal());
		nbt.setInteger("localId", localId);
		nbt.setString("class", getClass().getCanonicalName());
	}

	protected void readFromNBT(NBTTagCompound nbt) {
		index = new BlockIndex(nbt.getCompoundTag("index"));
		side = ForgeDirection.values()[nbt.getByte("side")];
		localId = nbt.getInteger("localId");
	}

	public static ResourceId load(NBTTagCompound nbt) {
		try {
			Class clas = Class.forName(nbt.getString("class"));

			ResourceId id = (ResourceId) clas.newInstance();
			id.readFromNBT(nbt);

			return id;
		} catch (Throwable e) {
			e.printStackTrace();
		}

		return null;
	}

	public void taken(long robotId) {

	}

	public void released(long robotId) {

	}
}
