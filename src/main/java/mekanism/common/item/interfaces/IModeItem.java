package mekanism.common.item.interfaces;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.ITextComponent;

public interface IModeItem {

    /**
     * Changes the current mode of the item
     *
     * @param player               The player who made the mode change.
     * @param stack                The stack to change the mode of
     * @param shift                The amount to shift the mode by, may be negative for indicating the mode should decrease.
     * @param displayChangeMessage {@code true} if a message should be displayed when the mode changes
     */
    void changeMode(@Nonnull PlayerEntity player, @Nonnull ItemStack stack, int shift, boolean displayChangeMessage);

    default boolean supportsSlotType(ItemStack stack, @Nonnull EquipmentSlotType slotType) {
        return slotType == EquipmentSlotType.MAINHAND || slotType == EquipmentSlotType.OFFHAND;
    }

    @Nullable
    default ITextComponent getScrollTextComponent(@Nonnull ItemStack stack) {
        return null;
    }

    static boolean isModeItem(@Nonnull PlayerEntity player, @Nonnull EquipmentSlotType slotType) {
        return isModeItem(player, slotType, true);
    }

    static boolean isModeItem(@Nonnull PlayerEntity player, @Nonnull EquipmentSlotType slotType, boolean allowRadial) {
        return isModeItem(player.getItemStackFromSlot(slotType), slotType, allowRadial);
    }

    static boolean isModeItem(@Nonnull ItemStack stack, @Nonnull EquipmentSlotType slotType) {
        return isModeItem(stack, slotType, true);
    }

    static boolean isModeItem(@Nonnull ItemStack stack, @Nonnull EquipmentSlotType slotType, boolean allowRadial) {
        return !stack.isEmpty() && stack.getItem() instanceof IModeItem &&
               ((IModeItem) stack.getItem()).supportsSlotType(stack, slotType) &&
               (allowRadial || !(stack.getItem() instanceof IRadialModeItem));
    }
}