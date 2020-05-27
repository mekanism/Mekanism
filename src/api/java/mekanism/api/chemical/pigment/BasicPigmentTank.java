package mekanism.api.chemical.pigment;

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
public class BasicPigmentTank extends BasicChemicalTank<Pigment, PigmentStack> implements IPigmentHandler, IPigmentTank {

    public static final Predicate<@NonNull Pigment> alwaysTrue = stack -> true;
    public static final BiPredicate<@NonNull Pigment, @NonNull AutomationType> alwaysTrueBi = (stack, automationType) -> true;
    public static final BiPredicate<@NonNull Pigment, @NonNull AutomationType> internalOnly = (stack, automationType) -> automationType == AutomationType.INTERNAL;
    public static final BiPredicate<@NonNull Pigment, @NonNull AutomationType> notExternal = (stack, automationType) -> automationType != AutomationType.EXTERNAL;

    public static BasicPigmentTank create(long capacity, @Nullable IContentsListener listener) {
        return create(capacity, alwaysTrue, listener);
    }

    public static BasicPigmentTank create(long capacity, Predicate<@NonNull Pigment> validator, @Nullable IContentsListener listener) {
        if (capacity < 0) {
            throw new IllegalArgumentException("Capacity must be at least zero");
        }
        Objects.requireNonNull(validator, "Pigment validity check cannot be null");
        return new BasicPigmentTank(capacity, alwaysTrueBi, alwaysTrueBi, validator, listener);
    }

    public static BasicPigmentTank create(long capacity, Predicate<@NonNull Pigment> canExtract, Predicate<@NonNull Pigment> canInsert,
          @Nullable IContentsListener listener) {
        return create(capacity, canExtract, canInsert, alwaysTrue, listener);
    }

    public static BasicPigmentTank input(long capacity, Predicate<@NonNull Pigment> validator, @Nullable IContentsListener listener) {
        if (capacity < 0) {
            throw new IllegalArgumentException("Capacity must be at least zero");
        }
        Objects.requireNonNull(validator, "Pigment validity check cannot be null");
        return new BasicPigmentTank(capacity, notExternal, alwaysTrueBi, validator, listener);
    }

    public static BasicPigmentTank create(long capacity, Predicate<@NonNull Pigment> canExtract, Predicate<@NonNull Pigment> canInsert,
          Predicate<@NonNull Pigment> validator, @Nullable IContentsListener listener) {
        if (capacity < 0) {
            throw new IllegalArgumentException("Capacity must be at least zero");
        }
        Objects.requireNonNull(canExtract, "Extraction validity check cannot be null");
        Objects.requireNonNull(canInsert, "Insertion validity check cannot be null");
        Objects.requireNonNull(validator, "Pigment validity check cannot be null");
        return new BasicPigmentTank(capacity, canExtract, canInsert, validator, listener);
    }

    public static BasicPigmentTank create(long capacity, BiPredicate<@NonNull Pigment, @NonNull AutomationType> canExtract,
          BiPredicate<@NonNull Pigment, @NonNull AutomationType> canInsert, Predicate<@NonNull Pigment> validator, @Nullable IContentsListener listener) {
        if (capacity < 0) {
            throw new IllegalArgumentException("Capacity must be at least zero");
        }
        Objects.requireNonNull(canExtract, "Extraction validity check cannot be null");
        Objects.requireNonNull(canInsert, "Insertion validity check cannot be null");
        Objects.requireNonNull(validator, "Pigment validity check cannot be null");
        return new BasicPigmentTank(capacity, canExtract, canInsert, validator, listener);
    }

    protected BasicPigmentTank(long capacity, Predicate<@NonNull Pigment> canExtract, Predicate<@NonNull Pigment> canInsert, Predicate<@NonNull Pigment> validator,
          @Nullable IContentsListener listener) {
        this(capacity, (stack, automationType) -> automationType == AutomationType.MANUAL || canExtract.test(stack), (stack, automationType) -> canInsert.test(stack),
              validator, listener);
    }

    protected BasicPigmentTank(long capacity, BiPredicate<@NonNull Pigment, @NonNull AutomationType> canExtract,
          BiPredicate<@NonNull Pigment, @NonNull AutomationType> canInsert, Predicate<@NonNull Pigment> validator, @Nullable IContentsListener listener) {
        this(capacity, canExtract, canInsert, validator, null, listener);
    }

    protected BasicPigmentTank(long capacity, BiPredicate<@NonNull Pigment, @NonNull AutomationType> canExtract, BiPredicate<@NonNull Pigment, @NonNull AutomationType> canInsert,
          Predicate<@NonNull Pigment> validator, @Nullable ChemicalAttributeValidator attributeValidator, @Nullable IContentsListener listener) {
        super(capacity, canExtract, canInsert, validator, attributeValidator, listener);
    }
}