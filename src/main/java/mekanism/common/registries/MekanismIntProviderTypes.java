package mekanism.common.registries;

import mekanism.common.Mekanism;
import mekanism.common.registration.MekanismDeferredHolder;
import mekanism.common.registration.MekanismDeferredRegister;
import mekanism.common.world.ConfigurableConstantInt;
import mekanism.common.world.ConfigurableUniformInt;
import net.minecraft.core.registries.Registries;
import net.minecraft.util.valueproviders.IntProviderType;

public class MekanismIntProviderTypes {

    private MekanismIntProviderTypes() {
    }

    public static final MekanismDeferredRegister<IntProviderType<?>> INT_PROVIDER_TYPES = new MekanismDeferredRegister<>(Registries.INT_PROVIDER_TYPE, Mekanism.MODID);

    public static final MekanismDeferredHolder<IntProviderType<?>, IntProviderType<ConfigurableConstantInt>> CONFIGURABLE_CONSTANT = INT_PROVIDER_TYPES.register("configurable_constant", () -> () -> ConfigurableConstantInt.CODEC);
    public static final MekanismDeferredHolder<IntProviderType<?>, IntProviderType<ConfigurableUniformInt>> CONFIGURABLE_UNIFORM = INT_PROVIDER_TYPES.register("configurable_uniform", () -> () -> ConfigurableUniformInt.CODEC);
}