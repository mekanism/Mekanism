package mekanism.api.chemical;

import java.util.Objects;
import java.util.function.BiPredicate;
import java.util.function.Predicate;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import mcp.MethodsReturnNonnullByDefault;
import mekanism.api.IContentsListener;
import mekanism.api.annotations.FieldsAreNonnullByDefault;
import mekanism.api.annotations.NonNull;
import mekanism.api.chemical.attribute.ChemicalAttributeValidator;
import mekanism.api.chemical.gas.Gas;
import mekanism.api.chemical.gas.GasStack;
import mekanism.api.chemical.gas.IGasHandler;
import mekanism.api.chemical.gas.IGasTank;
import mekanism.api.chemical.infuse.IInfusionHandler;
import mekanism.api.chemical.infuse.IInfusionTank;
import mekanism.api.chemical.infuse.InfuseType;
import mekanism.api.chemical.infuse.InfusionStack;
import mekanism.api.chemical.pigment.IPigmentHandler;
import mekanism.api.chemical.pigment.IPigmentTank;
import mekanism.api.chemical.pigment.Pigment;
import mekanism.api.chemical.pigment.PigmentStack;
import mekanism.api.chemical.slurry.ISlurryHandler;
import mekanism.api.chemical.slurry.ISlurryTank;
import mekanism.api.chemical.slurry.Slurry;
import mekanism.api.chemical.slurry.SlurryStack;
import mekanism.api.inventory.AutomationType;

/**
 * Helper class for creating Chemical Tanks.
 */
@FieldsAreNonnullByDefault
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class ChemicalTankBuilder<CHEMICAL extends Chemical<CHEMICAL>, STACK extends ChemicalStack<CHEMICAL>, TANK extends IChemicalTank<CHEMICAL, STACK>> {

    public static final ChemicalTankBuilder<Gas, GasStack, IGasTank> GAS = new ChemicalTankBuilder<>(BasicGasTank::new);
    public static final ChemicalTankBuilder<InfuseType, InfusionStack, IInfusionTank> INFUSION = new ChemicalTankBuilder<>(BasicInfusionTank::new);
    public static final ChemicalTankBuilder<Pigment, PigmentStack, IPigmentTank> PIGMENT = new ChemicalTankBuilder<>(BasicPigmentTank::new);
    public static final ChemicalTankBuilder<Slurry, SlurryStack, ISlurryTank> SLURRY = new ChemicalTankBuilder<>(BasicSlurryTank::new);

    public final Predicate<@NonNull CHEMICAL> alwaysTrue = stack -> true;
    public final Predicate<@NonNull CHEMICAL> alwaysFalse = stack -> false;
    public final BiPredicate<@NonNull CHEMICAL, @NonNull AutomationType> alwaysTrueBi = (stack, automationType) -> true;
    public final BiPredicate<@NonNull CHEMICAL, @NonNull AutomationType> internalOnly = (stack, automationType) -> automationType == AutomationType.INTERNAL;
    public final BiPredicate<@NonNull CHEMICAL, @NonNull AutomationType> notExternal = (stack, automationType) -> automationType != AutomationType.EXTERNAL;

    private final BasicTankCreator<CHEMICAL, STACK, TANK> tankCreator;

    private ChemicalTankBuilder(BasicTankCreator<CHEMICAL, STACK, TANK> tankCreator) {
        this.tankCreator = tankCreator;
    }

    /**
     * Creates a dummy tank with a given capacity.
     *
     * @param capacity Tank capacity.
     */
    public TANK createDummy(long capacity) {
        return createAllValid(capacity, null);
    }

    /**
     * Creates a tank with a given capacity, and content listener, using the default attribute validator {@link ChemicalAttributeValidator#DEFAULT}.
     *
     * @param capacity Tank capacity.
     * @param listener Contents change listener.
     */
    public TANK create(long capacity, @Nullable IContentsListener listener) {
        return createWithValidator(capacity, null, listener);
    }

    /**
     * Creates a tank with a given capacity, attribute validator, and content listener.
     *
     * @param capacity           Tank capacity.
     * @param attributeValidator Chemical Attribute Validator, or {@code null} to fall back to {@link ChemicalAttributeValidator#DEFAULT}.
     * @param listener           Contents change listener.
     */
    public TANK createWithValidator(long capacity, @Nullable ChemicalAttributeValidator attributeValidator, @Nullable IContentsListener listener) {
        if (capacity < 0) {
            throw new IllegalArgumentException("Capacity must be at least zero");
        }
        return tankCreator.create(capacity, alwaysTrueBi, alwaysTrueBi, alwaysTrue, attributeValidator, listener);
    }

    /**
     * Creates a tank with a given capacity, and content listener, that allows chemicals with any attributes.
     *
     * @param capacity Tank capacity.
     * @param listener Contents change listener.
     */
    public TANK createAllValid(long capacity, @Nullable IContentsListener listener) {
        return createWithValidator(capacity, ChemicalAttributeValidator.ALWAYS_ALLOW, listener);
    }

    /**
     * Creates a tank with a given capacity, extract predicate, insert predicate, and content listener, using the default attribute validator {@link
     * ChemicalAttributeValidator#DEFAULT}.
     *
     * @param capacity   Tank capacity.
     * @param canExtract Extract predicate.
     * @param canInsert  Insert predicate.
     * @param listener   Contents change listener.
     *
     * @implNote The created tank will always allow {@link AutomationType#MANUAL} extraction, and allow any {@link AutomationType} to insert into it.
     */
    public TANK create(long capacity, Predicate<@NonNull CHEMICAL> canExtract, Predicate<@NonNull CHEMICAL> canInsert, @Nullable IContentsListener listener) {
        return create(capacity, canExtract, canInsert, alwaysTrue, listener);
    }

    /**
     * Creates a tank with a given capacity, validation predicate, and content listener, using the default attribute validator {@link
     * ChemicalAttributeValidator#DEFAULT}.
     *
     * @param capacity  Tank capacity.
     * @param validator Validation predicate.
     * @param listener  Contents change listener.
     */
    public TANK create(long capacity, Predicate<@NonNull CHEMICAL> validator, @Nullable IContentsListener listener) {
        if (capacity < 0) {
            throw new IllegalArgumentException("Capacity must be at least zero");
        }
        Objects.requireNonNull(validator, "Chemical validity check cannot be null");
        return tankCreator.create(capacity, alwaysTrueBi, alwaysTrueBi, validator, null, listener);
    }

    /**
     * Creates an input tank with a given capacity, validation predicate, and content listener, using the default attribute validator {@link
     * ChemicalAttributeValidator#DEFAULT}. Input tanks don't allow for external ({@link AutomationType#EXTERNAL}) extraction.
     *
     * @param capacity  Tank capacity.
     * @param validator Validation predicate.
     * @param listener  Contents change listener.
     */
    public TANK input(long capacity, Predicate<@NonNull CHEMICAL> validator, @Nullable IContentsListener listener) {
        if (capacity < 0) {
            throw new IllegalArgumentException("Capacity must be at least zero");
        }
        Objects.requireNonNull(validator, "Chemical validity check cannot be null");
        return tankCreator.create(capacity, notExternal, alwaysTrueBi, validator, null, listener);
    }

    /**
     * Creates an input tank with a given capacity, insertion predicate, validation predicate, and content listener, using the default attribute validator {@link
     * ChemicalAttributeValidator#DEFAULT}. Input tanks don't allow for external ({@link AutomationType#EXTERNAL}) extraction.
     *
     * @param capacity  Tank capacity.
     * @param canInsert Insert predicate.
     * @param validator Validation predicate.
     * @param listener  Contents change listener.
     */
    public TANK input(long capacity, Predicate<@NonNull CHEMICAL> canInsert, Predicate<@NonNull CHEMICAL> validator, @Nullable IContentsListener listener) {
        if (capacity < 0) {
            throw new IllegalArgumentException("Capacity must be at least zero");
        }
        Objects.requireNonNull(canInsert, "Insertion validity check cannot be null");
        Objects.requireNonNull(validator, "Chemical validity check cannot be null");
        return tankCreator.create(capacity, notExternal, (stack, automationType) -> canInsert.test(stack), validator, null, listener);
    }

    /**
     * Creates an output tank with a given capacity, and content listener, that allows chemicals with any attributes. Output tanks only allow for internal ({@link
     * AutomationType#INTERNAL}) insertion.
     *
     * @param capacity Tank capacity.
     * @param listener Contents change listener.
     */
    public TANK output(long capacity, @Nullable IContentsListener listener) {
        if (capacity < 0) {
            throw new IllegalArgumentException("Capacity must be at least zero");
        }
        return tankCreator.create(capacity, alwaysTrueBi, internalOnly, alwaysTrue, ChemicalAttributeValidator.ALWAYS_ALLOW, listener);
    }

    @Deprecated//TODO - 1.18: Remove
    public TANK ejectOutput(long capacity, @Nullable IContentsListener listener) {
        if (capacity < 0) {
            throw new IllegalArgumentException("Capacity must be at least zero");
        }
        return tankCreator.create(capacity, internalOnly, internalOnly, alwaysTrue, ChemicalAttributeValidator.ALWAYS_ALLOW, listener);
    }

    /**
     * Creates a tank with a given capacity, extract predicate, insert predicate, validation predicate, and content listener, using the default attribute validator {@link
     * ChemicalAttributeValidator#DEFAULT}.
     *
     * @param capacity   Tank capacity.
     * @param canExtract Extract predicate.
     * @param canInsert  Insert predicate.
     * @param validator  Validation predicate.
     * @param listener   Contents change listener.
     *
     * @implNote The created tank will always allow {@link AutomationType#MANUAL} extraction, and allow any {@link AutomationType} to insert into it.
     */
    public TANK create(long capacity, Predicate<@NonNull CHEMICAL> canExtract, Predicate<@NonNull CHEMICAL> canInsert, Predicate<@NonNull CHEMICAL> validator,
          @Nullable IContentsListener listener) {
        return create(capacity, canExtract, canInsert, validator, null, listener);
    }

    /**
     * Creates a tank with a given capacity, extract predicate, insert predicate, validation predicate, and content listener, using the default attribute validator {@link
     * ChemicalAttributeValidator#DEFAULT}.
     *
     * @param capacity   Tank capacity.
     * @param canExtract Extract predicate.
     * @param canInsert  Insert predicate.
     * @param validator  Validation predicate.
     * @param listener   Contents change listener.
     */
    public TANK create(long capacity, BiPredicate<@NonNull CHEMICAL, @NonNull AutomationType> canExtract,
          BiPredicate<@NonNull CHEMICAL, @NonNull AutomationType> canInsert, Predicate<@NonNull CHEMICAL> validator, @Nullable IContentsListener listener) {
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
    public TANK create(long capacity, Predicate<@NonNull CHEMICAL> canExtract, Predicate<@NonNull CHEMICAL> canInsert, Predicate<@NonNull CHEMICAL> validator,
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
    public TANK create(long capacity, BiPredicate<@NonNull CHEMICAL, @NonNull AutomationType> canExtract, BiPredicate<@NonNull CHEMICAL, @NonNull AutomationType> canInsert,
          Predicate<@NonNull CHEMICAL> validator, @Nullable ChemicalAttributeValidator attributeValidator, @Nullable IContentsListener listener) {
        if (capacity < 0) {
            throw new IllegalArgumentException("Capacity must be at least zero");
        }
        Objects.requireNonNull(canExtract, "Extraction validity check cannot be null");
        Objects.requireNonNull(canInsert, "Insertion validity check cannot be null");
        Objects.requireNonNull(validator, "Chemical validity check cannot be null");
        return tankCreator.create(capacity, canExtract, canInsert, validator, attributeValidator, listener);
    }

    private TANK createUnchecked(long capacity, Predicate<@NonNull CHEMICAL> canExtract, Predicate<@NonNull CHEMICAL> canInsert, Predicate<@NonNull CHEMICAL> validator,
          @Nullable ChemicalAttributeValidator attributeValidator, @Nullable IContentsListener listener) {
        return tankCreator.create(capacity, (stack, automationType) -> automationType == AutomationType.MANUAL || canExtract.test(stack),
              (stack, automationType) -> canInsert.test(stack), validator, attributeValidator, listener);
    }

    @FunctionalInterface
    private interface BasicTankCreator<CHEMICAL extends Chemical<CHEMICAL>, STACK extends ChemicalStack<CHEMICAL>, TANK extends IChemicalTank<CHEMICAL, STACK>> {

        TANK create(long capacity, BiPredicate<@NonNull CHEMICAL, @NonNull AutomationType> canExtract, BiPredicate<@NonNull CHEMICAL, @NonNull AutomationType> canInsert,
              Predicate<@NonNull CHEMICAL> validator, @Nullable ChemicalAttributeValidator attributeValidator, @Nullable IContentsListener listener);
    }

    public static class BasicGasTank extends BasicChemicalTank<Gas, GasStack> implements IGasHandler, IGasTank {

        protected BasicGasTank(long capacity, BiPredicate<@NonNull Gas, @NonNull AutomationType> canExtract, BiPredicate<@NonNull Gas, @NonNull AutomationType> canInsert,
              Predicate<@NonNull Gas> validator, @Nullable ChemicalAttributeValidator attributeValidator, @Nullable IContentsListener listener) {
            super(capacity, canExtract, canInsert, validator, attributeValidator, listener);
        }
    }

    public static class BasicInfusionTank extends BasicChemicalTank<InfuseType, InfusionStack> implements IInfusionHandler, IInfusionTank {

        protected BasicInfusionTank(long capacity, BiPredicate<@NonNull InfuseType, @NonNull AutomationType> canExtract,
              BiPredicate<@NonNull InfuseType, @NonNull AutomationType> canInsert, Predicate<@NonNull InfuseType> validator,
              @Nullable ChemicalAttributeValidator attributeValidator, @Nullable IContentsListener listener) {
            super(capacity, canExtract, canInsert, validator, attributeValidator, listener);
        }
    }

    public static class BasicPigmentTank extends BasicChemicalTank<Pigment, PigmentStack> implements IPigmentHandler, IPigmentTank {

        protected BasicPigmentTank(long capacity, BiPredicate<@NonNull Pigment, @NonNull AutomationType> canExtract,
              BiPredicate<@NonNull Pigment, @NonNull AutomationType> canInsert, Predicate<@NonNull Pigment> validator,
              @Nullable ChemicalAttributeValidator attributeValidator, @Nullable IContentsListener listener) {
            super(capacity, canExtract, canInsert, validator, attributeValidator, listener);
        }
    }

    public static class BasicSlurryTank extends BasicChemicalTank<Slurry, SlurryStack> implements ISlurryHandler, ISlurryTank {

        protected BasicSlurryTank(long capacity, BiPredicate<@NonNull Slurry, @NonNull AutomationType> canExtract,
              BiPredicate<@NonNull Slurry, @NonNull AutomationType> canInsert, Predicate<@NonNull Slurry> validator,
              @Nullable ChemicalAttributeValidator attributeValidator, @Nullable IContentsListener listener) {
            super(capacity, canExtract, canInsert, validator, attributeValidator, listener);
        }
    }
}