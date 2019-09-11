package mekanism.common.integration.crafttweaker.gas;

import crafttweaker.annotations.ZenRegister;
import crafttweaker.api.item.IIngredient;
import java.util.Collections;
import java.util.List;
import stanhebben.zenscript.annotations.ZenExpansion;
import stanhebben.zenscript.annotations.ZenGetter;
import stanhebben.zenscript.annotations.ZenMethod;

@ZenExpansion("crafttweaker.item.IIngredient")
@ZenRegister
public class IngredientExpansion {

    //TODO: Make the different handlers support multi liquid and multi gas types

    //TODO: Figure out if it has issues figuring out which method to use given I also declare these in IGasStack

    @ZenGetter("gases")
    public static List<IGasStack> getGases(IIngredient ingredient) {
        //TODO: Do we want to use the GasConversionHandler for getting it from the stack
        //TODO: Implement
        return Collections.emptyList();
    }

    @ZenMethod
    public static boolean matches(IIngredient ingredient, IGasStack gasStack) {
        //TODO: Implement
        return false;
    }
}