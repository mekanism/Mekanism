package mekanism.common.tile.interfaces;

import mekanism.common.content.filter.FilterManager;
import mekanism.common.content.filter.IFilter;

public interface ITileFilterHolder<FILTER extends IFilter<?>> {

    FilterManager<FILTER> getFilterManager();
}