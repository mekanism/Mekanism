package mekanism.common.item;

import ic2.api.item.IElectricItemManager;
import ic2.api.item.ISpecialElectricItem;
import mekanism.api.energy.IEnergizedItem;
import mekanism.common.integration.MekanismHooks;
import mekanism.common.integration.ic2.IC2ItemManager;
import mekanism.common.util.ItemDataUtils;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.Optional.Interface;
import net.minecraftforge.fml.common.Optional.InterfaceList;
import net.minecraftforge.fml.common.Optional.Method;

/**
 * Used as a impl wrapper for energy items that have default implementations that use IEnergizedItem
 */
@InterfaceList({
      @Interface(iface = "ic2.api.item.ISpecialElectricItem", modid = MekanismHooks.IC2_MOD_ID)
})
public interface IItemEnergized extends IEnergizedItem, ISpecialElectricItem {

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
    @Override
    @Method(modid = MekanismHooks.IC2_MOD_ID)
    default IElectricItemManager getManager(ItemStack itemStack) {
        return IC2ItemManager.getManager(this);
    }
}