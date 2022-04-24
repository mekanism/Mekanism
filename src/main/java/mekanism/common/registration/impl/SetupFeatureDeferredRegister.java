package mekanism.common.registration.impl;

import it.unimi.dsi.fastutil.booleans.Boolean2ObjectFunction;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;
import mekanism.common.registration.DoubleDeferredRegister;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.minecraft.world.level.levelgen.placement.PlacementModifier;

public class SetupFeatureDeferredRegister extends DoubleDeferredRegister<ConfiguredFeature<?, ?>, PlacedFeature> {

    public SetupFeatureDeferredRegister(String modid) {
        super(modid, Registry.CONFIGURED_FEATURE_REGISTRY, Registry.PLACED_FEATURE_REGISTRY);
    }

    public <FEATURE_CONFIG extends FeatureConfiguration, FEATURE extends Feature<FEATURE_CONFIG>> SetupFeatureRegistryObject<FEATURE_CONFIG, FEATURE> registerSingle(
          String name, Supplier<? extends ConfiguredFeature<FEATURE_CONFIG, FEATURE>> configuredFeatureConfig,
          Function<Holder<ConfiguredFeature<FEATURE_CONFIG, FEATURE>>, PlacedFeature> placedFeatureCreator) {
        return registerAdvanced(name, configuredFeatureConfig, configuredRO -> placedFeatureCreator.apply(configuredRO.getHolder().orElseThrow()),
              SetupFeatureRegistryObject::new);
    }

    public <FEATURE_CONFIG extends FeatureConfiguration, FEATURE extends Feature<FEATURE_CONFIG>> MekFeature<FEATURE_CONFIG, FEATURE> register(String name,
          Supplier<? extends ConfiguredFeature<FEATURE_CONFIG, FEATURE>> configuredFeatureConfig, Boolean2ObjectFunction<List<PlacementModifier>> featureModifier) {
        return register(name, configuredFeatureConfig, configuredFeatureConfig, featureModifier);
    }

    public <FEATURE_CONFIG extends FeatureConfiguration, FEATURE extends Feature<FEATURE_CONFIG>> MekFeature<FEATURE_CONFIG, FEATURE> register(String name,
          Supplier<? extends ConfiguredFeature<FEATURE_CONFIG, FEATURE>> configuredFeatureConfig,
          Supplier<? extends ConfiguredFeature<FEATURE_CONFIG, FEATURE>> configuredRetrogenFeatureConfig,
          Boolean2ObjectFunction<List<PlacementModifier>> featureModifier) {
        return new MekFeature<>(
              registerSingle(name, configuredFeatureConfig, feature -> new PlacedFeature(Holder.hackyErase(feature), featureModifier.apply(false))),
              registerSingle(name + "_retrogen", configuredRetrogenFeatureConfig, feature -> new PlacedFeature(Holder.hackyErase(feature), featureModifier.apply(true)))
        );
    }

    public record MekFeature<FEATURE_CONFIG extends FeatureConfiguration, FEATURE extends Feature<FEATURE_CONFIG>>(
          SetupFeatureRegistryObject<FEATURE_CONFIG, FEATURE> feature,
          SetupFeatureRegistryObject<FEATURE_CONFIG, FEATURE> retrogen) {

        public Holder<PlacedFeature> placedFeature() {
            return feature.getPlacedFeatureHolder();
        }
    }
}