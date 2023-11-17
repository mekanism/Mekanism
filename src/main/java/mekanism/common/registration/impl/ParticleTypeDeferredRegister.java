package mekanism.common.registration.impl;

import java.util.function.Supplier;
import mekanism.common.registration.WrappedDeferredRegister;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.core.registries.Registries;

public class ParticleTypeDeferredRegister extends WrappedDeferredRegister<ParticleType<?>> {

    public ParticleTypeDeferredRegister(String modid) {
        super(modid, Registries.PARTICLE_TYPE);
    }

    public ParticleTypeRegistryObject<SimpleParticleType, SimpleParticleType> registerBasicParticle(String name) {
        return register(name, () -> new SimpleParticleType(false));
    }

    public <PARTICLE extends ParticleOptions, TYPE extends ParticleType<PARTICLE>> ParticleTypeRegistryObject<PARTICLE, TYPE> register(String name, Supplier<TYPE> sup) {
        return register(name, sup, ParticleTypeRegistryObject::new);
    }
}