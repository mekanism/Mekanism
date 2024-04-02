package mekanism.common.network.to_client;

import mekanism.common.Mekanism;
import mekanism.common.network.IMekanismPacket;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.network.handling.PlayPayloadContext;
import org.jetbrains.annotations.NotNull;

public record PacketSetDeltaMovement(Vec3 deltaMovement) implements IMekanismPacket<PlayPayloadContext> {

    public static final ResourceLocation ID = Mekanism.rl("set_delta_movement");

    public PacketSetDeltaMovement(FriendlyByteBuf buffer) {
        this(buffer.readVec3());
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
            player.lerpMotion(deltaMovement.x, deltaMovement.y, deltaMovement.z);
        }
    }

    @Override
    public void write(@NotNull FriendlyByteBuf buffer) {
        buffer.writeVec3(deltaMovement);
    }
}