package mekanism.common.network.to_server.qio;

import io.netty.buffer.ByteBuf;
import java.util.UUID;
import mekanism.common.Mekanism;
import mekanism.common.content.qio.QIOFrequency;
import mekanism.common.content.qio.QIOGlobalItemLookup;
import mekanism.common.inventory.container.QIOItemViewerContainer;
import mekanism.common.lib.inventory.HashedItem;
import mekanism.common.network.IMekanismPacket;
import mekanism.common.util.InventoryUtils;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.NotNull;

public record PacketQIOItemViewerSlotTake(UUID typeUUID, int count) implements IMekanismPacket {

    public static final CustomPacketPayload.Type<PacketQIOItemViewerSlotTake> TYPE = new CustomPacketPayload.Type<>(Mekanism.rl("qio_take"));
    public static final StreamCodec<ByteBuf, PacketQIOItemViewerSlotTake> STREAM_CODEC = StreamCodec.composite(
          UUIDUtil.STREAM_CODEC, PacketQIOItemViewerSlotTake::typeUUID,
          ByteBufCodecs.VAR_INT, PacketQIOItemViewerSlotTake::count,
          PacketQIOItemViewerSlotTake::new
    );

    @NotNull
    @Override
    public CustomPacketPayload.Type<PacketQIOItemViewerSlotTake> type() {
        return TYPE;
    }

    @Override
    public void handle(IPayloadContext context) {
        if (context.player().containerMenu instanceof QIOItemViewerContainer container) {
            QIOFrequency freq = container.getFrequency();
            if (freq != null) {
                HashedItem itemType = QIOGlobalItemLookup.INSTANCE.getTypeByUUID(typeUUID);
                if (itemType != null) {
                    ItemStack curStack = container.getCarried();
                    //Clamp amount to extract by max stack size in case something is wrong with the packet that got sent
                    // or multiple packets got sent before the server's response got to the client
                    //Note: Rather than checking if the cur stack is empty to know whether to grab the max stack size from it,
                    // we just assume they are the same type, as we will validate the type matches before actually extracting
                    int toRemove = Math.min(count, itemType.getMaxStackSize() - curStack.getCount());
                    //Check to make sure we actually have room in the carried stack for any more items
                    //Note: The current stack and the grabbed stack should always be stackable unless the client sent multiple packets
                    // before processing our response to the first one, but we need to validate it to make sure it can actually stack
                    // so that we can avoid accidentally voiding any items
                    if (toRemove > 0 && InventoryUtils.areItemsStackable(curStack, itemType.getInternalStack())) {
                        ItemStack extracted = freq.removeByType(itemType, toRemove);
                        if (!extracted.isEmpty()) {
                            if (curStack.isEmpty()) {
                                container.setCarried(extracted);
                            } else {
                                //If we removed any from the held stack, shrink the held stack (which will cause it to be updated on the client)
                                curStack.grow(extracted.getCount());
                            }
                        }
                    }
                }
            }
        }
    }
}
