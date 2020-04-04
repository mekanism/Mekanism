package mekanism.common.network;

import java.util.function.Supplier;
import mekanism.common.HashList;
import mekanism.common.PacketHandler;
import mekanism.common.content.filter.BaseFilter;
import mekanism.common.content.filter.IFilter;
import mekanism.common.content.miner.MinerFilter;
import mekanism.common.content.transporter.TransporterFilter;
import mekanism.common.tile.TileEntityDigitalMiner;
import mekanism.common.tile.TileEntityLogisticalSorter;
import mekanism.common.tile.TileEntityOredictionificator;
import mekanism.common.tile.TileEntityOredictionificator.OredictionificatorFilter;
import mekanism.common.util.MekanismUtils;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.network.NetworkEvent.Context;

public class PacketEditFilter {

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

    public static void handle(PacketEditFilter message, Supplier<Context> context) {
        PlayerEntity player = PacketHandler.getPlayer(context);
        if (player == null) {
            return;
        }
        context.get().enqueueWork(() -> {
            TileEntity tile = MekanismUtils.getTileEntity(player.world, message.pos);
            if (message.filter instanceof TransporterFilter && tile instanceof TileEntityLogisticalSorter) {
                HashList<TransporterFilter<?>> filters = ((TileEntityLogisticalSorter) tile).getFilters();
                int index = filters.indexOf(message.filter);
                if (index != -1) {
                    filters.remove(index);
                    if (!message.delete) {
                        filters.add(index, (TransporterFilter<?>) message.edited);
                    }
                }
            } else if (message.filter instanceof MinerFilter && tile instanceof TileEntityDigitalMiner) {
                HashList<MinerFilter<?>> filters = ((TileEntityDigitalMiner) tile).getFilters();
                int index = filters.indexOf(message.filter);
                if (index != -1) {
                    filters.remove(index);
                    if (!message.delete) {
                        filters.add(index, (MinerFilter<?>) message.edited);
                    }
                }
            } else if (message.filter instanceof OredictionificatorFilter && tile instanceof TileEntityOredictionificator) {
                HashList<OredictionificatorFilter> filters = ((TileEntityOredictionificator) tile).getFilters();
                int index = filters.indexOf(message.filter);
                if (index != -1) {
                    filters.remove(index);
                    if (!message.delete) {
                        filters.add(index, (OredictionificatorFilter) message.edited);
                    }
                }
            }
        });
        context.get().setPacketHandled(true);
    }

    public static void encode(PacketEditFilter pkt, PacketBuffer buf) {
        buf.writeBlockPos(pkt.pos);
        buf.writeBoolean(pkt.delete);
        pkt.filter.write(buf);
        if (!pkt.delete) {
            pkt.edited.write(buf);
        }
    }

    public static PacketEditFilter decode(PacketBuffer buf) {
        BlockPos pos = buf.readBlockPos();
        IFilter<?> edited = null;
        boolean delete = buf.readBoolean();
        IFilter<?> filter = BaseFilter.readFromPacket(buf);
        if (!delete) {
            edited = BaseFilter.readFromPacket(buf);
        }
        return new PacketEditFilter(pos, delete, filter, edited);
    }
}