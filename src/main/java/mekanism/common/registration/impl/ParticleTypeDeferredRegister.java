package mekanism.common.registration.impl;

import java.util.function.Supplier;
import mekanism.common.registration.WrappedForgeDeferredRegister;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraftforge.registries.ForgeRegistries;

public class ParticleTypeDeferredRegister extends WrappedForgeDeferredRegister<ParticleType<?>> {

    public ParticleTypeDeferredRegister(String modid) {
        super(modid, ForgeRegistries.PARTICLE_TYPES);
    }

    public ParticleTypeRegistryObject<SimpleParticleType, SimpleParticleType> registerBasicParticle(String name) {
        return register(name, () -> new SimpleParticleType(false));
    }

    public <PARTICLE extends ParticleOptions, TYPE extends ParticleType<PARTICLE>> ParticleTypeRegistryObject<PARTICLE, TYPE> register(String name, Supplier<TYPE> sup) {
        return register(name, sup, ParticleTypeRegistryObject::new);
    }
}