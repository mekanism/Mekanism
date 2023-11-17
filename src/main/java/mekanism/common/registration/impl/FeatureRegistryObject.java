package mekanism.common.registration.impl;

import mekanism.common.registration.WrappedRegistryObject;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;
import net.neoforged.neoforge.registries.DeferredHolder;

public class FeatureRegistryObject<CONFIG extends FeatureConfiguration, FEATURE extends Feature<CONFIG>> extends WrappedRegistryObject<Feature<?>, FEATURE> {

    public FeatureRegistryObject(DeferredHolder<Feature<?>, FEATURE> registryObject) {
        super(registryObject);
    }
}