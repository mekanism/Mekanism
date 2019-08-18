package mekanism.common.integration.crafttweaker.helpers;

import com.blamejared.crafttweaker.api.item.IIngredient;
import mekanism.api.gas.GasRegistry;
import mekanism.api.gas.GasStack;
import mekanism.common.integration.crafttweaker.gas.IGasStack;
import mekanism.common.temporary.IngredientAny;

public class GasHelper {

    private GasHelper() {
    }

    public static boolean matches(IIngredient ingredient, IGasStack gasStack) {
        if (ingredient == null) {
            return false;
        }
        if (ingredient == IngredientAny.INSTANCE) {
            return true;
        }
        if (ingredient instanceof IGasStack) {
            return toGas((IGasStack) ingredient).isGasEqual(toGas(gasStack));
        }
        return false;
    }

    public static GasStack toGas(IGasStack iStack) {
        return iStack == null ? null : new GasStack(GasRegistry.getGas(iStack.getName()), iStack.getAmount());
    }

    public static GasStack[] toGases(IGasStack[] iStack) {
        GasStack[] stack = new GasStack[iStack.length];
        for (int i = 0; i < stack.length; i++) {
            stack[i] = toGas(iStack[i]);
        }
        return stack;
    }
}