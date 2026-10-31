package mekanism.common.network.to_client;

import io.netty.buffer.ByteBuf;
import mekanism.client.recipe_viewer.RVTransferUtils;
import mekanism.common.Mekanism;
import mekanism.common.network.IMekanismPacket;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record PacketRecipeTransferResult(int transferId, boolean successful) implements IMekanismPacket {

    public static final CustomPacketPayload.Type<PacketRecipeTransferResult> TYPE = new CustomPacketPayload.Type<>(Mekanism.rl("recipe_transfer_result"));
    public static final StreamCodec<ByteBuf, PacketRecipeTransferResult> STREAM_CODEC = StreamCodec.composite(
          ByteBufCodecs.VAR_INT, PacketRecipeTransferResult::transferId,
          ByteBufCodecs.BOOL, PacketRecipeTransferResult::successful,
          PacketRecipeTransferResult::new
    );

    @Override
    public CustomPacketPayload.Type<PacketRecipeTransferResult> type() {
        return TYPE;
    }

    @Override
    public void handle(IPayloadContext context) {
        RVTransferUtils.completeTransfer(transferId, successful);
    }
}
