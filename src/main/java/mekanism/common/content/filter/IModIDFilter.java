package mekanism.common.content.filter;

import mekanism.common.integration.computer.annotation.ComputerMethod;

public interface IModIDFilter<FILTER extends IModIDFilter<FILTER>> extends IFilter<FILTER> {

    @ComputerMethod(threadSafe = true)
    void setModID(String id);

    @ComputerMethod(threadSafe = true)
    String getModID();

    @Override
    default boolean hasFilter() {
        return getModID() != null && !getModID().isEmpty();
    }
}