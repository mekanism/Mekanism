package mekanism.common.item.interfaces;

import java.util.function.Function;
import mekanism.client.render.hud.MekanismStatusOverlay;
import mekanism.common.Mekanism;
import mekanism.common.lib.radial.IGenericRadialModeItem;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface IModeItem {

    /**
     * Changes the current mode of the item
     *
     * @param player        The player who made the mode change.
     * @param stack         The stack to change the mode of
     * @param shift         The amount to shift the mode by, may be negative for indicating the mode should decrease.
     * @param displayChange {@code true} if a message should be displayed when the mode changes
     */
    void changeMode(@NotNull Player player, @NotNull ItemStack stack, int shift, DisplayChange displayChange);

    default boolean supportsSlotType(ItemStack stack, @NotNull EquipmentSlot slotType) {
        return slotType == EquipmentSlot.MAINHAND || slotType == EquipmentSlot.OFFHAND;
    }

    @Nullable
    default Component getScrollTextComponent(@NotNull ItemStack stack) {
        return null;
    }

    static boolean isModeItem(@NotNull Player player, @NotNull EquipmentSlot slotType) {
        return isModeItem(player, slotType, true);
    }

    static boolean isModeItem(@NotNull Player player, @NotNull EquipmentSlot slotType, boolean allowRadial) {
        return isModeItem(player.getItemBySlot(slotType), slotType, allowRadial);
    }

    static boolean isModeItem(@NotNull ItemStack stack, @NotNull EquipmentSlot slotType) {
        return isModeItem(stack, slotType, true);
    }

    static boolean isModeItem(@NotNull ItemStack stack, @NotNull EquipmentSlot slotType, boolean allowRadial) {
        if (!stack.isEmpty() && stack.getItem() instanceof IModeItem modeItem && modeItem.supportsSlotType(stack, slotType)) {
            return allowRadial || !(modeItem instanceof IGenericRadialModeItem radialModeItem) || radialModeItem.getRadialData(stack) == null;
        }
        return false;
    }

    static void displayModeChange(Player player) {
        if (player instanceof ServerPlayer serverPlayer) {
            Mekanism.packetHandler().showModeChange(serverPlayer);
        } else {
            MekanismStatusOverlay.INSTANCE.setTimer();
        }
    }

    enum DisplayChange {
        NONE,
        MAIN_HAND,
        OTHER;

        public <DATA> void sendMessage(Player player, DATA data, Function<DATA, Component> message) {
            if (this == MAIN_HAND) {
                //TODO: Eventually decide if we want to make it so that it checks if IModeItem#getScrollTextComponent is null and otherwise just make it a system message
                displayModeChange(player);
            } else if (this == OTHER) {
                player.displayClientMessage(message.apply(data), true);
            }
        }
    }

    interface IAttachmentBasedModeItem<MODE> extends IModeItem {

        DataComponentType<MODE> getModeDataType();

        MODE getDefaultMode();

        default MODE getMode(ItemStack stack) {
            return stack.getOrDefault(getModeDataType(), getDefaultMode());
        }

        default void setMode(ItemStack stack, Player player, MODE mode) {
            stack.set(getModeDataType(), mode);
        }
    }
}