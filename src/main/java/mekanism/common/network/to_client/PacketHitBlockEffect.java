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
import org.jetbrains.annotations.NotNull;

public record PacketHitBlockEffect(BlockHitResult result) implements IMekanismPacket {

    public static final CustomPacketPayload.Type<PacketHitBlockEffect> TYPE = new CustomPacketPayload.Type<>(Mekanism.rl("hit_block"));
    public static final StreamCodec<FriendlyByteBuf, PacketHitBlockEffect> STREAM_CODEC = PacketUtils.BLOCK_HIT_RESULT_STREAM_CODEC.map(
          PacketHitBlockEffect::new, PacketHitBlockEffect::result
    );

    @NotNull
    @Override
    public CustomPacketPayload.Type<PacketHitBlockEffect> type() {
        return TYPE;
    }

    @Override
    public void handle(IPayloadContext context) {
        Minecraft.getInstance().particleEngine.addBlockHitEffects(result.getBlockPos(), result);
    }
}