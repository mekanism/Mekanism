package mekanism.common.item.interfaces;

import javax.annotation.Nonnull;
import net.minecraft.world.item.ItemStack;

public interface ISaveModeItem {

    /**
     * Saves the current state to a profile of the item
     * @param stack                The stack to change the mode of
     * @param modeId               The mode to save to
     */
    void saveMode(@Nonnull ItemStack stack, int modeId);
}