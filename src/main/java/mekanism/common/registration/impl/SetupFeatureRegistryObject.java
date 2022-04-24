package mekanism.common.registration.impl;

import javax.annotation.Nonnull;
import mekanism.common.registration.DoubleWrappedRegistryObject;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.minecraftforge.registries.RegistryObject;

public class SetupFeatureRegistryObject<FEATURE_CONFIG extends FeatureConfiguration, FEATURE extends Feature<FEATURE_CONFIG>> extends
      DoubleWrappedRegistryObject<ConfiguredFeature<FEATURE_CONFIG, FEATURE>, PlacedFeature> {

    public SetupFeatureRegistryObject(RegistryObject<ConfiguredFeature<FEATURE_CONFIG, FEATURE>> blockRegistryObject, RegistryObject<PlacedFeature> itemRegistryObject) {
        super(blockRegistryObject, itemRegistryObject);
    }

    @Nonnull
    public ConfiguredFeature<FEATURE_CONFIG, FEATURE> getConfiguredFeature() {
        return getPrimary();
    }

    @Nonnull
    public PlacedFeature getPlacedFeature() {
        return getSecondary();
    }

    @Nonnull
    public Holder<PlacedFeature> getPlacedFeatureHolder() {
        return secondaryRO.getHolder().orElseThrow();
    }

    public ResourceKey<PlacedFeature> getPlacedFeatureKey() {
        return secondaryRO.getKey();
    }
}