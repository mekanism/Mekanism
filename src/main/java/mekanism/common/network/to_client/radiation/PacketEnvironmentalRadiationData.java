package mekanism.common.network.to_client.radiation;

import io.netty.buffer.ByteBuf;
import mekanism.common.Mekanism;
import mekanism.common.lib.radiation.ClientRadiation;
import mekanism.common.lib.radiation.LevelAndMaxMagnitude;
import mekanism.common.network.IMekanismPacket;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.NotNull;

public record PacketEnvironmentalRadiationData(double radiation, double maxMagnitude) implements IMekanismPacket {

    public static final CustomPacketPayload.Type<PacketEnvironmentalRadiationData> TYPE = new CustomPacketPayload.Type<>(Mekanism.rl("environmental_radiation"));
    public static final StreamCodec<ByteBuf, PacketEnvironmentalRadiationData> STREAM_CODEC = StreamCodec.composite(
          ByteBufCodecs.DOUBLE, PacketEnvironmentalRadiationData::radiation,
          ByteBufCodecs.DOUBLE, PacketEnvironmentalRadiationData::maxMagnitude,
          PacketEnvironmentalRadiationData::new
    );

    public PacketEnvironmentalRadiationData(LevelAndMaxMagnitude levelAndMaxMagnitude) {
        this(levelAndMaxMagnitude.level(), levelAndMaxMagnitude.maxMagnitude());
    }

    @NotNull
    @Override
    public CustomPacketPayload.Type<PacketEnvironmentalRadiationData> type() {
        return TYPE;
    }

    @Override
    public void handle(IPayloadContext context) {
        ClientRadiation.setClientEnvironmentalRadiation(radiation, maxMagnitude);
    }
}
