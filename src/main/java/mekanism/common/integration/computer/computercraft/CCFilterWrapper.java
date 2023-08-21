package mekanism.common.integration.computer.computercraft;

import dan200.computercraft.api.lua.IDynamicLuaObject;
import mekanism.common.content.filter.IFilter;
import mekanism.common.integration.computer.FactoryRegistry;

public class CCFilterWrapper<FILTER extends IFilter<?>> extends CCMethodCaller implements IDynamicLuaObject {
    final FILTER filter;

    public CCFilterWrapper(FILTER filter) {
        this.filter = filter;
        FactoryRegistry.bindTo(this, filter);
    }
}
