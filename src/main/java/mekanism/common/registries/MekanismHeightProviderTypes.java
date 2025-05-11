package mekanism.common.registries;

import mekanism.common.Mekanism;
import mekanism.common.world.height.ConfigurableHeightProvider;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.levelgen.heightproviders.HeightProviderType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class MekanismHeightProviderTypes {

    private MekanismHeightProviderTypes() {
    }

    public static final DeferredRegister<HeightProviderType<?>> HEIGHT_PROVIDER_TYPES = DeferredRegister.create(Registries.HEIGHT_PROVIDER_TYPE, Mekanism.MODID);

    public static final DeferredHolder<HeightProviderType<?>, HeightProviderType<ConfigurableHeightProvider>> CONFIGURABLE = HEIGHT_PROVIDER_TYPES.register("configurable", () -> () -> ConfigurableHeightProvider.CODEC);
}