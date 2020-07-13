package mekanism.common.network;

import java.util.function.Supplier;
import mekanism.common.content.teleporter.TeleporterFrequency;
import mekanism.common.lib.frequency.Frequency.FrequencyIdentity;
import mekanism.common.lib.frequency.FrequencyType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.network.NetworkEvent.Context;

public class PacketTeleporterSetColor {

    private final int extra;
    private final FrequencyIdentity identity;
    private final BlockPos tilePosition;

    private PacketTeleporterSetColor(int extra, FrequencyIdentity identity, BlockPos tilePosition) {
        this.extra = extra;
        this.identity = identity;
        this.tilePosition = tilePosition;
    }

    public static PacketTeleporterSetColor create(BlockPos tilePosition, TeleporterFrequency freq, int extra) {
        return new PacketTeleporterSetColor(extra, freq.getIdentity(), tilePosition);
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
        });
        context.get().setPacketHandled(true);
    }

    public static void encode(PacketTeleporterSetColor pkt, PacketBuffer buf) {
        buf.writeVarInt(pkt.extra);
        FrequencyType.TELEPORTER.getIdentitySerializer().write(buf, pkt.identity);
        buf.writeBlockPos(pkt.tilePosition);
    }

    public static PacketTeleporterSetColor decode(PacketBuffer buf) {
        int extra = buf.readVarInt();
        FrequencyIdentity identity = FrequencyType.TELEPORTER.getIdentitySerializer().read(buf);
        BlockPos pos = buf.readBlockPos();
        return new PacketTeleporterSetColor(extra, identity, pos);
    }

    public enum Type {
        TILE,
        ITEM;
    }
}
