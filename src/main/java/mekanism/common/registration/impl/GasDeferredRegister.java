package mekanism.common.registration.impl;

import java.util.function.Supplier;
import mekanism.api.MekanismAPI;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.chemical.attribute.ChemicalAttribute;
import mekanism.api.chemical.gas.Gas;
import mekanism.api.chemical.gas.GasBuilder;
import mekanism.common.base.IChemicalConstant;
import mekanism.common.registration.MekanismDeferredRegister;
import mekanism.common.registration.impl.DeferredChemical.DeferredGas;

@NothingNullByDefault//TODO - 1.20.2: Do we want to expose a basic form of this to the API
public class GasDeferredRegister extends MekanismDeferredRegister<Gas> {

    public GasDeferredRegister(String modid) {
        super(MekanismAPI.GAS_REGISTRY_NAME, modid, DeferredGas::new);
    }

    public DeferredGas<Gas> register(IChemicalConstant constants, ChemicalAttribute... attributes) {
        return register(constants.getName(), constants.getColor(), attributes);
    }

    public DeferredGas<Gas> register(String name, int color, ChemicalAttribute... attributes) {
        return register(name, () -> {
            GasBuilder builder = GasBuilder.builder().tint(color);
            for (ChemicalAttribute attribute : attributes) {
                builder.with(attribute);
            }
            return new Gas(builder);
        });
    }

    @Override
    @SuppressWarnings("unchecked")
    public <GAS extends Gas> DeferredGas<GAS> register(String name, Supplier<? extends GAS> sup) {
        return (DeferredGas<GAS>) super.register(name, sup);
    }
}
