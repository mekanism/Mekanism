package mekanism.api.chemical.pigment;

import mekanism.api.NBTConstants;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.chemical.IChemicalTank;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;

/**
 * Convenience extension to make working with generics easier.
 */
@NothingNullByDefault
public interface IPigmentTank extends IChemicalTank<Pigment, PigmentStack>, IEmptyPigmentProvider {

    @Override
    default PigmentStack createStack(PigmentStack stored, long size) {
        return new PigmentStack(stored, size);
    }

    @Override
    default void deserializeNBT(CompoundTag nbt) {
        if (nbt.contains(NBTConstants.STORED, Tag.TAG_COMPOUND)) {
            setStackUnchecked(PigmentStack.readFromNBT(nbt.getCompound(NBTConstants.STORED)));
        }
    }
}