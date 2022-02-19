package mekanism.common.tile.interfaces;

import mekanism.common.content.filter.IFilter;
import mekanism.common.lib.collection.HashList;

public interface ITileFilterHolder<FILTER extends IFilter<?>> {

    HashList<FILTER> getFilters();
}