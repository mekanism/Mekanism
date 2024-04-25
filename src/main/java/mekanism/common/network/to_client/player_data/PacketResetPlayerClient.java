package mekanism.common.network.to_client.player_data;

import io.netty.buffer.ByteBuf;
import java.util.UUID;
import mekanism.common.Mekanism;
import mekanism.common.network.IMekanismPacket;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.NotNull;

public record PacketResetPlayerClient(UUID uuid) implements IMekanismPacket {

    public static final CustomPacketPayload.Type<PacketResetPlayerClient> TYPE = new CustomPacketPayload.Type<>(Mekanism.rl("reset_client"));
    public static final StreamCodec<ByteBuf, PacketResetPlayerClient> STREAM_CODEC = UUIDUtil.STREAM_CODEC.map(
          PacketResetPlayerClient::new, PacketResetPlayerClient::uuid
    );

    @NotNull
    @Override
    public CustomPacketPayload.Type<PacketResetPlayerClient> type() {
        return TYPE;
    }

    @Override
    public void handle(IPayloadContext context) {
        Mekanism.playerState.clearPlayer(uuid, true);
    }
}
