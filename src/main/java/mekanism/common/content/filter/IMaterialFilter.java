package mekanism.common.content.filter;

import javax.annotation.Nonnull;
import net.minecraft.item.ItemStack;

public interface IMaterialFilter extends IFilter {

    @Nonnull
    ItemStack getMaterialItem();

    void setMaterialItem(@Nonnull ItemStack stack);
}