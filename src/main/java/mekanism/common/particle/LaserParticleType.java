package mekanism.common.particle;

import com.mojang.serialization.Codec;
import javax.annotation.Nonnull;
import net.minecraft.particles.ParticleType;

public class LaserParticleType extends ParticleType<LaserParticleData> {

    private final Codec<LaserParticleData> codec;

    public LaserParticleType() {
        super(false, LaserParticleData.DESERIALIZER);
        //TODO - 1.16: Implement
        this.codec = null;
    }

    @Nonnull
    @Override
    public Codec<LaserParticleData> func_230522_e_() {
        return this.codec;
    }
}