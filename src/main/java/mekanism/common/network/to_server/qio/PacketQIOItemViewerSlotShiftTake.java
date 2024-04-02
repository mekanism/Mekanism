package mekanism.common.network.to_server.qio;

import java.util.UUID;
import mekanism.common.Mekanism;
import mekanism.common.content.qio.QIOFrequency;
import mekanism.common.content.qio.QIOGlobalItemLookup;
import mekanism.common.inventory.container.QIOItemViewerContainer;
import mekanism.common.lib.inventory.HashedItem;
import mekanism.common.network.IMekanismPacket;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.handling.PlayPayloadContext;
import org.jetbrains.annotations.NotNull;

public record PacketQIOItemViewerSlotShiftTake(UUID typeUUID) implements IMekanismPacket<PlayPayloadContext> {

    public static final ResourceLocation ID = Mekanism.rl("qio_shift_take");

    public PacketQIOItemViewerSlotShiftTake(FriendlyByteBuf buffer) {
        this(buffer.readUUID());
    }

    @NotNull
    @Override
    public ResourceLocation id() {
        return ID;
    }

    @Override
    public void handle(PlayPayloadContext context) {
        Player player = context.player().orElse(null);
        if (player != null && player.containerMenu instanceof QIOItemViewerContainer container) {
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

    @Override
    public void write(@NotNull FriendlyByteBuf buffer) {
        buffer.writeUUID(typeUUID);
    }
}
