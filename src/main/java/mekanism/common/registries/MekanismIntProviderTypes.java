package mekanism.common.registries;

import com.mojang.serialization.MapCodec;
import mekanism.common.Mekanism;
import mekanism.common.world.ConfigurableConstantInt;
import mekanism.common.world.ConfigurableUniformInt;
import net.minecraft.core.registries.Registries;
import net.minecraft.util.valueproviders.IntProvider;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class MekanismIntProviderTypes {

    private MekanismIntProviderTypes() {
    }

    public static final DeferredRegister<MapCodec<? extends IntProvider>> INT_PROVIDER_TYPES = DeferredRegister.create(Registries.INT_PROVIDER_TYPE, Mekanism.MODID);

    public static final DeferredHolder<MapCodec<? extends IntProvider>, MapCodec<ConfigurableConstantInt>> CONFIGURABLE_CONSTANT = INT_PROVIDER_TYPES.register("configurable_constant", () -> ConfigurableConstantInt.CODEC);
    public static final DeferredHolder<MapCodec<? extends IntProvider>, MapCodec<ConfigurableUniformInt>> CONFIGURABLE_UNIFORM = INT_PROVIDER_TYPES.register("configurable_uniform", () -> ConfigurableUniformInt.CODEC);
}