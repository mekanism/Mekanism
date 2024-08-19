package mekanism.common.integration;

import java.util.function.Supplier;
import mekanism.api.MekanismAPI;
import mekanism.api.chemical.Chemical;
import mekanism.api.providers.IChemicalProvider;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

public class LazyChemicalProvider implements IChemicalProvider {

    private Supplier<Chemical> chemicalSupplier;
    private Chemical chemical = MekanismAPI.EMPTY_CHEMICAL;

    /**
     * Helper class to cache the result of the {@link Chemical} supplier after doing a registry lookup once it has properly been added to the registry.
     */
    public LazyChemicalProvider(ResourceLocation chemicalRegistryName) {
        this(() -> MekanismAPI.CHEMICAL_REGISTRY.get(chemicalRegistryName));
    }

    /**
     * Helper class to cache the result of the {@link Chemical} supplier, so that we don't have to do registry lookups once it has properly been added to the registry.
     */
    public LazyChemicalProvider(Supplier<Chemical> chemicalSupplier) {
        this.chemicalSupplier = chemicalSupplier;
    }

    @NotNull
    @Override
    public Chemical getChemical() {
        if (chemical.isEmptyType()) {
            //If our gas hasn't actually been set yet, set it from the gas supplier we have
            chemical = chemicalSupplier.get().getChemical();
            if (chemical.isEmptyType()) {
                //If it is still empty (because the supplier was for an empty gas which we couldn't
                // evaluate initially, throw an illegal state exception)
                throw new IllegalStateException("Empty chemical used for coolant attribute via a CraftTweaker Script.");
            }
            //Free memory of the supplier
            chemicalSupplier = null;
        }
        return chemical;
    }
}