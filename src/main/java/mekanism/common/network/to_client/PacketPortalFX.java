package mekanism.common.network.to_client;

import mekanism.common.network.IMekanismPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.network.PacketBuffer;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.network.NetworkEvent;

public class PacketPortalFX implements IMekanismPacket {

    private final BlockPos pos;

    public PacketPortalFX(BlockPos pos) {
        this.pos = pos;
    }

    @Override
    public void handle(NetworkEvent.Context context) {
        ClientWorld world = Minecraft.getInstance().world;
        if (world != null) {
            for (int i = 0; i < 50; i++) {
                world.addParticle(ParticleTypes.PORTAL, pos.getX() + world.rand.nextFloat(), pos.getY() + world.rand.nextFloat(),
                      pos.getZ() + world.rand.nextFloat(), 0.0F, 0.0F, 0.0F);
                world.addParticle(ParticleTypes.PORTAL, pos.getX() + world.rand.nextFloat(), pos.getY() + 1 + world.rand.nextFloat(),
                      pos.getZ() + world.rand.nextFloat(), 0.0F, 0.0F, 0.0F);
            }
        }
    }

    @Override
    public void encode(PacketBuffer buffer) {
        buffer.writeBlockPos(pos);
    }

    public static PacketPortalFX decode(PacketBuffer buffer) {
        return new PacketPortalFX(buffer.readBlockPos());
    }
}