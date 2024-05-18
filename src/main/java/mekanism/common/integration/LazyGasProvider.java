package mekanism.common.integration;

import java.util.function.Supplier;
import mekanism.api.MekanismAPI;
import mekanism.api.chemical.gas.Gas;
import mekanism.api.providers.IGasProvider;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

public class LazyGasProvider implements IGasProvider {

    private Supplier<Gas> gasSupplier;
    private Gas gas = MekanismAPI.EMPTY_GAS;

    /**
     * Helper class to cache the result of the {@link Gas} supplier after doing a registry lookup once it has properly been added to the registry.
     */
    public LazyGasProvider(ResourceLocation gasRegistryName) {
        this(() -> MekanismAPI.GAS_REGISTRY.get(gasRegistryName));
    }

    /**
     * Helper class to cache the result of the {@link Gas} supplier, so that we don't have to do registry lookups once it has properly been added to the registry.
     */
    public LazyGasProvider(Supplier<Gas> gasSupplier) {
        this.gasSupplier = gasSupplier;
    }

    @NotNull
    @Override
    public Gas getChemical() {
        if (gas.isEmptyType()) {
            //If our gas hasn't actually been set yet, set it from the gas supplier we have
            gas = gasSupplier.get().getChemical();
            if (gas.isEmptyType()) {
                //If it is still empty (because the supplier was for an empty gas which we couldn't
                // evaluate initially, throw an illegal state exception)
                throw new IllegalStateException("Empty gas used for coolant attribute via a CraftTweaker Script.");
            }
            //Free memory of the supplier
            gasSupplier = null;
        }
        return gas;
    }
}