package mekanism.common.particle;

import mekanism.common.Mekanism;
import mekanism.common.registration.impl.ParticleTypeDeferredRegister;
import mekanism.common.registration.impl.ParticleTypeRegistryObject;

public class MekanismParticleType {

    public static final ParticleTypeDeferredRegister PARTICLE_TYPES = new ParticleTypeDeferredRegister(Mekanism.MODID);

    public static final ParticleTypeRegistryObject<LaserParticleData> LASER = PARTICLE_TYPES.register("laser", LaserParticleType::new);
}