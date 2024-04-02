package mekanism.common.network.to_server.filter;

import mekanism.common.Mekanism;
import mekanism.common.content.filter.BaseFilter;
import mekanism.common.content.filter.FilterManager;
import mekanism.common.content.filter.IFilter;
import mekanism.common.network.IMekanismPacket;
import mekanism.common.network.PacketUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.PlayPayloadContext;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

//TODO - 1.20.4: SP: Validate this doesn't have any issues in single player, I believe the edited filter is always new on the client and then the gui is closed
// and the referenced removed, so we can use that implementation detail to not require any copying or stuff, and the filter is only used for lookup
public record PacketEditFilter<FILTER extends IFilter<FILTER>>(BlockPos pos, FILTER filter, @Nullable FILTER edited) implements IMekanismPacket<PlayPayloadContext> {

    public static final ResourceLocation ID = Mekanism.rl("edit_filter");
    private static final PacketEditFilter<?> ERROR = new PacketEditFilter<>(BlockPos.ZERO, null, null);

    @NotNull
    @Override
    public ResourceLocation id() {
        return ID;
    }

    @Override
    public void handle(PlayPayloadContext context) {
        if (filter != null) {
            FilterManager<?> filterManager = PacketUtils.filterManager(context, pos);
            if (filterManager != null) {
                filterManager.tryEditFilter(filter, edited);
            }
        }
    }

    @Override
    public void write(@NotNull FriendlyByteBuf buffer) {
        buffer.writeBlockPos(pos);
        filter.write(buffer);
        buffer.writeNullable(edited, (buf, editedFilter) -> editedFilter.write(buf));
    }

    @SuppressWarnings("unchecked")
    public static <FILTER extends IFilter<FILTER>> PacketEditFilter<?> decode(FriendlyByteBuf buffer) {
        BlockPos pos = buffer.readBlockPos();
        FILTER filter = (FILTER) BaseFilter.readFromPacket(buffer);
        IFilter<?> edited = buffer.readNullable(BaseFilter::readFromPacket);
        if (edited != null && edited.getFilterType() != filter.getFilterType()) {
            return ERROR;
        }
        return new PacketEditFilter<>(pos, filter, (FILTER) edited);
    }
}