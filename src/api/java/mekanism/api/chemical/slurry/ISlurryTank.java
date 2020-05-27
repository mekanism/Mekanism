package mekanism.api.chemical.slurry;

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
public interface ISlurryTank extends IChemicalTank<Slurry, SlurryStack>, IEmptySlurryProvider {

    @Override
    default SlurryStack createStack(SlurryStack stored, long size) {
        return new SlurryStack(stored, size);
    }

    @Override
    default void deserializeNBT(CompoundNBT nbt) {
        if (nbt.contains(NBTConstants.STORED, NBT.TAG_COMPOUND)) {
            setStackUnchecked(SlurryStack.readFromNBT(nbt.getCompound(NBTConstants.STORED)));
        }
    }
}