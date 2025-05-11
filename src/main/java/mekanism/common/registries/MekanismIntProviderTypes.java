package mekanism.common.registries;

import mekanism.common.Mekanism;
import mekanism.common.world.ConfigurableConstantInt;
import mekanism.common.world.ConfigurableUniformInt;
import net.minecraft.core.registries.Registries;
import net.minecraft.util.valueproviders.IntProviderType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class MekanismIntProviderTypes {

    private MekanismIntProviderTypes() {
    }

    public static final DeferredRegister<IntProviderType<?>> INT_PROVIDER_TYPES = DeferredRegister.create(Registries.INT_PROVIDER_TYPE, Mekanism.MODID);

    public static final DeferredHolder<IntProviderType<?>, IntProviderType<ConfigurableConstantInt>> CONFIGURABLE_CONSTANT = INT_PROVIDER_TYPES.register("configurable_constant", () -> () -> ConfigurableConstantInt.CODEC);
    public static final DeferredHolder<IntProviderType<?>, IntProviderType<ConfigurableUniformInt>> CONFIGURABLE_UNIFORM = INT_PROVIDER_TYPES.register("configurable_uniform", () -> () -> ConfigurableUniformInt.CODEC);
}