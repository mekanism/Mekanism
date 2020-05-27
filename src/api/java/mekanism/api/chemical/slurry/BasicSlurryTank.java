package mekanism.api.chemical.slurry;

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
public class BasicSlurryTank extends BasicChemicalTank<Slurry, SlurryStack> implements ISlurryHandler, ISlurryTank {

    public static final Predicate<@NonNull Slurry> alwaysTrue = stack -> true;
    public static final BiPredicate<@NonNull Slurry, @NonNull AutomationType> alwaysTrueBi = (stack, automationType) -> true;
    public static final BiPredicate<@NonNull Slurry, @NonNull AutomationType> internalOnly = (stack, automationType) -> automationType == AutomationType.INTERNAL;
    public static final BiPredicate<@NonNull Slurry, @NonNull AutomationType> notExternal = (stack, automationType) -> automationType != AutomationType.EXTERNAL;

    public static BasicSlurryTank create(long capacity, @Nullable IContentsListener listener) {
        return create(capacity, alwaysTrue, listener);
    }

    public static BasicSlurryTank create(long capacity, Predicate<@NonNull Slurry> validator, @Nullable IContentsListener listener) {
        if (capacity < 0) {
            throw new IllegalArgumentException("Capacity must be at least zero");
        }
        Objects.requireNonNull(validator, "Slurry validity check cannot be null");
        return new BasicSlurryTank(capacity, alwaysTrueBi, alwaysTrueBi, validator, listener);
    }

    public static BasicSlurryTank create(long capacity, Predicate<@NonNull Slurry> canExtract, Predicate<@NonNull Slurry> canInsert, @Nullable IContentsListener listener) {
        return create(capacity, canExtract, canInsert, alwaysTrue, listener);
    }

    public static BasicSlurryTank input(long capacity, Predicate<@NonNull Slurry> validator, @Nullable IContentsListener listener) {
        if (capacity < 0) {
            throw new IllegalArgumentException("Capacity must be at least zero");
        }
        Objects.requireNonNull(validator, "Slurry validity check cannot be null");
        return new BasicSlurryTank(capacity, notExternal, alwaysTrueBi, validator, listener);
    }

    public static BasicSlurryTank create(long capacity, Predicate<@NonNull Slurry> canExtract, Predicate<@NonNull Slurry> canInsert,
          Predicate<@NonNull Slurry> validator, @Nullable IContentsListener listener) {
        if (capacity < 0) {
            throw new IllegalArgumentException("Capacity must be at least zero");
        }
        Objects.requireNonNull(canExtract, "Extraction validity check cannot be null");
        Objects.requireNonNull(canInsert, "Insertion validity check cannot be null");
        Objects.requireNonNull(validator, "Slurry validity check cannot be null");
        return new BasicSlurryTank(capacity, canExtract, canInsert, validator, listener);
    }

    public static BasicSlurryTank create(long capacity, BiPredicate<@NonNull Slurry, @NonNull AutomationType> canExtract,
          BiPredicate<@NonNull Slurry, @NonNull AutomationType> canInsert, Predicate<@NonNull Slurry> validator, @Nullable IContentsListener listener) {
        if (capacity < 0) {
            throw new IllegalArgumentException("Capacity must be at least zero");
        }
        Objects.requireNonNull(canExtract, "Extraction validity check cannot be null");
        Objects.requireNonNull(canInsert, "Insertion validity check cannot be null");
        Objects.requireNonNull(validator, "Slurry validity check cannot be null");
        return new BasicSlurryTank(capacity, canExtract, canInsert, validator, listener);
    }

    protected BasicSlurryTank(long capacity, Predicate<@NonNull Slurry> canExtract, Predicate<@NonNull Slurry> canInsert, Predicate<@NonNull Slurry> validator,
          @Nullable IContentsListener listener) {
        this(capacity, (stack, automationType) -> automationType == AutomationType.MANUAL || canExtract.test(stack), (stack, automationType) -> canInsert.test(stack),
              validator, listener);
    }

    protected BasicSlurryTank(long capacity, BiPredicate<@NonNull Slurry, @NonNull AutomationType> canExtract,
          BiPredicate<@NonNull Slurry, @NonNull AutomationType> canInsert, Predicate<@NonNull Slurry> validator, @Nullable IContentsListener listener) {
        this(capacity, canExtract, canInsert, validator, null, listener);
    }

    protected BasicSlurryTank(long capacity, BiPredicate<@NonNull Slurry, @NonNull AutomationType> canExtract, BiPredicate<@NonNull Slurry, @NonNull AutomationType> canInsert,
          Predicate<@NonNull Slurry> validator, @Nullable ChemicalAttributeValidator attributeValidator, @Nullable IContentsListener listener) {
        super(capacity, canExtract, canInsert, validator, attributeValidator, listener);
    }
}