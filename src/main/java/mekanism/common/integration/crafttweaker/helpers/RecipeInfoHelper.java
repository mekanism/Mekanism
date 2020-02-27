/*package mekanism.common.integration.crafttweaker.helpers;

import com.blamejared.crafttweaker.impl.item.MCItemStack;
import java.util.List;
import javax.annotation.Nonnull;
import mekanism.api.annotations.NonNull;
import mekanism.api.chemical.gas.Gas;
import mekanism.api.chemical.gas.GasStack;
import mekanism.api.recipes.AmbientAccumulatorRecipe;
import mekanism.api.recipes.ChemicalInfuserRecipe;
import mekanism.api.recipes.CombinerRecipe;
import mekanism.api.recipes.ElectrolysisRecipe;
import mekanism.api.recipes.FluidGasToGasRecipe;
import mekanism.api.recipes.FluidToFluidRecipe;
import mekanism.api.recipes.GasToGasRecipe;
import mekanism.api.recipes.GasToItemStackRecipe;
import mekanism.api.recipes.ItemStackGasToGasRecipe;
import mekanism.api.recipes.ItemStackGasToItemStackRecipe;
import mekanism.api.recipes.ItemStackToGasRecipe;
import mekanism.api.recipes.ItemStackToItemStackRecipe;
import mekanism.api.recipes.MekanismRecipe;
import mekanism.api.recipes.MetallurgicInfuserRecipe;
import mekanism.api.recipes.PressurizedReactionRecipe;
import mekanism.api.recipes.SawmillRecipe;
import mekanism.api.recipes.SawmillRecipe.ChanceOutput;
import mekanism.api.recipes.inputs.FluidStackIngredient;
import mekanism.api.recipes.inputs.ItemStackIngredient;
import net.minecraft.fluid.Fluid;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;
import org.apache.commons.lang3.tuple.Pair;

public class RecipeInfoHelper {

    private RecipeInfoHelper() {
    }

    public static String getRecipeInfo(MekanismRecipe recipe) {
        //TODO: Don't use nulls
        //Item input
        if (recipe instanceof GasToItemStackRecipe) {
            return getItemName(((GasToItemStackRecipe) recipe).getOutput(null));
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
        } else if (recipe instanceof FluidGasToGasRecipe) {
            return getGasName(((FluidGasToGasRecipe) recipe).getOutput(null, null));
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
            Pair<@NonNull GasStack, @NonNull GasStack> output = castedRecipe.getOutput(null);
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
        return "TODO: THIS SHOULD NOT BE HERE, IF THIS MESSAGE SOMEHOW GETS INTO AN ACTUAL RELEASE PLEASE REPORT IT.";
    }

    public static String getGasName(@Nonnull GasStack stack) {
        return stack.getAmount() > 1 ? String.format("<gas:%s> * %s", stack.getType().getName(), stack.getAmount()) : getGasName(stack.getType());
    }

    public static String getGasName(@Nonnull Gas gas) {
        return String.format("<gas:%s>", gas.getName());
    }

    public static String getFluidName(FluidStackIngredient ingredient) {
        //TODO: Implement
        return "TODO: THIS SHOULD NOT BE HERE, IF THIS MESSAGE SOMEHOW GETS INTO AN ACTUAL RELEASE PLEASE REPORT IT.";
    }

    public static String getFluidName(@Nonnull FluidStack stack) {
        //TODO: Used to be name
        return stack.getAmount() > 1 ? String.format("<liquid:%s> * %s", stack.getFluid().getRegistryName().toString(), stack.getAmount()) : getFluidName(stack.getFluid());
    }

    public static String getFluidName(@Nonnull Fluid fluid) {
        //TODO: Used to be name
        return String.format("<liquid:%s>", fluid.getRegistryName().toString());
    }

    public static String getItemName(ItemStackIngredient ingredient) {
        //TODO: Implement, make sure to test if adding extra paren around the different things is needed
        // ex: (<dirt> * 2) |  (<stone> * 4) to ensure it doesn't process it as ((<dirt> * 2) | <stone>) * 4
        return "TODO: THIS SHOULD NOT BE HERE, IF THIS MESSAGE SOMEHOW GETS INTO AN ACTUAL RELEASE PLEASE REPORT IT.";
    }


    public static String getItemName(ItemStack stack) {
        return new MCItemStack(stack).getCommandString();
    }
}*/