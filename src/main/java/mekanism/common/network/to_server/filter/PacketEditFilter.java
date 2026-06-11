package mekanism.common.network.to_server.filter;

import io.netty.handler.codec.DecoderException;
import java.util.Optional;
import mekanism.common.Mekanism;
import mekanism.common.content.filter.BaseFilter;
import mekanism.common.content.filter.FilterManager;
import mekanism.common.content.filter.IFilter;
import mekanism.common.network.IMekanismPacket;
import mekanism.common.network.PacketUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jspecify.annotations.Nullable;

public record PacketEditFilter<FILTER extends IFilter<FILTER>>(BlockPos pos, FILTER filter, @Nullable FILTER edited) implements IMekanismPacket {

    public static final CustomPacketPayload.Type<PacketEditFilter<?>> TYPE = new CustomPacketPayload.Type<>(Mekanism.rl("edit_filter"));
    public static final StreamCodec<RegistryFriendlyByteBuf, PacketEditFilter<?>> STREAM_CODEC = StreamCodec.composite(
          BlockPos.STREAM_CODEC, PacketEditFilter::pos,
          BaseFilter.GENERIC_STREAM_CODEC, PacketEditFilter::filter,
          ByteBufCodecs.optional(BaseFilter.GENERIC_STREAM_CODEC), packet -> Optional.ofNullable(packet.edited()),
          (pos, filter, edited) -> unchecked(pos, filter, edited.orElse(null))
    );

    @SuppressWarnings("unchecked")
    private static <FILTER extends IFilter<FILTER>> PacketEditFilter<FILTER> unchecked(BlockPos pos, IFilter<?> filter, @Nullable IFilter<?> edited) {
        if (edited != null && edited.getFilterType() != filter.getFilterType()) {
            throw new DecoderException("Expected filter and edited filter to be of the same type");
        }
        return new PacketEditFilter<>(pos, (FILTER) filter, (FILTER) edited);
    }

    @SuppressWarnings("Convert2Diamond")//Confuses IntelliJ about the nullability state
    public static <FILTER extends IFilter<FILTER>> PacketEditFilter<FILTER> delete(BlockPos pos, FILTER filter) {
        return new PacketEditFilter<FILTER>(pos, filter, null);
    }

    @Override
    public CustomPacketPayload.Type<PacketEditFilter<?>> type() {
        return TYPE;
    }

    @Override
    public void handle(IPayloadContext context) {
        FilterManager<?> filterManager = PacketUtils.filterManager(context, pos);
        if (filterManager != null) {
            filterManager.tryEditFilter(filter, edited);
        }
    }
}