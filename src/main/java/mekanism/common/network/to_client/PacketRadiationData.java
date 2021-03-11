package mekanism.common.network.to_client;

import mekanism.common.Mekanism;
import mekanism.common.capabilities.Capabilities;
import mekanism.common.lib.radiation.RadiationManager.RadiationScale;
import mekanism.common.network.IMekanismPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

public class PacketRadiationData implements IMekanismPacket {

    private final RadiationPacketType type;
    private RadiationScale scale;
    private double radiation;

    private PacketRadiationData(RadiationScale scale) {
        this.type = RadiationPacketType.SCALE;
        this.scale = scale;
    }

    private PacketRadiationData(double radiation) {
        this.type = RadiationPacketType.PLAYER;
        this.radiation = radiation;
    }

    public static PacketRadiationData create(RadiationScale scale) {
        return new PacketRadiationData(scale);
    }

    public static void sync(ServerPlayerEntity player) {
        player.getCapability(Capabilities.RADIATION_ENTITY_CAPABILITY).ifPresent(c -> Mekanism.packetHandler.sendTo(new PacketRadiationData(c.getRadiation()), player));
    }

    @Override
    public void handle(NetworkEvent.Context context) {
        if (type == RadiationPacketType.SCALE) {
            Mekanism.radiationManager.setClientScale(scale);
        } else if (type == RadiationPacketType.PLAYER) {
            ClientPlayerEntity player = Minecraft.getInstance().player;
            if (player != null) {
                player.getCapability(Capabilities.RADIATION_ENTITY_CAPABILITY).ifPresent(c -> c.set(radiation));
            }
        }
    }

    @Override
    public void encode(PacketBuffer buffer) {
        buffer.writeEnum(type);
        if (type == RadiationPacketType.SCALE) {
            buffer.writeEnum(scale);
        } else if (type == RadiationPacketType.PLAYER) {
            buffer.writeDouble(radiation);
        }
    }

    public static PacketRadiationData decode(PacketBuffer buffer) {
        RadiationPacketType type = buffer.readEnum(RadiationPacketType.class);
        if (type == RadiationPacketType.SCALE) {
            return new PacketRadiationData(buffer.readEnum(RadiationScale.class));
        } else if (type == RadiationPacketType.PLAYER) {
            return new PacketRadiationData(buffer.readDouble());
        }
        return null;
    }

    public enum RadiationPacketType {
        SCALE,
        PLAYER
    }
}
