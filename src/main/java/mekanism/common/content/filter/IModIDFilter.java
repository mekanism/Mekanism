package mekanism.common.content.filter;

import mekanism.common.base.TagCache;
import mekanism.common.integration.computer.annotation.ComputerMethod;
import net.minecraft.core.HolderLookup;
import net.minecraft.world.item.crafting.display.SlotDisplay;

public interface IModIDFilter<FILTER extends IModIDFilter<FILTER>> extends IFilter<FILTER> {

    @ComputerMethod(threadSafe = true)
    void setModID(String id);

    @ComputerMethod(threadSafe = true)
    String getModID();

    @Override
    default boolean hasFilter() {
        return !getModID().isEmpty();
    }

    @Override
    default SlotDisplay asSlotDisplay(HolderLookup.Provider registries) {
        return TagCache.getModIdItems(registries, getModID()).display();
    }
}