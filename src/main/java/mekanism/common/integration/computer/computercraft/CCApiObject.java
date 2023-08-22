package mekanism.common.integration.computer.computercraft;

import dan200.computercraft.api.lua.IDynamicLuaObject;
import dan200.computercraft.api.lua.ILuaAPI;
import dan200.computercraft.api.lua.ILuaAPIFactory;
import mekanism.common.integration.computer.FactoryRegistry;

/**
 * Registers a CC Api handler. Static classes only for now.
 */
public class CCApiObject extends CCMethodCaller implements IDynamicLuaObject, ILuaAPI {
    static ILuaAPIFactory create(Class<?> source, String... apiNames) {
        return computer -> {
            CCApiObject apiObject = new CCApiObject(apiNames);
            FactoryRegistry.bindTo(apiObject, null, source);
            return apiObject;
        };
    }

    private final String[] apiNames;

    private CCApiObject(String[] apiNames) {
        this.apiNames = apiNames;
    }

    @Override
    public String[] getNames() {
        return apiNames;
    }
}
