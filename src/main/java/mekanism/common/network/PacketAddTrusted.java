package mekanism.common.network;

import java.util.function.Supplier;
import mekanism.common.tile.TileEntitySecurityDesk;
import mekanism.common.util.MekanismUtils;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.network.NetworkEvent.Context;

public class PacketAddTrusted {

    //Constant to make it more clear what is going on and make it easier to change in case Mojang ever ups the max name length
    public static final int MAX_NAME_LENGTH = 16;

    private final BlockPos tilePosition;
    private final String name;

    public PacketAddTrusted(BlockPos tilePosition, String name) {
        this.tilePosition = tilePosition;
        this.name = name;
    }

    public static boolean validateNameLength(int length) {
        return length >= 3 && length <= MAX_NAME_LENGTH;
    }

    public static void handle(PacketAddTrusted message, Supplier<Context> context) {
        PlayerEntity player = BasePacketHandler.getPlayer(context);
        if (player == null) {
            return;
        }
        context.get().enqueueWork(() -> {
            TileEntitySecurityDesk tile = MekanismUtils.getTileEntity(TileEntitySecurityDesk.class, player.world, message.tilePosition);
            if (tile != null) {
                tile.addTrusted(message.name);
            }
        });
        context.get().setPacketHandled(true);
    }

    public static void encode(PacketAddTrusted pkt, PacketBuffer buf) {
        buf.writeBlockPos(pkt.tilePosition);
        buf.writeString(pkt.name, MAX_NAME_LENGTH);
    }

    public static PacketAddTrusted decode(PacketBuffer buf) {
        return new PacketAddTrusted(buf.readBlockPos(), buf.readString(MAX_NAME_LENGTH));
    }
}