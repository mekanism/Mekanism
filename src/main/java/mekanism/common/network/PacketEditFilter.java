package mekanism.common.network;

import java.util.function.Supplier;
import mekanism.api.Coord4D;
import mekanism.api.TileNetworkList;
import mekanism.common.Mekanism;
import mekanism.common.PacketHandler;
import mekanism.common.content.filter.IFilter;
import mekanism.common.content.miner.MinerFilter;
import mekanism.common.content.transporter.TransporterFilter;
import mekanism.common.network.PacketTileEntity.TileEntityMessage;
import mekanism.common.tile.TileEntityDigitalMiner;
import mekanism.common.tile.TileEntityLogisticalSorter;
import mekanism.common.tile.TileEntityOredictionificator;
import mekanism.common.tile.TileEntityOredictionificator.OredictionificatorFilter;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.fml.network.NetworkEvent.Context;
import net.minecraftforge.fml.server.ServerLifecycleHooks;

public class PacketEditFilter {

    private OredictionificatorFilter oFilter;
    private OredictionificatorFilter oEdited;
    private TransporterFilter tFilter;
    private TransporterFilter tEdited;
    private MinerFilter mFilter;
    private MinerFilter mEdited;

    private Coord4D coord4D;
    private boolean delete;
    private byte type = -1;

    public PacketEditFilter(Coord4D coord, boolean deletion, IFilter filter, IFilter edited) {
        coord4D = coord;
        delete = deletion;

        if (filter instanceof TransporterFilter) {
            tFilter = (TransporterFilter) filter;
            if (!delete) {
                tEdited = (TransporterFilter) edited;
            }
            type = 0;
        } else if (filter instanceof MinerFilter) {
            mFilter = (MinerFilter) filter;
            if (!delete) {
                mEdited = (MinerFilter) edited;
            }
            type = 1;
        } else if (filter instanceof OredictionificatorFilter) {
            oFilter = (OredictionificatorFilter) filter;
            if (!delete) {
                oEdited = (OredictionificatorFilter) edited;
            }
            type = 2;
        }
    }

    public static void handle(PacketEditFilter message, Supplier<Context> context) {
        ServerWorld worldServer = ServerLifecycleHooks.getCurrentServer().getWorld(message.coord4D.dimension);
        worldServer.addScheduledTask(() -> {
            if (message.type == 0 && message.coord4D.getTileEntity(worldServer) instanceof TileEntityLogisticalSorter) {
                TileEntityLogisticalSorter sorter = (TileEntityLogisticalSorter) message.coord4D.getTileEntity(worldServer);

                if (!sorter.filters.contains(message.tFilter)) {
                    return;
                }
                int index = sorter.filters.indexOf(message.tFilter);
                sorter.filters.remove(index);
                if (!message.delete) {
                    sorter.filters.add(index, message.tEdited);
                }
                for (PlayerEntity iterPlayer : sorter.playersUsing) {
                    Mekanism.packetHandler.sendTo(new TileEntityMessage(sorter, sorter.getFilterPacket(new TileNetworkList())), (ServerPlayerEntity) iterPlayer);
                }
            } else if (message.type == 1 && message.coord4D.getTileEntity(worldServer) instanceof TileEntityDigitalMiner) {
                TileEntityDigitalMiner miner = (TileEntityDigitalMiner) message.coord4D.getTileEntity(worldServer);

                if (!miner.filters.contains(message.mFilter)) {
                    return;
                }
                int index = miner.filters.indexOf(message.mFilter);
                miner.filters.remove(index);
                if (!message.delete) {
                    miner.filters.add(index, message.mEdited);
                }
                for (PlayerEntity iterPlayer : miner.playersUsing) {
                    Mekanism.packetHandler.sendTo(new TileEntityMessage(miner, miner.getFilterPacket(new TileNetworkList())), (ServerPlayerEntity) iterPlayer);
                }
            } else if (message.type == 2 && message.coord4D.getTileEntity(worldServer) instanceof TileEntityOredictionificator) {
                TileEntityOredictionificator oredictionificator = (TileEntityOredictionificator) message.coord4D.getTileEntity(worldServer);
                if (!oredictionificator.filters.contains(message.oFilter)) {
                    return;
                }
                int index = oredictionificator.filters.indexOf(message.oFilter);
                oredictionificator.filters.remove(index);
                if (!message.delete) {
                    oredictionificator.filters.add(index, message.oEdited);
                }
                for (PlayerEntity iterPlayer : oredictionificator.playersUsing) {
                    Mekanism.packetHandler.sendTo(new TileEntityMessage(oredictionificator, oredictionificator.getFilterPacket(new TileNetworkList())), (ServerPlayerEntity) iterPlayer);
                }
            }
        });
    }

    public static void encode(PacketEditFilter pkt, PacketBuffer buf) {
        pkt.coord4D.write(buf);

        buf.writeByte(pkt.type);

        buf.writeBoolean(pkt.delete);

        TileNetworkList data = new TileNetworkList();

        if (pkt.type == 0) {
            pkt.tFilter.write(data);
            if (!pkt.delete) {
                pkt.tEdited.write(data);
            }
        } else if (pkt.type == 1) {
            pkt.mFilter.write(data);
            if (!pkt.delete) {
                pkt.mEdited.write(data);
            }
        } else if (pkt.type == 2) {
            pkt.oFilter.write(data);
            if (!pkt.delete) {
                pkt.oEdited.write(data);
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