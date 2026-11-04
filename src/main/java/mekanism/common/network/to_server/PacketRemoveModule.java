package mekanism.common.network.to_server;

import mekanism.api.MekanismRegistries;
import mekanism.api.gear.ModuleData;
import mekanism.common.Mekanism;
import mekanism.common.network.IMekanismPacket;
import mekanism.common.network.PacketUtils;
import mekanism.common.tile.TileEntityModificationStation;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record PacketRemoveModule(BlockPos pos, Holder<ModuleData<?>> moduleType, boolean removeAll) implements IMekanismPacket {

    public static final CustomPacketPayload.Type<PacketRemoveModule> TYPE = new CustomPacketPayload.Type<>(Mekanism.rl("remove_module"));
    public static final StreamCodec<RegistryFriendlyByteBuf, PacketRemoveModule> STREAM_CODEC = StreamCodec.composite(
          BlockPos.STREAM_CODEC, PacketRemoveModule::pos,
          ByteBufCodecs.holderRegistry(MekanismRegistries.Keys.MODULES), PacketRemoveModule::moduleType,
          ByteBufCodecs.BOOL, PacketRemoveModule::removeAll,
          PacketRemoveModule::new
    );

    @Override
    public CustomPacketPayload.Type<PacketRemoveModule> type() {
        return TYPE;
    }

    @Override
    public void handle(IPayloadContext context) {
        Player player = context.player();
        if (PacketUtils.blockEntity(context, pos) instanceof TileEntityModificationStation tile) {
            tile.removeModule(player, moduleType, removeAll);
        }
    }
}
