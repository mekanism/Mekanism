package mekanism.common.registration.impl;

import mekanism.api.chemical.gas.Gas;
import mekanism.api.providers.IGasProvider;
import mekanism.common.registration.WrappedRegistryObject;
import net.neoforged.neoforge.registries.DeferredHolder;
import org.jetbrains.annotations.NotNull;

public class GasRegistryObject<GAS extends Gas> extends WrappedRegistryObject<Gas, GAS> implements IGasProvider {

    public GasRegistryObject(DeferredHolder<Gas, GAS> registryObject) {
        super(registryObject);
    }

    @NotNull
    @Override
    public GAS getChemical() {
        return get();
    }
}