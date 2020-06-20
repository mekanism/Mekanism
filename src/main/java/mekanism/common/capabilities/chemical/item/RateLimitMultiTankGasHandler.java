package mekanism.common.capabilities.chemical.item;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.BiPredicate;
import java.util.function.Function;
import java.util.function.LongSupplier;
import java.util.function.Predicate;
import javax.annotation.ParametersAreNonnullByDefault;
import mcp.MethodsReturnNonnullByDefault;
import mekanism.api.annotations.NonNull;
import mekanism.api.chemical.ChemicalTankBuilder;
import mekanism.api.chemical.gas.Gas;
import mekanism.api.chemical.gas.IGasTank;
import mekanism.api.inventory.AutomationType;
import mekanism.common.capabilities.chemical.variable.RateLimitChemicalTank.RateLimitGasTank;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class RateLimitMultiTankGasHandler extends ItemStackMekanismGasHandler {

    public static RateLimitMultiTankGasHandler create(@NonNull Collection<GasTankSpec> gasTanks) {
        List<Function<IMekanismGasHandler, IGasTank>> tankProviders = new ArrayList<>();
        for (GasTankSpec spec : gasTanks) {
            tankProviders.add(handler -> new RateLimitGasTank(spec.rate, spec.capacity, spec.canExtract, spec.canInsert, spec.isValid, null, handler));
        }
        return new RateLimitMultiTankGasHandler(tankProviders);
    }

    private final List<IGasTank> tanks;

    private RateLimitMultiTankGasHandler(List<Function<IMekanismGasHandler, IGasTank>> tankProviders) {
        tanks = new ArrayList<>(tankProviders.size());
        for (Function<IMekanismGasHandler, IGasTank> provider : tankProviders) {
            tanks.add(provider.apply(this));
        }
    }

    @Override
    protected List<IGasTank> getInitialTanks() {
        return tanks;
    }

    public static class GasTankSpec {

        final LongSupplier rate;
        final LongSupplier capacity;
        final Predicate<@NonNull Gas> isValid;
        final BiPredicate<@NonNull Gas, @NonNull AutomationType> canExtract;
        final BiPredicate<@NonNull Gas, @NonNull AutomationType> canInsert;

        public GasTankSpec(LongSupplier rate, LongSupplier capacity, BiPredicate<@NonNull Gas, @NonNull AutomationType> canExtract,
              BiPredicate<@NonNull Gas, @NonNull AutomationType> canInsert, Predicate<@NonNull Gas> isValid) {
            this.rate = rate;
            this.capacity = capacity;
            this.isValid = isValid;
            this.canExtract = canExtract;
            this.canInsert = canInsert;
        }

        public static GasTankSpec create(LongSupplier rate, LongSupplier capacity) {
            return new GasTankSpec(rate, capacity, ChemicalTankBuilder.GAS.alwaysTrueBi, ChemicalTankBuilder.GAS.alwaysTrueBi, ChemicalTankBuilder.GAS.alwaysTrue);
        }

        public static GasTankSpec createFillOnly(LongSupplier rate, LongSupplier capacity, Predicate<@NonNull Gas> isValid) {
            return new GasTankSpec(rate, capacity, (item, automationType) -> automationType != AutomationType.EXTERNAL, ChemicalTankBuilder.GAS.alwaysTrueBi, isValid);
        }

        public boolean isValid(@NonNull Gas gas) {
            return isValid.test(gas);
        }
    }
}
