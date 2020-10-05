package mekanism.common.registries;

import mekanism.common.Mekanism;
import mekanism.common.registration.impl.FeatureDeferredRegister;
import mekanism.common.registration.impl.FeatureRegistryObject;
import mekanism.common.world.OreRetrogenFeature;
import net.minecraft.world.gen.feature.OreFeatureConfig;

public class MekanismFeatures {

    private MekanismFeatures() {
    }

    public static final FeatureDeferredRegister FEATURES = new FeatureDeferredRegister(Mekanism.MODID);

    public static final FeatureRegistryObject<OreFeatureConfig, OreRetrogenFeature> ORE_RETROGEN = FEATURES.register("ore_retrogen", () -> new OreRetrogenFeature(OreFeatureConfig.field_236566_a_));
}