package mekanism.common.capabilities.merged;

import java.util.function.BooleanSupplier;
import mekanism.api.Action;
import mekanism.api.AutomationType;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.chemical.Chemical;
import mekanism.api.chemical.ChemicalResource;
import mekanism.api.chemical.ChemicalStack;
import mekanism.api.chemical.IChemicalTank;
import mekanism.api.chemical.attribute.ChemicalAttributeValidator;
import net.minecraft.core.Holder;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;

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
    public ChemicalResource getResource() {
        return internal.getResource();
    }

    @Override
    public void setContents(ChemicalResource type, long amount) {
        internal.setContents(type, amount);
    }

    @Override
    public void setContentsUnchecked(ChemicalResource type, long amount) {
        internal.setContentsUnchecked(type, amount);
    }

    private boolean canInsert() {
        return insertCheck.getAsBoolean();
    }

    @Override
    public int insert(ChemicalResource resource, int amount, TransactionContext transaction, AutomationType automationType) {
        //Only allow inserting if we pass the check
        return canInsert() ? internal.insert(resource, amount, transaction, automationType) : 0;
    }

    @Override
    public int extract(ChemicalResource resource, int amount, TransactionContext transaction, AutomationType automationType) {
        return internal.extract(resource, amount, transaction, automationType);
    }

    @Override
    public long getLimitAsLong(ChemicalResource chemicalType) {
        return internal.getLimitAsLong(chemicalType);
    }

    @Override
    public boolean isValid(ChemicalResource chemicalType) {
        return internal.isValid(chemicalType);
    }

    @Override
    public boolean isCurrentValidForExtraction(AutomationType automationType) {
        return internal.isCurrentValidForExtraction(automationType);
    }

    @Override
    public boolean isValidForInsertion(ChemicalResource type, AutomationType automationType) {
        return internal.isValidForInsertion(type, automationType);
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
    public long amountAsLong() {
        return internal.amountAsLong();
    }

    @Override
    public long getNeededAsLong() {
        return internal.getNeededAsLong();
    }

    @Override
    public Holder<Chemical> getType() {
        return internal.getType();
    }

    @Override
    public boolean isTypeEqual(ChemicalStack other) {
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
    public void serialize(ValueOutput output) {
        internal.serialize(output);
    }

    @Override
    public void deserialize(ValueInput input) {
        internal.deserialize(input);
    }
}