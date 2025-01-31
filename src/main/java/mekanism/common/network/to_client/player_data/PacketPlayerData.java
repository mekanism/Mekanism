package mekanism.common.network.to_client.player_data;

import io.netty.buffer.ByteBuf;
import java.util.UUID;
import mekanism.common.Mekanism;
import mekanism.common.network.IMekanismPacket;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.NotNull;

public record PacketPlayerData(UUID uuid, boolean activeJetpack, boolean activeScubaMask, boolean activeModulator) implements IMekanismPacket {

    public static final CustomPacketPayload.Type<PacketPlayerData> TYPE = new CustomPacketPayload.Type<>(Mekanism.rl("player_data"));
    public static final StreamCodec<ByteBuf, PacketPlayerData> STREAM_CODEC = StreamCodec.composite(
          UUIDUtil.STREAM_CODEC, PacketPlayerData::uuid,
          ByteBufCodecs.BOOL, PacketPlayerData::activeJetpack,
          ByteBufCodecs.BOOL, PacketPlayerData::activeScubaMask,
          ByteBufCodecs.BOOL, PacketPlayerData::activeModulator,
          PacketPlayerData::new
    );

    public PacketPlayerData(UUID uuid) {
        this(uuid,
              Mekanism.playerState.isJetpackOn(uuid),
              Mekanism.playerState.isScubaMaskOn(uuid),
              Mekanism.playerState.isGravitationalModulationOn(uuid)
        );
    }

    @NotNull
    @Override
    public CustomPacketPayload.Type<PacketPlayerData> type() {
        return TYPE;
    }

    @Override
    public void handle(IPayloadContext context) {
        Mekanism.playerState.setJetpackState(uuid, activeJetpack, false);
        Mekanism.playerState.setScubaMaskState(uuid, activeScubaMask, false);
        Mekanism.playerState.setGravitationalModulationState(uuid, activeModulator, false);
    }
}