package mekanism.api.chemical.infuse;

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
public interface IInfusionTank extends IChemicalTank<InfuseType, InfusionStack>, IEmptyInfusionProvider {

    @Override
    default InfusionStack createStack(InfusionStack stored, long size) {
        return new InfusionStack(stored, size);
    }

    @Override
    default void deserializeNBT(CompoundNBT nbt) {
        if (nbt.contains(NBTConstants.STORED, NBT.TAG_COMPOUND)) {
            setStackUnchecked(InfusionStack.readFromNBT(nbt.getCompound(NBTConstants.STORED)));
        }
    }
}