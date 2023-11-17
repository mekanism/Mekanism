package mekanism.common.registration.impl;

import mekanism.common.registration.WrappedRegistryObject;
import net.minecraft.world.level.levelgen.heightproviders.HeightProvider;
import net.minecraft.world.level.levelgen.heightproviders.HeightProviderType;
import net.neoforged.neoforge.registries.DeferredHolder;

public class HeightProviderTypeRegistryObject<PROVIDER extends HeightProvider> extends WrappedRegistryObject<HeightProviderType<?>, HeightProviderType<PROVIDER>> {

    public HeightProviderTypeRegistryObject(DeferredHolder<HeightProviderType<?>, HeightProviderType<PROVIDER>> registryObject) {
        super(registryObject);
    }
}