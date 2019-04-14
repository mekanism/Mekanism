package mekanism.common.integration.crafttweaker.handlers;

import crafttweaker.annotations.ZenRegister;
import mekanism.common.integration.crafttweaker.gas.GasBracketHandler;
import mekanism.common.integration.crafttweaker.gas.IGasStack;
import stanhebben.zenscript.annotations.ZenClass;
import stanhebben.zenscript.annotations.ZenMethod;

@ZenClass("mods.mekanism.mekaTypeUtils")
@ZenRegister
public class MekaTypeUtils {

    @ZenMethod
    public static IGasStack getGas(String name) {
        return GasBracketHandler.getGas(name);
    }
}