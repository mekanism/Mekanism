package buildcraft.api.core;

import net.minecraft.nbt.NBTBase;

/** @param <T> The type that will be loaded from and saved too. This essentially allows Immutable objects to be safely
 *            loaded without helper methods everywhere. However you are allowed to return this if you are a mutable
 *            object */
public interface INBTLoadable_BC8<T> {
    /** @return An object that has the properties loaded. WARNIG! This might be the same object as the one this method
     *         was called on! (You should always replace the object you have stored with whatever this returns) */
    T readFromNBT(NBTBase nbt);

    NBTBase writeToNBT();
}
