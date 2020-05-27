package mekanism.api.chemical.gas;

import javax.annotation.ParametersAreNonnullByDefault;
import mcp.MethodsReturnNonnullByDefault;
import mekanism.api.NBTConstants;
import mekanism.api.chemical.IChemicalTank;
import net.minecraft.nbt.CompoundNBT;
import net.minecraftforge.common.util.Constants.NBT;

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
    default void deserializeNBT(CompoundNBT nbt) {
        if (nbt.contains(NBTConstants.STORED, NBT.TAG_COMPOUND)) {
            setStackUnchecked(GasStack.readFromNBT(nbt.getCompound(NBTConstants.STORED)));
        }
    }
}