package mekanism.common.registration.impl;

import java.util.function.Supplier;
import mekanism.common.registration.WrappedDeferredRegister;
import net.minecraft.world.gen.feature.Feature;
import net.minecraft.world.gen.feature.IFeatureConfig;
import net.minecraftforge.registries.ForgeRegistries;

public class FeatureDeferredRegister extends WrappedDeferredRegister<Feature<?>> {

    public FeatureDeferredRegister(String modid) {
        super(modid, ForgeRegistries.FEATURES);
    }

    public <CONFIG extends IFeatureConfig, FEATURE extends Feature<CONFIG>> FeatureRegistryObject<CONFIG, FEATURE> register(String name, Supplier<FEATURE> sup) {
        return register(name, sup, FeatureRegistryObject::new);
    }
}