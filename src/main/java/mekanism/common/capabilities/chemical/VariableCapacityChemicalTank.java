package mekanism.common.capabilities.chemical;

import java.util.Objects;
import java.util.function.BiPredicate;
import java.util.function.LongSupplier;
import java.util.function.Predicate;
import mekanism.api.Action;
import mekanism.api.AutomationType;
import mekanism.api.IContentsListener;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.chemical.BasicChemicalTank;
import mekanism.api.chemical.ChemicalStack;
import mekanism.api.chemical.IChemicalTank;
import mekanism.api.chemical.attribute.ChemicalAttributeValidator;
import mekanism.api.functions.ConstantPredicates;
import mekanism.common.lib.multiblock.MultiblockData;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@NothingNullByDefault
public class VariableCapacityChemicalTank extends BasicChemicalTank {

    public static IChemicalTank createAllValid(LongSupplier capacity, @Nullable IContentsListener listener) {
        Objects.requireNonNull(capacity, "Capacity supplier cannot be null");
        return new VariableCapacityChemicalTank(capacity, ConstantPredicates.alwaysTrueBi(), ConstantPredicates.alwaysTrueBi(), ConstantPredicates.alwaysTrue(),
              ChemicalAttributeValidator.ALWAYS_ALLOW, listener);
    }

    public static IChemicalTank output(LongSupplier capacity, Predicate<ChemicalStack> validator, @Nullable IContentsListener listener) {
        Objects.requireNonNull(capacity, "Capacity supplier cannot be null");
        Objects.requireNonNull(validator, "Chemical validity check cannot be null");
        return new VariableCapacityChemicalTank(capacity, ConstantPredicates.alwaysTrueBi(), ConstantPredicates.internalOnly(), validator, null, listener);
    }

    public static IChemicalTank create(MultiblockData multiblock, LongSupplier capacity, Predicate<ChemicalStack> validator, @Nullable IContentsListener listener) {
        return create(capacity, multiblock.formedBiPred(), multiblock.formedBiPred(), validator, null, listener);
    }

    public static IChemicalTank create(LongSupplier capacity, BiPredicate<ChemicalStack, @NotNull AutomationType> canExtract,
          BiPredicate<ChemicalStack, @NotNull AutomationType> canInsert, Predicate<ChemicalStack> validator, @Nullable IContentsListener listener) {
        return create(capacity, canExtract, canInsert, validator, null, listener);
    }

    public static IChemicalTank input(MultiblockData multiblock, LongSupplier capacity, Predicate<ChemicalStack> validator, @Nullable IContentsListener listener) {
        return input(multiblock, capacity, validator, null, listener);
    }

    public static IChemicalTank input(MultiblockData multiblock, LongSupplier capacity, Predicate<ChemicalStack> validator, @Nullable ChemicalAttributeValidator attributeValidator,
          @Nullable IContentsListener listener) {
        return create(capacity, multiblock.notExternalFormedBiPred(), multiblock.formedBiPred(), validator, attributeValidator, listener);
    }

    public static IChemicalTank output(MultiblockData multiblock, LongSupplier capacity, Predicate<ChemicalStack> validator, @Nullable IContentsListener listener) {
        return output(multiblock, capacity, validator, null, listener);
    }

    public static IChemicalTank output(MultiblockData multiblock, LongSupplier capacity, Predicate<ChemicalStack> validator,
          @Nullable ChemicalAttributeValidator attributeValidator, @Nullable IContentsListener listener) {
        return create(capacity, multiblock.formedBiPred(), multiblock.notExternalFormedBiPred(), validator, attributeValidator, listener);
    }

    public static IChemicalTank create(LongSupplier capacity, BiPredicate<ChemicalStack, @NotNull AutomationType> canExtract,
          BiPredicate<ChemicalStack, @NotNull AutomationType> canInsert, Predicate<ChemicalStack> validator,
          @Nullable ChemicalAttributeValidator attributeValidator, @Nullable IContentsListener listener) {
        Objects.requireNonNull(capacity, "Capacity supplier cannot be null");
        Objects.requireNonNull(validator, "Chemical validity check cannot be null");
        Objects.requireNonNull(canExtract, "Extraction validity check cannot be null");
        Objects.requireNonNull(canInsert, "Insertion validity check cannot be null");
        return new VariableCapacityChemicalTank(capacity, canExtract, canInsert, validator, attributeValidator, listener);
    }

    private final LongSupplier capacity;

    public VariableCapacityChemicalTank(LongSupplier capacity, BiPredicate<ChemicalStack, @NotNull AutomationType> canExtract,
          BiPredicate<ChemicalStack, @NotNull AutomationType> canInsert, Predicate<ChemicalStack> validator,
          @Nullable ChemicalAttributeValidator attributeValidator, @Nullable IContentsListener listener) {
        super(capacity.getAsLong(), canExtract, canInsert, validator, attributeValidator, listener, null);
        this.capacity = capacity;
    }

    @Override
    public long getCapacity() {
        return capacity.getAsLong();
    }

    @Override
    public long setStackSize(long amount, @NotNull Action action) {
        if (isEmpty()) {
            return 0;
        } else if (amount <= 0) {
            if (action.execute()) {
                setEmpty();
            }
            return 0;
        }
        long maxStackSize = getCapacity();
        //Our capacity should never actually be zero, and given we fake it being zero
        // until we finish building the network, we need to override this method to bypass the upper limit check
        // when our upper limit is zero
        if (maxStackSize > 0 && amount > maxStackSize) {
            amount = maxStackSize;
        }
        if (getStored() == amount || action.simulate()) {
            //If our size is not changing, or we are only simulating the change, don't do anything
            return amount;
        }
        stored.setAmount(amount);
        onContentsChanged();
        return amount;
    }
}