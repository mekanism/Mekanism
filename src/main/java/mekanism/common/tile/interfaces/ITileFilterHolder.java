package mekanism.common.tile.interfaces;

import mekanism.common.HashList;
import mekanism.common.content.filter.IFilter;

public interface ITileFilterHolder<FILTER extends IFilter<?>> {

    HashList<FILTER> getFilters();
}