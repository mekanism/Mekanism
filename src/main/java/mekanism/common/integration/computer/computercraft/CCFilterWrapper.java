package mekanism.common.integration.computer.computercraft;

import dan200.computercraft.api.lua.IDynamicLuaObject;
import mekanism.common.content.filter.IFilter;
import mekanism.common.integration.computer.ComputerException;
import mekanism.common.integration.computer.FactoryRegistry;

/**
 * Wrap a filter as a copy that can be modified by instance methods on the CC side. Clones the filter on creation and output to ensure the filter is not modified while in
 * use.
 *
 * @param <FILTER> the filter Parent type
 */
public class CCFilterWrapper<FILTER extends IFilter<?>> extends CCMethodCaller implements IDynamicLuaObject {

    private final FILTER filter;

    @SuppressWarnings("unchecked")
    public CCFilterWrapper(FILTER filter) {
        this.filter = (FILTER) filter.clone();
        FactoryRegistry.bindTo(this, this.filter);
    }

    public <EXPECTED extends IFilter<EXPECTED>> EXPECTED getAs(Class<EXPECTED> expectedType) throws ComputerException {
        if (!expectedType.isInstance(filter)) {
            throw new ComputerException("Wrong filter type supplied - expected " + expectedType.getSimpleName() + " but found " + filter.getClass().getSimpleName());
        }
        return expectedType.cast(filter.clone());
    }
}
