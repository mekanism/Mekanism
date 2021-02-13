package mekanism.common.network;

import java.util.function.Supplier;
import mekanism.common.Mekanism;
import mekanism.common.content.qio.QIOFrequency;
import mekanism.common.item.ItemPortableQIODashboard;
import mekanism.common.lib.frequency.Frequency.FrequencyIdentity;
import mekanism.common.lib.frequency.FrequencyType;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.network.NetworkEvent.Context;

public class PacketQIOSetColor {

    private final Type type;
    private final int extra;
    private final FrequencyIdentity identity;
    private final BlockPos tilePosition;
    private final Hand currentHand;

    private PacketQIOSetColor(Type type, int extra, FrequencyIdentity identity, BlockPos tilePosition, Hand currentHand) {
        this.type = type;
        this.extra = extra;
        this.identity = identity;
        this.tilePosition = tilePosition;
        this.currentHand = currentHand;
    }

    public static PacketQIOSetColor create(BlockPos tilePosition, QIOFrequency freq, int extra) {
        return new PacketQIOSetColor(Type.TILE, extra, freq.getIdentity(), tilePosition, null);
    }

    public static PacketQIOSetColor create(Hand currentHand, QIOFrequency freq, int extra) {
        return new PacketQIOSetColor(Type.ITEM, extra, freq.getIdentity(), null, currentHand);
    }

    public static void handle(PacketQIOSetColor message, Supplier<Context> context) {
        Context ctx = context.get();
        ctx.enqueueWork(() -> {
            ServerPlayerEntity player = ctx.getSender();
            if (player != null) {
                QIOFrequency freq = FrequencyType.QIO.getFrequency(message.identity, player.getUniqueID());
                if (freq != null && freq.ownerMatches(player.getUniqueID())) {
                    freq.setColor(message.extra == 0 ? freq.getColor().getNext() : freq.getColor().getPrevious());
                    if (message.type == Type.ITEM) {
                        ItemStack stack = player.getItemStackFromSlot(EquipmentSlotType.MAINHAND);
                        if (stack.getItem() instanceof ItemPortableQIODashboard) {
                            ((ItemPortableQIODashboard) stack.getItem()).setColor(stack, freq.getColor());
                        }
                        Mekanism.packetHandler.sendTo(PacketFrequencyItemGuiUpdate.update(message.currentHand, FrequencyType.QIO, player.getUniqueID(), freq), player);
                    }
                }
            }
        });
        ctx.setPacketHandled(true);
    }

    public static void encode(PacketQIOSetColor pkt, PacketBuffer buf) {
        buf.writeEnumValue(pkt.type);
        buf.writeVarInt(pkt.extra);
        FrequencyType.QIO.getIdentitySerializer().write(buf, pkt.identity);
        if (pkt.type == Type.TILE) {
            buf.writeBlockPos(pkt.tilePosition);
        } else {
            buf.writeEnumValue(pkt.currentHand);
        }
    }

    public static PacketQIOSetColor decode(PacketBuffer buf) {
        Type type = buf.readEnumValue(Type.class);
        int extra = buf.readVarInt();
        FrequencyIdentity identity = FrequencyType.QIO.getIdentitySerializer().read(buf);
        BlockPos pos = type == Type.TILE ? buf.readBlockPos() : null;
        Hand hand = type == Type.ITEM ? buf.readEnumValue(Hand.class) : null;
        return new PacketQIOSetColor(type, extra, identity, pos, hand);
    }

    public enum Type {
        TILE,
        ITEM;
    }
}
