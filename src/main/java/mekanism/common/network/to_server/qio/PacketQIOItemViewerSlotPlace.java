package mekanism.common.network.to_server.qio;

import io.netty.buffer.ByteBuf;
import mekanism.common.Mekanism;
import mekanism.common.content.qio.QIOFrequency;
import mekanism.common.inventory.container.QIOItemViewerContainer;
import mekanism.common.network.IMekanismPacket;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.NotNull;

public record PacketQIOItemViewerSlotPlace(int count) implements IMekanismPacket {

    public static final CustomPacketPayload.Type<PacketQIOItemViewerSlotPlace> TYPE = new CustomPacketPayload.Type<>(Mekanism.rl("qio_place"));
    public static final StreamCodec<ByteBuf, PacketQIOItemViewerSlotPlace> STREAM_CODEC = ByteBufCodecs.VAR_INT.map(
          PacketQIOItemViewerSlotPlace::new, PacketQIOItemViewerSlotPlace::count
    );

    @NotNull
    @Override
    public CustomPacketPayload.Type<PacketQIOItemViewerSlotPlace> type() {
        return TYPE;
    }

    @Override
    public void handle(IPayloadContext context) {
        if (context.player().containerMenu instanceof QIOItemViewerContainer container) {
            QIOFrequency freq = container.getFrequency();
            if (freq != null) {
                ItemStack curStack = container.getCarried();
                //Count should always be greater than zero but validate against invalid packets
                if (!curStack.isEmpty() && count > 0) {
                    ItemStack toAdd;
                    if (count < curStack.getCount()) {//Only adding part of the stack
                        toAdd = curStack.copyWithCount(count);
                    } else {//Try to add the full held stack
                        toAdd = curStack;
                    }
                    ItemStack rejects = freq.addItem(toAdd);
                    //Calculate actual amount we were able to add of what we tried to add
                    int placed = toAdd.getCount() - rejects.getCount();
                    if (placed > 0) {
                        //If we added any from the held stack, shrink the held stack (which will cause it to be updated on the client)
                        curStack.shrink(placed);
                    }
                }
            }
        }
    }
}
