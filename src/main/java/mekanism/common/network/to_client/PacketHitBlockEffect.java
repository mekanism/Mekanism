package mekanism.common.network.to_client;

import mekanism.common.Mekanism;
import mekanism.common.network.IMekanismPacket;
import mekanism.common.network.PacketUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.phys.BlockHitResult;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record PacketHitBlockEffect(BlockHitResult result) implements IMekanismPacket {

    public static final CustomPacketPayload.Type<PacketHitBlockEffect> TYPE = new CustomPacketPayload.Type<>(Mekanism.rl("hit_block"));
    public static final StreamCodec<FriendlyByteBuf, PacketHitBlockEffect> STREAM_CODEC = PacketUtils.BLOCK_HIT_RESULT_STREAM_CODEC.map(
          PacketHitBlockEffect::new, PacketHitBlockEffect::result
    );

    @Override
    public CustomPacketPayload.Type<PacketHitBlockEffect> type() {
        return TYPE;
    }

    @Override
    public void handle(IPayloadContext context) {
        //TODO - 26.1: Can we grab the level from the context, or would that require a cast that then might crash on the server?
        Minecraft.getInstance().level.addBreakingBlockEffect(result.getBlockPos(), result.getDirection(), result);
    }
}