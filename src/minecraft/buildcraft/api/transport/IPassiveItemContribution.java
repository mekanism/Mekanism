/** 
 * Copyright (c) SpaceToad, 2012
 * http://www.mod-buildcraft.com
 * 
 * BuildCraft is distributed under the terms of the Minecraft Mod Public 
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */

package buildcraft.api.transport;

import net.minecraft.nbt.NBTTagCompound;

public interface IPassiveItemContribution {

	public void readFromNBT(NBTTagCompound nbttagcompound);

	public void writeToNBT(NBTTagCompound nbttagcompound);

}
