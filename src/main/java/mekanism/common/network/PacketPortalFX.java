package mekanism.common.network;

import java.util.Random;
import java.util.function.Supplier;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.network.NetworkEvent.Context;

public class PacketPortalFX {

    private final BlockPos pos;

    public PacketPortalFX(BlockPos pos) {
        this.pos = pos;
    }

    public static void handle(PacketPortalFX message, Supplier<Context> context) {
        PlayerEntity player = BasePacketHandler.getPlayer(context);
        if (player == null) {
            return;
        }
        context.get().enqueueWork(() -> {
            Random random = new Random();
            for (int i = 0; i < 50; i++) {
                player.world.addParticle(ParticleTypes.PORTAL, message.pos.getX() + random.nextFloat(), message.pos.getY() + random.nextFloat(),
                      message.pos.getZ() + random.nextFloat(), 0.0F, 0.0F, 0.0F);
                player.world.addParticle(ParticleTypes.PORTAL, message.pos.getX() + random.nextFloat(), message.pos.getY() + 1 + random.nextFloat(),
                      message.pos.getZ() + random.nextFloat(), 0.0F, 0.0F, 0.0F);
            }
        });
        context.get().setPacketHandled(true);
    }

    public static void encode(PacketPortalFX pkt, PacketBuffer buf) {
        buf.writeBlockPos(pkt.pos);
    }

    public static PacketPortalFX decode(PacketBuffer buf) {
        return new PacketPortalFX(buf.readBlockPos());
    }
}