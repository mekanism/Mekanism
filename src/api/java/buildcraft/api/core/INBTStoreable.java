package buildcraft.api.core;

import net.minecraft.nbt.NBTTagCompound;

public interface INBTStoreable {
	void readFromNBT(NBTTagCompound tag);
	void writeToNBT(NBTTagCompound tag);
}
