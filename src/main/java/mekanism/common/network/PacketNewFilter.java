package mekanism.common.network;

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

public class PacketNewFilter {

    private Coord4D coord4D;
    private IFilter<?> filter;

    public PacketNewFilter(Coord4D coord, IFilter<?> filter) {
        coord4D = coord;
        this.filter = filter;
    }

    public static void handle(PacketNewFilter message, Supplier<Context> context) {
        PlayerEntity player = PacketHandler.getPlayer(context);
        if (player == null) {
            return;
        }
        context.get().enqueueWork(() -> {
            TileEntity tile = MekanismUtils.getTileEntity(player.world, message.coord4D.getPos());
            if (message.filter instanceof TransporterFilter && tile instanceof TileEntityLogisticalSorter) {
                ((TileEntityLogisticalSorter) tile).getFilters().add((TransporterFilter<?>) message.filter);
            } else if (message.filter instanceof MinerFilter && tile instanceof TileEntityDigitalMiner) {
                ((TileEntityDigitalMiner) tile).getFilters().add((MinerFilter<?>) message.filter);
            } else if (message.filter instanceof OredictionificatorFilter && tile instanceof TileEntityOredictionificator) {
                ((TileEntityOredictionificator) tile).getFilters().add((OredictionificatorFilter) message.filter);
            }
        });
        context.get().setPacketHandled(true);
    }

    public static void encode(PacketNewFilter pkt, PacketBuffer buf) {
        pkt.coord4D.write(buf);
        pkt.filter.write(buf);
    }

    public static PacketNewFilter decode(PacketBuffer buf) {
        return new PacketNewFilter(Coord4D.read(buf), BaseFilter.readFromPacket(buf));
    }
}