package mekanism.common.network;

import java.util.UUID;
import java.util.function.Supplier;
import mekanism.common.Mekanism;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent.Context;

public class PacketPlayerData {

    private final UUID uuid;
    private final boolean activeFlamethrower;
    private final boolean activeJetpack;
    private final boolean activeGasMask;

    public PacketPlayerData(UUID uuid) {
        this.uuid = uuid;
        this.activeFlamethrower = Mekanism.playerState.getActiveFlamethrowers().contains(uuid);
        this.activeJetpack = Mekanism.playerState.getActiveJetpacks().contains(uuid);
        this.activeGasMask = Mekanism.playerState.getActiveGasmasks().contains(uuid);
    }

    private PacketPlayerData(UUID uuid, boolean activeFlamethrower, boolean activeJetpack, boolean activeGasMask) {
        this.uuid = uuid;
        this.activeFlamethrower = activeFlamethrower;
        this.activeJetpack = activeJetpack;
        this.activeGasMask = activeGasMask;
    }

    public static void handle(PacketPlayerData message, Supplier<Context> context) {
        context.get().enqueueWork(() -> {
            Mekanism.playerState.setFlamethrowerState(message.uuid, message.activeFlamethrower, false);
            Mekanism.playerState.setJetpackState(message.uuid, message.activeJetpack, false);
            Mekanism.playerState.setGasmaskState(message.uuid, message.activeGasMask, false);
        });
        context.get().setPacketHandled(true);
    }

    public static void encode(PacketPlayerData pkt, PacketBuffer buf) {
        buf.writeUniqueId(pkt.uuid);
        buf.writeBoolean(pkt.activeFlamethrower);
        buf.writeBoolean(pkt.activeJetpack);
        buf.writeBoolean(pkt.activeGasMask);
    }

    public static PacketPlayerData decode(PacketBuffer buf) {
        return new PacketPlayerData(buf.readUniqueId(), buf.readBoolean(), buf.readBoolean(), buf.readBoolean());
    }
}