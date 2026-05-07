package mekanism.common.capabilities.fluid;

import mekanism.api.Action;
import mekanism.api.AutomationType;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.chemical.IChemicalTank;
import mekanism.api.fluid.IExtendedFluidTank;
import mekanism.common.capabilities.merged.ChemicalTankWrapper;
import mekanism.common.capabilities.merged.MergedTank;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.transfer.fluid.FluidResource;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;

/**
 * Like {@link ChemicalTankWrapper}
 */
@NothingNullByDefault
public class FluidTankWrapper implements IExtendedFluidTank {

    private final IChemicalTank chemicalTank;
    private final IExtendedFluidTank internal;
    private final MergedTank mergedTank;

    public FluidTankWrapper(MergedTank mergedTank, IExtendedFluidTank internal, IChemicalTank chemicalTank) {
        //TODO: Do we want to short circuit it so that if we are not empty it allows for inserting before checking the insertCheck
        this.mergedTank = mergedTank;
        this.internal = internal;
        this.chemicalTank = chemicalTank;
    }

    public MergedTank getMergedTank() {
        return mergedTank;
    }

    @Override
    public void setStackUnchecked(FluidStack stack) {
        internal.setStackUnchecked(stack);
    }

    @Override
    public void setContents(FluidResource itemType, int storedAmount) {
        internal.setContents(itemType, storedAmount);
    }

    private boolean canInsert() {
        return chemicalTank.isEmpty();
    }

    @Override
    public int insert(FluidResource resource, int amount, TransactionContext transaction, AutomationType automationType) {
        //Only allow inserting if we pass the check
        return canInsert() ? internal.insert(resource, amount, transaction, automationType) : 0;
    }

    @Override
    public int extract(FluidResource resource, int amount, TransactionContext transaction, AutomationType automationType) {
        return internal.extract(resource, amount, transaction, automationType);
    }

    @Override
    public void onContentsChanged() {
        internal.onContentsChanged();
    }

    @Override
    public int setStackSize(int amount, Action action) {
        return internal.setStackSize(amount, action);
    }

    @Override
    public int growStack(int amount, Action action) {
        return internal.growStack(amount, action);
    }

    @Override
    public int shrinkStack(int amount, Action action) {
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
    public boolean isFluidEqual(FluidStack other) {
        return internal.isFluidEqual(other);
    }

    @Override
    public int getNeeded() {
        return internal.getNeeded();
    }

    @Override
    public void serialize(ValueOutput output) {
        internal.serialize(output);
    }

    @Override
    public void deserialize(ValueInput input) {
        internal.deserialize(input);
    }

    @Override
    public FluidResource getResource() {
        return internal.getResource();
    }

    @Override
    public int amount() {
        return internal.amount();
    }

    @Override
    public int getLimit(FluidResource resource) {
        return internal.getLimit(resource);
    }

    @Override
    public boolean isValid(FluidResource fluidType) {
        return internal.isValid(fluidType);
    }
}