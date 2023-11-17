package mekanism.common.registration.impl;

import mekanism.common.registration.WrappedRegistryObject;
import net.minecraft.util.valueproviders.IntProvider;
import net.minecraft.util.valueproviders.IntProviderType;
import net.neoforged.neoforge.registries.DeferredHolder;

public class IntProviderTypeRegistryObject<PROVIDER extends IntProvider> extends WrappedRegistryObject<IntProviderType<?>, IntProviderType<PROVIDER>> {

    public IntProviderTypeRegistryObject(DeferredHolder<IntProviderType<?>, IntProviderType<PROVIDER>> registryObject) {
        super(registryObject);
    }
}