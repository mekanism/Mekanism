package mekanism.common.registration.impl;

import java.util.function.UnaryOperator;
import mekanism.api.chemical.slurry.Slurry;
import mekanism.api.chemical.slurry.SlurryBuilder;
import mekanism.common.registration.WrappedDeferredRegister;
import mekanism.common.resource.PrimaryResource;

public class SlurryDeferredRegister extends WrappedDeferredRegister<Slurry> {

    public SlurryDeferredRegister(String modid) {
        super(modid, Slurry.class);
    }

    public SlurryRegistryObject<Slurry, Slurry> register(PrimaryResource resource) {
        return register(resource.getName(), builder -> builder.color(resource.getTint()).ore(resource.getOreTag()));
    }

    public SlurryRegistryObject<Slurry, Slurry> register(String baseName, UnaryOperator<SlurryBuilder> builderModifier) {
        return new SlurryRegistryObject<>(internal.register("dirty_" + baseName, () -> new Slurry(builderModifier.apply(SlurryBuilder.dirty()))),
              internal.register("clean_" + baseName, () -> new Slurry(builderModifier.apply(SlurryBuilder.clean()))));
    }
}
