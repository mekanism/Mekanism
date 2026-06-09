package mekanism.common.content.filter;

import mekanism.common.integration.computer.annotation.ComputerMethod;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.transfer.item.ItemResource;

public interface IItemStackFilter<FILTER extends IItemStackFilter<FILTER>> extends IFilter<FILTER> {

    @ComputerMethod(threadSafe = true)
    ItemResource getItemType();

    @ComputerMethod(threadSafe = true)
    void setItemType(ItemResource itemType);

    @Override
    default boolean hasFilter() {
        return !getItemType().isEmpty();
    }

    @ComputerMethod
    default void setItem(Item item) {
        setItemType(ItemResource.of(item));
    }
}
