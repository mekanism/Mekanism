package mekanism.common.registration.impl;

import com.mojang.serialization.Codec;
import mekanism.common.registration.MekanismDeferredHolder;
import mekanism.common.registration.MekanismDeferredRegister;
import net.minecraft.core.registries.Registries;
import net.minecraft.util.valueproviders.IntProvider;
import net.minecraft.util.valueproviders.IntProviderType;

public class IntProviderTypeDeferredRegister extends MekanismDeferredRegister<IntProviderType<?>> {

    public IntProviderTypeDeferredRegister(String modid) {
        super(Registries.INT_PROVIDER_TYPE, modid);
    }

    public <PROVIDER extends IntProvider> MekanismDeferredHolder<IntProviderType<?>, IntProviderType<PROVIDER>> register(String name, Codec<PROVIDER> codec) {
        return register(name, () -> () -> codec);
    }
}