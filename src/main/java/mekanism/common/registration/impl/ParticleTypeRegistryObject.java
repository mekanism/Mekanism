package mekanism.common.registration.impl;

import mekanism.common.registration.WrappedRegistryObject;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.neoforged.neoforge.registries.DeferredHolder;

public class ParticleTypeRegistryObject<PARTICLE extends ParticleOptions, TYPE extends ParticleType<PARTICLE>> extends WrappedRegistryObject<ParticleType<?>, TYPE> {

    public ParticleTypeRegistryObject(DeferredHolder<ParticleType<?>, TYPE> registryObject) {
        super(registryObject);
    }
}