package mekanism.common.network.to_client.transmitter;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;
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

public record PacketTransporterBatch(BlockPos pos, IntSet deletes, byte[] rawUpdates) implements IMekanismPacket<PlayPayloadContext> {

    public static final ResourceLocation ID = Mekanism.rl("transporter_batch");

    public PacketTransporterBatch(FriendlyByteBuf buffer) {
        this(buffer.readBlockPos(), buffer.readCollection(IntOpenHashSet::new, FriendlyByteBuf::readVarInt), buffer.readByteArray());
    }

    public PacketTransporterBatch(BlockPos pos, IntSet deletes, Int2ObjectMap<TransporterStack> updates) {
        //TODO - 1.20.4: SP: Figure out if there is a better way for us to handle not leaking the instance than just forcing a write and read
        // Also validate that we can actually just directly use deletes without copying it or anything
        this(pos, deletes, FriendlyByteBufUtil.writeCustomData(buffer -> buffer.writeMap(updates, FriendlyByteBuf::writeVarInt, (buf, stack) -> stack.write(buf, pos))));
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
            Int2ObjectMap<TransporterStack> updates = PacketUtils.read(rawUpdates, buffer -> buffer.readMap(Int2ObjectOpenHashMap::new, FriendlyByteBuf::readVarInt, TransporterStack::readFromPacket));
            for (Int2ObjectMap.Entry<TransporterStack> entry : updates.int2ObjectEntrySet()) {
                transporter.addStack(entry.getIntKey(), entry.getValue());
            }
            for (int toDelete : deletes) {
                transporter.deleteStack(toDelete);
            }
        }
    }

    @Override
    public void write(@NotNull FriendlyByteBuf buffer) {
        buffer.writeBlockPos(pos);
        buffer.writeCollection(deletes, FriendlyByteBuf::writeVarInt);
        buffer.writeByteArray(rawUpdates);
    }
}