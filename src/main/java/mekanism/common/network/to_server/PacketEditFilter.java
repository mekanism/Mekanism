package mekanism.common.network.to_server;

import javax.annotation.Nullable;
import mekanism.common.content.filter.BaseFilter;
import mekanism.common.content.filter.IFilter;
import mekanism.common.network.IMekanismPacket;
import mekanism.common.tile.interfaces.ITileFilterHolder;
import mekanism.common.util.WorldUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.network.NetworkEvent;

public class PacketEditFilter<FILTER extends IFilter<FILTER>> implements IMekanismPacket {

    private static final PacketEditFilter<?> ERROR = new PacketEditFilter<>(BlockPos.ZERO, null, null);

    private final FILTER filter;
    @Nullable
    private final FILTER edited;
    private final BlockPos pos;

    public PacketEditFilter(BlockPos pos, FILTER filter, @Nullable FILTER edited) {
        this.pos = pos;
        this.filter = filter;
        this.edited = edited;
    }

    @Override
    public void handle(NetworkEvent.Context context) {
        Player player = context.getSender();
        if (player == null || filter == null) {
            return;
        }
        BlockEntity tile = WorldUtils.getTileEntity(player.level(), pos);
        if (tile instanceof ITileFilterHolder<?> filterHolder) {
            filterHolder.getFilterManager().tryEditFilter(filter, edited);
        }
    }

    @Override
    public void encode(FriendlyByteBuf buffer) {
        buffer.writeBlockPos(pos);
        filter.write(buffer);
        if (edited == null) {
            buffer.writeBoolean(false);
        } else {
            buffer.writeBoolean(true);
            edited.write(buffer);
        }
    }

    @SuppressWarnings("unchecked")
    public static <FILTER extends IFilter<FILTER>> PacketEditFilter<?> decode(FriendlyByteBuf buffer) {
        BlockPos pos = buffer.readBlockPos();
        FILTER filter = (FILTER) BaseFilter.readFromPacket(buffer);
        IFilter<?> edited = null;
        if (buffer.readBoolean()) {
            edited = BaseFilter.readFromPacket(buffer);
            if (edited.getFilterType() != filter.getFilterType()) {
                return ERROR;
            }
        }
        return new PacketEditFilter<>(pos, filter, (FILTER) edited);
    }
}