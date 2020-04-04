package mekanism.common.network;

import java.util.function.Supplier;
import mekanism.api.TileNetworkList;
import mekanism.common.Mekanism;
import mekanism.common.PacketHandler;
import mekanism.common.base.ITileNetwork;
import mekanism.common.util.MekanismUtils;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.network.NetworkEvent.Context;

@Deprecated//TODO: Remove
public class PacketTileEntity {

    private TileNetworkList parameters;
    private PacketBuffer storedBuffer;
    private BlockPos pos;

    public PacketTileEntity(TileEntity tile, TileNetworkList params) {
        this(tile.getPos());
        parameters = params;
    }

    private PacketTileEntity(BlockPos pos) {
        this.pos = pos;
    }

    public static void handle(PacketTileEntity message, Supplier<Context> context) {
        PlayerEntity player = PacketHandler.getPlayer(context);
        if (player == null) {
            return;
        }
        context.get().enqueueWork(() -> {
            TileEntity tile = MekanismUtils.getTileEntity(player.world, message.pos);
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
        buf.writeBlockPos(pkt.pos);
        PacketHandler.encode(pkt.parameters.toArray(), buf);
    }

    public static PacketTileEntity decode(PacketBuffer buf) {
        PacketTileEntity packet = new PacketTileEntity(buf.readBlockPos());
        packet.storedBuffer = new PacketBuffer(buf.copy());
        return packet;
    }
}