package mekanism.common.network.to_client;

import mekanism.common.network.IMekanismPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

public class PacketFlyingSync implements IMekanismPacket {

    private final boolean allowFlying;
    private final boolean isFlying;

    public PacketFlyingSync(boolean allowFlying, boolean isFlying) {
        this.allowFlying = allowFlying;
        this.isFlying = isFlying;
    }

    @Override
    public void handle(NetworkEvent.Context context) {
        ClientPlayerEntity player = Minecraft.getInstance().player;
        if (player != null) {
            player.abilities.mayfly = allowFlying;
            player.abilities.flying = isFlying;
        }
    }

    @Override
    public void encode(PacketBuffer buffer) {
        buffer.writeBoolean(allowFlying);
        buffer.writeBoolean(isFlying);
    }

    public static PacketFlyingSync decode(PacketBuffer buffer) {
        return new PacketFlyingSync(buffer.readBoolean(), buffer.readBoolean());
    }
}