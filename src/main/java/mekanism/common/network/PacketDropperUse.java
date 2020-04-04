package mekanism.common.network;

import java.util.function.Supplier;
import mekanism.common.Mekanism;
import mekanism.common.base.ITankManager;
import mekanism.common.base.ITankManager.DropperHandler;
import mekanism.common.util.MekanismUtils;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.network.NetworkEvent.Context;

//TODO: Re-evaluate/rewrite
public class PacketDropperUse {

    private final BlockPos pos;
    private final int mouseButton;
    private final int tankId;

    public PacketDropperUse(BlockPos pos, int button, int id) {
        this.pos = pos;
        mouseButton = button;
        tankId = id;
    }

    public static void handle(PacketDropperUse message, Supplier<Context> context) {
        PlayerEntity player = BasePacketHandler.getPlayer(context);
        if (player == null) {
            return;
        }
        context.get().enqueueWork(() -> {
            TileEntity tile = MekanismUtils.getTileEntity(player.world, message.pos);
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
        buf.writeBlockPos(pkt.pos);
        buf.writeVarInt(pkt.mouseButton);
        buf.writeVarInt(pkt.tankId);
    }

    public static PacketDropperUse decode(PacketBuffer buf) {
        return new PacketDropperUse(buf.readBlockPos(), buf.readVarInt(), buf.readVarInt());
    }
}