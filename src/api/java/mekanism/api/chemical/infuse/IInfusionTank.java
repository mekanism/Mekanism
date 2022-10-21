package mekanism.api.chemical.infuse;

import mekanism.api.NBTConstants;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.chemical.IChemicalTank;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;

/**
 * Convenience extension to make working with generics easier.
 */
@NothingNullByDefault
public interface IInfusionTank extends IChemicalTank<InfuseType, InfusionStack>, IEmptyInfusionProvider {

    @Override
    default InfusionStack createStack(InfusionStack stored, long size) {
        return new InfusionStack(stored, size);
    }

    @Override
    default void deserializeNBT(CompoundTag nbt) {
        if (nbt.contains(NBTConstants.STORED, Tag.TAG_COMPOUND)) {
            setStackUnchecked(InfusionStack.readFromNBT(nbt.getCompound(NBTConstants.STORED)));
        }
    }
}