package mekanism.common.content.filter;

import mekanism.common.base.TagCache;
import mekanism.common.integration.computer.annotation.ComputerMethod;
import net.minecraft.core.HolderLookup;
import net.minecraft.world.item.crafting.display.SlotDisplay;

public interface ITagFilter<FILTER extends ITagFilter<FILTER>> extends IFilter<FILTER> {

    @ComputerMethod(threadSafe = true)
    void setTagName(String name);

    @ComputerMethod(threadSafe = true)
    String getTagName();

    @Override
    default boolean hasFilter() {
        return !getTagName().isEmpty();
    }

    @Override
    default SlotDisplay asSlotDisplay(HolderLookup.Provider registries) {
        return TagCache.getTagItems(registries, getTagName()).display();
    }
}