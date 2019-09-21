package mekanism.common.integration.crafttweaker.helpers;

import com.blamejared.crafttweaker.api.item.IIngredient;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mekanism.api.gas.GasStack;
import mekanism.api.recipes.inputs.GasStackIngredient;
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

    @Nonnull
    public static GasStack toGas(IGasStack iStack) {
        return iStack == null ? GasStack.EMPTY : iStack.getInternal();
    }

    @Nullable
    public static GasStackIngredient toGasStackIngredient(IGasStack iStack) {
        GasStack gasStack = toGas(iStack);
        return gasStack.isEmpty() ? null : GasStackIngredient.from(gasStack);
    }

    public static GasStack[] toGases(IGasStack[] iStack) {
        GasStack[] stack = new GasStack[iStack.length];
        for (int i = 0; i < stack.length; i++) {
            stack[i] = toGas(iStack[i]);
        }
        return stack;
    }
}