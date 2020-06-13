package mekanism.common.registration.impl;

import java.util.function.Supplier;
import mekanism.api.chemical.attribute.ChemicalAttribute;
import mekanism.api.chemical.gas.Gas;
import mekanism.api.chemical.gas.GasBuilder;
import mekanism.common.base.IChemicalConstant;
import mekanism.common.registration.WrappedDeferredRegister;

public class GasDeferredRegister extends WrappedDeferredRegister<Gas> {

    public GasDeferredRegister(String modid) {
        super(modid, Gas.class);
    }

    public GasRegistryObject<Gas> register(IChemicalConstant constants, ChemicalAttribute... attributes) {
        return register(constants.getName(), constants.getColor(), attributes);
    }

    public GasRegistryObject<Gas> register(String name, int color, ChemicalAttribute... attributes) {
        return register(name, () -> {
            GasBuilder builder = GasBuilder.builder().color(color);
            for (ChemicalAttribute attribute : attributes) {
                builder.with(attribute);
            }
            return new Gas(builder);
        });
    }

    public <GAS extends Gas> GasRegistryObject<GAS> register(String name, Supplier<? extends GAS> sup) {
        return register(name, sup, GasRegistryObject::new);
    }
}
