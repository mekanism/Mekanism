package mekanism.common.network.to_server;

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

public class PacketNewFilter implements IMekanismPacket {

    private final BlockPos pos;
    private final IFilter<?> filter;

    public PacketNewFilter(BlockPos pos, IFilter<?> filter) {
        this.pos = pos;
        this.filter = filter;
    }

    @Override
    public void handle(NetworkEvent.Context context) {
        Player player = context.getSender();
        if (player != null) {
            BlockEntity blockEntity = WorldUtils.getTileEntity(player.level(), pos);
            if (blockEntity instanceof ITileFilterHolder<?> filterHolder) {
                filterHolder.getFilterManager().tryAddFilter(filter, true);
            }
        }
    }

    @Override
    public void encode(FriendlyByteBuf buffer) {
        buffer.writeBlockPos(pos);
        filter.write(buffer);
    }

    public static PacketNewFilter decode(FriendlyByteBuf buffer) {
        return new PacketNewFilter(buffer.readBlockPos(), BaseFilter.readFromPacket(buffer));
    }
}