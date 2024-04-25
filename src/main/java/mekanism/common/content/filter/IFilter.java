package mekanism.common.content.filter;

import mekanism.common.integration.computer.annotation.ComputerMethod;

public interface IFilter<FILTER extends IFilter<FILTER>> {

    FILTER clone();

    @ComputerMethod(threadSafe = true)
    FilterType getFilterType();

    boolean hasFilter();

    @ComputerMethod(threadSafe = true)
    boolean isEnabled();

    @ComputerMethod(threadSafe = true)
    void setEnabled(boolean enabled);
}