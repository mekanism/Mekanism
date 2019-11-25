package mekanism.common.item;

import mekanism.api.energy.IEnergizedItem;
import mekanism.common.util.ItemDataUtils;
import net.minecraft.item.ItemStack;

/**
 * Used as a impl wrapper for energy items that have default implementations that use IEnergizedItem
 */
public interface IItemEnergized extends IEnergizedItem {

    //Mekanism
    @Override
    default double getEnergy(ItemStack itemStack) {
        return ItemDataUtils.getDouble(itemStack, "energyStored");
    }

    @Override
    default void setEnergy(ItemStack itemStack, double amount) {
        ItemDataUtils.setDouble(itemStack, "energyStored", Math.max(Math.min(amount, getMaxEnergy(itemStack)), 0));
    }
}