package mekanism.fabric_shim.common.util;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.Tag;
import org.jetbrains.annotations.UnknownNullability;

/**
 * An object that can serialize itself to and from NBT (stand-in for NeoForge's INBTSerializable;
 * same surface).
 */
public interface INBTSerializable<T extends Tag> {

    @UnknownNullability
    T serializeNBT(HolderLookup.Provider provider);

    void deserializeNBT(HolderLookup.Provider provider, T nbt);
}
