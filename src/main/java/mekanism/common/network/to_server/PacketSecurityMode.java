package mekanism.common.network.to_server;

import mekanism.api.security.IItemSecurityUtils;
import mekanism.api.security.ISecurityObject;
import mekanism.common.Mekanism;
import mekanism.common.lib.security.SecurityUtils;
import mekanism.common.network.IMekanismPacket;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.neoforged.neoforge.network.handling.PlayPayloadContext;
import org.jetbrains.annotations.NotNull;

public record PacketSecurityMode(InteractionHand currentHand, boolean increment) implements IMekanismPacket<PlayPayloadContext> {

    public static final ResourceLocation ID = Mekanism.rl("security_mode");

    public PacketSecurityMode(FriendlyByteBuf buffer) {
        this(buffer.readEnum(InteractionHand.class), buffer.readBoolean());
    }

    @NotNull
    @Override
    public ResourceLocation id() {
        return ID;
    }

    @Override
    public void handle(PlayPayloadContext context) {
        context.player().ifPresent(player -> {
            ISecurityObject securityObject = IItemSecurityUtils.INSTANCE.securityCapability(player.getItemInHand(currentHand));
            if (increment) {
                SecurityUtils.get().incrementSecurityMode(player, securityObject);
            } else {
                SecurityUtils.get().decrementSecurityMode(player, securityObject);
            }
        });
    }

    @Override
    public void write(@NotNull FriendlyByteBuf buffer) {
        buffer.writeEnum(currentHand);
        buffer.writeBoolean(increment);
    }
}