package mekanism.common.network;

import java.util.function.Supplier;
import mekanism.common.frequency.Frequency.FrequencyIdentity;
import mekanism.common.frequency.FrequencyType;
import mekanism.common.frequency.IFrequencyHandler;
import mekanism.common.util.MekanismUtils;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.network.NetworkEvent.Context;

public class PacketGuiSetFrequency {

    private final BlockPos tilePosition;
    private final FrequencyType<?> type;
    private final FrequencyUpdate updateType;
    private final FrequencyIdentity data;

    public PacketGuiSetFrequency(BlockPos tilePosition, FrequencyType<?> type, FrequencyUpdate updateType, FrequencyIdentity data) {
        this.tilePosition = tilePosition;
        this.type = type;
        this.updateType = updateType;
        this.data = data;
    }

    public static void handle(PacketGuiSetFrequency message, Supplier<Context> context) {
        PlayerEntity player = BasePacketHandler.getPlayer(context);
        if (player == null) {
            return;
        }
        context.get().enqueueWork(() -> {
            TileEntity tile = MekanismUtils.getTileEntity(player.world, message.tilePosition);
            if (tile instanceof IFrequencyHandler) {
                if (message.updateType == FrequencyUpdate.SET) {
                    ((IFrequencyHandler) tile).setFrequency(message.type, message.data);
                } else {
                    ((IFrequencyHandler) tile).removeFrequency(message.type, message.data);
                }
            }
        });
        context.get().setPacketHandled(true);
    }

    public static void encode(PacketGuiSetFrequency pkt, PacketBuffer buf) {
        buf.writeBlockPos(pkt.tilePosition);
        pkt.type.write(buf);
        buf.writeEnumValue(pkt.updateType);
        pkt.type.getKey().write(buf, pkt.data);
    }

    public static PacketGuiSetFrequency decode(PacketBuffer buf) {
        BlockPos pos = buf.readBlockPos();
        FrequencyType<?> type = FrequencyType.load(buf);
        return new PacketGuiSetFrequency(pos, type, buf.readEnumValue(FrequencyUpdate.class), type.getKey().read(buf));
    }

    public enum FrequencyUpdate {
        SET,
        REMOVE;
    }
}