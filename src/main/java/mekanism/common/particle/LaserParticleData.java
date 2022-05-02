package mekanism.common.particle;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Locale;
import javax.annotation.Nonnull;
import mekanism.common.registries.MekanismParticleTypes;
import mekanism.common.util.MekanismUtils;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.network.FriendlyByteBuf;

public record LaserParticleData(Direction direction, double distance, float energyScale) implements ParticleOptions {

    public static final Deserializer<LaserParticleData> DESERIALIZER = new Deserializer<>() {
        @Nonnull
        @Override
        public LaserParticleData fromCommand(@Nonnull ParticleType<LaserParticleData> type, @Nonnull StringReader reader) throws CommandSyntaxException {
            reader.expect(' ');
            Direction direction = Direction.from3DDataValue(reader.readInt());
            reader.expect(' ');
            double distance = reader.readDouble();
            reader.expect(' ');
            float energyScale = reader.readFloat();
            return new LaserParticleData(direction, distance, energyScale);
        }

        @Nonnull
        @Override
        public LaserParticleData fromNetwork(@Nonnull ParticleType<LaserParticleData> type, FriendlyByteBuf buf) {
            return new LaserParticleData(buf.readEnum(Direction.class), buf.readDouble(), buf.readFloat());
        }
    };
    public static final Codec<LaserParticleData> CODEC = RecordCodecBuilder.create(val -> val.group(
          MekanismUtils.DIRECTION_CODEC.fieldOf("direction").forGetter(data -> data.direction),
          Codec.DOUBLE.fieldOf("distance").forGetter(data -> data.distance),
          Codec.FLOAT.fieldOf("energyScale").forGetter(data -> data.energyScale)
    ).apply(val, LaserParticleData::new));

    @Nonnull
    @Override
    public ParticleType<?> getType() {
        return MekanismParticleTypes.LASER.get();
    }

    @Override
    public void writeToNetwork(@Nonnull FriendlyByteBuf buffer) {
        buffer.writeEnum(direction);
        buffer.writeDouble(distance);
        buffer.writeFloat(energyScale);
    }

    @Nonnull
    @Override
    public String writeToString() {
        return String.format(Locale.ROOT, "%s %d %.2f %.2f", getType().getRegistryName(), direction.ordinal(), distance, energyScale);
    }
}