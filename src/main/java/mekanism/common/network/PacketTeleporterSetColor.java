package mekanism.common.network;

import java.util.function.Supplier;
import mekanism.common.Mekanism;
import mekanism.common.content.teleporter.TeleporterFrequency;
import mekanism.common.lib.frequency.Frequency.FrequencyIdentity;
import mekanism.common.lib.frequency.FrequencyType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.network.NetworkEvent.Context;

public class PacketTeleporterSetColor {

    private final Type type;
    private final int extra;
    private final FrequencyIdentity identity;
    private final BlockPos tilePosition;
    private final Hand currentHand;

    private PacketTeleporterSetColor(Type type, int extra, FrequencyIdentity identity, BlockPos tilePosition, Hand currentHand) {
        this.type = type;
        this.extra = extra;
        this.identity = identity;
        this.tilePosition = tilePosition;
        this.currentHand = currentHand;
    }

    public static PacketTeleporterSetColor create(BlockPos tilePosition, TeleporterFrequency freq, int extra) {
        return new PacketTeleporterSetColor(Type.TILE, extra, freq.getIdentity(), tilePosition, null);
    }

    public static PacketTeleporterSetColor create(Hand currentHand, TeleporterFrequency freq, int extra) {
        return new PacketTeleporterSetColor(Type.ITEM, extra, freq.getIdentity(), null, currentHand);
    }

    public static void handle(PacketTeleporterSetColor message, Supplier<Context> context) {
        PlayerEntity player = BasePacketHandler.getPlayer(context);
        if (player == null) {
            return;
        }
        context.get().enqueueWork(() -> {
            TeleporterFrequency freq = FrequencyType.TELEPORTER.getFrequency(message.identity, player.getUniqueID());
            if (freq == null || !freq.getOwner().equals(player.getUniqueID())) {
                return;
            }
            freq.setColor(message.extra == 0 ? freq.getColor().getNext() : freq.getColor().getPrevious());
            if (message.type == Type.ITEM) {
                Mekanism.packetHandler.sendTo(PacketFrequencyItemGuiUpdate.update(message.currentHand, FrequencyType.TELEPORTER, player.getUniqueID(), freq), (ServerPlayerEntity) player);
            }
        });
        context.get().setPacketHandled(true);
    }

    public static void encode(PacketTeleporterSetColor pkt, PacketBuffer buf) {
        buf.writeEnumValue(pkt.type);
        buf.writeVarInt(pkt.extra);
        FrequencyType.TELEPORTER.getIdentitySerializer().write(buf, pkt.identity);
        if (pkt.type == Type.TILE) {
            buf.writeBlockPos(pkt.tilePosition);
        } else {
            buf.writeEnumValue(pkt.currentHand);
        }
    }

    public static PacketTeleporterSetColor decode(PacketBuffer buf) {
        Type type = buf.readEnumValue(Type.class);
        int extra = buf.readVarInt();
        FrequencyIdentity identity = FrequencyType.TELEPORTER.getIdentitySerializer().read(buf);
        BlockPos pos = type == Type.TILE ? buf.readBlockPos() : null;
        Hand hand = type == Type.ITEM ? buf.readEnumValue(Hand.class) : null;
        return new PacketTeleporterSetColor(type, extra, identity, pos, hand);
    }

    public enum Type {
        TILE,
        ITEM;
    }
}
