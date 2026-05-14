package mekanism.common.content.filter;

import mekanism.common.integration.computer.annotation.ComputerMethod;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.transfer.item.ItemResource;
import org.jetbrains.annotations.NotNull;

public interface IItemStackFilter<FILTER extends IItemStackFilter<FILTER>> extends IFilter<FILTER> {

    @NotNull
    //@ComputerMethod(threadSafe = true)//TODO - 26.1: Expose item resources to computers
    ItemResource getItemType();

    //@ComputerMethod(threadSafe = true)//TODO - 26.1: Expose item resources to computers
    void setItemType(@NotNull ItemResource itemType);

    @Override
    default boolean hasFilter() {
        return !getItemType().isEmpty();
    }

    @ComputerMethod
    default void setItem(@NotNull Item item) {
        setItemType(ItemResource.of(item));
    }
}
