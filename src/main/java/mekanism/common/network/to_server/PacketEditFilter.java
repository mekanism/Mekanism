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
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.network.NetworkEvent;

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
        PlayerEntity player = context.getSender();
        if (player == null) {
            return;
        }
        TileEntity tile = WorldUtils.getTileEntity(player.level, pos);
        if (tile != null) {
            if (filter instanceof SorterFilter && tile instanceof TileEntityLogisticalSorter) {
                HashList<SorterFilter<?>> filters = ((TileEntityLogisticalSorter) tile).getFilters();
                int index = filters.indexOf(filter);
                if (index != -1) {
                    filters.remove(index);
                    if (!delete) {
                        filters.add(index, (SorterFilter<?>) edited);
                    }
                }
            } else if (filter instanceof MinerFilter && tile instanceof TileEntityDigitalMiner) {
                HashList<MinerFilter<?>> filters = ((TileEntityDigitalMiner) tile).getFilters();
                int index = filters.indexOf(filter);
                if (index != -1) {
                    filters.remove(index);
                    if (!delete) {
                        filters.add(index, (MinerFilter<?>) edited);
                    }
                }
            } else if (filter instanceof OredictionificatorItemFilter && tile instanceof TileEntityOredictionificator) {
                HashList<OredictionificatorItemFilter> filters = ((TileEntityOredictionificator) tile).getFilters();
                int index = filters.indexOf(filter);
                if (index != -1) {
                    filters.remove(index);
                    if (!delete) {
                        filters.add(index, (OredictionificatorItemFilter) edited);
                    }
                }
            } else if (filter instanceof QIOFilter && tile instanceof TileEntityQIOFilterHandler) {
                HashList<QIOFilter<?>> filters = ((TileEntityQIOFilterHandler) tile).getFilters();
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
    public void encode(PacketBuffer buffer) {
        buffer.writeBlockPos(pos);
        buffer.writeBoolean(delete);
        filter.write(buffer);
        if (!delete) {
            edited.write(buffer);
        }
    }

    public static PacketEditFilter decode(PacketBuffer buffer) {
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