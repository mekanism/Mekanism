package mekanism.api.chemical;

import java.util.Objects;
import java.util.function.BiPredicate;
import java.util.function.Predicate;
import mekanism.api.AutomationType;
import mekanism.api.IContentsListener;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.chemical.attribute.ChemicalAttributeValidator;
import mekanism.api.functions.ConstantPredicates;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Helper class for creating Chemical Tanks.
 */
@NothingNullByDefault
public final class ChemicalTankBuilder {

    public static final Predicate<Chemical> alwaysTrue = ConstantPredicates.alwaysTrue();
    public static final Predicate<Chemical> alwaysFalse = ConstantPredicates.alwaysFalse();
    public static final BiPredicate<Chemical, @NotNull AutomationType> alwaysTrueBi = ConstantPredicates.alwaysTrueBi();
    public static final BiPredicate<Chemical, @NotNull AutomationType> internalOnly = ConstantPredicates.internalOnly();
    public static final BiPredicate<Chemical, @NotNull AutomationType> notExternal = ConstantPredicates.notExternal();
    /**
     * @since 10.5.0
     */
    public static final BiPredicate<Chemical, @NotNull AutomationType> manualOnly = (chemical, automationType) -> automationType == AutomationType.MANUAL;

    private ChemicalTankBuilder() {
    }

    /**
     * Creates a dummy tank with a given capacity.
     *
     * @param capacity Tank capacity.
     */
    public static IChemicalTank createDummy(long capacity) {
        return createAllValid(capacity, null);
    }

    /**
     * Creates a tank with a given capacity, and content listener, using the default attribute validator {@link ChemicalAttributeValidator#DEFAULT}.
     *
     * @param capacity Tank capacity.
     * @param listener Contents change listener.
     */
    public static IChemicalTank create(long capacity, @Nullable IContentsListener listener) {
        return createWithValidator(capacity, null, listener);
    }

    /**
     * Creates a tank with a given capacity, attribute validator, and content listener.
     *
     * @param capacity           Tank capacity.
     * @param attributeValidator Chemical Attribute Validator, or {@code null} to fall back to {@link ChemicalAttributeValidator#DEFAULT}.
     * @param listener           Contents change listener.
     */
    public static IChemicalTank createWithValidator(long capacity, @Nullable ChemicalAttributeValidator attributeValidator, @Nullable IContentsListener listener) {
        if (capacity < 0) {
            throw new IllegalArgumentException("Capacity must be at least zero");
        }
        return new BasicChemicalTank(capacity, alwaysTrueBi, alwaysTrueBi, alwaysTrue, attributeValidator, listener);
    }

    /**
     * Creates a tank with a given capacity, and content listener, that allows chemicals with any attributes.
     *
     * @param capacity Tank capacity.
     * @param listener Contents change listener.
     */
    public static IChemicalTank createAllValid(long capacity, @Nullable IContentsListener listener) {
        return createWithValidator(capacity, ChemicalAttributeValidator.ALWAYS_ALLOW, listener);
    }

    /**
     * Creates a tank with a given capacity, extract predicate, insert predicate, and content listener, using the default attribute validator
     * {@link ChemicalAttributeValidator#DEFAULT}.
     *
     * @param capacity   Tank capacity.
     * @param canExtract Extract predicate.
     * @param canInsert  Insert predicate.
     * @param listener   Contents change listener.
     *
     * @implNote The created tank will always allow {@link AutomationType#MANUAL} extraction, and allow any {@link AutomationType} to insert into it.
     */
    public static IChemicalTank create(long capacity, Predicate<Chemical> canExtract, Predicate<Chemical> canInsert, @Nullable IContentsListener listener) {
        return create(capacity, canExtract, canInsert, alwaysTrue, listener);
    }

    /**
     * Creates a tank with a given capacity, validation predicate, and content listener, using the default attribute validator
     * {@link ChemicalAttributeValidator#DEFAULT}.
     *
     * @param capacity  Tank capacity.
     * @param validator Validation predicate.
     * @param listener  Contents change listener.
     */
    public static IChemicalTank create(long capacity, Predicate<Chemical> validator, @Nullable IContentsListener listener) {
        if (capacity < 0) {
            throw new IllegalArgumentException("Capacity must be at least zero");
        }
        Objects.requireNonNull(validator, "Chemical validity check cannot be null");
        return new BasicChemicalTank(capacity, alwaysTrueBi, alwaysTrueBi, validator, null, listener);
    }

    /**
     * Creates an input tank with a given capacity, validation predicate, and content listener, using the default attribute validator
     * {@link ChemicalAttributeValidator#DEFAULT}. Input tanks don't allow for external ({@link AutomationType#EXTERNAL}) extraction.
     *
     * @param capacity  Tank capacity.
     * @param validator Validation predicate.
     * @param listener  Contents change listener.
     */
    public static IChemicalTank input(long capacity, Predicate<Chemical> validator, @Nullable IContentsListener listener) {
        if (capacity < 0) {
            throw new IllegalArgumentException("Capacity must be at least zero");
        }
        Objects.requireNonNull(validator, "Chemical validity check cannot be null");
        return new BasicChemicalTank(capacity, notExternal, alwaysTrueBi, validator, null, listener);
    }

    /**
     * Creates an input tank with a given capacity, insertion predicate, validation predicate, and content listener, using the default attribute validator
     * {@link ChemicalAttributeValidator#DEFAULT}. Input tanks don't allow for external ({@link AutomationType#EXTERNAL}) extraction.
     *
     * @param capacity  Tank capacity.
     * @param canInsert Insert predicate.
     * @param validator Validation predicate.
     * @param listener  Contents change listener.
     */
    public static IChemicalTank input(long capacity, Predicate<Chemical> canInsert, Predicate<Chemical> validator, @Nullable IContentsListener listener) {
        if (capacity < 0) {
            throw new IllegalArgumentException("Capacity must be at least zero");
        }
        Objects.requireNonNull(canInsert, "Insertion validity check cannot be null");
        Objects.requireNonNull(validator, "Chemical validity check cannot be null");
        return new BasicChemicalTank(capacity, notExternal, (stack, automationType) -> canInsert.test(stack), validator, null, listener);
    }

    /**
     * Creates an output tank with a given capacity, and content listener, that allows chemicals with any attributes. Output tanks only allow for internal
     * ({@link AutomationType#INTERNAL}) insertion.
     *
     * @param capacity Tank capacity.
     * @param listener Contents change listener.
     */
    public static IChemicalTank output(long capacity, @Nullable IContentsListener listener) {
        if (capacity < 0) {
            throw new IllegalArgumentException("Capacity must be at least zero");
        }
        return new BasicChemicalTank(capacity, alwaysTrueBi, internalOnly, alwaysTrue, ChemicalAttributeValidator.ALWAYS_ALLOW, listener);
    }

    /**
     * Creates a tank with a given capacity, extract predicate, insert predicate, validation predicate, and content listener, using the default attribute validator
     * {@link ChemicalAttributeValidator#DEFAULT}.
     *
     * @param capacity   Tank capacity.
     * @param canExtract Extract predicate.
     * @param canInsert  Insert predicate.
     * @param validator  Validation predicate.
     * @param listener   Contents change listener.
     *
     * @implNote The created tank will always allow {@link AutomationType#MANUAL} extraction, and allow any {@link AutomationType} to insert into it.
     */
    public static IChemicalTank create(long capacity, Predicate<Chemical> canExtract, Predicate<Chemical> canInsert, Predicate<Chemical> validator,
          @Nullable IContentsListener listener) {
        return create(capacity, canExtract, canInsert, validator, null, listener);
    }

    /**
     * Creates a tank with a given capacity, extract predicate, insert predicate, validation predicate, and content listener, using the default attribute validator
     * {@link ChemicalAttributeValidator#DEFAULT}.
     *
     * @param capacity   Tank capacity.
     * @param canExtract Extract predicate.
     * @param canInsert  Insert predicate.
     * @param validator  Validation predicate.
     * @param listener   Contents change listener.
     */
    public static IChemicalTank create(long capacity, BiPredicate<Chemical, @NotNull AutomationType> canExtract,
          BiPredicate<Chemical, @NotNull AutomationType> canInsert, Predicate<Chemical> validator, @Nullable IContentsListener listener) {
        return create(capacity, canExtract, canInsert, validator, null, listener);
    }

    /**
     * Creates a tank with a given capacity, extract predicate, insert predicate, validation predicate, attribute validator, and content listener.
     *
     * @param capacity           Tank capacity.
     * @param canExtract         Extract predicate.
     * @param canInsert          Insert predicate.
     * @param validator          Validation predicate.
     * @param attributeValidator Chemical Attribute Validator, or {@code null} to fall back to {@link ChemicalAttributeValidator#DEFAULT}.
     * @param listener           Contents change listener.
     *
     * @implNote The created tank will always allow {@link AutomationType#MANUAL} extraction, and allow any {@link AutomationType} to insert into it.
     */
    public static IChemicalTank create(long capacity, Predicate<Chemical> canExtract, Predicate<Chemical> canInsert, Predicate<Chemical> validator,
          @Nullable ChemicalAttributeValidator attributeValidator, @Nullable IContentsListener listener) {
        if (capacity < 0) {
            throw new IllegalArgumentException("Capacity must be at least zero");
        }
        Objects.requireNonNull(canExtract, "Extraction validity check cannot be null");
        Objects.requireNonNull(canInsert, "Insertion validity check cannot be null");
        Objects.requireNonNull(validator, "Chemical validity check cannot be null");
        return createUnchecked(capacity, canExtract, canInsert, validator, attributeValidator, listener);
    }

    /**
     * Creates a tank with a given capacity, extract predicate, insert predicate, validation predicate, attribute validator, and content listener.
     *
     * @param capacity           Tank capacity.
     * @param canExtract         Extract predicate.
     * @param canInsert          Insert predicate.
     * @param validator          Validation predicate.
     * @param attributeValidator Chemical Attribute Validator, or {@code null} to fall back to {@link ChemicalAttributeValidator#DEFAULT}.
     * @param listener           Contents change listener.
     */
    public static IChemicalTank create(long capacity, BiPredicate<Chemical, @NotNull AutomationType> canExtract, BiPredicate<Chemical, @NotNull AutomationType> canInsert,
          Predicate<Chemical> validator, @Nullable ChemicalAttributeValidator attributeValidator, @Nullable IContentsListener listener) {
        if (capacity < 0) {
            throw new IllegalArgumentException("Capacity must be at least zero");
        }
        Objects.requireNonNull(canExtract, "Extraction validity check cannot be null");
        Objects.requireNonNull(canInsert, "Insertion validity check cannot be null");
        Objects.requireNonNull(validator, "Chemical validity check cannot be null");
        return new BasicChemicalTank(capacity, canExtract, canInsert, validator, attributeValidator, listener);
    }

    private static IChemicalTank createUnchecked(long capacity, Predicate<Chemical> canExtract, Predicate<Chemical> canInsert, Predicate<Chemical> validator,
          @Nullable ChemicalAttributeValidator attributeValidator, @Nullable IContentsListener listener) {
        return new BasicChemicalTank(capacity, (stack, automationType) -> automationType == AutomationType.MANUAL || canExtract.test(stack),
              (stack, automationType) -> canInsert.test(stack), validator, attributeValidator, listener);
    }
}