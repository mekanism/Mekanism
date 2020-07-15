package mekanism.common.network;

import java.util.function.Supplier;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent.Context;

public class PacketStepHeightSync {

    private final float stepHeight;

    public PacketStepHeightSync(float stepHeight) {
        this.stepHeight = stepHeight;
    }

    public static void handle(PacketStepHeightSync message, Supplier<Context> context) {
        PlayerEntity player = BasePacketHandler.getPlayer(context);
        if (player == null) {
            return;
        }
        context.get().enqueueWork(() -> player.stepHeight = message.stepHeight);
        context.get().setPacketHandled(true);
    }

    public static void encode(PacketStepHeightSync pkt, PacketBuffer buf) {
        buf.writeFloat(pkt.stepHeight);
    }

    public static PacketStepHeightSync decode(PacketBuffer buf) {
        return new PacketStepHeightSync(buf.readFloat());
    }
}