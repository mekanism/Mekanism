package mekanism.common.content.qio.filter;

import mekanism.common.content.filter.BaseFilter;
import mekanism.common.integration.computer.annotation.ComputerMethod;
import mekanism.common.lib.inventory.Finder;

public abstract class QIOFilter<FILTER extends QIOFilter<FILTER>> extends BaseFilter<FILTER> {

    protected QIOFilter() {
    }

    protected QIOFilter(FILTER filter) {
        super(filter);
    }

    public abstract Finder getFinder();

    @Override
    @ComputerMethod(threadSafe = true)
    public abstract FILTER clone();
}
