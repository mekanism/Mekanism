package mekanism.common.network.to_server;

import io.netty.buffer.ByteBuf;
import mekanism.common.Mekanism;
import mekanism.common.item.interfaces.IModeItem;
import mekanism.common.item.interfaces.IModeItem.DisplayChange;
import mekanism.common.network.IMekanismPacket;
import mekanism.common.network.PacketUtils;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.NotNull;

public record PacketModeChange(EquipmentSlot slot, int shift, boolean displayChangeMessage) implements IMekanismPacket {

    public static final CustomPacketPayload.Type<PacketModeChange> TYPE = new CustomPacketPayload.Type<>(Mekanism.rl("mode"));
    public static final StreamCodec<ByteBuf, PacketModeChange> STREAM_CODEC = StreamCodec.composite(
          PacketUtils.EQUIPMENT_SLOT_STREAM_CODEC, PacketModeChange::slot,
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
        ItemStack stack = player.getItemBySlot(slot);
        if (!stack.isEmpty() && stack.getItem() instanceof IModeItem modeItem) {
            DisplayChange displayChange;
            if (displayChangeMessage) {
                displayChange = slot == EquipmentSlot.MAINHAND ? DisplayChange.MAIN_HAND : DisplayChange.OTHER;
            } else {
                displayChange = DisplayChange.NONE;
            }
            modeItem.changeMode(player, stack, shift, displayChange);
        }
    }
}