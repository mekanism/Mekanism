package mekanism.common.integration.computer.computercraft;

import dan200.computercraft.api.lua.IComputerSystem;
import dan200.computercraft.api.lua.IDynamicLuaObject;
import dan200.computercraft.api.lua.ILuaAPI;
import dan200.computercraft.api.lua.ILuaAPIFactory;
import mekanism.common.integration.computer.FactoryRegistry;
import org.jetbrains.annotations.Nullable;

/**
 * Registers a CC Api handler. Static classes only for now.
 */
public class CCApiObject extends CCMethodCaller implements IDynamicLuaObject, ILuaAPI {

    static ILuaAPIFactory create(Class<?> source, String... apiNames) {
        return new Factory(source, apiNames);
    }

    private final String[] apiNames;

    private CCApiObject(String[] apiNames) {
        this.apiNames = apiNames;
    }

    @Override
    public String[] getNames() {
        return apiNames;
    }

    private static class Factory implements ILuaAPIFactory {

        private final Class<?> source;
        private final String[] apiNames;
        private CCApiObject instance;

        Factory(Class<?> source, String[] apiNames) {
            this.source = source;
            this.apiNames = apiNames;
        }

        @Nullable
        @Override
        public ILuaAPI create(IComputerSystem computer) {
            if (instance == null) {
                instance = new CCApiObject(apiNames);
                FactoryRegistry.bindTo(instance, null, source);
            }
            return instance;
        }
    }
}
