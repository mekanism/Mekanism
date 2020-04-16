package mekanism.common.particle;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import java.util.Locale;
import javax.annotation.Nonnull;
import mekanism.common.registries.MekanismParticleTypes;
import net.minecraft.network.PacketBuffer;
import net.minecraft.particles.IParticleData;
import net.minecraft.particles.ParticleType;
import net.minecraft.util.Direction;

public class LaserParticleData implements IParticleData {

    public final Direction direction;
    public final double distance;
    public final float energyScale;

    public LaserParticleData(Direction direction, double distance, float energyScale) {
        this.direction = direction;
        this.distance = distance;
        this.energyScale = energyScale;
    }

    @Nonnull
    @Override
    public ParticleType<?> getType() {
        return MekanismParticleTypes.LASER.getParticleType();
    }

    @Override
    public void write(@Nonnull PacketBuffer buffer) {
        buffer.writeEnumValue(direction);
        buffer.writeDouble(distance);
        buffer.writeFloat(energyScale);
    }

    @Nonnull
    @Override
    public String getParameters() {
        return String.format(Locale.ROOT, "%s %d %.2f %.2f", getType().getRegistryName(), direction.ordinal(), distance, energyScale);
    }

    public static final IDeserializer<LaserParticleData> DESERIALIZER = new IDeserializer<LaserParticleData>() {
        @Nonnull
        @Override
        public LaserParticleData deserialize(@Nonnull ParticleType<LaserParticleData> type, @Nonnull StringReader reader) throws CommandSyntaxException {
            reader.expect(' ');
            Direction direction = Direction.byIndex(reader.readInt());
            reader.expect(' ');
            double distance = reader.readDouble();
            reader.expect(' ');
            float energyScale = reader.readFloat();
            return new LaserParticleData(direction, distance, energyScale);
        }

        @Override
        public LaserParticleData read(@Nonnull ParticleType<LaserParticleData> type, PacketBuffer buf) {
            return new LaserParticleData(buf.readEnumValue(Direction.class), buf.readDouble(), buf.readFloat());
        }
    };
}