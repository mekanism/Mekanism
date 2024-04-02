package mekanism.common.network.to_server;

import mekanism.common.Mekanism;
import mekanism.common.integration.curios.CuriosIntegration;
import mekanism.common.item.interfaces.IModeItem;
import mekanism.common.item.interfaces.IModeItem.DisplayChange;
import mekanism.common.network.IMekanismPacket;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.handling.PlayPayloadContext;
import org.jetbrains.annotations.NotNull;

public record PacketModeChangeCurios(String slotType, int slot, int shift, boolean displayChangeMessage) implements IMekanismPacket<PlayPayloadContext> {

    public static final ResourceLocation ID = Mekanism.rl("mode_curios");

    public PacketModeChangeCurios(FriendlyByteBuf buffer) {
        this(buffer.readUtf(), buffer.readVarInt(), buffer.readVarInt(), buffer.readBoolean());
    }

    public PacketModeChangeCurios(String slotType, int slot, boolean holdingShift) {
        this(slotType, slot, holdingShift ? -1 : 1, true);
    }

    public PacketModeChangeCurios(String slotType, int slot, int shift) {
        this(slotType, slot, shift, false);
    }

    @NotNull
    @Override
    public ResourceLocation id() {
        return ID;
    }

    @Override
    public void handle(PlayPayloadContext context) {
        if (Mekanism.hooks.CuriosLoaded) {
            Player player = context.player().orElse(null);
            if (player != null) {
                ItemStack stack = CuriosIntegration.getCurioStack(player, slotType, slot);
                if (!stack.isEmpty() && stack.getItem() instanceof IModeItem modeItem) {
                    modeItem.changeMode(player, stack, shift, displayChangeMessage ? DisplayChange.OTHER : DisplayChange.NONE);
                }
            }
        }
    }

    @Override
    public void write(@NotNull FriendlyByteBuf buffer) {
        buffer.writeUtf(slotType);
        buffer.writeVarInt(slot);
        buffer.writeVarInt(shift);
        buffer.writeBoolean(displayChangeMessage);
    }
}