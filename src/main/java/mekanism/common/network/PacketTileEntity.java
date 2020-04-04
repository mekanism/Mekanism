package mekanism.common.network;

import java.util.function.Supplier;
import mekanism.api.Coord4D;
import mekanism.api.TileNetworkList;
import mekanism.common.Mekanism;
import mekanism.common.PacketHandler;
import mekanism.common.base.ITileNetwork;
import mekanism.common.util.MekanismUtils;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.fml.network.NetworkEvent.Context;
import net.minecraftforge.fml.server.ServerLifecycleHooks;

@Deprecated
public class PacketTileEntity {

    private TileNetworkList parameters;
    private PacketBuffer storedBuffer;
    private Coord4D coord4D;

    public PacketTileEntity(TileEntity tile, TileNetworkList params) {
        this(Coord4D.get(tile), params);
    }

    public PacketTileEntity(Coord4D coord, TileNetworkList params) {
        this(coord);
        parameters = params;
    }

    private PacketTileEntity(Coord4D coord) {
        coord4D = coord;
    }

    public static void handle(PacketTileEntity message, Supplier<Context> context) {
        PlayerEntity player = PacketHandler.getPlayer(context);
        if (player == null) {
            return;
        }
        context.get().enqueueWork(() -> {
            TileEntity tile = MekanismUtils.getTileEntity(player.world, message.coord4D.getPos());
            if (tile instanceof ITileNetwork) {
                try {
                    ((ITileNetwork) tile).handlePacketData(message.storedBuffer);
                } catch (Exception e) {
                    Mekanism.logger.error("FIXME: Packet handling error", e);
                }
            }
            message.storedBuffer.release();
        });
        context.get().setPacketHandled(true);
    }

    public static void encode(PacketTileEntity pkt, PacketBuffer buf) {
        pkt.coord4D.write(buf);
        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        if (server != null) {
            World world = server.getWorld(pkt.coord4D.dimension);
            PacketHandler.log("Sending TileEntity packet from coordinate " + pkt.coord4D + " (" + MekanismUtils.getTileEntity(world, pkt.coord4D.getPos()) + ")");
        }
        PacketHandler.encode(pkt.parameters.toArray(), buf);
    }

    public static PacketTileEntity decode(PacketBuffer buf) {
        PacketTileEntity packet = new PacketTileEntity(Coord4D.read(buf));
        packet.storedBuffer = new PacketBuffer(buf.copy());
        return packet;
    }
}