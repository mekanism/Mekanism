package mekanism.common.item;

import mekanism.api.energy.IEnergizedItem;
import mekanism.common.util.ItemDataUtils;
import net.minecraft.item.ItemStack;

/**
 * Used as a impl wrapper for energy items that have default implementations that use IEnergizedItem
 */
//TODO: IC2
/*@InterfaceList({
      @Interface(iface = "ic2.api.item.ISpecialElectricItem", modid = MekanismHooks.IC2_MOD_ID)
})*/
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

    //IC2
    //TODO: IC2
    /*@Override
    @Method(modid = MekanismHooks.IC2_MOD_ID)
    default IElectricItemManager getManager(ItemStack itemStack) {
        return IC2ItemManager.getManager(this);
    }*/
}