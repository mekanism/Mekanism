package mekanism.common.network;

import java.util.function.Supplier;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent.Context;

public class PacketStepHeightSync {

    private final float stepHeight;

    public PacketStepHeightSync(float stepHeight) {
        this.stepHeight = stepHeight;
    }

    public static void handle(PacketStepHeightSync message, Supplier<Context> context) {
        Context ctx = context.get();
        ctx.enqueueWork(() -> {
            ClientPlayerEntity player = Minecraft.getInstance().player;
            if (player != null) {
                player.stepHeight = message.stepHeight;
            }
        });
        ctx.setPacketHandled(true);
    }

    public static void encode(PacketStepHeightSync pkt, PacketBuffer buf) {
        buf.writeFloat(pkt.stepHeight);
    }

    public static PacketStepHeightSync decode(PacketBuffer buf) {
        return new PacketStepHeightSync(buf.readFloat());
    }
}