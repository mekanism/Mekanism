package mekanism.common.network;

import java.util.function.Supplier;
import mekanism.api.Coord4D;
import mekanism.common.Mekanism;
import mekanism.common.PacketHandler;
import mekanism.common.capabilities.Capabilities;
import mekanism.common.tile.TileEntityMultiblock;
import mekanism.common.util.CapabilityUtils;
import mekanism.common.util.MekanismUtils;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.fml.network.NetworkEvent.Context;

//TODO: Can the need for this be removed all together?
public class PacketDataRequest {

    private Coord4D coord4D;

    public PacketDataRequest(Coord4D coord) {
        coord4D = coord;
    }

    public static void handle(PacketDataRequest message, Supplier<Context> context) {
        PlayerEntity player = PacketHandler.getPlayer(context);
        if (player == null) {
            return;
        }
        context.get().enqueueWork(() -> {
            //TODO: Verify this
            TileEntity tile = MekanismUtils.getTileEntity(player.world, message.coord4D.getPos());
            if (tile instanceof TileEntityMultiblock) {
                ((TileEntityMultiblock<?>) tile).sendStructure = true;
            }
            CapabilityUtils.getCapability(tile, Capabilities.GRID_TRANSMITTER_CAPABILITY, null).ifPresent(transmitter -> {
                transmitter.setRequestsUpdate();
                if (transmitter.hasTransmitterNetwork()) {
                    transmitter.getTransmitterNetwork().addUpdate(player);
                }
            });
            CapabilityUtils.getCapability(tile, Capabilities.TILE_NETWORK_CAPABILITY, null).ifPresent(
                  network -> Mekanism.packetHandler.sendTo(new PacketTileEntity(tile, network.getNetworkedData()), (ServerPlayerEntity) player)
            );
        });
        context.get().setPacketHandled(true);
    }

    public static void encode(PacketDataRequest pkt, PacketBuffer buf) {
        pkt.coord4D.write(buf);
    }

    public static PacketDataRequest decode(PacketBuffer buf) {
        return new PacketDataRequest(Coord4D.read(buf));
    }
}