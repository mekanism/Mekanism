package mekanism.common.network.to_client;

import java.util.UUID;
import mekanism.common.Mekanism;
import mekanism.common.network.IMekanismPacket;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

public class PacketPlayerData implements IMekanismPacket {

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

    @Override
    public void handle(NetworkEvent.Context context) {
        Mekanism.playerState.setFlamethrowerState(uuid, activeFlamethrower, false);
        Mekanism.playerState.setJetpackState(uuid, activeJetpack, false);
        Mekanism.playerState.setScubaMaskState(uuid, activeScubaMask, false);
        Mekanism.playerState.setGravitationalModulationState(uuid, activeModulator, false);
    }

    @Override
    public void encode(PacketBuffer buffer) {
        buffer.writeUUID(uuid);
        buffer.writeBoolean(activeFlamethrower);
        buffer.writeBoolean(activeJetpack);
        buffer.writeBoolean(activeScubaMask);
        buffer.writeBoolean(activeModulator);
    }

    public static PacketPlayerData decode(PacketBuffer buffer) {
        return new PacketPlayerData(buffer.readUUID(), buffer.readBoolean(), buffer.readBoolean(), buffer.readBoolean(), buffer.readBoolean());
    }
}