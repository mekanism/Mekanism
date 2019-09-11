package mekanism.common.integration.crafttweaker.helpers;

import crafttweaker.api.item.IIngredient;
import crafttweaker.api.item.IngredientAny;
import javax.annotation.Nullable;
import mekanism.api.gas.Gas;
import mekanism.api.gas.GasRegistry;
import mekanism.api.gas.GasStack;
import mekanism.api.recipes.inputs.GasStackIngredient;
import mekanism.common.integration.crafttweaker.gas.IGasStack;

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

    @Nullable
    public static GasStack toGas(IGasStack iStack) {
        if (iStack == null) {
            return null;
        }
        Gas gas = GasRegistry.getGas(iStack.getName());
        return gas == null ? null : new GasStack(gas, iStack.getAmount());
    }

    @Nullable
    public static GasStackIngredient toGasStackIngredient(IGasStack iStack) {
        GasStack gasStack = toGas(iStack);
        return gasStack == null ? null : GasStackIngredient.from(gasStack);
    }

    public static GasStack[] toGases(IGasStack[] iStack) {
        GasStack[] stack = new GasStack[iStack.length];
        for (int i = 0; i < stack.length; i++) {
            stack[i] = toGas(iStack[i]);
        }
        return stack;
    }
}