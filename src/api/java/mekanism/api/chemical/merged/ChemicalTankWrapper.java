package mekanism.api.chemical.merged;

import java.util.function.BooleanSupplier;
import javax.annotation.ParametersAreNonnullByDefault;
import mcp.MethodsReturnNonnullByDefault;
import mekanism.api.Action;
import mekanism.api.chemical.Chemical;
import mekanism.api.chemical.ChemicalStack;
import mekanism.api.chemical.IChemicalTank;
import mekanism.api.chemical.attribute.ChemicalAttributeValidator;
import mekanism.api.inventory.AutomationType;
import net.minecraft.nbt.CompoundNBT;

/**
 * Helper class for wrapping a chemical tank for use in a multi chemical type. Disallowing interacting with various tanks if other tanks have contents. For example only
 * one chemical tank of a {@link MergedChemicalTank} can have a chemical in it at any time.
 */
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public abstract class ChemicalTankWrapper<CHEMICAL extends Chemical<CHEMICAL>, STACK extends ChemicalStack<CHEMICAL>> implements IChemicalTank<CHEMICAL, STACK> {

    private final IChemicalTank<CHEMICAL, STACK> internal;
    private final BooleanSupplier insertCheck;
    private final MergedChemicalTank mergedTank;

    protected ChemicalTankWrapper(MergedChemicalTank mergedTank, IChemicalTank<CHEMICAL, STACK> internal, BooleanSupplier insertCheck) {
        //TODO: Do we want to short circuit it so that if we are not empty it allows for inserting before checking the insertCheck
        this.mergedTank = mergedTank;
        this.internal = internal;
        this.insertCheck = insertCheck;
    }

    public MergedChemicalTank getMergedTank() {
        return mergedTank;
    }

    @Override
    public STACK getStack() {
        return internal.getStack();
    }

    @Override
    public void setStack(STACK stack) {
        internal.setStack(stack);
    }

    @Override
    public void setStackUnchecked(STACK stack) {
        internal.setStackUnchecked(stack);
    }

    @Override
    public STACK insert(STACK stack, Action action, AutomationType automationType) {
        //Only allow inserting if we pass the check
        return insertCheck.getAsBoolean() ? internal.insert(stack, action, automationType) : stack;
    }

    @Override
    public STACK extract(long amount, Action action, AutomationType automationType) {
        return internal.extract(amount, action, automationType);
    }

    @Override
    public long getCapacity() {
        return internal.getCapacity();
    }

    @Override
    public boolean isValid(STACK stack) {
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
    public CHEMICAL getType() {
        return internal.getType();
    }

    @Override
    public boolean isTypeEqual(STACK other) {
        return internal.isTypeEqual(other);
    }

    @Override
    public boolean isTypeEqual(CHEMICAL other) {
        return internal.isTypeEqual(other);
    }

    @Override
    public ChemicalAttributeValidator getAttributeValidator() {
        return internal.getAttributeValidator();
    }

    @Override
    public CompoundNBT serializeNBT() {
        return internal.serializeNBT();
    }

    @Override
    public void deserializeNBT(CompoundNBT nbt) {
        internal.deserializeNBT(nbt);
    }
}