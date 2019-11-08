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
import mekanism.common.tile.base.TileEntityMekanism;
import mekanism.common.tile.interfaces.ITileFilterHolder;
import mekanism.common.util.MekanismUtils;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
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

    private static <FILTER extends IFilter<FILTER>, TILE extends TileEntityMekanism & ITileFilterHolder<FILTER>> void handleFilter(TILE tile, PacketNewFilter message) {
        tile.getFilters().add((FILTER) message.filter);
        for (PlayerEntity iterPlayer : tile.playersUsing) {
            Mekanism.packetHandler.sendTo(new PacketTileEntity(tile, tile.getFilterPacket()), (ServerPlayerEntity) iterPlayer);
        }
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