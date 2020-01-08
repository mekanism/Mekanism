package mekanism.common.network;

import java.util.function.Supplier;
import mekanism.api.Coord4D;
import mekanism.api.TileNetworkList;
import mekanism.common.HashList;
import mekanism.common.Mekanism;
import mekanism.common.PacketHandler;
import mekanism.common.content.filter.IFilter;
import mekanism.common.content.miner.MinerFilter;
import mekanism.common.content.transporter.TransporterFilter;
import mekanism.common.tile.TileEntityDigitalMiner;
import mekanism.common.tile.TileEntityLogisticalSorter;
import mekanism.common.tile.TileEntityOredictionificator;
import mekanism.common.tile.TileEntityOredictionificator.OredictionificatorFilter;
import mekanism.common.tile.base.TileEntityMekanism;
import mekanism.common.tile.interfaces.ITileFilterHolder;
import mekanism.common.util.MekanismUtils;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.fml.network.NetworkEvent.Context;

public class PacketEditFilter {

    private IFilter<?> filter;
    private IFilter<?> edited;
    private Coord4D coord4D;
    private boolean delete;
    private byte type = -1;

    public PacketEditFilter(Coord4D coord, boolean deletion, IFilter<?> filter, IFilter<?> edited) {
        coord4D = coord;
        delete = deletion;
        this.filter = filter;
        this.edited = edited;
        if (filter instanceof TransporterFilter) {
            type = 0;
        } else if (filter instanceof MinerFilter) {
            type = 1;
        } else if (filter instanceof OredictionificatorFilter) {
            type = 2;
        }
    }

    public static void handle(PacketEditFilter message, Supplier<Context> context) {
        PlayerEntity player = PacketHandler.getPlayer(context);
        if (player == null) {
            return;
        }
        context.get().enqueueWork(() -> {
            //TODO: Verify this
            TileEntity tile = MekanismUtils.getTileEntity(player.world, message.coord4D.getPos());
            if (message.type == 0 && tile instanceof TileEntityLogisticalSorter) {
                handleFilter((TileEntityLogisticalSorter) tile, message);
            } else if (message.type == 1 && tile instanceof TileEntityDigitalMiner) {
                handleFilter((TileEntityDigitalMiner) tile, message);
            } else if (message.type == 2 && tile instanceof TileEntityOredictionificator) {
                handleFilter((TileEntityOredictionificator) tile, message);
            }
        });
        context.get().setPacketHandled(true);
    }

    private static <FILTER extends IFilter, TILE extends TileEntityMekanism & ITileFilterHolder<FILTER>> void handleFilter(TILE tile, PacketEditFilter message) {
        HashList<FILTER> filters = tile.getFilters();
        FILTER filter = (FILTER) message.filter;
        if (!filters.contains(filter)) {
            return;
        }
        int index = filters.indexOf(filter);
        filters.remove(index);
        if (!message.delete) {
            filters.add(index, (FILTER) message.edited);
        }
        for (PlayerEntity iterPlayer : tile.playersUsing) {
            Mekanism.packetHandler.sendTo(new PacketTileEntity(tile, tile.getFilterPacket()), (ServerPlayerEntity) iterPlayer);
        }
    }

    public static void encode(PacketEditFilter pkt, PacketBuffer buf) {
        pkt.coord4D.write(buf);

        buf.writeByte(pkt.type);

        buf.writeBoolean(pkt.delete);

        TileNetworkList data = new TileNetworkList();

        if (pkt.type == 0) {
            ((TransporterFilter<?>) pkt.filter).write(data);
            if (!pkt.delete) {
                ((TransporterFilter<?>) pkt.edited).write(data);
            }
        } else if (pkt.type == 1) {
            ((MinerFilter<?>) pkt.filter).write(data);
            if (!pkt.delete) {
                ((MinerFilter<?>) pkt.edited).write(data);
            }
        } else if (pkt.type == 2) {
            ((OredictionificatorFilter) pkt.filter).write(data);
            if (!pkt.delete) {
                ((OredictionificatorFilter) pkt.edited).write(data);
            }
        }
        PacketHandler.encode(data.toArray(), buf);
    }

    public static PacketEditFilter decode(PacketBuffer buf) {
        Coord4D coord4D = Coord4D.read(buf);
        IFilter<?> filter = null;
        IFilter<?> edited = null;

        byte type = buf.readByte();
        boolean delete = buf.readBoolean();
        if (type == 0) {
            filter = TransporterFilter.readFromPacket(buf);
            if (!delete) {
                edited = TransporterFilter.readFromPacket(buf);
            }
        } else if (type == 1) {
            filter = MinerFilter.readFromPacket(buf);
            if (!delete) {
                edited = MinerFilter.readFromPacket(buf);
            }
        } else if (type == 2) {
            filter = OredictionificatorFilter.readFromPacket(buf);
            if (!delete) {
                edited = OredictionificatorFilter.readFromPacket(buf);
            }
        }
        return new PacketEditFilter(coord4D, delete, filter, edited);
    }
}