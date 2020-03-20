package mekanism.common.network;

import java.util.List;
import java.util.function.Supplier;
import mekanism.api.Coord4D;
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
import net.minecraftforge.fml.network.NetworkEvent.Context;

public class PacketEditFilter {

    private IFilter<?> filter;
    private IFilter<?> edited;
    private Coord4D coord4D;
    private boolean delete;

    public PacketEditFilter(Coord4D coord, boolean deletion, IFilter<?> filter, IFilter<?> edited) {
        coord4D = coord;
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
            TileEntity tile = MekanismUtils.getTileEntity(player.world, message.coord4D.getPos());
            if (message.filter instanceof TransporterFilter && tile instanceof TileEntityLogisticalSorter) {
                List<TransporterFilter<?>> filters = ((TileEntityLogisticalSorter) tile).getFilters();
                int index = filters.indexOf(message.filter);
                if (index != -1) {
                    filters.remove(index);
                    if (!message.delete) {
                        filters.add(index, (TransporterFilter<?>) message.edited);
                    }
                }
            } else if (message.filter instanceof MinerFilter && tile instanceof TileEntityDigitalMiner) {
                List<MinerFilter<?>> filters = ((TileEntityDigitalMiner) tile).getFilters();
                int index = filters.indexOf(message.filter);
                if (index != -1) {
                    filters.remove(index);
                    if (!message.delete) {
                        filters.add(index, (MinerFilter<?>) message.edited);
                    }
                }
            } else if (message.filter instanceof OredictionificatorFilter && tile instanceof TileEntityOredictionificator) {
                List<OredictionificatorFilter> filters = ((TileEntityOredictionificator) tile).getFilters();
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
        pkt.coord4D.write(buf);
        buf.writeBoolean(pkt.delete);
        pkt.filter.write(buf);
        if (!pkt.delete) {
            pkt.edited.write(buf);
        }
    }

    public static PacketEditFilter decode(PacketBuffer buf) {
        Coord4D coord4D = Coord4D.read(buf);
        IFilter<?> edited = null;
        boolean delete = buf.readBoolean();
        IFilter<?> filter = BaseFilter.readFromPacket(buf);
        if (!delete) {
            edited = BaseFilter.readFromPacket(buf);
        }
        return new PacketEditFilter(coord4D, delete, filter, edited);
    }
}