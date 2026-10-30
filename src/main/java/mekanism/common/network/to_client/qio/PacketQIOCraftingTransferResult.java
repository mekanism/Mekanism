package mekanism.common.network.to_client.qio;

import io.netty.buffer.ByteBuf;
import mekanism.client.recipe_viewer.QIOCraftingTransferHandler;
import mekanism.common.Mekanism;
import mekanism.common.network.IMekanismPacket;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record PacketQIOCraftingTransferResult(int transferId, boolean successful) implements IMekanismPacket {

    public static final CustomPacketPayload.Type<PacketQIOCraftingTransferResult> TYPE = new CustomPacketPayload.Type<>(Mekanism.rl("qio_crafting_transfer_result"));
    public static final StreamCodec<ByteBuf, PacketQIOCraftingTransferResult> STREAM_CODEC = StreamCodec.composite(
          ByteBufCodecs.VAR_INT, PacketQIOCraftingTransferResult::transferId,
          ByteBufCodecs.BOOL, PacketQIOCraftingTransferResult::successful,
          PacketQIOCraftingTransferResult::new
    );

    @Override
    public CustomPacketPayload.Type<PacketQIOCraftingTransferResult> type() {
        return TYPE;
    }

    @Override
    public void handle(IPayloadContext context) {
        QIOCraftingTransferHandler.completeTransfer(transferId, successful);
    }
}
