package mekanism.common.network.to_server.qio;

import io.netty.buffer.ByteBuf;
import java.util.UUID;
import mekanism.common.Mekanism;
import mekanism.common.content.qio.QIOFrequency;
import mekanism.common.content.qio.QIOGlobalItemLookup;
import mekanism.common.inventory.container.MekanismContainer;
import mekanism.common.inventory.container.QIOItemViewerContainer;
import mekanism.common.inventory.container.SelectedWindowData;
import mekanism.common.inventory.container.slot.TransactionalSlot;
import mekanism.common.network.IMekanismPacket;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.entity.player.Player;
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
                    Iterable<TransactionalSlot> playerInventory = container.getPlayerSlots();
                    SelectedWindowData selectedWindow = container.getSelectedWindow(player.getUUID());
                    int amountInserted;
                    try (Transaction simulation = Transaction.openRoot()) {
                        //Simulate how much room we have in the player's inventory before trying to extract anything from the frequency
                        amountInserted = MekanismContainer.insertItem(playerInventory, itemType, itemType.getMaxStackSize(), simulation, selectedWindow);
                    }
                    if (amountInserted > 0) {
                        try (Transaction transaction = Transaction.openRoot()) {
                            //Extract a stack, or as much as the inventory has room for if it can't fit a full stack
                            int extracted = freq.removeByType(itemType, amountInserted, transaction);
                            if (extracted > 0 && MekanismContainer.insertItem(playerInventory, itemType, extracted, transaction, selectedWindow) == extracted) {
                                //In theory this should never fail as we simulate above to make sure we don't try moving more than we can
                                // but validate it just in case and roll back if we failed
                                transaction.commit();
                            }
                        }
                    }
                }
            }
        }
    }
}
