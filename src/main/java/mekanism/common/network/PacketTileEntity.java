package mekanism.common.network;

import io.netty.buffer.ByteBuf;
import java.util.function.Supplier;
import mekanism.api.Coord4D;
import mekanism.api.TileNetworkList;
import mekanism.common.Mekanism;
import mekanism.common.PacketHandler;
import mekanism.common.base.ITileNetwork;
import mekanism.common.capabilities.Capabilities;
import mekanism.common.util.CapabilityUtils;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.fml.network.NetworkEvent.Context;
import net.minecraftforge.fml.server.ServerLifecycleHooks;

public class PacketTileEntity {

    private TileNetworkList parameters;
    private ByteBuf storedBuffer;
    private Coord4D coord4D;

    public <TILE extends TileEntity & ITileNetwork> PacketTileEntity(TILE tile) {
        this(Coord4D.get(tile), tile.getNetworkedData());
    }

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
        PacketHandler.handlePacket(() -> {
            TileEntity tileEntity = message.coord4D.getTileEntity(player.world);
            if (CapabilityUtils.hasCapability(tileEntity, Capabilities.TILE_NETWORK_CAPABILITY, null)) {
                ITileNetwork network = CapabilityUtils.getCapability(tileEntity, Capabilities.TILE_NETWORK_CAPABILITY, null);
                try {
                    network.handlePacketData(message.storedBuffer);
                } catch (Exception e) {
                    Mekanism.logger.error("FIXME: Packet handling error", e);
                }
                message.storedBuffer.release();
            }
        }, player);
    }

    public static void encode(PacketTileEntity pkt, PacketBuffer buf) {
        pkt.coord4D.write(buf);
        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        if (server != null) {
            World world = server.getWorld(pkt.coord4D.dimension);
            PacketHandler.log("Sending TileEntity packet from coordinate " + pkt.coord4D + " (" + pkt.coord4D.getTileEntity(world) + ")");
        }
        PacketHandler.encode(pkt.parameters.toArray(), buf);
    }

    public static PacketTileEntity decode(PacketBuffer buf) {
        PacketTileEntity packet = new PacketTileEntity(Coord4D.read(buf));
        packet.storedBuffer = buf.copy();
        return packet;
    }
}