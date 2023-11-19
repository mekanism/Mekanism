package mekanism.common.registries;

import mekanism.common.Mekanism;
import mekanism.common.registration.MekanismDeferredHolder;
import mekanism.common.registration.MekanismDeferredRegister;
import mekanism.common.world.height.ConfigurableHeightProvider;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.levelgen.heightproviders.HeightProviderType;

public class MekanismHeightProviderTypes {

    private MekanismHeightProviderTypes() {
    }

    public static final MekanismDeferredRegister<HeightProviderType<?>> HEIGHT_PROVIDER_TYPES = new MekanismDeferredRegister<>(Registries.HEIGHT_PROVIDER_TYPE, Mekanism.MODID);

    public static final MekanismDeferredHolder<HeightProviderType<?>, HeightProviderType<ConfigurableHeightProvider>> CONFIGURABLE = HEIGHT_PROVIDER_TYPES.register("configurable", () -> () -> ConfigurableHeightProvider.CODEC);
}