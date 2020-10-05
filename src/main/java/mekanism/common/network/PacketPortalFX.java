package mekanism.common.network;

import java.util.function.Supplier;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
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
            World world = player.world;
            BlockPos pos = message.pos;
            for (int i = 0; i < 50; i++) {
                world.addParticle(ParticleTypes.PORTAL, pos.getX() + world.rand.nextFloat(), pos.getY() + world.rand.nextFloat(),
                      pos.getZ() + world.rand.nextFloat(), 0.0F, 0.0F, 0.0F);
                world.addParticle(ParticleTypes.PORTAL, pos.getX() + world.rand.nextFloat(), pos.getY() + 1 + world.rand.nextFloat(),
                      pos.getZ() + world.rand.nextFloat(), 0.0F, 0.0F, 0.0F);
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