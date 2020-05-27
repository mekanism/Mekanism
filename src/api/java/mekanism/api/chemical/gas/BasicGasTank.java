package mekanism.api.chemical.gas;

import java.util.Objects;
import java.util.function.BiPredicate;
import java.util.function.Predicate;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import mcp.MethodsReturnNonnullByDefault;
import mekanism.api.IContentsListener;
import mekanism.api.annotations.NonNull;
import mekanism.api.chemical.BasicChemicalTank;
import mekanism.api.chemical.attribute.ChemicalAttributeValidator;
import mekanism.api.inventory.AutomationType;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class BasicGasTank extends BasicChemicalTank<Gas, GasStack> implements IGasHandler, IGasTank {

    public static final Predicate<@NonNull Gas> alwaysTrue = stack -> true;
    public static final Predicate<@NonNull Gas> alwaysFalse = stack -> false;
    public static final BiPredicate<@NonNull Gas, @NonNull AutomationType> alwaysTrueBi = (stack, automationType) -> true;
    public static final BiPredicate<@NonNull Gas, @NonNull AutomationType> internalOnly = (stack, automationType) -> automationType == AutomationType.INTERNAL;
    public static final BiPredicate<@NonNull Gas, @NonNull AutomationType> notExternal = (stack, automationType) -> automationType != AutomationType.EXTERNAL;

    public static BasicGasTank createDummy(long capacity) {
        return create(capacity, alwaysTrueBi, alwaysTrueBi, alwaysTrue, ChemicalAttributeValidator.ALWAYS_ALLOW, null);
    }

    public static BasicGasTank create(long capacity, @Nullable IContentsListener listener) {
        if (capacity < 0) {
            throw new IllegalArgumentException("Capacity must be at least zero");
        }
        return new BasicGasTank(capacity, alwaysTrueBi, alwaysTrueBi, alwaysTrue, listener);
    }

    public static BasicGasTank create(long capacity, Predicate<@NonNull Gas> canExtract, Predicate<@NonNull Gas> canInsert, @Nullable IContentsListener listener) {
        return create(capacity, canExtract, canInsert, alwaysTrue, listener);
    }

    public static BasicGasTank create(long capacity, Predicate<@NonNull Gas> validator, @Nullable IContentsListener listener) {
        if (capacity < 0) {
            throw new IllegalArgumentException("Capacity must be at least zero");
        }
        Objects.requireNonNull(validator, "Infuse type validity check cannot be null");
        return new BasicGasTank(capacity, alwaysTrueBi, alwaysTrueBi, validator, listener);
    }

    public static BasicGasTank input(long capacity, Predicate<@NonNull Gas> validator, @Nullable IContentsListener listener) {
        if (capacity < 0) {
            throw new IllegalArgumentException("Capacity must be at least zero");
        }
        Objects.requireNonNull(validator, "Gas validity check cannot be null");
        return new BasicGasTank(capacity, notExternal, alwaysTrueBi, validator, listener);
    }

    public static BasicGasTank input(long capacity, Predicate<@NonNull Gas> canInsert, Predicate<@NonNull Gas> validator, @Nullable IContentsListener listener) {
        if (capacity < 0) {
            throw new IllegalArgumentException("Capacity must be at least zero");
        }
        Objects.requireNonNull(canInsert, "Insertion validity check cannot be null");
        Objects.requireNonNull(validator, "Gas validity check cannot be null");
        return new BasicGasTank(capacity, notExternal, (stack, automationType) -> canInsert.test(stack), validator, listener);
    }

    public static BasicGasTank output(long capacity, @Nullable IContentsListener listener) {
        if (capacity < 0) {
            throw new IllegalArgumentException("Capacity must be at least zero");
        }
        return new BasicGasTank(capacity, alwaysTrueBi, internalOnly, alwaysTrue, ChemicalAttributeValidator.ALWAYS_ALLOW, listener);
    }

    public static BasicGasTank ejectOutput(long capacity, @Nullable IContentsListener listener) {
        if (capacity < 0) {
            throw new IllegalArgumentException("Capacity must be at least zero");
        }
        return new BasicGasTank(capacity, internalOnly, internalOnly, alwaysTrue, ChemicalAttributeValidator.ALWAYS_ALLOW, listener);
    }

    public static BasicGasTank create(long capacity, Predicate<@NonNull Gas> canExtract, Predicate<@NonNull Gas> canInsert, Predicate<@NonNull Gas> validator,
          @Nullable IContentsListener listener) {
        return create(capacity, canExtract, canInsert, validator, null, listener);
    }

    public static BasicGasTank create(long capacity, BiPredicate<@NonNull Gas, @NonNull AutomationType> canExtract,
          BiPredicate<@NonNull Gas, @NonNull AutomationType> canInsert, Predicate<@NonNull Gas> validator, @Nullable IContentsListener listener) {
        return create(capacity, canExtract, canInsert, validator, null, listener);
    }

    public static BasicGasTank create(long capacity, Predicate<@NonNull Gas> canExtract, Predicate<@NonNull Gas> canInsert, Predicate<@NonNull Gas> validator,
          @Nullable ChemicalAttributeValidator attributeValidator, @Nullable IContentsListener listener) {
        if (capacity < 0) {
            throw new IllegalArgumentException("Capacity must be at least zero");
        }
        Objects.requireNonNull(canExtract, "Extraction validity check cannot be null");
        Objects.requireNonNull(canInsert, "Insertion validity check cannot be null");
        Objects.requireNonNull(validator, "Gas validity check cannot be null");
        return new BasicGasTank(capacity, canExtract, canInsert, validator, attributeValidator, listener);
    }

    public static BasicGasTank create(long capacity, BiPredicate<@NonNull Gas, @NonNull AutomationType> canExtract, BiPredicate<@NonNull Gas, @NonNull AutomationType> canInsert,
          Predicate<@NonNull Gas> validator, @Nullable ChemicalAttributeValidator attributeValidator, @Nullable IContentsListener listener) {
        if (capacity < 0) {
            throw new IllegalArgumentException("Capacity must be at least zero");
        }
        Objects.requireNonNull(canExtract, "Extraction validity check cannot be null");
        Objects.requireNonNull(canInsert, "Insertion validity check cannot be null");
        Objects.requireNonNull(validator, "Gas validity check cannot be null");
        return new BasicGasTank(capacity, canExtract, canInsert, validator, attributeValidator, listener);
    }

    protected BasicGasTank(long capacity, Predicate<@NonNull Gas> canExtract, Predicate<@NonNull Gas> canInsert, Predicate<@NonNull Gas> validator,
          @Nullable IContentsListener listener) {
        this(capacity, (stack, automationType) -> automationType == AutomationType.MANUAL || canExtract.test(stack), (stack, automationType) -> canInsert.test(stack),
              validator, listener);
    }

    protected BasicGasTank(long capacity, BiPredicate<@NonNull Gas, @NonNull AutomationType> canExtract, BiPredicate<@NonNull Gas, @NonNull AutomationType> canInsert,
          Predicate<@NonNull Gas> validator, @Nullable IContentsListener listener) {
        this(capacity, canExtract, canInsert, validator, null, listener);
    }

    protected BasicGasTank(long capacity, Predicate<@NonNull Gas> canExtract, Predicate<@NonNull Gas> canInsert, Predicate<@NonNull Gas> validator,
          @Nullable ChemicalAttributeValidator attributeValidator, @Nullable IContentsListener listener) {
        this(capacity, (stack, automationType) -> automationType == AutomationType.MANUAL || canExtract.test(stack), (stack, automationType) -> canInsert.test(stack),
              validator, attributeValidator, listener);
    }

    protected BasicGasTank(long capacity, BiPredicate<@NonNull Gas, @NonNull AutomationType> canExtract, BiPredicate<@NonNull Gas, @NonNull AutomationType> canInsert,
          Predicate<@NonNull Gas> validator, @Nullable ChemicalAttributeValidator attributeValidator, @Nullable IContentsListener listener) {
        super(capacity, canExtract, canInsert, validator, attributeValidator, listener);
    }
}