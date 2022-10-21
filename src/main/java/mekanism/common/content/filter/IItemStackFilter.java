package mekanism.common.content.filter;

import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

public interface IItemStackFilter<FILTER extends IItemStackFilter<FILTER>> extends IFilter<FILTER> {

    @NotNull
    ItemStack getItemStack();

    void setItemStack(@NotNull ItemStack stack);

    @Override
    default boolean hasFilter() {
        return !getItemStack().isEmpty();
    }
}