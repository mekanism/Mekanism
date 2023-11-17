package mekanism.common.registration.impl;

import java.util.function.Supplier;
import mekanism.common.registration.WrappedDeferredRegister;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;

public class FeatureDeferredRegister extends WrappedDeferredRegister<Feature<?>> {

    public FeatureDeferredRegister(String modid) {
        super(modid, Registries.FEATURE);
    }

    public <CONFIG extends FeatureConfiguration, FEATURE extends Feature<CONFIG>> FeatureRegistryObject<CONFIG, FEATURE> register(String name, Supplier<FEATURE> sup) {
        return register(name, sup, FeatureRegistryObject::new);
    }
}