package mekanism.common.content.filter;

import mekanism.common.integration.computer.annotation.ComputerMethod;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

public interface IItemStackFilter<FILTER extends IItemStackFilter<FILTER>> extends IFilter<FILTER> {

    @NotNull
    @ComputerMethod(threadSafe = true)
    ItemStack getItemStack();

    @ComputerMethod(threadSafe = true)
    void setItemStack(@NotNull ItemStack stack);

    @Override
    default boolean hasFilter() {
        return !getItemStack().isEmpty();
    }

    @ComputerMethod
    default void setItem(@NotNull Item item) {
        setItemStack(new ItemStack(item));
    }
}
