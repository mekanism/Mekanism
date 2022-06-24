package mekanism.common.particle;

import com.mojang.serialization.Codec;
import net.minecraft.core.particles.ParticleType;
import org.jetbrains.annotations.NotNull;

public class LaserParticleType extends ParticleType<LaserParticleData> {

    public LaserParticleType() {
        super(false, LaserParticleData.DESERIALIZER);
    }

    @NotNull
    @Override
    public Codec<LaserParticleData> codec() {
        return LaserParticleData.CODEC;
    }
}