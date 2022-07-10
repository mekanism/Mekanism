package mekanism.common.capabilities.chemical.item;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.function.BiPredicate;
import java.util.function.LongSupplier;
import java.util.function.Predicate;
import mekanism.api.AutomationType;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.chemical.ChemicalTankBuilder;
import mekanism.api.chemical.gas.Gas;
import mekanism.api.chemical.gas.IGasTank;
import mekanism.api.functions.ConstantPredicates;
import mekanism.common.capabilities.chemical.variable.RateLimitChemicalTank.RateLimitGasTank;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.util.TriPredicate;
import org.jetbrains.annotations.NotNull;

@NothingNullByDefault
public class RateLimitMultiTankGasHandler extends ItemStackMekanismGasHandler {

    public static RateLimitMultiTankGasHandler create(@NotNull Collection<GasTankSpec> gasTanks) {
        return new RateLimitMultiTankGasHandler(gasTanks);
    }

    private final List<IGasTank> tanks;

    private RateLimitMultiTankGasHandler(@NotNull Collection<GasTankSpec> gasTanks) {
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

        final LongSupplier rate;
        final LongSupplier capacity;
        final Predicate<@NotNull Gas> isValid;
        final BiPredicate<@NotNull Gas, @NotNull AutomationType> canExtract;
        final TriPredicate<@NotNull Gas, @NotNull AutomationType, @NotNull ItemStack> canInsert;

        public GasTankSpec(LongSupplier rate, LongSupplier capacity, BiPredicate<@NotNull Gas, @NotNull AutomationType> canExtract,
              TriPredicate<@NotNull Gas, @NotNull AutomationType, @NotNull ItemStack> canInsert, Predicate<@NotNull Gas> isValid) {
            this.rate = rate;
            this.capacity = capacity;
            this.isValid = isValid;
            this.canExtract = canExtract;
            this.canInsert = canInsert;
        }

        public static GasTankSpec create(LongSupplier rate, LongSupplier capacity) {
            return new GasTankSpec(rate, capacity, ChemicalTankBuilder.GAS.alwaysTrueBi, ConstantPredicates.alwaysTrueTri(), ChemicalTankBuilder.GAS.alwaysTrue);
        }

        public static GasTankSpec createFillOnly(LongSupplier rate, LongSupplier capacity, Predicate<@NotNull Gas> isValid) {
            return createFillOnly(rate, capacity, ConstantPredicates.alwaysTrueTri(), isValid);
        }

        public static GasTankSpec createFillOnly(LongSupplier rate, LongSupplier capacity, TriPredicate<@NotNull Gas, @NotNull AutomationType, @NotNull ItemStack> canInsert,
              Predicate<@NotNull Gas> isValid) {
            return new GasTankSpec(rate, capacity, ChemicalTankBuilder.GAS.notExternal, canInsert, isValid);
        }

        public boolean isValid(@NotNull Gas gas) {
            return isValid.test(gas);
        }
    }
}
