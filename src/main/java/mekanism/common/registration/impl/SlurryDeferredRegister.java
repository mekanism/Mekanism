package mekanism.common.registration.impl;

import java.util.function.Supplier;
import java.util.function.UnaryOperator;
import mekanism.api.MekanismAPI;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.chemical.slurry.Slurry;
import mekanism.api.chemical.slurry.SlurryBuilder;
import mekanism.common.registration.MekanismDeferredRegister;
import mekanism.common.registration.impl.DeferredChemical.DeferredSlurry;
import mekanism.common.resource.PrimaryResource;

@NothingNullByDefault
public class SlurryDeferredRegister extends MekanismDeferredRegister<Slurry> {

    public SlurryDeferredRegister(String modid) {
        super(MekanismAPI.SLURRY_REGISTRY_NAME, modid, DeferredSlurry::new);
    }

    public SlurryRegistryObject<Slurry, Slurry> register(PrimaryResource resource) {
        return register(resource.getRegistrySuffix(), (UnaryOperator<SlurryBuilder>) builder -> builder.tint(resource.getTint()).ore(resource.getOreTag()));
    }

    public SlurryRegistryObject<Slurry, Slurry> register(String baseName, UnaryOperator<SlurryBuilder> builderModifier) {
        return new SlurryRegistryObject<>(register("dirty_" + baseName, () -> new Slurry(builderModifier.apply(SlurryBuilder.dirty()))),
              register("clean_" + baseName, () -> new Slurry(builderModifier.apply(SlurryBuilder.clean()))));
    }

    @Override
    @SuppressWarnings("unchecked")
    public <SLURRY extends Slurry> DeferredSlurry<SLURRY> register(String name, Supplier<? extends SLURRY> sup) {
        return (DeferredSlurry<SLURRY>) super.register(name, sup);
    }
}