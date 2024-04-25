package mekanism.common.network.to_client.radiation;

import io.netty.buffer.ByteBuf;
import mekanism.common.Mekanism;
import mekanism.common.network.IMekanismPacket;
import mekanism.common.registries.MekanismAttachmentTypes;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.NotNull;

public record PacketPlayerRadiationData(double radiation) implements IMekanismPacket {

    public static final CustomPacketPayload.Type<PacketPlayerRadiationData> TYPE = new CustomPacketPayload.Type<>(Mekanism.rl("player_radiation"));
    public static final StreamCodec<ByteBuf, PacketPlayerRadiationData> STREAM_CODEC = ByteBufCodecs.DOUBLE.map(
          PacketPlayerRadiationData::new, PacketPlayerRadiationData::radiation
    );

    public PacketPlayerRadiationData(Player player) {
        this(player.getData(MekanismAttachmentTypes.RADIATION));
    }

    @NotNull
    @Override
    public CustomPacketPayload.Type<PacketPlayerRadiationData> type() {
        return TYPE;
    }

    @Override
    public void handle(IPayloadContext context) {
        context.player().setData(MekanismAttachmentTypes.RADIATION, radiation);
    }
}
