package mekanism.common.content.filter;

import javax.annotation.Nonnull;
import net.minecraft.item.ItemStack;

public interface IItemStackFilter<FILTER extends IItemStackFilter<FILTER>> extends IFilter<FILTER> {

    @Nonnull
    ItemStack getItemStack();

    void setItemStack(@Nonnull ItemStack stack);

    @Override
    default boolean hasFilter() {
        return !getItemStack().isEmpty();
    }
}