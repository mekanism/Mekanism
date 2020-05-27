package mekanism.common.capabilities.chemical.variable;

import java.util.function.BiPredicate;
import java.util.function.LongSupplier;
import java.util.function.Predicate;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import mcp.MethodsReturnNonnullByDefault;
import mekanism.api.IContentsListener;
import mekanism.api.annotations.FieldsAreNonnullByDefault;
import mekanism.api.annotations.NonNull;
import mekanism.api.chemical.Chemical;
import mekanism.api.chemical.ChemicalStack;
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

@FieldsAreNonnullByDefault
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public abstract class RateLimitChemicalTank<CHEMICAL extends Chemical<CHEMICAL>, STACK extends ChemicalStack<CHEMICAL>> extends
      VariableCapacityChemicalTank<CHEMICAL, STACK> {

    private final LongSupplier rate;

    public RateLimitChemicalTank(LongSupplier rate, LongSupplier capacity, BiPredicate<@NonNull CHEMICAL, @NonNull AutomationType> canExtract,
          BiPredicate<@NonNull CHEMICAL, @NonNull AutomationType> canInsert, Predicate<@NonNull CHEMICAL> isValid,
          @Nullable ChemicalAttributeValidator attributeValidator, @Nullable IContentsListener listener) {
        super(capacity, canExtract, canInsert, isValid, attributeValidator, listener);
        this.rate = rate;
    }

    @Override
    protected long getRate(@Nullable AutomationType automationType) {
        //Allow unknown or manual interaction to bypass rate limit for the item
        return automationType == null || automationType == AutomationType.MANUAL ? super.getRate(automationType) : rate.getAsLong();
    }

    public static class RateLimitGasTank extends RateLimitChemicalTank<Gas, GasStack> implements IGasHandler, IGasTank {

        public RateLimitGasTank(LongSupplier rate, LongSupplier capacity, BiPredicate<@NonNull Gas, @NonNull AutomationType> canExtract,
              BiPredicate<@NonNull Gas, @NonNull AutomationType> canInsert, Predicate<@NonNull Gas> isValid, @Nullable ChemicalAttributeValidator attributeValidator,
              @Nullable IContentsListener listener) {
            super(rate, capacity, canExtract, canInsert, isValid, attributeValidator, listener);
        }
    }

    public static class RateLimitInfusionTank extends RateLimitChemicalTank<InfuseType, InfusionStack> implements IInfusionHandler, IInfusionTank {

        public RateLimitInfusionTank(LongSupplier rate, LongSupplier capacity, BiPredicate<@NonNull InfuseType, @NonNull AutomationType> canExtract,
              BiPredicate<@NonNull InfuseType, @NonNull AutomationType> canInsert, Predicate<@NonNull InfuseType> isValid, @Nullable IContentsListener listener) {
            super(rate, capacity, canExtract, canInsert, isValid, null, listener);
        }
    }

    public static class RateLimitPigmentTank extends RateLimitChemicalTank<Pigment, PigmentStack> implements IPigmentHandler, IPigmentTank {

        public RateLimitPigmentTank(LongSupplier rate, LongSupplier capacity, BiPredicate<@NonNull Pigment, @NonNull AutomationType> canExtract,
              BiPredicate<@NonNull Pigment, @NonNull AutomationType> canInsert, Predicate<@NonNull Pigment> isValid, @Nullable IContentsListener listener) {
            super(rate, capacity, canExtract, canInsert, isValid, null, listener);
        }
    }

    public static class RateLimitSlurryTank extends RateLimitChemicalTank<Slurry, SlurryStack> implements ISlurryHandler, ISlurryTank {

        public RateLimitSlurryTank(LongSupplier rate, LongSupplier capacity, BiPredicate<@NonNull Slurry, @NonNull AutomationType> canExtract,
              BiPredicate<@NonNull Slurry, @NonNull AutomationType> canInsert, Predicate<@NonNull Slurry> isValid, @Nullable IContentsListener listener) {
            super(rate, capacity, canExtract, canInsert, isValid, null, listener);
        }
    }
}