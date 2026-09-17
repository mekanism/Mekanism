package mekanism.common.network.to_client;

import mekanism.common.Mekanism;
import mekanism.common.network.IMekanismPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record PacketHitBlockEffect(BlockPos pos, Direction direction, boolean playSound) implements IMekanismPacket {

    public static final CustomPacketPayload.Type<PacketHitBlockEffect> TYPE = new CustomPacketPayload.Type<>(Mekanism.rl("hit_block"));
    public static final StreamCodec<FriendlyByteBuf, PacketHitBlockEffect> STREAM_CODEC = StreamCodec.composite(
          BlockPos.STREAM_CODEC, PacketHitBlockEffect::pos,
          Direction.STREAM_CODEC, PacketHitBlockEffect::direction,
          ByteBufCodecs.BOOL, PacketHitBlockEffect::playSound,
          PacketHitBlockEffect::new
    );

    @Override
    public CustomPacketPayload.Type<PacketHitBlockEffect> type() {
        return TYPE;
    }

    @Override
    public void handle(IPayloadContext context) {
        //TODO - 26.2: Can we grab the level from the context, or would that require a cast that then might crash on the server?
        Minecraft.getInstance().level.addBreakingBlockEffects(pos, direction, playSound);
    }
}