package mekanism.common.network.to_client;

import mekanism.common.Mekanism;
import mekanism.common.capabilities.Capabilities;
import mekanism.common.lib.radiation.RadiationManager;
import mekanism.common.lib.radiation.RadiationManager.LevelAndMaxMagnitude;
import mekanism.common.network.IMekanismPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

public class PacketRadiationData implements IMekanismPacket {

    private final RadiationPacketType type;
    private final double radiation;
    private final double maxMagnitude;

    private PacketRadiationData(RadiationPacketType type, double radiation, double maxMagnitude) {
        this.type = type;
        this.radiation = radiation;
        this.maxMagnitude = maxMagnitude;
    }

    public static PacketRadiationData createEnvironmental(LevelAndMaxMagnitude levelAndMaxMagnitude) {
        return new PacketRadiationData(RadiationPacketType.ENVIRONMENTAL, levelAndMaxMagnitude.level(), levelAndMaxMagnitude.maxMagnitude());
    }

    public static void sync(ServerPlayer player) {
        player.getCapability(Capabilities.RADIATION_ENTITY).ifPresent(c ->
              Mekanism.packetHandler().sendTo(new PacketRadiationData(RadiationPacketType.PLAYER, c.getRadiation(), 0), player));
    }

    @Override
    public void handle(NetworkEvent.Context context) {
        if (type == RadiationPacketType.ENVIRONMENTAL) {
            RadiationManager.INSTANCE.setClientEnvironmentalRadiation(radiation, maxMagnitude);
        } else if (type == RadiationPacketType.PLAYER) {
            LocalPlayer player = Minecraft.getInstance().player;
            if (player != null) {
                player.getCapability(Capabilities.RADIATION_ENTITY).ifPresent(c -> c.set(radiation));
            }
        }
    }

    @Override
    public void encode(FriendlyByteBuf buffer) {
        buffer.writeEnum(type);
        buffer.writeDouble(radiation);
        if (type.tracksMaxMagnitude) {
            buffer.writeDouble(maxMagnitude);
        }
    }

    public static PacketRadiationData decode(FriendlyByteBuf buffer) {
        RadiationPacketType type = buffer.readEnum(RadiationPacketType.class);
        return new PacketRadiationData(type, buffer.readDouble(), type.tracksMaxMagnitude ? buffer.readDouble() : 0);
    }

    public enum RadiationPacketType {
        ENVIRONMENTAL(true),
        PLAYER(false);

        private final boolean tracksMaxMagnitude;

        RadiationPacketType(boolean tracksMaxMagnitude) {
            this.tracksMaxMagnitude = tracksMaxMagnitude;
        }
    }
}
