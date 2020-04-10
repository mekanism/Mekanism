package mekanism.common.network;

import java.util.function.Supplier;
import mekanism.common.tile.interfaces.IHasFrequency;
import mekanism.common.util.MekanismUtils;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.network.NetworkEvent.Context;

public class PacketGuiSetFrequency {

    private final BlockPos tilePosition;
    private final boolean setFrequency;
    private final boolean isPublic;
    private final String name;

    public PacketGuiSetFrequency(BlockPos tilePosition, boolean setFrequency, String name, boolean isPublic) {
        this.tilePosition = tilePosition;
        this.setFrequency = setFrequency;
        this.isPublic = isPublic;
        this.name = name;
    }

    public static void handle(PacketGuiSetFrequency message, Supplier<Context> context) {
        PlayerEntity player = BasePacketHandler.getPlayer(context);
        if (player == null) {
            return;
        }
        context.get().enqueueWork(() -> {
            TileEntity tile = MekanismUtils.getTileEntity(player.world, message.tilePosition);
            if (tile instanceof IHasFrequency) {
                if (message.setFrequency) {
                    ((IHasFrequency) tile).setFrequency(message.name, message.isPublic);
                } else {
                    ((IHasFrequency) tile).removeFrequency(message.name, message.isPublic);
                }
            }
        });
        context.get().setPacketHandled(true);
    }

    public static void encode(PacketGuiSetFrequency pkt, PacketBuffer buf) {
        buf.writeBlockPos(pkt.tilePosition);
        buf.writeBoolean(pkt.setFrequency);
        buf.writeString(pkt.name);
        buf.writeBoolean(pkt.isPublic);
    }

    public static PacketGuiSetFrequency decode(PacketBuffer buf) {
        return new PacketGuiSetFrequency(buf.readBlockPos(), buf.readBoolean(), BasePacketHandler.readString(buf), buf.readBoolean());
    }
}