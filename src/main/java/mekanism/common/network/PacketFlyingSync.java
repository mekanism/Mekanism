package mekanism.common.network;

import java.util.function.Supplier;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent.Context;

public class PacketFlyingSync {

    private final boolean allowFlying;
    private final boolean isFlying;

    public PacketFlyingSync(boolean allowFlying, boolean isFlying) {
        this.allowFlying = allowFlying;
        this.isFlying = isFlying;
    }

    public static void handle(PacketFlyingSync message, Supplier<Context> context) {
        Context ctx = context.get();
        ctx.enqueueWork(() -> {
            ClientPlayerEntity player = Minecraft.getInstance().player;
            if (player != null) {
                player.abilities.allowFlying = message.allowFlying;
                player.abilities.isFlying = message.isFlying;
            }
        });
        ctx.setPacketHandled(true);
    }

    public static void encode(PacketFlyingSync pkt, PacketBuffer buf) {
        buf.writeBoolean(pkt.allowFlying);
        buf.writeBoolean(pkt.isFlying);
    }

    public static PacketFlyingSync decode(PacketBuffer buf) {
        return new PacketFlyingSync(buf.readBoolean(), buf.readBoolean());
    }
}