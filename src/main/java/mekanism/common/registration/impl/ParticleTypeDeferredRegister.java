package mekanism.common.registration.impl;

import java.util.function.Supplier;
import mekanism.common.registration.WrappedDeferredRegister;
import net.minecraft.particles.BasicParticleType;
import net.minecraft.particles.IParticleData;
import net.minecraft.particles.ParticleType;
import net.minecraftforge.registries.ForgeRegistries;

public class ParticleTypeDeferredRegister extends WrappedDeferredRegister<ParticleType<?>> {

    public ParticleTypeDeferredRegister(String modid) {
        super(modid, ForgeRegistries.PARTICLE_TYPES);
    }

    public ParticleTypeRegistryObject<BasicParticleType, BasicParticleType> registerBasicParticle(String name) {
        return register(name, () -> new BasicParticleType(false));
    }

    public <PARTICLE extends IParticleData, TYPE extends ParticleType<PARTICLE>> ParticleTypeRegistryObject<PARTICLE, TYPE> register(String name, Supplier<TYPE> sup) {
        return register(name, sup, ParticleTypeRegistryObject::new);
    }
}