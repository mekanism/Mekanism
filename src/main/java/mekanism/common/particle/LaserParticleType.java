package mekanism.common.particle;

import com.mojang.serialization.Codec;
import javax.annotation.Nonnull;
import net.minecraft.particles.ParticleType;

public class LaserParticleType extends ParticleType<LaserParticleData> {

    public LaserParticleType() {
        super(false, LaserParticleData.DESERIALIZER);
    }

    @Nonnull
    @Override
    public Codec<LaserParticleData> func_230522_e_() {
        return LaserParticleData.CODEC;
    }
}