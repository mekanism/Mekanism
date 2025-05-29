package mekanism.common.network.to_server;

import io.netty.buffer.ByteBuf;
import java.util.UUID;
import java.util.function.IntFunction;
import mekanism.common.Mekanism;
import mekanism.common.network.IMekanismPacket;
import mekanism.common.network.to_client.player_data.PacketPlayerData;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.util.ByIdMap;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.NotNull;

public record PacketGearStateUpdate(GearType gearType, UUID uuid, boolean state) implements IMekanismPacket {

    public static final CustomPacketPayload.Type<PacketGearStateUpdate> TYPE = new CustomPacketPayload.Type<>(Mekanism.rl("update_gear"));
    public static final StreamCodec<ByteBuf, PacketGearStateUpdate> STREAM_CODEC = StreamCodec.composite(
          GearType.STREAM_CODEC, PacketGearStateUpdate::gearType,
          UUIDUtil.STREAM_CODEC, PacketGearStateUpdate::uuid,
          ByteBufCodecs.BOOL, PacketGearStateUpdate::state,
          PacketGearStateUpdate::new
    );

    @NotNull
    @Override
    public CustomPacketPayload.Type<PacketGearStateUpdate> type() {
        return TYPE;
    }

    @Override
    public void handle(IPayloadContext context) {
        switch (gearType) {
            case JETPACK -> Mekanism.playerState.setJetpackState(uuid, state, false);
            case SCUBA_MASK -> Mekanism.playerState.setScubaMaskState(uuid, state, false);
            case GRAVITATIONAL_MODULATOR -> Mekanism.playerState.setGravitationalModulationState(uuid, state, false);
        }
        //Inform all clients tracking the changed player
        //Note: We just resend all the data for the updated player as the packet size is about the same
        // and this allows us to separate the packet into a server to client and client to server packet
        PacketDistributor.sendToPlayersTrackingEntity(context.player(), new PacketPlayerData(uuid));
    }

    public enum GearType {
        JETPACK,
        SCUBA_MASK,
        GRAVITATIONAL_MODULATOR;

        public static final IntFunction<GearType> BY_ID = ByIdMap.continuous(GearType::ordinal, values(), ByIdMap.OutOfBoundsStrategy.WRAP);
        public static final StreamCodec<ByteBuf, GearType> STREAM_CODEC = ByteBufCodecs.idMapper(BY_ID, GearType::ordinal);
    }
}