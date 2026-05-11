package mekanism.common.capabilities.fluid;

import mekanism.api.AutomationType;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.chemical.IChemicalTank;
import mekanism.api.fluid.IFluidTank;
import mekanism.common.capabilities.merged.ChemicalTankWrapper;
import mekanism.common.capabilities.merged.MergedTank;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.transfer.fluid.FluidResource;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;
import org.jetbrains.annotations.Range;

/**
 * Like {@link ChemicalTankWrapper}
 */
@NothingNullByDefault
public class FluidTankWrapper implements IFluidTank {//TODO - 26.1: Re-evaluate this and make sure it proxies all the methods we end up with

    private final IChemicalTank chemicalTank;
    private final IFluidTank internal;
    private final MergedTank mergedTank;

    public FluidTankWrapper(MergedTank mergedTank, IFluidTank internal, IChemicalTank chemicalTank) {
        //TODO: Do we want to short circuit it so that if we are not empty it allows for inserting before checking the insertCheck
        this.mergedTank = mergedTank;
        this.internal = internal;
        this.chemicalTank = chemicalTank;
    }

    public MergedTank getMergedTank() {
        return mergedTank;
    }

    @Override
    public void setContentsUnchecked(FluidResource type, @Range(from = 0, to = Long.MAX_VALUE) long storedAmount) {
        internal.setContentsUnchecked(type, storedAmount);
    }

    @Override
    public void setContents(FluidResource itemType, @Range(from = 0, to = Long.MAX_VALUE) long storedAmount) {
        internal.setContents(itemType, storedAmount);
    }

    private boolean canInsert() {
        return chemicalTank.isEmpty();
    }

    @Override
    @Range(from = 0, to = Integer.MAX_VALUE)
    public int insert(FluidResource resource, @Range(from = 0, to = Integer.MAX_VALUE) int amount, TransactionContext transaction, AutomationType automationType) {
        //Only allow inserting if we pass the check
        return canInsert() ? internal.insert(resource, amount, transaction, automationType) : 0;
    }

    @Override
    @Range(from = 0, to = Integer.MAX_VALUE)
    public int extract(FluidResource resource, @Range(from = 0, to = Integer.MAX_VALUE) int amount, TransactionContext transaction, AutomationType automationType) {
        return internal.extract(resource, amount, transaction, automationType);
    }

    @Override
    public void onContentsChanged() {
        internal.onContentsChanged();
    }

    @Override
    public boolean isEmpty() {
        return internal.isEmpty();
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
    @Range(from = 0, to = Long.MAX_VALUE)
    public long amountAsLong() {
        return internal.amountAsLong();
    }

    @Override
    @Range(from = 0, to = Long.MAX_VALUE)
    public long getLimitAsLong(FluidResource resource) {
        return internal.getLimitAsLong(resource);
    }

    @Override
    public boolean isValid(FluidResource fluidType) {
        return internal.isValid(fluidType);
    }

    @Override
    public boolean isCurrentValidForExtraction(AutomationType automationType) {
        return internal.isCurrentValidForExtraction(automationType);
    }

    @Override
    public boolean isValidForInsertion(FluidResource type, AutomationType automationType) {
        return internal.isValidForInsertion(type, automationType);
    }
}