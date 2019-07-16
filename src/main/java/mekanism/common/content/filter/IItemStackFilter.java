package mekanism.common.content.filter;

import javax.annotation.Nonnull;
import net.minecraft.item.ItemStack;

public interface IItemStackFilter extends IFilter {

    @Nonnull
    ItemStack getItemStack();

    void setItemStack(@Nonnull ItemStack stack);
}