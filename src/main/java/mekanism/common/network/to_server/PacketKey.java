package mekanism.common.network.to_server;

import mekanism.common.Mekanism;
import mekanism.common.network.IMekanismPacket;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

public class PacketKey implements IMekanismPacket {

    private final int key;
    private final boolean add;

    public PacketKey(int key, boolean add) {
        this.key = key;
        this.add = add;
    }

    @Override
    public void handle(NetworkEvent.Context context) {
        PlayerEntity player = context.getSender();
        if (player != null) {
            if (add) {
                Mekanism.keyMap.add(player.getUUID(), key);
            } else {
                Mekanism.keyMap.remove(player.getUUID(), key);
            }
        }
    }

    @Override
    public void encode(PacketBuffer buffer) {
        buffer.writeVarInt(key);
        buffer.writeBoolean(add);
    }

    public static PacketKey decode(PacketBuffer buffer) {
        return new PacketKey(buffer.readVarInt(), buffer.readBoolean());
    }
}