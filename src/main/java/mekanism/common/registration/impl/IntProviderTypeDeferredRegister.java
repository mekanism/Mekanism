package mekanism.common.registration.impl;

import com.mojang.serialization.Codec;
import java.util.function.Supplier;
import mekanism.common.registration.WrappedDeferredRegister;
import net.minecraft.core.Registry;
import net.minecraft.util.valueproviders.IntProvider;
import net.minecraft.util.valueproviders.IntProviderType;

public class IntProviderTypeDeferredRegister extends WrappedDeferredRegister<IntProviderType<?>> {

    public IntProviderTypeDeferredRegister(String modid) {
        super(modid, Registry.INT_PROVIDER_TYPE_REGISTRY);
    }

    public <PROVIDER extends IntProvider> IntProviderTypeRegistryObject<PROVIDER> register(String name, Codec<PROVIDER> codec) {
        return register(name, () -> () -> codec);
    }

    public <PROVIDER extends IntProvider> IntProviderTypeRegistryObject<PROVIDER> register(String name, Supplier<? extends IntProviderType<PROVIDER>> sup) {
        return register(name, sup, IntProviderTypeRegistryObject::new);
    }
}