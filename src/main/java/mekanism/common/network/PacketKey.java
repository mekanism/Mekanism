package mekanism.common.network;

import java.util.function.Supplier;
import mekanism.common.Mekanism;
import mekanism.common.PacketHandler;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent.Context;

public class PacketKey {

    public int key;
    public boolean add;

    public PacketKey(int k, boolean a) {
        key = k;
        add = a;
    }

    public static void handle(PacketKey message, Supplier<Context> context) {
        if (message.add) {
            Mekanism.keyMap.add(PacketHandler.getPlayer(context), message.key);
        } else {
            Mekanism.keyMap.remove(PacketHandler.getPlayer(context), message.key);
        }
    }

    public static void encode(PacketKey pkt, PacketBuffer buf) {
        buf.writeInt(pkt.key);
        buf.writeBoolean(pkt.add);
    }

    public static PacketKey decode(PacketBuffer buf) {
        return new PacketKey(buf.readInt(), buf.readBoolean());
    }
}