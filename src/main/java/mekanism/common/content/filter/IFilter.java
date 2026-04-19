package mekanism.common.content.filter;

import mekanism.common.integration.computer.annotation.ComputerMethod;
import net.minecraft.core.HolderLookup;

public interface IFilter<FILTER extends IFilter<FILTER>> {

    FILTER clone();

    @ComputerMethod(threadSafe = true)
    FilterType getFilterType();

    boolean hasFilter();

    @ComputerMethod(threadSafe = true)
    boolean isEnabled();

    @ComputerMethod(threadSafe = true)
    void setEnabled(boolean enabled);

    void setRegistryAccess(HolderLookup.Provider registryAccess);
}