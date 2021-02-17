package mekanism.common.network.to_server;

import mekanism.common.Mekanism;
import mekanism.common.content.teleporter.TeleporterFrequency;
import mekanism.common.lib.frequency.Frequency.FrequencyIdentity;
import mekanism.common.lib.frequency.FrequencyType;
import mekanism.common.network.IMekanismPacket;
import mekanism.common.network.to_client.PacketFrequencyItemGuiUpdate;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.network.NetworkEvent;

public class PacketTeleporterSetColor implements IMekanismPacket {

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

    @Override
    public void handle(NetworkEvent.Context context) {
        ServerPlayerEntity player = context.getSender();
        if (player != null) {
            TeleporterFrequency freq = FrequencyType.TELEPORTER.getFrequency(identity, player.getUniqueID());
            if (freq != null && freq.ownerMatches(player.getUniqueID())) {
                freq.setColor(extra == 0 ? freq.getColor().getNext() : freq.getColor().getPrevious());
                if (type == Type.ITEM) {
                    Mekanism.packetHandler.sendTo(PacketFrequencyItemGuiUpdate.update(currentHand, FrequencyType.TELEPORTER, player.getUniqueID(), freq), player);
                }
            }
        }
    }

    @Override
    public void encode(PacketBuffer buffer) {
        buffer.writeEnumValue(type);
        buffer.writeVarInt(extra);
        FrequencyType.TELEPORTER.getIdentitySerializer().write(buffer, identity);
        if (type == Type.TILE) {
            buffer.writeBlockPos(tilePosition);
        } else {
            buffer.writeEnumValue(currentHand);
        }
    }

    public static PacketTeleporterSetColor decode(PacketBuffer buffer) {
        Type type = buffer.readEnumValue(Type.class);
        int extra = buffer.readVarInt();
        FrequencyIdentity identity = FrequencyType.TELEPORTER.getIdentitySerializer().read(buffer);
        BlockPos pos = type == Type.TILE ? buffer.readBlockPos() : null;
        Hand hand = type == Type.ITEM ? buffer.readEnumValue(Hand.class) : null;
        return new PacketTeleporterSetColor(type, extra, identity, pos, hand);
    }

    public enum Type {
        TILE,
        ITEM;
    }
}
