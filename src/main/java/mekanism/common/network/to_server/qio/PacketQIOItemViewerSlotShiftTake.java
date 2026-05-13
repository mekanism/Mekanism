package mekanism.common.network.to_server.qio;

import io.netty.buffer.ByteBuf;
import java.util.UUID;
import mekanism.common.Mekanism;
import mekanism.common.content.qio.QIOFrequency;
import mekanism.common.content.qio.QIOGlobalItemLookup;
import mekanism.common.inventory.container.QIOItemViewerContainer;
import mekanism.common.network.IMekanismPacket;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.transaction.Transaction;
import org.jetbrains.annotations.NotNull;

public record PacketQIOItemViewerSlotShiftTake(UUID typeUUID) implements IMekanismPacket {

    public static final CustomPacketPayload.Type<PacketQIOItemViewerSlotShiftTake> TYPE = new CustomPacketPayload.Type<>(Mekanism.rl("qio_shift_take"));
    public static final StreamCodec<ByteBuf, PacketQIOItemViewerSlotShiftTake> STREAM_CODEC = UUIDUtil.STREAM_CODEC.map(
          PacketQIOItemViewerSlotShiftTake::new, PacketQIOItemViewerSlotShiftTake::typeUUID
    );

    @NotNull
    @Override
    public CustomPacketPayload.Type<PacketQIOItemViewerSlotShiftTake> type() {
        return TYPE;
    }

    @Override
    public void handle(IPayloadContext context) {
        Player player = context.player();
        if (player.containerMenu instanceof QIOItemViewerContainer container) {
            QIOFrequency freq = container.getFrequency();
            if (freq != null) {
                ItemResource itemType = QIOGlobalItemLookup.instance().getTypeByUUID(typeUUID);
                if (!itemType.isEmpty()) {
                    ItemStack maxExtract = itemType.toStack(itemType.getMaxStackSize());
                    //Simulate how much room we have in the player's inventory before trying to extract anything from the frequency
                    int amountInserted = container.simulateInsertIntoPlayerInventory(player.getUUID(), maxExtract);
                    //Extract a stack, or as much as the inventory has room for if it can't fit a full stack
                    int extracted = freq.removeByType(itemType, amountInserted);
                    if (extracted > 0) {
                        try (Transaction transaction = Transaction.openRoot()) {
                            int toInsert = container.insertIntoPlayerInventory(player.getUUID(), itemType, extracted, transaction);
                            //In theory this should never fail as we simulate above to make sure we don't try moving more than we can
                            // but validate it just in case and handle it gracefully
                            if (toInsert > 0) {
                                toInsert -= freq.addItem(itemType, toInsert);
                                if (toInsert > 0) {
                                    //Something went wrong, and we couldn't add it back into the frequency after just removing
                                    // log an error and just drop the item on the ground to avoid voiding it
                                    Mekanism.logger.error("QIO shift-click transfer resulted in lost items ({} {}). This shouldn't happen!", toInsert, itemType);
                                    player.drop(itemType.toStack(toInsert), false);
                                }
                            }
                            transaction.commit();
                        }
                    }
                }
            }
        }
    }
}
