package mekanism.common.registration.impl;

import mekanism.common.registration.MekanismDeferredHolder;
import mekanism.common.registration.MekanismDeferredRegister;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.core.registries.Registries;

public class ParticleTypeDeferredRegister extends MekanismDeferredRegister<ParticleType<?>> {

    public ParticleTypeDeferredRegister(String modid) {
        super(Registries.PARTICLE_TYPE, modid);
    }

    public MekanismDeferredHolder<ParticleType<?>, SimpleParticleType> registerBasic(String name) {
        return register(name, () -> new SimpleParticleType(false));
    }
}