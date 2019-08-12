package mekanism.common.block.interfaces;

import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;

//TODO: Rename to IUpgradeHolder??
public interface ISupportsUpgrades {

    //TODO: Move list of supported upgrades to here from having to be in TileEntity

    static boolean isInstance(ItemStack stack) {
        return !stack.isEmpty() && stack.getItem() instanceof BlockItem && ((BlockItem) stack.getItem()).getBlock() instanceof ISupportsUpgrades;
    }
}