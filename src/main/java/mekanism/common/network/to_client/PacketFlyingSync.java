package mekanism.common.network.to_client;

import mekanism.common.network.IMekanismPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

public class PacketFlyingSync implements IMekanismPacket {

    private final boolean allowFlying;
    private final boolean isFlying;

    public PacketFlyingSync(boolean allowFlying, boolean isFlying) {
        this.allowFlying = allowFlying;
        this.isFlying = isFlying;
    }

    @Override
    public void handle(NetworkEvent.Context context) {
        LocalPlayer player = Minecraft.getInstance().player;
        if (player != null) {
            player.getAbilities().mayfly = allowFlying;
            player.getAbilities().flying = isFlying;
        }
    }

    @Override
    public void encode(FriendlyByteBuf buffer) {
        buffer.writeBoolean(allowFlying);
        buffer.writeBoolean(isFlying);
    }

    public static PacketFlyingSync decode(FriendlyByteBuf buffer) {
        return new PacketFlyingSync(buffer.readBoolean(), buffer.readBoolean());
    }
}