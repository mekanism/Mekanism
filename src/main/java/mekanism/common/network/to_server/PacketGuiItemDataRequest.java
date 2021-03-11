package mekanism.common.network.to_server;

import mekanism.common.Mekanism;
import mekanism.common.content.qio.QIOFrequency;
import mekanism.common.inventory.container.QIOItemViewerContainer;
import mekanism.common.inventory.container.item.FrequencyItemContainer;
import mekanism.common.lib.frequency.Frequency;
import mekanism.common.lib.frequency.Frequency.FrequencyIdentity;
import mekanism.common.lib.frequency.FrequencyManager;
import mekanism.common.lib.frequency.IFrequencyItem;
import mekanism.common.network.IMekanismPacket;
import mekanism.common.network.to_client.PacketFrequencyItemGuiUpdate;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.Hand;
import net.minecraftforge.fml.network.NetworkEvent;

public class PacketGuiItemDataRequest implements IMekanismPacket {

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

    @Override
    public void handle(NetworkEvent.Context context) {
        ServerPlayerEntity player = context.getSender();
        if (player != null) {
            if (type == Type.FREQUENCY_LIST_GUI) {
                if (player.containerMenu instanceof FrequencyItemContainer) {
                    handleFrequencyList(player);
                }
            } else if (type == Type.QIO_ITEM_VIEWER) {
                if (player.containerMenu instanceof QIOItemViewerContainer) {
                    QIOItemViewerContainer container = (QIOItemViewerContainer) player.containerMenu;
                    QIOFrequency freq = container.getFrequency();
                    if (!player.level.isClientSide() && freq != null) {
                        freq.openItemViewer(player);
                    }
                }
            }
        }
    }

    private <FREQ extends Frequency> void handleFrequencyList(PlayerEntity player) {
        FrequencyItemContainer<FREQ> container = (FrequencyItemContainer<FREQ>) player.containerMenu;
        ItemStack stack = player.getItemInHand(hand);
        FrequencyIdentity identity = ((IFrequencyItem) stack.getItem()).getFrequency(stack);
        FREQ freq = null;
        if (identity != null) {
            FrequencyManager<FREQ> manager = identity.isPublic() ? container.getFrequencyType().getManager(null)
                                                                 : container.getFrequencyType().getManager(player.getUUID());
            freq = manager.getFrequency(identity.getKey());
            // if this frequency no longer exists, remove the reference from the stack
            if (freq == null) {
                ((IFrequencyItem) stack.getItem()).setFrequency(stack, null);
            }
        }
        Mekanism.packetHandler.sendTo(PacketFrequencyItemGuiUpdate.update(hand, container.getFrequencyType(), player.getUUID(), freq), (ServerPlayerEntity) player);
    }

    @Override
    public void encode(PacketBuffer buffer) {
        buffer.writeEnum(type);
        if (type == Type.FREQUENCY_LIST_GUI) {
            buffer.writeEnum(hand);
        }
    }

    public static PacketGuiItemDataRequest decode(PacketBuffer buffer) {
        Type type = buffer.readEnum(Type.class);
        Hand hand = null;
        if (type == Type.FREQUENCY_LIST_GUI) {
            hand = buffer.readEnum(Hand.class);
        }
        return new PacketGuiItemDataRequest(type, hand);
    }

    private enum Type {
        FREQUENCY_LIST_GUI,
        QIO_ITEM_VIEWER
    }
}
