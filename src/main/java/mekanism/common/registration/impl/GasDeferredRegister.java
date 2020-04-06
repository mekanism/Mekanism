package mekanism.common.registration.impl;

import java.util.function.Supplier;
import mekanism.api.MekanismAPI;
import mekanism.api.chemical.gas.Gas;
import mekanism.api.chemical.gas.GasBuilder;
import mekanism.api.chemical.gas.Slurry;
import mekanism.common.ChemicalConstants;
import mekanism.common.Resource;
import mekanism.common.registration.WrappedDeferredRegister;

public class GasDeferredRegister extends WrappedDeferredRegister<Gas> {

    public GasDeferredRegister(String modid) {
        super(modid, MekanismAPI.GAS_REGISTRY);
    }

    public GasRegistryObject<Gas> register(ChemicalConstants constants) {
        //TODO: Double check if it matters that we are giving an ARGB color instead of an RGB color
        return register(constants.getName(), constants.getColor());
    }

    public GasRegistryObject<Gas> register(String name, int color) {
        return register(name, () -> new Gas(GasBuilder.builder().color(color)));
    }

    public <GAS extends Gas> GasRegistryObject<GAS> register(String name, Supplier<? extends GAS> sup) {
        return register(name, sup, GasRegistryObject::new);
    }

    public SlurryRegistryObject<Slurry, Slurry> registerSlurry(Resource resource) {
        String baseSlurryName = resource.getRegistrySuffix() + "_slurry";
        return new SlurryRegistryObject<>(internal.register("dirty_" + baseSlurryName, () -> new Slurry(false, resource.getTint(), resource.getOreTag())),
              internal.register("clean_" + baseSlurryName, () -> new Slurry(true, resource.getTint(), resource.getOreTag())));
    }
}