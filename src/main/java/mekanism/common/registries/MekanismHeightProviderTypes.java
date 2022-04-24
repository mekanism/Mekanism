package mekanism.common.registries;

import mekanism.common.Mekanism;
import mekanism.common.registration.impl.HeightProviderTypeDeferredRegister;
import mekanism.common.registration.impl.HeightProviderTypeRegistryObject;
import mekanism.common.world.height.ConfigurableHeightProvider;

public class MekanismHeightProviderTypes {

    private MekanismHeightProviderTypes() {
    }

    public static final HeightProviderTypeDeferredRegister HEIGHT_PROVIDER_TYPES = new HeightProviderTypeDeferredRegister(Mekanism.MODID);

    public static final HeightProviderTypeRegistryObject<ConfigurableHeightProvider> CONFIGURABLE = HEIGHT_PROVIDER_TYPES.register("configurable", ConfigurableHeightProvider.CODEC);
}