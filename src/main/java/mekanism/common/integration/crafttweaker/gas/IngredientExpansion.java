package mekanism.common.integration.crafttweaker.gas;

import com.blamejared.crafttweaker.api.annotations.ZenRegister;
import com.blamejared.crafttweaker.api.item.IIngredient;
import java.util.Collections;
import java.util.List;
import org.openzen.zencode.java.ZenCodeType;

@ZenCodeType.Expansion("crafttweaker.item.IIngredient")
@ZenRegister
public class IngredientExpansion {

    //TODO: Make the different handlers support multi liquid and multi gas types

    //TODO: Figure out if it has issues figuring out which method to use given I also declare these in IGasStack

    @ZenCodeType.Getter("gases")
    public static List<IGasStack> getGases(IIngredient ingredient) {
        //TODO: Do we want to use the GasConversionHandler for getting it from the stack
        //TODO: Implement
        return Collections.emptyList();
    }

    @ZenCodeType.Method
    public static boolean matches(IIngredient ingredient, IGasStack gasStack) {
        //TODO: Implement
        return false;
    }
}