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
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.network.NetworkEvent;

public class PacketNewFilter implements IMekanismPacket {

    private final BlockPos pos;
    private final IFilter<?> filter;

    public PacketNewFilter(BlockPos pos, IFilter<?> filter) {
        this.pos = pos;
        this.filter = filter;
    }

    @Override
    public void handle(NetworkEvent.Context context) {
        PlayerEntity player = context.getSender();
        if (player != null) {
            TileEntity tile = WorldUtils.getTileEntity(player.level, pos);
            if (tile != null) {
                if (filter instanceof SorterFilter && tile instanceof TileEntityLogisticalSorter) {
                    ((TileEntityLogisticalSorter) tile).getFilters().add((SorterFilter<?>) filter);
                } else if (filter instanceof MinerFilter && tile instanceof TileEntityDigitalMiner) {
                    ((TileEntityDigitalMiner) tile).getFilters().add((MinerFilter<?>) filter);
                } else if (filter instanceof OredictionificatorItemFilter && tile instanceof TileEntityOredictionificator) {
                    ((TileEntityOredictionificator) tile).getFilters().add((OredictionificatorItemFilter) filter);
                } else if (filter instanceof QIOFilter && tile instanceof TileEntityQIOFilterHandler) {
                    ((TileEntityQIOFilterHandler) tile).getFilters().add((QIOFilter<?>) filter);
                }
                tile.setChanged();
            }
        }
    }

    @Override
    public void encode(PacketBuffer buffer) {
        buffer.writeBlockPos(pos);
        filter.write(buffer);
    }

    public static PacketNewFilter decode(PacketBuffer buffer) {
        return new PacketNewFilter(buffer.readBlockPos(), BaseFilter.readFromPacket(buffer));
    }
}