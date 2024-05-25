package mekanism.common.particle;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import mekanism.api.SerializationConstants;
import mekanism.common.registries.MekanismParticleTypes;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import org.jetbrains.annotations.NotNull;

public record LaserParticleData(Direction direction, double distance, float energyScale) implements ParticleOptions {

    public static final MapCodec<LaserParticleData> CODEC = RecordCodecBuilder.mapCodec(val -> val.group(
          Direction.CODEC.fieldOf(SerializationConstants.DIRECTION).forGetter(data -> data.direction),
          Codec.DOUBLE.fieldOf(SerializationConstants.DISTANCE).forGetter(data -> data.distance),
          Codec.FLOAT.fieldOf(SerializationConstants.ENERGY).forGetter(data -> data.energyScale)
    ).apply(val, LaserParticleData::new));
    public static final StreamCodec<ByteBuf, LaserParticleData> STREAM_CODEC = StreamCodec.composite(
          Direction.STREAM_CODEC, LaserParticleData::direction,
          ByteBufCodecs.DOUBLE, LaserParticleData::distance,
          ByteBufCodecs.FLOAT, LaserParticleData::energyScale,
          LaserParticleData::new
    );

    @NotNull
    @Override
    public ParticleType<?> getType() {
        return MekanismParticleTypes.LASER.get();
    }
}