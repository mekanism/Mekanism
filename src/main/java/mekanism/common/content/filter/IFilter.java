package mekanism.common.content.filter;

import java.util.function.Supplier;
import mekanism.common.integration.computer.annotation.ComputerMethod;
import net.minecraft.core.HolderLookup;
import net.minecraft.world.item.crafting.display.SlotDisplay;
import org.jspecify.annotations.Nullable;

public interface IFilter<FILTER extends IFilter<FILTER>> {

    FILTER clone();

    @ComputerMethod(threadSafe = true)
    FilterType getFilterType();

    boolean hasFilter();

    SlotDisplay asSlotDisplay(HolderLookup.Provider registries);

    @ComputerMethod(threadSafe = true)
    boolean isEnabled();

    @ComputerMethod(threadSafe = true)
    void setEnabled(boolean enabled);

    void setRegistryAccess(Supplier<? extends HolderLookup.@Nullable Provider> registryAccess);
}