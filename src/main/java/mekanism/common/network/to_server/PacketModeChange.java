package mekanism.common.network.to_server;

import io.netty.buffer.ByteBuf;
import mekanism.common.Mekanism;
import mekanism.common.item.interfaces.IModeItem;
import mekanism.common.item.interfaces.IModeItem.DisplayChange;
import mekanism.common.network.IMekanismPacket;
import mekanism.common.util.ItemAccessUtils;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.neoforged.neoforge.transfer.access.ItemAccess;
import net.neoforged.neoforge.transfer.transaction.Transaction;
import org.jetbrains.annotations.NotNull;

public record PacketModeChange(EquipmentSlot slot, int shift, boolean displayChangeMessage) implements IMekanismPacket {

    public static final CustomPacketPayload.Type<PacketModeChange> TYPE = new CustomPacketPayload.Type<>(Mekanism.rl("mode"));
    public static final StreamCodec<ByteBuf, PacketModeChange> STREAM_CODEC = StreamCodec.composite(
          EquipmentSlot.STREAM_CODEC, PacketModeChange::slot,
          ByteBufCodecs.VAR_INT, PacketModeChange::shift,
          ByteBufCodecs.BOOL, PacketModeChange::displayChangeMessage,
          PacketModeChange::new
    );

    public PacketModeChange(EquipmentSlot slot, boolean holdingShift) {
        this(slot, holdingShift ? -1 : 1, true);
    }

    public PacketModeChange(EquipmentSlot slot, int shift) {
        this(slot, shift, false);
    }

    @NotNull
    @Override
    public CustomPacketPayload.Type<PacketModeChange> type() {
        return TYPE;
    }

    @Override
    public void handle(IPayloadContext context) {
        Player player = context.player();
        ItemAccess itemAccess = ItemAccessUtils.forEntitySlot(player, slot);
        if (itemAccess.getResource().getItem() instanceof IModeItem modeItem) {
            try (Transaction transaction = Transaction.openRoot()) {
                modeItem.changeMode(player, itemAccess, shift, displayChange(), transaction);
                transaction.commit();
            }
        }
    }

    private DisplayChange displayChange() {
        if (displayChangeMessage) {
            return slot == EquipmentSlot.MAINHAND ? DisplayChange.MAIN_HAND : DisplayChange.OTHER;
        }
        return DisplayChange.NONE;
    }
}