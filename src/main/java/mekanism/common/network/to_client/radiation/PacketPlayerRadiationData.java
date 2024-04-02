package mekanism.common.network.to_client.radiation;

import mekanism.common.Mekanism;
import mekanism.common.network.IMekanismPacket;
import mekanism.common.registries.MekanismAttachmentTypes;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.handling.PlayPayloadContext;
import org.jetbrains.annotations.NotNull;

public record PacketPlayerRadiationData(double radiation) implements IMekanismPacket<PlayPayloadContext> {

    public static final ResourceLocation ID = Mekanism.rl("player_radiation");

    public PacketPlayerRadiationData(FriendlyByteBuf buffer) {
        this(buffer.readDouble());
    }

    public PacketPlayerRadiationData(Player player) {
        this(player.getData(MekanismAttachmentTypes.RADIATION));
    }

    @NotNull
    @Override
    public ResourceLocation id() {
        return ID;
    }

    @Override
    public void handle(PlayPayloadContext context) {
        //noinspection SimplifyOptionalCallChains - Capturing lambda
        Player player = context.player().orElse(null);
        if (player != null) {
            player.setData(MekanismAttachmentTypes.RADIATION, radiation);
        }
    }

    @Override
    public void write(@NotNull FriendlyByteBuf buffer) {
        buffer.writeDouble(radiation);
    }
}
