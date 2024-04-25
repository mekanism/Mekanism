package mekanism.common.network.to_server.qio;

import io.netty.buffer.ByteBuf;
import java.util.UUID;
import mekanism.common.Mekanism;
import mekanism.common.content.qio.QIOFrequency;
import mekanism.common.content.qio.QIOGlobalItemLookup;
import mekanism.common.inventory.container.QIOItemViewerContainer;
import mekanism.common.lib.inventory.HashedItem;
import mekanism.common.network.IMekanismPacket;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.handling.IPayloadContext;
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
                HashedItem itemType = QIOGlobalItemLookup.INSTANCE.getTypeByUUID(typeUUID);
                if (itemType != null) {
                    ItemStack maxExtract = itemType.createStack(itemType.getMaxStackSize());
                    //Simulate how much room we have in the player's inventory before trying to extract anything from the frequency
                    ItemStack simulatedExcess = container.simulateInsertIntoPlayerInventory(player.getUUID(), maxExtract);
                    //Extract a stack, or as much as the inventory has room for if it can't fit a full stack
                    ItemStack extracted = freq.removeByType(itemType, maxExtract.getCount() - simulatedExcess.getCount());
                    if (!extracted.isEmpty()) {
                        ItemStack remainder = container.insertIntoPlayerInventory(player.getUUID(), extracted);
                        //In theory this should never fail as we simulate above to make sure we don't try moving more than we can
                        // but validate it just in case and handle it gracefully
                        if (!remainder.isEmpty()) {
                            remainder = freq.addItem(remainder);
                            if (!remainder.isEmpty()) {
                                //Something went wrong, and we couldn't add it back into the frequency after just removing
                                // log an error and just drop the item on the ground to avoid voiding it
                                Mekanism.logger.error("QIO shift-click transfer resulted in lost items ({}). This shouldn't happen!", remainder);
                                player.drop(remainder, false);
                            }
                        }
                    }
                }
            }
        }
    }
}
