package mekanism.common.network.to_client;

import io.netty.buffer.ByteBuf;
import mekanism.common.Mekanism;
import mekanism.common.network.IMekanismPacket;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.NotNull;

public record PacketPortalFX(BlockPos pos, Direction direction) implements IMekanismPacket {

    public static final CustomPacketPayload.Type<PacketPortalFX> TYPE = new CustomPacketPayload.Type<>(Mekanism.rl("portal_fx"));
    public static final StreamCodec<ByteBuf, PacketPortalFX> STREAM_CODEC = StreamCodec.composite(
          BlockPos.STREAM_CODEC, PacketPortalFX::pos,
          Direction.STREAM_CODEC, PacketPortalFX::direction,
          PacketPortalFX::new
    );

    public PacketPortalFX(BlockPos pos) {
        this(pos, Direction.UP);
    }

    @NotNull
    @Override
    public CustomPacketPayload.Type<PacketPortalFX> type() {
        return TYPE;
    }

    @Override
    public void handle(IPayloadContext context) {
        Level world = context.player().level();
        BlockPos secondPos = pos.relative(direction);
        for (int i = 0; i < 50; i++) {
            world.addParticle(ParticleTypes.PORTAL, pos.getX() + world.random.nextFloat(), pos.getY() + world.random.nextFloat(),
                  pos.getZ() + world.random.nextFloat(), 0.0F, 0.0F, 0.0F);
            world.addParticle(ParticleTypes.PORTAL, secondPos.getX() + world.random.nextFloat(), secondPos.getY() + world.random.nextFloat(),
                  secondPos.getZ() + world.random.nextFloat(), 0.0F, 0.0F, 0.0F);
        }
    }
}