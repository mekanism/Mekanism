package mekanism.common.item.interfaces;

import mekanism.common.lib.radial.IGenericRadialModeItem;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface IModeItem {

    /**
     * Changes the current mode of the item
     *
     * @param player               The player who made the mode change.
     * @param stack                The stack to change the mode of
     * @param shift                The amount to shift the mode by, may be negative for indicating the mode should decrease.
     * @param displayChangeMessage {@code true} if a message should be displayed when the mode changes
     */
    void changeMode(@NotNull Player player, @NotNull ItemStack stack, int shift, boolean displayChangeMessage);

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
}