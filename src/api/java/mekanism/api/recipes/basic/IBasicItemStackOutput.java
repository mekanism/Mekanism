package mekanism.api.recipes.basic;

import net.minecraft.world.item.ItemStackTemplate;

public interface IBasicItemStackOutput {

    /// For Serializer use. DO NOT MODIFY RETURN VALUE.
    ///
    /// @return the uncopied basic output
    ItemStackTemplate getOutputRaw();
}
