package mekanism.common.capabilities.chemical.item;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.chemical.gas.Gas;
import mekanism.api.chemical.gas.IGasTank;
import mekanism.common.capabilities.chemical.variable.RateLimitChemicalTank.RateLimitGasTank;
import org.jetbrains.annotations.NotNull;

@NothingNullByDefault
public class RateLimitMultiTankGasHandler extends ItemStackMekanismGasHandler {

    public static RateLimitMultiTankGasHandler create(@NotNull Collection<ChemicalTankSpec<Gas>> gasTanks) {
        return new RateLimitMultiTankGasHandler(gasTanks);
    }

    private final List<IGasTank> tanks;

    private RateLimitMultiTankGasHandler(@NotNull Collection<ChemicalTankSpec<Gas>> gasTanks) {
        List<IGasTank> tankProviders = new ArrayList<>();
        for (ChemicalTankSpec<Gas> spec : gasTanks) {
            tankProviders.add(new RateLimitGasTank(spec.rate, spec.capacity, spec.canExtract,
                  (gas, automationType) -> spec.canInsert.test(gas, automationType, getStack()), spec.isValid, null, this));
        }
        tanks = Collections.unmodifiableList(tankProviders);
    }

    @Override
    protected List<IGasTank> getInitialTanks() {
        return tanks;
    }
}