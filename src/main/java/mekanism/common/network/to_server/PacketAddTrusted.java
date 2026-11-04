package mekanism.common.network.to_server;

import io.netty.buffer.ByteBuf;
import mekanism.api.security.IBlockSecurityUtils;
import mekanism.common.Mekanism;
import mekanism.common.network.IMekanismPacket;
import mekanism.common.network.PacketUtils;
import mekanism.common.tile.TileEntitySecurityDesk;
import mekanism.common.util.text.InputValidator;
import net.minecraft.SharedConstants;
import net.minecraft.core.BlockPos;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record PacketAddTrusted(BlockPos pos, String name) implements IMekanismPacket {

    public static final CustomPacketPayload.Type<PacketAddTrusted> TYPE = new CustomPacketPayload.Type<>(Mekanism.rl("add_trusted"));
    public static final StreamCodec<ByteBuf, PacketAddTrusted> STREAM_CODEC = StreamCodec.composite(
          BlockPos.STREAM_CODEC, PacketAddTrusted::pos,
          ByteBufCodecs.stringUtf8(SharedConstants.MAX_PLAYER_NAME_LENGTH), PacketAddTrusted::name,
          PacketAddTrusted::new
    );

    @Override
    public CustomPacketPayload.Type<PacketAddTrusted> type() {
        return TYPE;
    }

    @Override
    public void handle(IPayloadContext context) {
        Player player = context.player();
        if (!name.isEmpty() && InputValidator.test(name, InputValidator.USERNAME) && PacketUtils.blockEntity(context, pos) instanceof TileEntitySecurityDesk desk
            && IBlockSecurityUtils.INSTANCE.canAccess(player, player.level(), pos, desk)) {
            desk.addTrusted(name);
        }
    }
}