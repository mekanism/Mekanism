package mekanism.common.particle;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import org.jetbrains.annotations.NotNull;

public class LaserParticleType extends ParticleType<LaserParticleData> {

    public LaserParticleType() {
        super(false);
    }

    @NotNull
    @Override
    public MapCodec<LaserParticleData> codec() {
        return LaserParticleData.CODEC;
    }

    @NotNull
    @Override
    public StreamCodec<? super RegistryFriendlyByteBuf, LaserParticleData> streamCodec() {
        return LaserParticleData.STREAM_CODEC;
    }
}