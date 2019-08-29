package mekanism.common.integration.crafttweaker.helpers;

import com.blamejared.crafttweaker.impl.item.MCItemStack;
import java.util.Map.Entry;
import javax.annotation.Nonnull;
import mekanism.api.gas.Gas;
import mekanism.api.gas.GasStack;
import mekanism.common.recipe.inputs.MachineInput;
import mekanism.common.recipe.machines.MachineRecipe;
import mekanism.common.recipe.outputs.ChanceOutput;
import mekanism.common.recipe.outputs.ChemicalPairOutput;
import mekanism.common.recipe.outputs.FluidOutput;
import mekanism.common.recipe.outputs.GasOutput;
import mekanism.common.recipe.outputs.ItemStackOutput;
import mekanism.common.recipe.outputs.MachineOutput;
import mekanism.common.recipe.outputs.PressurizedOutput;
import net.minecraft.fluid.Fluid;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;

public class RecipeInfoHelper {

    private RecipeInfoHelper() {
    }

    public static String getRecipeInfo(Entry<? extends MachineInput, ? extends MachineRecipe> recipe) {
        MachineOutput output = recipe.getValue().recipeOutput;
        if (output instanceof ItemStackOutput) {
            return getItemName(((ItemStackOutput) output).output);
        } else if (output instanceof GasOutput) {
            return getGasName(((GasOutput) output).output);
        } else if (output instanceof FluidOutput) {
            return getFluidName(((FluidOutput) output).output);
        } else if (output instanceof ChemicalPairOutput) {
            ChemicalPairOutput out = (ChemicalPairOutput) output;
            return "[" + getGasName(out.leftGas) + ", " + getGasName(out.rightGas) + "]";
        } else if (output instanceof ChanceOutput) {
            return getItemName(((ChanceOutput) output).primaryOutput);
        } else if (output instanceof PressurizedOutput) {
            PressurizedOutput out = (PressurizedOutput) output;
            return "[" + getItemName(out.getItemOutput()) + ", " + getGasName(out.getGasOutput()) + "]";
        }
        return null;
    }

    public static String getGasName(GasStack stack) {
        return stack.amount > 1 ? String.format("<gas:%s> * %s", stack.getGas().getName(), stack.amount) : getGasName(stack.getGas());
    }

    public static String getGasName(Gas gas) {
        return String.format("<gas:%s>", gas.getName());
    }

    public static String getFluidName(@Nonnull FluidStack stack) {
        return stack.getAmount() > 1 ? String.format("<liquid:%s> * %s", stack.getFluid().getAttributes().getName(), stack.getAmount()) : getFluidName(stack.getFluid());
    }

    public static String getFluidName(@Nonnull Fluid fluid) {
        return String.format("<liquid:%s>", fluid.getAttributes().getName());
    }

    public static String getItemName(ItemStack stack) {
        return new MCItemStack(stack).getCommandString();
    }
}