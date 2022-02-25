package mekanism.common.network.to_server;

import mekanism.common.Mekanism;
import mekanism.common.network.IMekanismPacket;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.network.NetworkEvent;

public class PacketKey implements IMekanismPacket {

    private final int key;
    private final boolean add;

    public PacketKey(int key, boolean add) {
        this.key = key;
        this.add = add;
    }

    @Override
    public void handle(NetworkEvent.Context context) {
        Player player = context.getSender();
        if (player != null) {
            if (add) {
                Mekanism.keyMap.add(player.getUUID(), key);
            } else {
                Mekanism.keyMap.remove(player.getUUID(), key);
            }
        }
    }

    @Override
    public void encode(FriendlyByteBuf buffer) {
        buffer.writeVarInt(key);
        buffer.writeBoolean(add);
    }

    public static PacketKey decode(FriendlyByteBuf buffer) {
        return new PacketKey(buffer.readVarInt(), buffer.readBoolean());
    }
}