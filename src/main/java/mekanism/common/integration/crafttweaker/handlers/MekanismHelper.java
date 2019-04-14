package mekanism.common.integration.crafttweaker.handlers;

import crafttweaker.annotations.ZenRegister;
import mekanism.common.integration.crafttweaker.gas.GasBracketHandler;
import mekanism.common.integration.crafttweaker.gas.IGasStack;
import stanhebben.zenscript.annotations.ZenClass;
import stanhebben.zenscript.annotations.ZenMethod;

@ZenClass("mods.mekanism.MekanismHelper")
@ZenRegister
public class MekanismHelper {

    @ZenMethod
    public static IGasStack getGas(String name) {
        return GasBracketHandler.getGas(name);
    }
}