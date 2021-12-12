package mekanism.common.capabilities.chemical.item;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.function.BiPredicate;
import java.util.function.LongSupplier;
import java.util.function.Predicate;
import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import mcp.MethodsReturnNonnullByDefault;
import mekanism.api.annotations.NonNull;
import mekanism.api.chemical.ChemicalTankBuilder;
import mekanism.api.chemical.gas.Gas;
import mekanism.api.chemical.gas.IGasTank;
import mekanism.api.inventory.AutomationType;
import mekanism.common.capabilities.chemical.variable.RateLimitChemicalTank.RateLimitGasTank;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.util.TriPredicate;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class RateLimitMultiTankGasHandler extends ItemStackMekanismGasHandler {

    public static RateLimitMultiTankGasHandler create(@Nonnull Collection<GasTankSpec> gasTanks) {
        return new RateLimitMultiTankGasHandler(gasTanks);
    }

    private final List<IGasTank> tanks;

    private RateLimitMultiTankGasHandler(@Nonnull Collection<GasTankSpec> gasTanks) {
        List<IGasTank> tankProviders = new ArrayList<>();
        for (GasTankSpec spec : gasTanks) {
            tankProviders.add(new RateLimitGasTank(spec.rate, spec.capacity, spec.canExtract,
                  (gas, automationType) -> spec.canInsert.test(gas, automationType, getStack()), spec.isValid, null, this));
        }
        tanks = Collections.unmodifiableList(tankProviders);
    }

    @Override
    protected List<IGasTank> getInitialTanks() {
        return tanks;
    }

    public static class GasTankSpec {

        private static final TriPredicate<@NonNull Gas, @NonNull AutomationType, @NonNull ItemStack> alwaysTrue = (gas, automationType, stack) -> true;

        final LongSupplier rate;
        final LongSupplier capacity;
        final Predicate<@NonNull Gas> isValid;
        final BiPredicate<@NonNull Gas, @NonNull AutomationType> canExtract;
        final TriPredicate<@NonNull Gas, @NonNull AutomationType, @NonNull ItemStack> canInsert;

        public GasTankSpec(LongSupplier rate, LongSupplier capacity, BiPredicate<@NonNull Gas, @NonNull AutomationType> canExtract,
              TriPredicate<@NonNull Gas, @NonNull AutomationType, @NonNull ItemStack> canInsert, Predicate<@NonNull Gas> isValid) {
            this.rate = rate;
            this.capacity = capacity;
            this.isValid = isValid;
            this.canExtract = canExtract;
            this.canInsert = canInsert;
        }

        public static GasTankSpec create(LongSupplier rate, LongSupplier capacity) {
            return new GasTankSpec(rate, capacity, ChemicalTankBuilder.GAS.alwaysTrueBi, alwaysTrue, ChemicalTankBuilder.GAS.alwaysTrue);
        }

        public static GasTankSpec createFillOnly(LongSupplier rate, LongSupplier capacity, Predicate<@NonNull Gas> isValid) {
            return createFillOnly(rate, capacity, alwaysTrue, isValid);
        }

        public static GasTankSpec createFillOnly(LongSupplier rate, LongSupplier capacity, TriPredicate<@NonNull Gas, @NonNull AutomationType, @NonNull ItemStack> canInsert,
              Predicate<@NonNull Gas> isValid) {
            return new GasTankSpec(rate, capacity, ChemicalTankBuilder.GAS.notExternal, canInsert, isValid);
        }

        public boolean isValid(@NonNull Gas gas) {
            return isValid.test(gas);
        }
    }
}
