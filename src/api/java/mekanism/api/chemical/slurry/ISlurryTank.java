package mekanism.api.chemical.slurry;

import mekanism.api.SerializationConstants;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.chemical.IChemicalTank;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;

/**
 * Convenience extension to make working with generics easier.
 */
@NothingNullByDefault
public interface ISlurryTank extends IChemicalTank<Slurry, SlurryStack>, IEmptySlurryProvider {

    @Override
    default void deserializeNBT(HolderLookup.Provider provider, CompoundTag nbt) {
        if (nbt.contains(SerializationConstants.STORED, Tag.TAG_COMPOUND)) {
            setStackUnchecked(SlurryStack.parseOptional(provider, nbt.getCompound(SerializationConstants.STORED)));
        }
    }
}