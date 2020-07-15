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
    private final boolean activeScubaMask;
    private final boolean activeModulator;

    public PacketPlayerData(UUID uuid) {
        this.uuid = uuid;
        this.activeFlamethrower = Mekanism.playerState.getActiveFlamethrowers().contains(uuid);
        this.activeJetpack = Mekanism.playerState.getActiveJetpacks().contains(uuid);
        this.activeScubaMask = Mekanism.playerState.getActiveScubaMasks().contains(uuid);
        this.activeModulator = Mekanism.playerState.getActiveGravitationalModulators().contains(uuid);
    }

    private PacketPlayerData(UUID uuid, boolean activeFlamethrower, boolean activeJetpack, boolean activeGasMask, boolean activeModulator) {
        this.uuid = uuid;
        this.activeFlamethrower = activeFlamethrower;
        this.activeJetpack = activeJetpack;
        this.activeScubaMask = activeGasMask;
        this.activeModulator = activeModulator;
    }

    public static void handle(PacketPlayerData message, Supplier<Context> context) {
        context.get().enqueueWork(() -> {
            Mekanism.playerState.setFlamethrowerState(message.uuid, message.activeFlamethrower, false);
            Mekanism.playerState.setJetpackState(message.uuid, message.activeJetpack, false);
            Mekanism.playerState.setScubaMaskState(message.uuid, message.activeScubaMask, false);
            Mekanism.playerState.setGravitationalModulationState(message.uuid, message.activeModulator, false);
        });
        context.get().setPacketHandled(true);
    }

    public static void encode(PacketPlayerData pkt, PacketBuffer buf) {
        buf.writeUniqueId(pkt.uuid);
        buf.writeBoolean(pkt.activeFlamethrower);
        buf.writeBoolean(pkt.activeJetpack);
        buf.writeBoolean(pkt.activeScubaMask);
        buf.writeBoolean(pkt.activeModulator);
    }

    public static PacketPlayerData decode(PacketBuffer buf) {
        return new PacketPlayerData(buf.readUniqueId(), buf.readBoolean(), buf.readBoolean(), buf.readBoolean(), buf.readBoolean());
    }
}