package mekanism.common.network;

import java.util.function.Supplier;
import mekanism.api.Coord4D;
import mekanism.common.Mekanism;
import mekanism.common.PacketHandler;
import mekanism.common.base.ITankManager;
import mekanism.common.base.ITankManager.DropperHandler;
import mekanism.common.util.MekanismUtils;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.fml.network.NetworkEvent.Context;

public class PacketDropperUse {

    private Coord4D coord4D;
    private int mouseButton;
    private int tankId;

    public PacketDropperUse(Coord4D coord, int button, int id) {
        coord4D = coord;
        mouseButton = button;
        tankId = id;
    }

    public static void handle(PacketDropperUse message, Supplier<Context> context) {
        PlayerEntity player = PacketHandler.getPlayer(context);
        if (player == null) {
            return;
        }
        context.get().enqueueWork(() -> {
            TileEntity tile = MekanismUtils.getTileEntity(player.world, message.coord4D.getPos());
            if (tile instanceof ITankManager) {
                try {
                    Object tank = ((ITankManager) tile).getManagedTanks()[message.tankId];
                    if (tank != null) {
                        DropperHandler.useDropper(player, tank, message.mouseButton);
                    }
                } catch (Exception e) {
                    Mekanism.logger.error("FIXME: Packet handling error", e);
                }
            }
        });
        context.get().setPacketHandled(true);
    }

    public static void encode(PacketDropperUse pkt, PacketBuffer buf) {
        pkt.coord4D.write(buf);
        buf.writeInt(pkt.mouseButton);
        buf.writeInt(pkt.tankId);
    }

    public static PacketDropperUse decode(PacketBuffer buf) {
        Coord4D coord4D = Coord4D.read(buf);
        return new PacketDropperUse(coord4D, buf.readInt(), buf.readInt());
    }
}