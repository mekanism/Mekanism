package mekanism.common.registries;

import mekanism.common.Mekanism;
import mekanism.common.registration.MekanismDeferredHolder;
import mekanism.common.registration.impl.HeightProviderTypeDeferredRegister;
import mekanism.common.world.height.ConfigurableHeightProvider;
import net.minecraft.world.level.levelgen.heightproviders.HeightProviderType;

public class MekanismHeightProviderTypes {

    private MekanismHeightProviderTypes() {
    }

    public static final HeightProviderTypeDeferredRegister HEIGHT_PROVIDER_TYPES = new HeightProviderTypeDeferredRegister(Mekanism.MODID);

    public static final MekanismDeferredHolder<HeightProviderType<?>, HeightProviderType<ConfigurableHeightProvider>> CONFIGURABLE = HEIGHT_PROVIDER_TYPES.register("configurable", ConfigurableHeightProvider.CODEC);
}