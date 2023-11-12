package mekanism.api.recipes.basic;

import net.minecraft.world.item.ItemStack;

public interface IBasicItemStackOutput {

    /**
     * For Serializer use. DO NOT MODIFY RETURN VALUE.
     *
     * @return the uncopied basic output
     */
    ItemStack getOutputRaw();
}
