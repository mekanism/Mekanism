package mekanism.api.infuse;

import javax.annotation.Nonnull;
import mekanism.api.chemical.ChemicalTank;
import net.minecraft.nbt.CompoundNBT;

//TODO: Rewrite this to be more like GasTank
public class InfusionTank extends ChemicalTank<InfuseType, InfusionStack> {

    public InfusionTank(int capacity) {
        super(capacity);
    }

    @Nonnull
    @Override
    protected InfusionStack getEmptyStack() {
        return InfusionStack.EMPTY;
    }

    @Nonnull
    @Override
    protected InfusionStack createStack(InfusionStack stored, int size) {
        return new InfusionStack(stored, size);
    }

    @Override
    public InfusionTank read(CompoundNBT nbtTags) {
        if (nbtTags.contains("stored")) {
            setStack(InfusionStack.readFromNBT(nbtTags.getCompound("stored")));
        }
        return this;
    }
}