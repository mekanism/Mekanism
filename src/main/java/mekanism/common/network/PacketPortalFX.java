package mekanism.common.network;

import java.util.Random;
import java.util.function.Supplier;
import mekanism.api.Coord4D;
import mekanism.common.PacketHandler;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.particles.ParticleTypes;
import net.minecraftforge.fml.network.NetworkEvent.Context;

public class PacketPortalFX {

    private Coord4D coord4D;

    public PacketPortalFX(Coord4D coord) {
        coord4D = coord;
    }

    public static void handle(PacketPortalFX message, Supplier<Context> context) {
        PlayerEntity player = PacketHandler.getPlayer(context);
        if (player == null) {
            return;
        }
        context.get().enqueueWork(() -> {
            Random random = new Random();
            for (int i = 0; i < 50; i++) {
                player.world.addParticle(ParticleTypes.PORTAL, message.coord4D.x + random.nextFloat(), message.coord4D.y + random.nextFloat(),
                      message.coord4D.z + random.nextFloat(), 0.0F, 0.0F, 0.0F);
                player.world.addParticle(ParticleTypes.PORTAL, message.coord4D.x + random.nextFloat(), message.coord4D.y + 1 + random.nextFloat(),
                      message.coord4D.z + random.nextFloat(), 0.0F, 0.0F, 0.0F);
            }
        });
        context.get().setPacketHandled(true);
    }

    public static void encode(PacketPortalFX pkt, PacketBuffer buf) {
        pkt.coord4D.write(buf);
    }

    public static PacketPortalFX decode(PacketBuffer buf) {
        return new PacketPortalFX(Coord4D.read(buf));
    }
}