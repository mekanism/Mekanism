package buildcraft.api.core;

import net.minecraft.nbt.NBTTagCompound;

/** Use {@link INBTLoadable_BC8} instead of this, as this gives you more control over what NBT goes where, and what
 * objects are allowed to do. */
@Deprecated
public interface INBTStoreable {
    void readFromNBT(NBTTagCompound tag);

    void writeToNBT(NBTTagCompound tag);
}
