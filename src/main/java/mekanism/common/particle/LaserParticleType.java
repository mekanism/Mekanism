package mekanism.common.particle;

import net.minecraft.particles.ParticleType;

public class LaserParticleType extends ParticleType<LaserParticleData> {

    public LaserParticleType() {
        super(false, LaserParticleData.DESERIALIZER);
    }
}