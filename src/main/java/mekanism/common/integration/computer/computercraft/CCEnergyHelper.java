package mekanism.common.integration.computer.computercraft;

import dan200.computercraft.api.lua.IComputerSystem;
import dan200.computercraft.api.lua.IDynamicLuaObject;
import dan200.computercraft.api.lua.ILuaAPI;
import java.util.Map;
import javax.annotation.Nonnull;
import mekanism.common.integration.computer.BoundComputerMethod;
import mekanism.common.integration.computer.ComputerEnergyHelper;

public class CCEnergyHelper extends CCMethodCaller implements IDynamicLuaObject, ILuaAPI {

    private static CCEnergyHelper energyHelper;

    public static CCEnergyHelper create(@Nonnull IComputerSystem computer) {
        //Lazy init the energy helper as the API is the same across all computers
        if (energyHelper == null) {
            energyHelper = new CCEnergyHelper(ComputerEnergyHelper.getMethods());
        }
        return energyHelper;
    }

    private CCEnergyHelper(Map<String, BoundComputerMethod> boundMethods) {
        super(boundMethods);
    }

    @Override
    protected String getCallerType() {
        return "Lua API";
    }

    @Override
    public String[] getNames() {
        return new String[]{"mekanismEnergyHelper"};
    }
}