package mekanism.common.integration.wrenches;

import mekanism.api.IMekWrench;
import net.minecraft.item.ItemStack;

/**
 * Default handler for IMekWrench items, returns the Itemstack#item
 */
public class MekPassthrough implements MekWrenchProxy {

    @Override
    public IMekWrench get(ItemStack stack) {
        return stack.getItem() instanceof IMekWrench ? (IMekWrench) stack.getItem() : null;
    }
}
