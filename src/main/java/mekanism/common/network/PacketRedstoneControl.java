package mekanism.common.network;

import java.util.function.Supplier;
import mekanism.api.Coord4D;
import mekanism.common.PacketHandler;
import mekanism.common.base.IRedstoneControl;
import mekanism.common.base.IRedstoneControl.RedstoneControl;
import mekanism.common.util.MekanismUtils;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.fml.network.NetworkEvent.Context;

public class PacketRedstoneControl {

    private RedstoneControl value;
    private Coord4D coord4D;

    public PacketRedstoneControl(Coord4D coord, RedstoneControl control) {
        coord4D = coord;
        value = control;
    }

    public static void handle(PacketRedstoneControl message, Supplier<Context> context) {
        PlayerEntity player = PacketHandler.getPlayer(context);
        if (player == null) {
            return;
        }
        context.get().enqueueWork(() -> {
            TileEntity tile = MekanismUtils.getTileEntity(player.world, message.coord4D.getPos());
            if (tile instanceof IRedstoneControl) {
                ((IRedstoneControl) tile).setControlType(message.value);
            }
        });
        context.get().setPacketHandled(true);
    }

    public static void encode(PacketRedstoneControl pkt, PacketBuffer buf) {
        pkt.coord4D.write(buf);
        buf.writeEnumValue(pkt.value);
    }

    public static PacketRedstoneControl decode(PacketBuffer buf) {
        return new PacketRedstoneControl(Coord4D.read(buf), buf.readEnumValue(RedstoneControl.class));
    }
}