package mekanism.common.registries;

import mekanism.common.Mekanism;
import mekanism.common.particle.LaserParticleType;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.core.registries.Registries;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class MekanismParticleTypes {

    private MekanismParticleTypes() {
    }

    public static final DeferredRegister<ParticleType<?>> PARTICLE_TYPES = DeferredRegister.create(Registries.PARTICLE_TYPE, Mekanism.MODID);

    public static final DeferredHolder<ParticleType<?>, LaserParticleType> LASER = PARTICLE_TYPES.register("laser", LaserParticleType::new);
    public static final DeferredHolder<ParticleType<?>, SimpleParticleType> JETPACK_FLAME = PARTICLE_TYPES.register("jetpack_flame", () -> new SimpleParticleType(false));
    public static final DeferredHolder<ParticleType<?>, SimpleParticleType> JETPACK_SMOKE = PARTICLE_TYPES.register("jetpack_smoke", () -> new SimpleParticleType(false));
    public static final DeferredHolder<ParticleType<?>, SimpleParticleType> SCUBA_BUBBLE = PARTICLE_TYPES.register("scuba_bubble", () -> new SimpleParticleType(false));
    public static final DeferredHolder<ParticleType<?>, SimpleParticleType> RADIATION = PARTICLE_TYPES.register("radiation", () -> new SimpleParticleType(false));
}