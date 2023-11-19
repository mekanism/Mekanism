package mekanism.common.registries;

import mekanism.common.Mekanism;
import mekanism.common.particle.LaserParticleType;
import mekanism.common.registration.MekanismDeferredHolder;
import mekanism.common.registration.impl.ParticleTypeDeferredRegister;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.SimpleParticleType;

public class MekanismParticleTypes {

    private MekanismParticleTypes() {
    }

    public static final ParticleTypeDeferredRegister PARTICLE_TYPES = new ParticleTypeDeferredRegister(Mekanism.MODID);

    public static final MekanismDeferredHolder<ParticleType<?>, LaserParticleType> LASER = PARTICLE_TYPES.register("laser", LaserParticleType::new);
    public static final MekanismDeferredHolder<ParticleType<?>, SimpleParticleType> JETPACK_FLAME = PARTICLE_TYPES.registerBasic("jetpack_flame");
    public static final MekanismDeferredHolder<ParticleType<?>, SimpleParticleType> JETPACK_SMOKE = PARTICLE_TYPES.registerBasic("jetpack_smoke");
    public static final MekanismDeferredHolder<ParticleType<?>, SimpleParticleType> SCUBA_BUBBLE = PARTICLE_TYPES.registerBasic("scuba_bubble");
    public static final MekanismDeferredHolder<ParticleType<?>, SimpleParticleType> RADIATION = PARTICLE_TYPES.registerBasic("radiation");
}