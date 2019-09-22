package mekanism.api.infuse;

import javax.annotation.Nonnull;
import mekanism.api.chemical.ChemicalTank;
import mekanism.api.sustained.ISustainedData;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;

//TODO: Rewrite this to be more like GsaTank
public class InfusionTank extends ChemicalTank<InfuseType, InfusionStack> implements ISustainedData {

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

    //TODO: Move the sustained data out OR move gas sustained data to ChemicalTank
    @Override
    public void writeSustainedData(@Nonnull ItemStack itemStack) {
        if (!isEmpty()) {
            write(itemStack.getOrCreateTag());
        }
    }

    @Override
    public void readSustainedData(@Nonnull ItemStack itemStack) {
        if (itemStack.hasTag()) {
            read(itemStack.getTag());
        } else {
            setEmpty();
        }
    }

    @Override
    public InfusionTank read(CompoundNBT nbtTags) {
        if (nbtTags.contains("stored")) {
            setStack(InfusionStack.readFromNBT(nbtTags.getCompound("stored")));
        }
        return this;
    }
}