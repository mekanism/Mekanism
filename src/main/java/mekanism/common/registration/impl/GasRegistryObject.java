package mekanism.common.registration.impl;

import javax.annotation.Nonnull;
import mekanism.api.chemical.gas.Gas;
import mekanism.api.providers.IGasProvider;
import mekanism.common.registration.WrappedRegistryObject;
import net.minecraftforge.fml.RegistryObject;

public class GasRegistryObject<GAS extends Gas> extends WrappedRegistryObject<GAS> implements IGasProvider {

    public GasRegistryObject(RegistryObject<GAS> registryObject) {
        super(registryObject);
    }

    @Nonnull
    @Override
    public GAS getChemical() {
        return get();
    }
}