package mekanism.common.network;

import java.util.function.Supplier;
import mekanism.common.Mekanism;
import mekanism.common.radiation.RadiationManager.RadiationScale;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent.Context;

public class PacketRadiationData {
    private RadiationScale scale;

    public PacketRadiationData(RadiationScale scale) {
        this.scale = scale;
    }

    public static void handle(PacketRadiationData message, Supplier<Context> context) {
        // Queue up the processing on the central thread
        PlayerEntity player = PacketHandler.getPlayer(context);
        if (player == null) {
            return;
        }
        context.get().enqueueWork(() -> {
            Mekanism.radiationManager.setClientScale(message.scale);
        });
        context.get().setPacketHandled(true);
    }

    public static void encode(PacketRadiationData pkt, PacketBuffer buf) {
        buf.writeInt(pkt.scale.ordinal());
    }

    public static PacketRadiationData decode(PacketBuffer buf) {
        return new PacketRadiationData(RadiationScale.values()[buf.readInt()]);
    }
}
