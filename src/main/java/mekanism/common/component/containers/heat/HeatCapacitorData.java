package mekanism.common.component.containers.heat;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import java.util.Optional;
import java.util.OptionalDouble;
import mekanism.api.MekanismPreconditions;
import mekanism.api.SerializationConstants;
import mekanism.api.SerializerHelper;
import mekanism.api.heat.HeatAPI;
import mekanism.common.network.PacketUtils;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.Mth;

public record HeatCapacitorData(OptionalDouble heat, double capacity) {

    public static final Codec<HeatCapacitorData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
          SerializerHelper.NON_NEGATIVE_DOUBLE.optionalFieldOf(SerializationConstants.HEAT_STORED).forGetter(data -> data.heat.isPresent() ? Optional.of(data.heat.getAsDouble()) : Optional.empty()),
          SerializerHelper.ONE_OR_GREATER_DOUBLE.optionalFieldOf(SerializationConstants.HEAT_CAPACITY, 1D).forGetter(HeatCapacitorData::capacity)
    ).apply(instance, (heat, capacity) -> new HeatCapacitorData(
          heat.map(OptionalDouble::of).orElseGet(OptionalDouble::empty),
          capacity
    )));
    public static final StreamCodec<ByteBuf, HeatCapacitorData> STREAM_CODEC = StreamCodec.composite(
          PacketUtils.OPTIONAL_DOUBLE_STREAM_CODEC, HeatCapacitorData::heat,
          ByteBufCodecs.DOUBLE, HeatCapacitorData::capacity,
          HeatCapacitorData::new
    );

    public HeatCapacitorData(double heat, double capacity) {
        MekanismPreconditions.checkNonNegative(heat);
        this(OptionalDouble.of(heat), capacity);
    }

    public HeatCapacitorData(double capacity) {
        this(OptionalDouble.empty(), capacity);
    }

    public HeatCapacitorData withHeat(double heat) {
        MekanismPreconditions.checkNonNegative(heat);
        if (Mth.equal(heatOrAmbient(), heat)) {
            return this;
        }
        return new HeatCapacitorData(heat, capacity);
    }

    public HeatCapacitorData withCapacity(double heatCapacity) {
        MekanismPreconditions.checkHeatCapacity(heatCapacity);
        if (Mth.equal(capacity, heatCapacity)) {
            return this;
        }
        return new HeatCapacitorData(heat, heatCapacity);
    }

    public double temperature() {
        if (heat.isPresent()) {
            return heat.getAsDouble() / capacity;
        }
        return HeatAPI.AMBIENT_TEMP;
    }

    public double heatOrAmbient() {
        if (heat.isPresent()) {
            return heat.getAsDouble();
        }
        return HeatAPI.AMBIENT_TEMP * capacity;
    }
}