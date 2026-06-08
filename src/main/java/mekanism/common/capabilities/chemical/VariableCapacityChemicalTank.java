package mekanism.common.capabilities.chemical;

import java.util.Objects;
import java.util.function.BiPredicate;
import java.util.function.LongSupplier;
import java.util.function.Predicate;
import mekanism.api.AutomationType;
import mekanism.api.IContentsListener;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.chemical.BasicChemicalTank;
import mekanism.api.chemical.ChemicalResource;
import mekanism.api.chemical.IChemicalTank;
import mekanism.api.chemical.attribute.ChemicalAttributeValidator;
import mekanism.api.functions.ConstantPredicates;
import mekanism.api.transaction.RateLimitTracker;
import mekanism.common.lib.multiblock.MultiblockData;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Range;

@NothingNullByDefault
public class VariableCapacityChemicalTank extends BasicChemicalTank {

    public static IChemicalTank createAllValid(LongSupplier capacity, BiPredicate<ChemicalResource, AutomationType> canExtract,
          BiPredicate<ChemicalResource, AutomationType> canInsert, Predicate<ChemicalResource> validator, @Nullable IContentsListener listener) {
        return create(capacity, canExtract, canInsert, validator, ChemicalAttributeValidator.ALWAYS_ALLOW, listener);
    }

    public static IChemicalTank output(LongSupplier capacity, Predicate<ChemicalResource> validator, @Nullable IContentsListener listener) {
        Objects.requireNonNull(capacity, "Capacity supplier cannot be null");
        Objects.requireNonNull(validator, "Chemical validity check cannot be null");
        return create(capacity, ConstantPredicates.alwaysTrueBi(), ConstantPredicates.internalOnly(), validator, null, listener);
    }

    public static IChemicalTank create(MultiblockData multiblock, LongSupplier capacity, Predicate<ChemicalResource> validator, @Nullable IContentsListener listener) {
        return create(capacity, multiblock.formedBiPred(), multiblock.formedBiPred(), validator, null, listener);
    }

    public static IChemicalTank create(LongSupplier capacity, BiPredicate<ChemicalResource, AutomationType> canExtract,
          BiPredicate<ChemicalResource, AutomationType> canInsert, Predicate<ChemicalResource> validator, @Nullable IContentsListener listener) {
        return create(capacity, canExtract, canInsert, validator, null, listener);
    }

    public static IChemicalTank input(MultiblockData multiblock, LongSupplier capacity, Predicate<ChemicalResource> validator, @Nullable IContentsListener listener) {
        return input(multiblock, capacity, validator, null, listener);
    }

    public static IChemicalTank input(MultiblockData multiblock, LongSupplier capacity, Predicate<ChemicalResource> validator, @Nullable ChemicalAttributeValidator attributeValidator,
          @Nullable IContentsListener listener) {
        return create(capacity, multiblock.notExternalFormedBiPred(), multiblock.formedBiPred(), validator, attributeValidator, listener);
    }

    public static IChemicalTank output(MultiblockData multiblock, LongSupplier capacity, Predicate<ChemicalResource> validator, @Nullable IContentsListener listener) {
        return output(multiblock, capacity, validator, null, listener);
    }

    public static IChemicalTank output(MultiblockData multiblock, LongSupplier capacity, Predicate<ChemicalResource> validator,
          @Nullable ChemicalAttributeValidator attributeValidator, @Nullable IContentsListener listener) {
        return create(capacity, multiblock.formedBiPred(), multiblock.notExternalFormedBiPred(), validator, attributeValidator, listener);
    }

    public static IChemicalTank create(LongSupplier capacity, BiPredicate<ChemicalResource, AutomationType> canExtract,
          BiPredicate<ChemicalResource, AutomationType> canInsert, Predicate<ChemicalResource> validator,
          @Nullable ChemicalAttributeValidator attributeValidator, @Nullable IContentsListener listener) {
        Objects.requireNonNull(capacity, "Capacity supplier cannot be null");
        Objects.requireNonNull(validator, "Chemical validity check cannot be null");
        Objects.requireNonNull(canExtract, "Extraction validity check cannot be null");
        Objects.requireNonNull(canInsert, "Insertion validity check cannot be null");
        return new VariableCapacityChemicalTank(capacity, canExtract, canInsert, validator, null, null, attributeValidator, listener);
    }

    private final LongSupplier capacity;

    public VariableCapacityChemicalTank(LongSupplier capacity, BiPredicate<ChemicalResource, AutomationType> canExtract,
          BiPredicate<ChemicalResource, AutomationType> canInsert, Predicate<ChemicalResource> validator, @Nullable RateLimitTracker insertionRateLimiter,
          @Nullable RateLimitTracker extractionRateLimiter, @Nullable ChemicalAttributeValidator attributeValidator, @Nullable IContentsListener listener) {
        super(capacity.getAsLong(), canExtract, canInsert, validator, insertionRateLimiter, extractionRateLimiter, attributeValidator, listener);
        this.capacity = capacity;
    }

    @Override
    @Range(from = 0, to = Long.MAX_VALUE)
    public long capacityAsLong(ChemicalResource resource) {
        //Ensure the resource is valid, and otherwise return zero
        if (resource.isEmpty() || isValid(resource)) {
            return capacity.getAsLong();
        }
        return 0;
    }
}