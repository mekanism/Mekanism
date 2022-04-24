package mekanism.common.network.to_client;

import mekanism.common.Mekanism;
import mekanism.common.capabilities.Capabilities;
import mekanism.common.lib.radiation.RadiationManager;
import mekanism.common.network.IMekanismPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

public class PacketRadiationData implements IMekanismPacket {

    private final RadiationPacketType type;
    private final double radiation;

    private PacketRadiationData(RadiationPacketType type, double radiation) {
        this.type = type;
        this.radiation = radiation;
    }

    public static PacketRadiationData createEnvironmental(double radiation) {
        return new PacketRadiationData(RadiationPacketType.ENVIRONMENTAL, radiation);
    }

    public static void sync(ServerPlayer player) {
        player.getCapability(Capabilities.RADIATION_ENTITY_CAPABILITY).ifPresent(c ->
              Mekanism.packetHandler().sendTo(new PacketRadiationData(RadiationPacketType.PLAYER, c.getRadiation()), player));
    }

    @Override
    public void handle(NetworkEvent.Context context) {
        if (type == RadiationPacketType.ENVIRONMENTAL) {
            RadiationManager.INSTANCE.setClientEnvironmentalRadiation(radiation);
        } else if (type == RadiationPacketType.PLAYER) {
            LocalPlayer player = Minecraft.getInstance().player;
            if (player != null) {
                player.getCapability(Capabilities.RADIATION_ENTITY_CAPABILITY).ifPresent(c -> c.set(radiation));
            }
        }
    }

    @Override
    public void encode(FriendlyByteBuf buffer) {
        buffer.writeEnum(type);
        buffer.writeDouble(radiation);
    }

    public static PacketRadiationData decode(FriendlyByteBuf buffer) {
        return new PacketRadiationData(buffer.readEnum(RadiationPacketType.class), buffer.readDouble());
    }

    public enum RadiationPacketType {
        ENVIRONMENTAL,
        PLAYER
    }
}
