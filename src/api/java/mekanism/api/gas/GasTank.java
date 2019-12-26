package mekanism.api.gas;

import javax.annotation.Nonnull;
import mekanism.api.chemical.ChemicalTank;
import net.minecraft.nbt.CompoundNBT;

/**
 * An optional way of managing and/or storing gases. Would be very useful in TileEntity and Entity gas storage.
 *
 * @author aidancbrady
 */
public class GasTank extends ChemicalTank<Gas, GasStack> implements GasTankInfo {

    /**
     * Creates a tank with a defined capacity.
     *
     * @param capacity - the maximum amount of gas this GasTank can hold
     */
    public GasTank(int capacity) {
        super(capacity);
    }

    @Nonnull
    @Override
    protected GasStack getEmptyStack() {
        return GasStack.EMPTY;
    }

    @Nonnull
    @Override
    protected GasStack createStack(GasStack stored, int size) {
        return new GasStack(stored, size);
    }

    /**
     * Reads this tank's data from a defined tag compound.
     *
     * @param nbtTags - tag compound to read from
     */
    @Override
    public GasTank read(CompoundNBT nbtTags) {
        if (nbtTags.contains("stored")) {
            setStack(GasStack.readFromNBT(nbtTags.getCompound("stored")));
        }
        return this;
    }
}