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

//TODO - 1.20.4: SP: Validate this doesn't have any issues in single player, I believe the filter is always new on the client and then the gui is closed
// and the referenced removed, so we can use that implementation detail to not require any copying or stuff
public record PacketNewFilter(BlockPos pos, IFilter<?> filter) implements IMekanismPacket<PlayPayloadContext> {

    public static final ResourceLocation ID = Mekanism.rl("new_filter");

    public PacketNewFilter(FriendlyByteBuf buffer) {
        this(buffer.readBlockPos(), BaseFilter.readFromPacket(buffer));
    }

    @NotNull
    @Override
    public ResourceLocation id() {
        return ID;
    }

    @Override
    public void handle(PlayPayloadContext context) {
        FilterManager<?> filterManager = PacketUtils.filterManager(context, pos);
        if (filterManager != null) {
            filterManager.tryAddFilter(filter, true);
        }
    }

    @Override
    public void write(@NotNull FriendlyByteBuf buffer) {
        buffer.writeBlockPos(pos);
        filter.write(buffer);
    }
}