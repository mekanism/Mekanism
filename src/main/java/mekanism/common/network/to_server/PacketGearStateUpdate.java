package mekanism.common.network.to_server;

import java.util.UUID;
import mekanism.common.Mekanism;
import mekanism.common.network.IMekanismPacket;
import mekanism.common.network.PacketUtils;
import mekanism.common.network.to_client.player_data.PacketPlayerData;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.handling.PlayPayloadContext;
import org.jetbrains.annotations.NotNull;

public class PacketGearStateUpdate implements IMekanismPacket<PlayPayloadContext> {

    public static final ResourceLocation ID = Mekanism.rl("update_gear");

    private final GearType gearType;
    private final boolean state;
    private final UUID uuid;

    public PacketGearStateUpdate(FriendlyByteBuf buffer) {
        this(buffer.readEnum(GearType.class), buffer.readUUID(), buffer.readBoolean());
    }

    //Client to server AND sort of server to client, as the server then reroutes it as PacketPlayerData to clients
    public PacketGearStateUpdate(GearType gearType, UUID uuid, boolean state) {
        this.gearType = gearType;
        this.uuid = uuid;
        this.state = state;
    }

    @NotNull
    @Override
    public ResourceLocation id() {
        return ID;
    }

    @Override
    public void handle(PlayPayloadContext context) {
        switch (gearType) {
            case JETPACK -> Mekanism.playerState.setJetpackState(uuid, state, false);
            case SCUBA_MASK -> Mekanism.playerState.setScubaMaskState(uuid, state, false);
            case GRAVITATIONAL_MODULATOR -> Mekanism.playerState.setGravitationalModulationState(uuid, state, false);
        }
        //Inform all clients tracking the changed player
        //Note: We just resend all the data for the updated player as the packet size is about the same
        // and this allows us to separate the packet into a server to client and client to server packet
        //noinspection SimplifyOptionalCallChains - Capturing lambda
        Player player = context.player().orElse(null);
        if (player != null) {
            PacketUtils.sendToAllTracking(new PacketPlayerData(uuid), player);
        }
    }

    @Override
    public void write(@NotNull FriendlyByteBuf buffer) {
        buffer.writeEnum(gearType);
        buffer.writeUUID(uuid);
        buffer.writeBoolean(state);
    }

    public enum GearType {
        JETPACK,
        SCUBA_MASK,
        GRAVITATIONAL_MODULATOR
    }
}