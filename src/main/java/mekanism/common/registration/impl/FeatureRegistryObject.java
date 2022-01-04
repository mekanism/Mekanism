package mekanism.common.registration.impl;

import mekanism.common.registration.WrappedRegistryObject;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;
import net.minecraftforge.registries.RegistryObject;

public class FeatureRegistryObject<CONFIG extends FeatureConfiguration, FEATURE extends Feature<CONFIG>> extends WrappedRegistryObject<FEATURE> {

    public FeatureRegistryObject(RegistryObject<FEATURE> registryObject) {
        super(registryObject);
    }
}