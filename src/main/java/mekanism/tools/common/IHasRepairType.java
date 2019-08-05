package mekanism.tools.common;

import net.minecraft.item.ItemStack;

public interface IHasRepairType {

    /**
     * Gets the stack that can be used to repair this item. This is used to simplify getting repair type dynamically for adding to JEI's Anvil recipe category.
     */
    ItemStack getRepairStack();
}