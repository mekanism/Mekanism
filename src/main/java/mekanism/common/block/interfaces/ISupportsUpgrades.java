package mekanism.common.block.interfaces;

import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;

//TODO: Rename to IUpgradeHolder??
public interface ISupportsUpgrades {

    static boolean isInstance(ItemStack stack) {
        return !stack.isEmpty() && stack.getItem() instanceof BlockItem && ((BlockItem) stack.getItem()).getBlock() instanceof ISupportsUpgrades;
    }
}