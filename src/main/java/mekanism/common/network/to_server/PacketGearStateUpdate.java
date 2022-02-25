package mekanism.common.network.to_server;

import java.util.UUID;
import mekanism.common.Mekanism;
import mekanism.common.network.IMekanismPacket;
import mekanism.common.network.to_client.PacketPlayerData;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.network.NetworkEvent;

public class PacketGearStateUpdate implements IMekanismPacket {

    private final GearType gearType;
    private final boolean state;
    private final UUID uuid;

    //Client to server AND sort of server to client, as the server then reroutes it as PacketPlayerData to clients
    public PacketGearStateUpdate(GearType gearType, UUID uuid, boolean state) {
        this.gearType = gearType;
        this.uuid = uuid;
        this.state = state;
    }

    @Override
    public void handle(NetworkEvent.Context context) {
        if (gearType == GearType.FLAMETHROWER) {
            Mekanism.playerState.setFlamethrowerState(uuid, state, false);
        } else if (gearType == GearType.JETPACK) {
            Mekanism.playerState.setJetpackState(uuid, state, false);
        } else if (gearType == GearType.SCUBA_MASK) {
            Mekanism.playerState.setScubaMaskState(uuid, state, false);
        } else if (gearType == GearType.GRAVITATIONAL_MODULATOR) {
            Mekanism.playerState.setGravitationalModulationState(uuid, state, false);
        }
        //If we got this packet on the server, inform all clients tracking the changed player
        Player player = context.getSender();
        if (player != null) {
            //Note: We just resend all the data for the updated player as the packet size is about the same
            // and this allows us to separate the packet into a server to client and client to server packet
            Mekanism.packetHandler().sendToAllTracking(new PacketPlayerData(uuid), player);
        }
    }

    @Override
    public void encode(FriendlyByteBuf buffer) {
        buffer.writeEnum(gearType);
        buffer.writeUUID(uuid);
        buffer.writeBoolean(state);
    }

    public static PacketGearStateUpdate decode(FriendlyByteBuf buffer) {
        return new PacketGearStateUpdate(buffer.readEnum(GearType.class), buffer.readUUID(), buffer.readBoolean());
    }

    public enum GearType {
        FLAMETHROWER,
        JETPACK,
        SCUBA_MASK,
        GRAVITATIONAL_MODULATOR
    }
}