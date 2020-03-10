package mekanism.common.network;

import java.util.function.Supplier;
import mekanism.api.Coord4D;
import mekanism.common.PacketHandler;
import mekanism.common.capabilities.Capabilities;
import mekanism.common.tile.TileEntityMultiblock;
import mekanism.common.util.CapabilityUtils;
import mekanism.common.util.MekanismUtils;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.fml.network.NetworkEvent.Context;

//TODO: Remove the need for this all together
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
            TileEntity tile = MekanismUtils.getTileEntity(player.world, message.coord4D.getPos());
            if (tile instanceof TileEntityMultiblock) {
                //TODO: FIXME, replace this call with something else
                ((TileEntityMultiblock<?>) tile).sendStructure = true;
            }
            CapabilityUtils.getCapability(tile, Capabilities.GRID_TRANSMITTER_CAPABILITY, null).ifPresent(transmitter -> {
                //TODO: FIXME, replace this call with something else
                if (transmitter.hasTransmitterNetwork()) {
                    transmitter.getTransmitterNetwork().addUpdate(player);
                }
            });
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