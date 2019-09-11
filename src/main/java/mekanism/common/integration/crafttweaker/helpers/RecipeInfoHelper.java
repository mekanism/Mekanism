package mekanism.common.integration.crafttweaker.helpers;

import crafttweaker.mc1120.item.MCItemStack;
import java.util.List;
import mekanism.api.annotations.NonNull;
import mekanism.api.gas.Gas;
import mekanism.api.gas.GasStack;
import mekanism.api.recipes.AmbientAccumulatorRecipe;
import mekanism.api.recipes.ChemicalCrystallizerRecipe;
import mekanism.api.recipes.ChemicalInfuserRecipe;
import mekanism.api.recipes.ChemicalWasherRecipe;
import mekanism.api.recipes.CombinerRecipe;
import mekanism.api.recipes.ElectrolysisRecipe;
import mekanism.api.recipes.FluidToFluidRecipe;
import mekanism.api.recipes.GasToGasRecipe;
import mekanism.api.recipes.IMekanismRecipe;
import mekanism.api.recipes.ItemStackGasToGasRecipe;
import mekanism.api.recipes.ItemStackGasToItemStackRecipe;
import mekanism.api.recipes.ItemStackToGasRecipe;
import mekanism.api.recipes.ItemStackToItemStackRecipe;
import mekanism.api.recipes.MetallurgicInfuserRecipe;
import mekanism.api.recipes.PressurizedReactionRecipe;
import mekanism.api.recipes.SawmillRecipe;
import mekanism.api.recipes.SawmillRecipe.ChanceOutput;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;
import org.apache.commons.lang3.tuple.Pair;

public class RecipeInfoHelper {

    private RecipeInfoHelper() {
    }

    public static String getRecipeInfo(IMekanismRecipe recipe) {
        //TODO: Don't use nulls
        //Item input
        if (recipe instanceof ChemicalCrystallizerRecipe) {
            return getItemName(((ChemicalCrystallizerRecipe) recipe).getOutput(null));
        } else if (recipe instanceof CombinerRecipe) {
            return getItemName(((CombinerRecipe) recipe).getOutput(null, null));
        } else if (recipe instanceof ItemStackGasToItemStackRecipe) {
            return getItemName(((ItemStackGasToItemStackRecipe) recipe).getOutput(null, null));
        } else if (recipe instanceof ItemStackToItemStackRecipe) {
            return getItemName(((ItemStackToItemStackRecipe) recipe).getOutput(null));
        } else if (recipe instanceof MetallurgicInfuserRecipe) {
            return getItemName(((MetallurgicInfuserRecipe) recipe).getOutput(null, null));
        }
        //Gas output
        else if (recipe instanceof AmbientAccumulatorRecipe) {
            return getGasName(((AmbientAccumulatorRecipe) recipe).getOutput());
        } else if (recipe instanceof ChemicalInfuserRecipe) {
            return getGasName(((ChemicalInfuserRecipe) recipe).getOutput(null, null));
        } else if (recipe instanceof ChemicalWasherRecipe) {
            return getGasName(((ChemicalWasherRecipe) recipe).getOutput(null, null));
        } else if (recipe instanceof GasToGasRecipe) {
            return getGasName(((GasToGasRecipe) recipe).getOutput(null));
        } else if (recipe instanceof ItemStackGasToGasRecipe) {
            return getGasName(((ItemStackGasToGasRecipe) recipe).getOutput(null, null));
        } else if (recipe instanceof ItemStackToGasRecipe) {
            return getGasName(((ItemStackToGasRecipe) recipe).getOutput(null));
        }
        //Fluid
        else if (recipe instanceof FluidToFluidRecipe) {
            return getFluidName(((FluidToFluidRecipe) recipe).getOutput(null));
        }
        //Double Gas output
        else if (recipe instanceof ElectrolysisRecipe) {
            ElectrolysisRecipe castedRecipe = (ElectrolysisRecipe) recipe;
            Pair<GasStack, GasStack> output = castedRecipe.getOutput(null);
            return "[" + getGasName(output.getLeft()) + ", " + getGasName(output.getRight()) + "]";
        }
        //item gas output
        else if (recipe instanceof PressurizedReactionRecipe) {
            PressurizedReactionRecipe castedRecipe = (PressurizedReactionRecipe) recipe;
            @NonNull Pair<@NonNull ItemStack, @NonNull GasStack> output = castedRecipe.getOutput(null, null, null);
            return "[" + getItemName(output.getLeft()) + ", " + getGasName(output.getRight()) + "]";
        }
        //item item
        else if (recipe instanceof SawmillRecipe) {
            SawmillRecipe castedRecipe = (SawmillRecipe) recipe;
            ChanceOutput output = castedRecipe.getOutput(null);
            ItemStack secondaryOutput = output.getMaxSecondaryOutput();
            if (castedRecipe.getSecondaryChance() > 0 && !secondaryOutput.isEmpty()) {
                return "[" + getItemName(output.getMainOutput()) + ", " + getItemName(secondaryOutput) + " " + castedRecipe.getSecondaryChance() + "%]";
            }
            return getItemName(output.getMainOutput());
        }
        //TODO: Else print a warning so that we can find if we are missing something
        return null;
    }

    public static String getGasName(List<GasStack> stacks) {
        //TODO: Implement
    }

    public static String getGasName(GasStack stack) {
        return stack.amount > 1 ? String.format("<gas:%s> * %s", stack.getGas().getName(), stack.amount) : getGasName(stack.getGas());
    }

    public static String getGasName(Gas gas) {
        return String.format("<gas:%s>", gas.getName());
    }

    public static String getFluidName(List<FluidStack> stacks) {
        //TODO: Implement
    }

    public static String getFluidName(FluidStack stack) {
        return stack.amount > 1 ? String.format("<liquid:%s> * %s", stack.getFluid().getName(), stack.amount) : getFluidName(stack.getFluid());
    }

    public static String getFluidName(Fluid fluid) {
        return String.format("<liquid:%s>", fluid.getName());
    }

    public static String getItemName(List<ItemStack> stacks) {
        //TODO: Implement
    }

    public static String getItemName(ItemStack stack) {
        return new MCItemStack(stack).toString();
    }
}