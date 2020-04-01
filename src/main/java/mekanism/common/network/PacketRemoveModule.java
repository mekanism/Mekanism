package mekanism.common.network;

import java.util.function.Supplier;
import mekanism.api.Coord4D;
import mekanism.common.content.gear.Modules;
import mekanism.common.content.gear.Modules.ModuleData;
import mekanism.common.tile.TileEntityModificationStation;
import mekanism.common.util.MekanismUtils;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent.Context;

public class PacketRemoveModule {

    private Coord4D coord4D;
    private ModuleData<?> moduleType;

    public PacketRemoveModule(Coord4D coord, ModuleData<?> type) {
        coord4D = coord;
        moduleType = type;
    }

    public static void handle(PacketRemoveModule message, Supplier<Context> context) {
        PlayerEntity player = context.get().getSender();
        if (player == null) {
            return;
        }
        context.get().enqueueWork(() -> {
            TileEntityModificationStation tile = MekanismUtils.getTileEntity(TileEntityModificationStation.class, player.world, message.coord4D.getPos());
            if (tile != null) {
                tile.removeModule(player, message.moduleType);
            }
        });
        context.get().setPacketHandled(true);
    }

    public static void encode(PacketRemoveModule pkt, PacketBuffer buf) {
        pkt.coord4D.write(buf);
        buf.writeString(pkt.moduleType.getName());
    }

    public static PacketRemoveModule decode(PacketBuffer buf) {
        return new PacketRemoveModule(Coord4D.read(buf), Modules.get(buf.readString()));
    }
}
