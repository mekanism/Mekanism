package mekanism.common.network.to_server;

import mekanism.common.content.filter.BaseFilter;
import mekanism.common.content.filter.IFilter;
import mekanism.common.content.miner.MinerFilter;
import mekanism.common.content.oredictionificator.OredictionificatorItemFilter;
import mekanism.common.content.qio.filter.QIOFilter;
import mekanism.common.content.transporter.SorterFilter;
import mekanism.common.lib.collection.HashList;
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

public class PacketEditFilter implements IMekanismPacket {

    private final IFilter<?> filter;
    private final IFilter<?> edited;
    private final boolean delete;
    private final BlockPos pos;

    public PacketEditFilter(BlockPos pos, boolean deletion, IFilter<?> filter, IFilter<?> edited) {
        this.pos = pos;
        delete = deletion;
        this.filter = filter;
        this.edited = edited;
    }

    @Override
    public void handle(NetworkEvent.Context context) {
        Player player = context.getSender();
        if (player == null) {
            return;
        }
        BlockEntity tile = WorldUtils.getTileEntity(player.level, pos);
        if (tile != null) {
            if (filter instanceof SorterFilter && tile instanceof TileEntityLogisticalSorter sorter) {
                HashList<SorterFilter<?>> filters = sorter.getFilters();
                int index = filters.indexOf(filter);
                if (index != -1) {
                    filters.remove(index);
                    if (!delete) {
                        filters.add(index, (SorterFilter<?>) edited);
                    }
                }
            } else if (filter instanceof MinerFilter && tile instanceof TileEntityDigitalMiner miner) {
                HashList<MinerFilter<?>> filters = miner.getFilters();
                int index = filters.indexOf(filter);
                if (index != -1) {
                    filters.remove(index);
                    if (!delete) {
                        filters.add(index, (MinerFilter<?>) edited);
                    }
                }
            } else if (filter instanceof OredictionificatorItemFilter && tile instanceof TileEntityOredictionificator oredictionificator) {
                HashList<OredictionificatorItemFilter> filters = oredictionificator.getFilters();
                int index = filters.indexOf(filter);
                if (index != -1) {
                    filters.remove(index);
                    if (!delete) {
                        filters.add(index, (OredictionificatorItemFilter) edited);
                    }
                }
            } else if (filter instanceof QIOFilter && tile instanceof TileEntityQIOFilterHandler qioFilterHandler) {
                HashList<QIOFilter<?>> filters = qioFilterHandler.getFilters();
                int index = filters.indexOf(filter);
                if (index != -1) {
                    filters.remove(index);
                    if (!delete) {
                        filters.add(index, (QIOFilter<?>) edited);
                    }
                }
            }
            tile.setChanged();
        }
    }

    @Override
    public void encode(FriendlyByteBuf buffer) {
        buffer.writeBlockPos(pos);
        buffer.writeBoolean(delete);
        filter.write(buffer);
        if (!delete) {
            edited.write(buffer);
        }
    }

    public static PacketEditFilter decode(FriendlyByteBuf buffer) {
        BlockPos pos = buffer.readBlockPos();
        boolean delete = buffer.readBoolean();
        IFilter<?> filter = BaseFilter.readFromPacket(buffer);
        IFilter<?> edited = null;
        if (!delete) {
            edited = BaseFilter.readFromPacket(buffer);
        }
        return new PacketEditFilter(pos, delete, filter, edited);
    }
}