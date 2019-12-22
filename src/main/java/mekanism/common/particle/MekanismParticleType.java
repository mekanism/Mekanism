package mekanism.common.particle;

import mekanism.common.Mekanism;
import mekanism.common.registration.impl.ParticleTypeDeferredRegister;
import mekanism.common.registration.impl.ParticleTypeRegistryObject;
import net.minecraft.particles.BasicParticleType;

public class MekanismParticleType {

    public static final ParticleTypeDeferredRegister PARTICLE_TYPES = new ParticleTypeDeferredRegister(Mekanism.MODID);

    public static final ParticleTypeRegistryObject<LaserParticleData> LASER = PARTICLE_TYPES.register("laser", LaserParticleType::new);
    public static final ParticleTypeRegistryObject<BasicParticleType> JETPACK_FLAME = PARTICLE_TYPES.registerBasicParticle("jetpack_flame");
    public static final ParticleTypeRegistryObject<BasicParticleType> JETPACK_SMOKE = PARTICLE_TYPES.registerBasicParticle("jetpack_smoke");
    public static final ParticleTypeRegistryObject<BasicParticleType> SCUBA_BUBBLE = PARTICLE_TYPES.registerBasicParticle("scuba_bubble");
}