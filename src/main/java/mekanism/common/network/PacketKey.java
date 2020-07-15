package mekanism.common.network;

import java.util.function.Supplier;
import mekanism.common.Mekanism;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent.Context;

public class PacketKey {

    private final int key;
    private final boolean add;

    public PacketKey(int key, boolean add) {
        this.key = key;
        this.add = add;
    }

    public static void handle(PacketKey message, Supplier<Context> context) {
        PlayerEntity player = BasePacketHandler.getPlayer(context);
        if (player == null) {
            return;
        }
        context.get().enqueueWork(() -> {
            if (message.add) {
                Mekanism.keyMap.add(player.getUniqueID(), message.key);
            } else {
                Mekanism.keyMap.remove(player.getUniqueID(), message.key);
            }
        });
        context.get().setPacketHandled(true);
    }

    public static void encode(PacketKey pkt, PacketBuffer buf) {
        buf.writeVarInt(pkt.key);
        buf.writeBoolean(pkt.add);
    }

    public static PacketKey decode(PacketBuffer buf) {
        return new PacketKey(buf.readVarInt(), buf.readBoolean());
    }
}