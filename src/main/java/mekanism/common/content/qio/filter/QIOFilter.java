package mekanism.common.content.qio.filter;

import mekanism.common.content.filter.BaseFilter;
import mekanism.common.lib.inventory.Finder;

public abstract class QIOFilter<FILTER extends QIOFilter<FILTER>> extends BaseFilter<FILTER> {

    public abstract Finder getFinder();
}
