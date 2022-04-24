package mekanism.api.chemical.gas;

import javax.annotation.ParametersAreNonnullByDefault;
import mekanism.api.NBTConstants;
import mekanism.api.chemical.IChemicalTank;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;

/**
 * Convenience extension to make working with generics easier.
 */
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public interface IGasTank extends IChemicalTank<Gas, GasStack>, IEmptyGasProvider {

    @Override
    default GasStack createStack(GasStack stored, long size) {
        return new GasStack(stored, size);
    }

    @Override
    default void deserializeNBT(CompoundTag nbt) {
        if (nbt.contains(NBTConstants.STORED, Tag.TAG_COMPOUND)) {
            setStackUnchecked(GasStack.readFromNBT(nbt.getCompound(NBTConstants.STORED)));
        }
    }
}