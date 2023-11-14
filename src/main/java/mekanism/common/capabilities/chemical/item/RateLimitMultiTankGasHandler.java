package mekanism.common.capabilities.chemical.item;

import java.util.Collection;
import java.util.function.Function;
import mekanism.api.IContentsListener;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.chemical.gas.Gas;
import mekanism.api.chemical.gas.IGasTank;
import mekanism.common.capabilities.chemical.variable.RateLimitChemicalTank.RateLimitGasTank;
import net.minecraft.world.item.ItemStack;

@NothingNullByDefault
public class RateLimitMultiTankGasHandler extends ItemStackMekanismGasHandler {

    public static RateLimitMultiTankGasHandler create(ItemStack stack, Collection<ChemicalTankSpec<Gas>> gasTanks) {
        return new RateLimitMultiTankGasHandler(stack, gasTanks);
    }

    @SuppressWarnings("unchecked")
    private RateLimitMultiTankGasHandler(ItemStack stack, Collection<ChemicalTankSpec<Gas>> gasTanks) {
        super(stack, gasTanks.stream()
              .map(spec -> (Function<IContentsListener, IGasTank>) listener -> new RateLimitGasTank(spec.rate, spec.capacity, spec.canExtract,
                    (gas, automationType) -> spec.canInsert.test(gas, automationType, stack), spec.isValid, null, listener))
              .toArray(Function[]::new));
    }
}