package mekanism.common.registration.impl;

import com.mojang.serialization.Codec;
import mekanism.common.registration.MekanismDeferredHolder;
import mekanism.common.registration.MekanismDeferredRegister;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.levelgen.heightproviders.HeightProvider;
import net.minecraft.world.level.levelgen.heightproviders.HeightProviderType;

public class HeightProviderTypeDeferredRegister extends MekanismDeferredRegister<HeightProviderType<?>> {

    public HeightProviderTypeDeferredRegister(String modid) {
        super(Registries.HEIGHT_PROVIDER_TYPE, modid);
    }

    public <PROVIDER extends HeightProvider> MekanismDeferredHolder<HeightProviderType<?>, HeightProviderType<PROVIDER>> register(String name, Codec<PROVIDER> codec) {
        return register(name, () -> () -> codec);
    }
}