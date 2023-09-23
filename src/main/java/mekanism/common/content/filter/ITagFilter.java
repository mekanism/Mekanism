package mekanism.common.content.filter;

import mekanism.common.integration.computer.annotation.ComputerMethod;

public interface ITagFilter<FILTER extends ITagFilter<FILTER>> extends IFilter<FILTER> {

    @ComputerMethod(threadSafe = true)
    void setTagName(String name);

    @ComputerMethod(threadSafe = true)
    String getTagName();

    @Override
    default boolean hasFilter() {
        return getTagName() != null && !getTagName().isEmpty();
    }
}