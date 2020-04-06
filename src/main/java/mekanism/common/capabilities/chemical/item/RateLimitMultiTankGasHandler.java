package mekanism.common.capabilities.chemical.item;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.BiPredicate;
import java.util.function.Function;
import java.util.function.IntSupplier;
import java.util.function.Predicate;
import javax.annotation.ParametersAreNonnullByDefault;
import mcp.MethodsReturnNonnullByDefault;
import mekanism.api.annotations.NonNull;
import mekanism.api.chemical.IChemicalTank;
import mekanism.api.chemical.gas.BasicGasTank;
import mekanism.api.chemical.gas.Gas;
import mekanism.api.chemical.gas.GasStack;
import mekanism.api.chemical.gas.IMekanismGasHandler;
import mekanism.api.inventory.AutomationType;
import mekanism.common.capabilities.chemical.item.RateLimitGasHandler.RateLimitGasTank;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class RateLimitMultiTankGasHandler extends ItemStackMekanismGasHandler {

    public static RateLimitMultiTankGasHandler create(@NonNull Collection<GasTankSpec> gasTanks) {
        List<Function<IMekanismGasHandler, IChemicalTank<Gas, GasStack>>> tankProviders = new ArrayList<>();
        for (GasTankSpec spec : gasTanks) {
            tankProviders.add(handler -> new RateLimitGasTank(spec.rate, spec.capacity, spec.canExtract, spec.canInsert, spec.isValid, null, handler));
        }
        return new RateLimitMultiTankGasHandler(tankProviders);
    }

    private List<IChemicalTank<Gas, GasStack>> tanks;

    private RateLimitMultiTankGasHandler(List<Function<IMekanismGasHandler, IChemicalTank<Gas, GasStack>>> tankProviders) {
        tanks = new ArrayList<>(tankProviders.size());
        for (Function<IMekanismGasHandler, IChemicalTank<Gas, GasStack>> provider : tankProviders) {
            tanks.add(provider.apply(this));
        }
    }

    @Override
    protected List<? extends IChemicalTank<Gas, GasStack>> getInitialTanks() {
        return tanks;
    }

    public static class GasTankSpec {
        int rate;
        IntSupplier capacity;
        Predicate<@NonNull Gas> isValid;
        BiPredicate<@NonNull Gas, @NonNull AutomationType> canExtract;
        BiPredicate<@NonNull Gas, @NonNull AutomationType> canInsert;
        public GasTankSpec(int rate, IntSupplier capacity, BiPredicate<@NonNull Gas, @NonNull AutomationType> canExtract,
              BiPredicate<@NonNull Gas, @NonNull AutomationType> canInsert, Predicate<@NonNull Gas> isValid) {
            this.rate = rate;
            this.capacity = capacity;
            this.isValid = isValid;
            this.canExtract = canExtract;
            this.canInsert = canInsert;
        }
        public static GasTankSpec create(int rate, IntSupplier capacity) {
            return new GasTankSpec(rate, capacity, BasicGasTank.alwaysTrueBi, BasicGasTank.alwaysTrueBi, BasicGasTank.alwaysTrue);
        }
        public static GasTankSpec createFillOnly(int rate, IntSupplier capacity, Predicate<@NonNull Gas> isValid) {
            return new GasTankSpec(rate, capacity, (item, automationType) -> automationType != AutomationType.EXTERNAL, BasicGasTank.alwaysTrueBi, isValid);
        }
        public boolean isValid(@NonNull Gas gas) {
            return isValid.test(gas);
        }
    }
}
