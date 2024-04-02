package mekanism.common.network.to_client.transmitter;

import mekanism.common.Mekanism;
import mekanism.common.content.network.transmitter.LogisticalTransporterBase;
import mekanism.common.content.transporter.TransporterStack;
import mekanism.common.network.IMekanismPacket;
import mekanism.common.network.PacketUtils;
import mekanism.common.tile.transmitter.TileEntityLogisticalTransporterBase;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.common.util.FriendlyByteBufUtil;
import net.neoforged.neoforge.network.handling.PlayPayloadContext;
import org.jetbrains.annotations.NotNull;

public record PacketTransporterSync(BlockPos pos, int stackId, byte[] rawStack) implements IMekanismPacket<PlayPayloadContext> {

    public static final ResourceLocation ID = Mekanism.rl("transporter_sync");

    public PacketTransporterSync(FriendlyByteBuf buffer) {
        this(buffer.readBlockPos(), buffer.readVarInt(), buffer.readByteArray());
    }

    public PacketTransporterSync(BlockPos pos, int stackId, TransporterStack stack) {
        //TODO - 1.20.4: SP: Figure out if there is a better way for us to handle not leaking the instance than just forcing a write and read
        this(pos, stackId, FriendlyByteBufUtil.writeCustomData(buffer -> stack.write(buffer, pos)));
    }

    @NotNull
    @Override
    public ResourceLocation id() {
        return ID;
    }

    @Override
    public void handle(PlayPayloadContext context) {
        TileEntityLogisticalTransporterBase tile = PacketUtils.blockEntity(context, pos, TileEntityLogisticalTransporterBase.class);
        if (tile != null) {
            LogisticalTransporterBase transporter = tile.getTransmitter();
            TransporterStack stack = PacketUtils.read(rawStack, TransporterStack::readFromPacket);
            transporter.addStack(stackId, stack);
        }
    }

    @Override
    public void write(@NotNull FriendlyByteBuf buffer) {
        buffer.writeBlockPos(pos);
        buffer.writeVarInt(stackId);
        buffer.writeByteArray(rawStack);
    }
}