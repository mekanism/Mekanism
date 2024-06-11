package mekanism.common.network.to_server.filter;

import mekanism.common.Mekanism;
import mekanism.common.content.filter.BaseFilter;
import mekanism.common.content.filter.FilterManager;
import mekanism.common.content.filter.IFilter;
import mekanism.common.network.IMekanismPacket;
import mekanism.common.network.PacketUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.NotNull;

public record PacketNewFilter(BlockPos pos, IFilter<?> filter) implements IMekanismPacket {

    public static final CustomPacketPayload.Type<PacketNewFilter> TYPE = new CustomPacketPayload.Type<>(Mekanism.rl("new_filter"));
    public static final StreamCodec<RegistryFriendlyByteBuf, PacketNewFilter> STREAM_CODEC = StreamCodec.composite(
          BlockPos.STREAM_CODEC, PacketNewFilter::pos,
          BaseFilter.GENERIC_STREAM_CODEC, PacketNewFilter::filter,
          PacketNewFilter::new
    );

    @NotNull
    @Override
    public CustomPacketPayload.Type<PacketNewFilter> type() {
        return TYPE;
    }

    @Override
    public void handle(IPayloadContext context) {
        FilterManager<?> filterManager = PacketUtils.filterManager(context, pos);
        if (filterManager != null) {
            filterManager.tryAddFilter(filter, true);
        }
    }
}