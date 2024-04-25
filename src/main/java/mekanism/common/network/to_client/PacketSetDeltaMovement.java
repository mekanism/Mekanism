package mekanism.common.network.to_client;

import io.netty.buffer.ByteBuf;
import mekanism.common.Mekanism;
import mekanism.common.network.IMekanismPacket;
import mekanism.common.network.PacketUtils;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.NotNull;

public record PacketSetDeltaMovement(Vec3 deltaMovement) implements IMekanismPacket {

    public static final CustomPacketPayload.Type<PacketSetDeltaMovement> TYPE = new CustomPacketPayload.Type<>(Mekanism.rl("set_delta_movement"));
    public static final StreamCodec<ByteBuf, PacketSetDeltaMovement> STREAM_CODEC = PacketUtils.VEC3_STREAM_CODEC.map(
          PacketSetDeltaMovement::new, PacketSetDeltaMovement::deltaMovement
    );

    @NotNull
    @Override
    public CustomPacketPayload.Type<PacketSetDeltaMovement> type() {
        return TYPE;
    }

    @Override
    public void handle(IPayloadContext context) {
        context.player().lerpMotion(deltaMovement.x, deltaMovement.y, deltaMovement.z);
    }
}