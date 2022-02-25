package mekanism.common.network.to_server;

import mekanism.common.content.filter.BaseFilter;
import mekanism.common.content.filter.IFilter;
import mekanism.common.content.miner.MinerFilter;
import mekanism.common.content.oredictionificator.OredictionificatorItemFilter;
import mekanism.common.content.qio.filter.QIOFilter;
import mekanism.common.content.transporter.SorterFilter;
import mekanism.common.network.IMekanismPacket;
import mekanism.common.tile.TileEntityLogisticalSorter;
import mekanism.common.tile.machine.TileEntityDigitalMiner;
import mekanism.common.tile.machine.TileEntityOredictionificator;
import mekanism.common.tile.qio.TileEntityQIOFilterHandler;
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
            BlockEntity blockEntity = WorldUtils.getTileEntity(player.level, pos);
            if (blockEntity != null) {
                if (filter instanceof SorterFilter<?> filter && blockEntity instanceof TileEntityLogisticalSorter tile) {
                    tile.getFilters().add(filter);
                } else if (filter instanceof MinerFilter<?> filter && blockEntity instanceof TileEntityDigitalMiner tile) {
                    tile.getFilters().add(filter);
                } else if (filter instanceof OredictionificatorItemFilter filter && blockEntity instanceof TileEntityOredictionificator tile) {
                    tile.getFilters().add(filter);
                } else if (filter instanceof QIOFilter<?> filter && blockEntity instanceof TileEntityQIOFilterHandler tile) {
                    tile.getFilters().add(filter);
                }
                blockEntity.setChanged();
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