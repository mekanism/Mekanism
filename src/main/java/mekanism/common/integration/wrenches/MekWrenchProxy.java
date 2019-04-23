package mekanism.common.integration.wrenches;

import mekanism.api.IMekWrench;
import net.minecraft.item.ItemStack;

/**
 * Abstraction for non-Mek wrench providers.
 */
public interface MekWrenchProxy {

    /**
     * Get a handler for the wrench
     *
     * @param stack the instance to be used.
     * @return a IMekWrench instance that can handle translation to the external provider.
     */
    IMekWrench get(ItemStack stack);
}
