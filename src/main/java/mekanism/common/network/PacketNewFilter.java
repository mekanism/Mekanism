package mekanism.common.network;

import java.util.function.Supplier;
import mekanism.common.content.filter.BaseFilter;
import mekanism.common.content.filter.IFilter;
import mekanism.common.content.miner.MinerFilter;
import mekanism.common.content.qio.filter.QIOFilter;
import mekanism.common.content.transporter.SorterFilter;
import mekanism.common.tile.TileEntityLogisticalSorter;
import mekanism.common.tile.machine.TileEntityDigitalMiner;
import mekanism.common.tile.machine.TileEntityOredictionificator;
import mekanism.common.tile.machine.TileEntityOredictionificator.OredictionificatorFilter;
import mekanism.common.tile.qio.TileEntityQIOFilterHandler;
import mekanism.common.util.MekanismUtils;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.network.NetworkEvent.Context;

public class PacketNewFilter {

    private final BlockPos pos;
    private final IFilter<?> filter;

    public PacketNewFilter(BlockPos pos, IFilter<?> filter) {
        this.pos = pos;
        this.filter = filter;
    }

    public static void handle(PacketNewFilter message, Supplier<Context> context) {
        PlayerEntity player = BasePacketHandler.getPlayer(context);
        if (player == null) {
            return;
        }
        context.get().enqueueWork(() -> {
            TileEntity tile = MekanismUtils.getTileEntity(player.world, message.pos);
            if (tile != null) {
                if (message.filter instanceof SorterFilter && tile instanceof TileEntityLogisticalSorter) {
                    ((TileEntityLogisticalSorter) tile).getFilters().add((SorterFilter<?>) message.filter);
                } else if (message.filter instanceof MinerFilter && tile instanceof TileEntityDigitalMiner) {
                    ((TileEntityDigitalMiner) tile).getFilters().add((MinerFilter<?>) message.filter);
                } else if (message.filter instanceof OredictionificatorFilter && tile instanceof TileEntityOredictionificator) {
                    ((TileEntityOredictionificator) tile).getFilters().add((OredictionificatorFilter) message.filter);
                } else if (message.filter instanceof QIOFilter && tile instanceof TileEntityQIOFilterHandler) {
                    ((TileEntityQIOFilterHandler) tile).getFilters().add((QIOFilter<?>) message.filter);
                }
                tile.markDirty();
            }
        });
        context.get().setPacketHandled(true);
    }

    public static void encode(PacketNewFilter pkt, PacketBuffer buf) {
        buf.writeBlockPos(pkt.pos);
        pkt.filter.write(buf);
    }

    public static PacketNewFilter decode(PacketBuffer buf) {
        return new PacketNewFilter(buf.readBlockPos(), BaseFilter.readFromPacket(buf));
    }
}