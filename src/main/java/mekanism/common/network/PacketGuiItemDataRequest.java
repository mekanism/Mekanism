package mekanism.common.network;

import java.util.function.Supplier;
import mekanism.common.Mekanism;
import mekanism.common.content.qio.QIOFrequency;
import mekanism.common.inventory.container.QIOItemViewerContainer;
import mekanism.common.inventory.container.item.FrequencyItemContainer;
import mekanism.common.lib.frequency.Frequency;
import mekanism.common.lib.frequency.Frequency.FrequencyIdentity;
import mekanism.common.lib.frequency.FrequencyManager;
import mekanism.common.lib.frequency.IFrequencyItem;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.Hand;
import net.minecraftforge.fml.network.NetworkEvent.Context;

public class PacketGuiItemDataRequest {

    private final Type type;
    private final Hand hand;

    public PacketGuiItemDataRequest(Type type, Hand hand) {
        this.type = type;
        this.hand = hand;
    }

    public static PacketGuiItemDataRequest frequencyList(Hand hand) {
        return new PacketGuiItemDataRequest(Type.FREQUENCY_LIST_GUI, hand);
    }

    public static PacketGuiItemDataRequest qioItemViewer() {
        return new PacketGuiItemDataRequest(Type.QIO_ITEM_VIEWER, null);
    }

    public static void handle(PacketGuiItemDataRequest message, Supplier<Context> context) {
        Context ctx = context.get();
        ctx.enqueueWork(() -> {
            ServerPlayerEntity player = ctx.getSender();
            if (player != null) {
                if (message.type == Type.FREQUENCY_LIST_GUI) {
                    if (player.openContainer instanceof FrequencyItemContainer) {
                        handleFrequencyList(message, player);
                    }
                } else if (message.type == Type.QIO_ITEM_VIEWER) {
                    if (player.openContainer instanceof QIOItemViewerContainer) {
                        QIOItemViewerContainer container = (QIOItemViewerContainer) player.openContainer;
                        QIOFrequency freq = container.getFrequency();
                        if (!player.world.isRemote() && freq != null) {
                            freq.openItemViewer(player);
                        }
                    }
                }
            }
        });
        ctx.setPacketHandled(true);
    }

    private static <FREQ extends Frequency> void handleFrequencyList(PacketGuiItemDataRequest message, PlayerEntity player) {
        FrequencyItemContainer<FREQ> container = (FrequencyItemContainer<FREQ>) player.openContainer;
        ItemStack stack = player.getHeldItem(message.hand);
        FrequencyIdentity identity = ((IFrequencyItem) stack.getItem()).getFrequency(stack);
        FREQ freq = null;
        if (identity != null) {
            FrequencyManager<FREQ> manager = identity.isPublic() ? container.getFrequencyType().getManager(null) : container.getFrequencyType().getManager(player.getUniqueID());
            freq = manager.getFrequency(identity.getKey());
            // if this frequency no longer exists, remove the reference from the stack
            if (freq == null) {
                ((IFrequencyItem) stack.getItem()).setFrequency(stack, null);
            }
        }
        Mekanism.packetHandler.sendTo(PacketFrequencyItemGuiUpdate.update(message.hand, container.getFrequencyType(), player.getUniqueID(), freq), (ServerPlayerEntity) player);
    }

    public static void encode(PacketGuiItemDataRequest pkt, PacketBuffer buf) {
        buf.writeEnumValue(pkt.type);
        if (pkt.type == Type.FREQUENCY_LIST_GUI) {
            buf.writeEnumValue(pkt.hand);
        }
    }

    public static PacketGuiItemDataRequest decode(PacketBuffer buf) {
        Type type = buf.readEnumValue(Type.class);
        Hand hand = null;
        if (type == Type.FREQUENCY_LIST_GUI) {
            hand = buf.readEnumValue(Hand.class);
        }
        return new PacketGuiItemDataRequest(type, hand);
    }

    private enum Type {
        FREQUENCY_LIST_GUI,
        QIO_ITEM_VIEWER
    }
}
