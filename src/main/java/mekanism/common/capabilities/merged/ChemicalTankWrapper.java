package mekanism.common.capabilities.merged;

import java.util.function.BooleanSupplier;
import mekanism.api.Action;
import mekanism.api.AutomationType;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.chemical.Chemical;
import mekanism.api.chemical.ChemicalStack;
import mekanism.api.chemical.IChemicalTank;
import mekanism.api.chemical.attribute.ChemicalAttributeValidator;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;

/**
 * Helper class for wrapping a chemical tank for use in a multi chemical type. Disallowing interacting with various tanks if other tanks have contents. For example only
 * one chemical tank of a {@link MergedTank} can have a chemical in it at any time.
 */
@NothingNullByDefault
public class ChemicalTankWrapper implements IChemicalTank {

    private final IChemicalTank internal;
    private final BooleanSupplier insertCheck;
    private final MergedTank mergedTank;

    public ChemicalTankWrapper(MergedTank mergedTank, IChemicalTank internal, BooleanSupplier insertCheck) {
        this.mergedTank = mergedTank;
        this.internal = internal;
        this.insertCheck = insertCheck;
    }

    /**
     * Gets the merged chemical tank.
     */
    public MergedTank getMergedTank() {
        return mergedTank;
    }

    @Override
    public ChemicalStack getStack() {
        return internal.getStack();
    }

    @Override
    public void setStack(ChemicalStack stack) {
        internal.setStack(stack);
    }

    @Override
    public void setStackUnchecked(ChemicalStack stack) {
        internal.setStackUnchecked(stack);
    }

    private boolean canInsert() {
        return insertCheck.getAsBoolean();
    }

    @Override
    public ChemicalStack insert(ChemicalStack stack, Action action, AutomationType automationType) {
        //Only allow inserting if we pass the check
        return canInsert() ? internal.insert(stack, action, automationType) : stack;
    }

    @Override
    public ChemicalStack extract(long amount, Action action, AutomationType automationType) {
        return internal.extract(amount, action, automationType);
    }

    @Override
    public long getCapacity() {
        return internal.getCapacity();
    }

    @Override
    public boolean isValid(ChemicalStack stack) {
        return internal.isValid(stack);
    }

    @Override
    public void onContentsChanged() {
        internal.onContentsChanged();
    }

    @Override
    public long setStackSize(long amount, Action action) {
        return internal.setStackSize(amount, action);
    }

    @Override
    public long growStack(long amount, Action action) {
        return internal.growStack(amount, action);
    }

    @Override
    public long shrinkStack(long amount, Action action) {
        return internal.shrinkStack(amount, action);
    }

    @Override
    public boolean isEmpty() {
        return internal.isEmpty();
    }

    @Override
    public void setEmpty() {
        internal.setEmpty();
    }

    @Override
    public long getStored() {
        return internal.getStored();
    }

    @Override
    public long getNeeded() {
        return internal.getNeeded();
    }

    @Override
    public Holder<Chemical> getTypeHolder() {
        return internal.getTypeHolder();
    }

    @Override
    @SuppressWarnings("removal")
    @Deprecated(forRemoval = true, since = "10.7.11")
    public Chemical getType() {
        return internal.getType();
    }

    @Override
    public boolean isTypeEqual(ChemicalStack other) {
        return internal.isTypeEqual(other);
    }

    @Override
    @SuppressWarnings("removal")
    @Deprecated(forRemoval = true, since = "10.7.11")
    public boolean isTypeEqual(Chemical other) {
        return internal.isTypeEqual(other);
    }

    @Override
    public boolean isTypeEqual(Holder<Chemical> holder) {
        return internal.isTypeEqual(holder);
    }

    @Override
    public ChemicalAttributeValidator getAttributeValidator() {
        return internal.getAttributeValidator();
    }

    @Override
    public CompoundTag serializeNBT(HolderLookup.Provider provider) {
        return internal.serializeNBT(provider);
    }

    @Override
    public void deserializeNBT(HolderLookup.Provider provider, CompoundTag nbt) {
        internal.deserializeNBT(provider, nbt);
    }
}