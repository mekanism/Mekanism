package mekanism.common.particle;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;

public class LaserParticleType extends ParticleType<LaserParticleData> {

    public LaserParticleType() {
        super(false);
    }

    @Override
    public MapCodec<LaserParticleData> codec() {
        return LaserParticleData.CODEC;
    }

    @Override
    public StreamCodec<? super RegistryFriendlyByteBuf, LaserParticleData> streamCodec() {
        return LaserParticleData.STREAM_CODEC;
    }
}