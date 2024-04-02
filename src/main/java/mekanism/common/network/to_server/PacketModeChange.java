package mekanism.common.network.to_server;

import mekanism.common.Mekanism;
import mekanism.common.item.interfaces.IModeItem;
import mekanism.common.item.interfaces.IModeItem.DisplayChange;
import mekanism.common.network.IMekanismPacket;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.handling.PlayPayloadContext;
import org.jetbrains.annotations.NotNull;

public record PacketModeChange(EquipmentSlot slot, int shift, boolean displayChangeMessage) implements IMekanismPacket<PlayPayloadContext> {

    public static final ResourceLocation ID = Mekanism.rl("mode");

    public PacketModeChange(FriendlyByteBuf buffer) {
        this(buffer.readEnum(EquipmentSlot.class), buffer.readVarInt(), buffer.readBoolean());
    }

    public PacketModeChange(EquipmentSlot slot, boolean holdingShift) {
        this(slot, holdingShift ? -1 : 1, true);
    }

    public PacketModeChange(EquipmentSlot slot, int shift) {
        this(slot, shift, false);
    }

    @NotNull
    @Override
    public ResourceLocation id() {
        return ID;
    }

    @Override
    public void handle(PlayPayloadContext context) {
        Player player = context.player().orElse(null);
        if (player != null) {
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

    @Override
    public void write(@NotNull FriendlyByteBuf buffer) {
        buffer.writeEnum(slot);
        buffer.writeVarInt(shift);
        buffer.writeBoolean(displayChangeMessage);
    }
}