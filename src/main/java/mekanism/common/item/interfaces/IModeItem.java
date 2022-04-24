package mekanism.common.item.interfaces;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public interface IModeItem {

    /**
     * Changes the current mode of the item
     *
     * @param player               The player who made the mode change.
     * @param stack                The stack to change the mode of
     * @param shift                The amount to shift the mode by, may be negative for indicating the mode should decrease.
     * @param displayChangeMessage {@code true} if a message should be displayed when the mode changes
     */
    void changeMode(@Nonnull Player player, @Nonnull ItemStack stack, int shift, boolean displayChangeMessage);

    default boolean supportsSlotType(ItemStack stack, @Nonnull EquipmentSlot slotType) {
        return slotType == EquipmentSlot.MAINHAND || slotType == EquipmentSlot.OFFHAND;
    }

    @Nullable
    default Component getScrollTextComponent(@Nonnull ItemStack stack) {
        return null;
    }

    static boolean isModeItem(@Nonnull Player player, @Nonnull EquipmentSlot slotType) {
        return isModeItem(player, slotType, true);
    }

    static boolean isModeItem(@Nonnull Player player, @Nonnull EquipmentSlot slotType, boolean allowRadial) {
        return isModeItem(player.getItemBySlot(slotType), slotType, allowRadial);
    }

    static boolean isModeItem(@Nonnull ItemStack stack, @Nonnull EquipmentSlot slotType) {
        return isModeItem(stack, slotType, true);
    }

    static boolean isModeItem(@Nonnull ItemStack stack, @Nonnull EquipmentSlot slotType, boolean allowRadial) {
        return !stack.isEmpty() && stack.getItem() instanceof IModeItem modeItem && modeItem.supportsSlotType(stack, slotType) &&
               (allowRadial || !(stack.getItem() instanceof IRadialModeItem));
    }
}