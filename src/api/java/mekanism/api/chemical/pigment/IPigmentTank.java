package mekanism.api.chemical.pigment;

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
public interface IPigmentTank extends IChemicalTank<Pigment, PigmentStack>, IEmptyPigmentProvider {

    @Override
    default PigmentStack createStack(PigmentStack stored, long size) {
        return new PigmentStack(stored, size);
    }

    @Override
    default void deserializeNBT(CompoundNBT nbt) {
        if (nbt.contains(NBTConstants.STORED, NBT.TAG_COMPOUND)) {
            setStackUnchecked(PigmentStack.readFromNBT(nbt.getCompound(NBTConstants.STORED)));
        }
    }
}