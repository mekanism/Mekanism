package mekanism.common.network;

import java.util.function.Supplier;
import mekanism.api.Coord4D;
import mekanism.common.PacketHandler;
import mekanism.common.base.IFluidContainerManager;
import mekanism.common.util.FluidContainerUtils.ContainerEditMode;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.fml.network.NetworkEvent.Context;

public class PacketContainerEditMode {

    private ContainerEditMode value;
    private Coord4D coord4D;

    public PacketContainerEditMode(Coord4D coord, ContainerEditMode mode) {
        coord4D = coord;
        value = mode;
    }

    public static void handle(PacketContainerEditMode message, Supplier<Context> context) {
        PlayerEntity player = PacketHandler.getPlayer(context);
        if (player == null) {
            return;
        }
        context.get().enqueueWork(() -> {
            TileEntity tileEntity = message.coord4D.getTileEntity(player.world);
            if (tileEntity instanceof IFluidContainerManager) {
                ((IFluidContainerManager) tileEntity).setContainerEditMode(message.value);
            }
        });
        context.get().setPacketHandled(true);
    }

    public static void encode(PacketContainerEditMode pkt, PacketBuffer buf) {
        pkt.coord4D.write(buf);
        buf.writeEnumValue(pkt.value);
    }

    public static PacketContainerEditMode decode(PacketBuffer buf) {
        Coord4D coord4D = Coord4D.read(buf);
        ContainerEditMode value = buf.readEnumValue(ContainerEditMode.class);
        return new PacketContainerEditMode(coord4D, value);
    }
}