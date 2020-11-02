package mekanism.common.registries;

import mekanism.common.Mekanism;
import mekanism.common.registration.impl.FeatureDeferredRegister;
import mekanism.common.registration.impl.FeatureRegistryObject;
import mekanism.common.world.OreRetrogenFeature;
import mekanism.common.world.ResizableOreFeature;
import mekanism.common.world.ResizableOreFeatureConfig;
import mekanism.common.world.ResizableSphereReplaceConfig;
import mekanism.common.world.ResizableSphereReplaceFeature;

public class MekanismFeatures {

    private MekanismFeatures() {
    }

    public static final FeatureDeferredRegister FEATURES = new FeatureDeferredRegister(Mekanism.MODID);

    public static final FeatureRegistryObject<ResizableSphereReplaceConfig, ResizableSphereReplaceFeature> DISK = FEATURES.register("disk", () -> new ResizableSphereReplaceFeature(ResizableSphereReplaceConfig.CODEC));
    public static final FeatureRegistryObject<ResizableOreFeatureConfig, ResizableOreFeature> ORE = FEATURES.register("ore", ResizableOreFeature::new);
    public static final FeatureRegistryObject<ResizableOreFeatureConfig, OreRetrogenFeature> ORE_RETROGEN = FEATURES.register("ore_retrogen", OreRetrogenFeature::new);
}