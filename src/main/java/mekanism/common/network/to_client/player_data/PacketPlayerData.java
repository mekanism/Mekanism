package mekanism.common.network.to_client.player_data;

import java.util.UUID;
import mekanism.common.Mekanism;
import mekanism.common.network.IMekanismPacket;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.PlayPayloadContext;
import org.jetbrains.annotations.NotNull;

public record PacketPlayerData(UUID uuid, boolean activeJetpack, boolean activeScubaMask,
                               boolean activeModulator) implements IMekanismPacket<PlayPayloadContext> {

    public static final ResourceLocation ID = Mekanism.rl("player_data");

    public PacketPlayerData(FriendlyByteBuf buffer) {
        this(buffer.readUUID(), buffer.readBoolean(), buffer.readBoolean(), buffer.readBoolean());
    }

    public PacketPlayerData(UUID uuid) {
        this(uuid,
              Mekanism.playerState.getActiveJetpacks().contains(uuid),
              Mekanism.playerState.getActiveScubaMasks().contains(uuid),
              Mekanism.playerState.getActiveGravitationalModulators().contains(uuid)
        );
    }

    @NotNull
    @Override
    public ResourceLocation id() {
        return ID;
    }

    @Override
    public void handle(PlayPayloadContext context) {
        Mekanism.playerState.setJetpackState(uuid, activeJetpack, false);
        Mekanism.playerState.setScubaMaskState(uuid, activeScubaMask, false);
        Mekanism.playerState.setGravitationalModulationState(uuid, activeModulator, false);
    }

    @Override
    public void write(@NotNull FriendlyByteBuf buffer) {
        buffer.writeUUID(uuid);
        buffer.writeBoolean(activeJetpack);
        buffer.writeBoolean(activeScubaMask);
        buffer.writeBoolean(activeModulator);
    }
}