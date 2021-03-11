package mekanism.common.network.to_client;

import java.util.UUID;
import mekanism.common.Mekanism;
import mekanism.common.network.IMekanismPacket;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

public class PacketResetPlayerClient implements IMekanismPacket {

    private final UUID uuid;

    public PacketResetPlayerClient(UUID uuid) {
        this.uuid = uuid;
    }

    @Override
    public void handle(NetworkEvent.Context context) {
        Mekanism.playerState.clearPlayer(uuid, true);
    }

    @Override
    public void encode(PacketBuffer buffer) {
        buffer.writeUUID(uuid);
    }

    public static PacketResetPlayerClient decode(PacketBuffer buffer) {
        return new PacketResetPlayerClient(buffer.readUUID());
    }
}
