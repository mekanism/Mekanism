package mekanism.common.block.interfaces;

import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;

/**
 * Implement this if the block is electric
 */
public interface IBlockElectric {

    static boolean isInstance(ItemStack stack) {
        return !stack.isEmpty() && stack.getItem() instanceof ItemBlock && ((ItemBlock) stack.getItem()).getBlock() instanceof IBlockElectric;
    }

    default double getUsage() {
        return 0;
    }

    default double getConfigStorage() {
        return 400 * getUsage();
    }

    default double getStorage() {
        return Math.max(getConfigStorage(), getUsage());
    }
}