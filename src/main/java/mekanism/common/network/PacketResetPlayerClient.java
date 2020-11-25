package mekanism.common.network;

import java.util.UUID;
import java.util.function.Supplier;
import mekanism.common.Mekanism;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent.Context;

public class PacketResetPlayerClient {

    private final UUID uuid;

    public PacketResetPlayerClient(UUID uuid) {
        this.uuid = uuid;
    }

    public static void handle(PacketResetPlayerClient message, Supplier<Context> context) {
        Context ctx = context.get();
        ctx.enqueueWork(() -> Mekanism.playerState.clearPlayer(message.uuid, true));
        ctx.setPacketHandled(true);
    }

    public static void encode(PacketResetPlayerClient pkt, PacketBuffer buf) {
        buf.writeUniqueId(pkt.uuid);
    }

    public static PacketResetPlayerClient decode(PacketBuffer buf) {
        return new PacketResetPlayerClient(buf.readUniqueId());
    }
}
