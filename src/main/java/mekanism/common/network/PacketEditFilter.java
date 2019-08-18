package mekanism.common.network;

import java.util.function.Supplier;
import mekanism.api.Coord4D;
import mekanism.api.TileNetworkList;
import mekanism.common.Mekanism;
import mekanism.common.PacketHandler;
import mekanism.common.content.filter.IFilter;
import mekanism.common.content.miner.MinerFilter;
import mekanism.common.content.transporter.TransporterFilter;
import mekanism.common.tile.TileEntityDigitalMiner;
import mekanism.common.tile.TileEntityLogisticalSorter;
import mekanism.common.tile.TileEntityOredictionificator;
import mekanism.common.tile.TileEntityOredictionificator.OredictionificatorFilter;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.world.World;
import net.minecraftforge.fml.network.NetworkEvent.Context;

public class PacketEditFilter {

    private IFilter filter;
    private IFilter edited;
    private Coord4D coord4D;
    private boolean delete;
    private byte type = -1;

    public PacketEditFilter(Coord4D coord, boolean deletion, IFilter filter, IFilter edited) {
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
        PacketHandler.handlePacket(() -> {
            //TODO: Verify this
            World worldServer = player.world;
            if (message.type == 0 && message.coord4D.getTileEntity(worldServer) instanceof TileEntityLogisticalSorter) {
                TileEntityLogisticalSorter sorter = (TileEntityLogisticalSorter) message.coord4D.getTileEntity(worldServer);

                if (!sorter.filters.contains((TransporterFilter) message.filter)) {
                    return;
                }
                int index = sorter.filters.indexOf((TransporterFilter) message.filter);
                sorter.filters.remove(index);
                if (!message.delete) {
                    sorter.filters.add(index, (TransporterFilter) message.edited);
                }
                for (PlayerEntity iterPlayer : sorter.playersUsing) {
                    Mekanism.packetHandler.sendTo(new PacketTileEntity(sorter, sorter.getFilterPacket(new TileNetworkList())), (ServerPlayerEntity) iterPlayer);
                }
            } else if (message.type == 1 && message.coord4D.getTileEntity(worldServer) instanceof TileEntityDigitalMiner) {
                TileEntityDigitalMiner miner = (TileEntityDigitalMiner) message.coord4D.getTileEntity(worldServer);

                if (!miner.filters.contains((MinerFilter) message.filter)) {
                    return;
                }
                int index = miner.filters.indexOf((MinerFilter) message.filter);
                miner.filters.remove(index);
                if (!message.delete) {
                    miner.filters.add(index, (MinerFilter) message.edited);
                }
                for (PlayerEntity iterPlayer : miner.playersUsing) {
                    Mekanism.packetHandler.sendTo(new PacketTileEntity(miner, miner.getFilterPacket(new TileNetworkList())), (ServerPlayerEntity) iterPlayer);
                }
            } else if (message.type == 2 && message.coord4D.getTileEntity(worldServer) instanceof TileEntityOredictionificator) {
                TileEntityOredictionificator oredictionificator = (TileEntityOredictionificator) message.coord4D.getTileEntity(worldServer);
                if (!oredictionificator.filters.contains((OredictionificatorFilter) message.filter)) {
                    return;
                }
                int index = oredictionificator.filters.indexOf((OredictionificatorFilter) message.filter);
                oredictionificator.filters.remove(index);
                if (!message.delete) {
                    oredictionificator.filters.add(index, (OredictionificatorFilter) message.edited);
                }
                for (PlayerEntity iterPlayer : oredictionificator.playersUsing) {
                    Mekanism.packetHandler.sendTo(new PacketTileEntity(oredictionificator, oredictionificator.getFilterPacket(new TileNetworkList())), (ServerPlayerEntity) iterPlayer);
                }
            }
        }, player);
    }

    public static void encode(PacketEditFilter pkt, PacketBuffer buf) {
        pkt.coord4D.write(buf);

        buf.writeByte(pkt.type);

        buf.writeBoolean(pkt.delete);

        TileNetworkList data = new TileNetworkList();

        if (pkt.type == 0) {
            ((TransporterFilter) pkt.filter).write(data);
            if (!pkt.delete) {
                ((TransporterFilter) pkt.edited).write(data);
            }
        } else if (pkt.type == 1) {
            ((MinerFilter) pkt.filter).write(data);
            if (!pkt.delete) {
                ((MinerFilter) pkt.edited).write(data);
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
        IFilter filter = null;
        IFilter edited = null;

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