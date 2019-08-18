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

public class PacketNewFilter {

    private Coord4D coord4D;
    private IFilter filter;
    private byte type = -1;

    public PacketNewFilter(Coord4D coord, IFilter filter) {
        coord4D = coord;
        this.filter = filter;
        if (filter instanceof TransporterFilter) {
            type = 0;
        } else if (filter instanceof MinerFilter) {
            type = 1;
        } else if (filter instanceof OredictionificatorFilter) {
            type = 2;
        }
    }

    public static void handle(PacketNewFilter message, Supplier<Context> context) {
        PlayerEntity player = PacketHandler.getPlayer(context);
        PacketHandler.handlePacket(() -> {
            //TODO: Verify this
            World worldServer = player.world;
            if (message.type == 0 && message.coord4D.getTileEntity(worldServer) instanceof TileEntityLogisticalSorter) {
                TileEntityLogisticalSorter sorter = (TileEntityLogisticalSorter) message.coord4D.getTileEntity(worldServer);
                sorter.filters.add((TransporterFilter) message.filter);
                for (PlayerEntity iterPlayer : sorter.playersUsing) {
                    Mekanism.packetHandler.sendTo(new PacketTileEntity(sorter, sorter.getFilterPacket(new TileNetworkList())), (ServerPlayerEntity) iterPlayer);
                }
            } else if (message.type == 1 && message.coord4D.getTileEntity(worldServer) instanceof TileEntityDigitalMiner) {
                TileEntityDigitalMiner miner = (TileEntityDigitalMiner) message.coord4D.getTileEntity(worldServer);
                miner.filters.add((MinerFilter) message.filter);
                for (PlayerEntity iterPlayer : miner.playersUsing) {
                    Mekanism.packetHandler.sendTo(new PacketTileEntity(miner, miner.getFilterPacket(new TileNetworkList())), (ServerPlayerEntity) iterPlayer);
                }
            } else if (message.type == 2 && message.coord4D.getTileEntity(worldServer) instanceof TileEntityOredictionificator) {
                TileEntityOredictionificator oredictionificator = (TileEntityOredictionificator) message.coord4D.getTileEntity(worldServer);
                oredictionificator.filters.add((OredictionificatorFilter) message.filter);
                for (PlayerEntity iterPlayer : oredictionificator.playersUsing) {
                    Mekanism.packetHandler.sendTo(new PacketTileEntity(oredictionificator, oredictionificator.getFilterPacket(new TileNetworkList())), (ServerPlayerEntity) iterPlayer);
                }
            }
        }, player);
    }

    public static void encode(PacketNewFilter pkt, PacketBuffer buf) {
        pkt.coord4D.write(buf);
        buf.writeByte(pkt.type);
        TileNetworkList data = new TileNetworkList();
        if (pkt.type == 0) {
            ((TransporterFilter) pkt.filter).write(data);
        } else if (pkt.type == 1) {
            ((MinerFilter) pkt.filter).write(data);
        } else if (pkt.type == 2) {
            ((OredictionificatorFilter) pkt.filter).write(data);
        }
        PacketHandler.encode(data.toArray(), buf);
    }

    public static PacketNewFilter decode(PacketBuffer buf) {
        Coord4D coord4D = Coord4D.read(buf);
        byte type = buf.readByte();
        IFilter filter = null;
        if (type == 0) {
            filter = TransporterFilter.readFromPacket(buf);
        } else if (type == 1) {
            filter = MinerFilter.readFromPacket(buf);
        } else if (type == 2) {
            filter = OredictionificatorFilter.readFromPacket(buf);
        }
        return new PacketNewFilter(coord4D, filter);
    }
}