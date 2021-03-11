package mekanism.common.network.to_server;

import mekanism.common.Mekanism;
import mekanism.common.content.qio.QIOFrequency;
import mekanism.common.item.ItemPortableQIODashboard;
import mekanism.common.lib.frequency.Frequency.FrequencyIdentity;
import mekanism.common.lib.frequency.FrequencyType;
import mekanism.common.network.IMekanismPacket;
import mekanism.common.network.to_client.PacketFrequencyItemGuiUpdate;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.network.NetworkEvent;

public class PacketQIOSetColor implements IMekanismPacket {

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

    @Override
    public void handle(NetworkEvent.Context context) {
        ServerPlayerEntity player = context.getSender();
        if (player != null) {
            QIOFrequency freq = FrequencyType.QIO.getFrequency(identity, player.getUUID());
            if (freq != null && freq.ownerMatches(player.getUUID())) {
                freq.setColor(extra == 0 ? freq.getColor().getNext() : freq.getColor().getPrevious());
                if (type == Type.ITEM) {
                    ItemStack stack = player.getItemBySlot(EquipmentSlotType.MAINHAND);
                    if (stack.getItem() instanceof ItemPortableQIODashboard) {
                        ((ItemPortableQIODashboard) stack.getItem()).setColor(stack, freq.getColor());
                    }
                    Mekanism.packetHandler.sendTo(PacketFrequencyItemGuiUpdate.update(currentHand, FrequencyType.QIO, player.getUUID(), freq), player);
                }
            }
        }
    }

    @Override
    public void encode(PacketBuffer buffer) {
        buffer.writeEnum(type);
        buffer.writeVarInt(extra);
        FrequencyType.QIO.getIdentitySerializer().write(buffer, identity);
        if (type == Type.TILE) {
            buffer.writeBlockPos(tilePosition);
        } else {
            buffer.writeEnum(currentHand);
        }
    }

    public static PacketQIOSetColor decode(PacketBuffer buffer) {
        Type type = buffer.readEnum(Type.class);
        int extra = buffer.readVarInt();
        FrequencyIdentity identity = FrequencyType.QIO.getIdentitySerializer().read(buffer);
        BlockPos pos = type == Type.TILE ? buffer.readBlockPos() : null;
        Hand hand = type == Type.ITEM ? buffer.readEnum(Hand.class) : null;
        return new PacketQIOSetColor(type, extra, identity, pos, hand);
    }

    public enum Type {
        TILE,
        ITEM;
    }
}
