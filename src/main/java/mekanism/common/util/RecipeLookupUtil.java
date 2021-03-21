package mekanism.common.util;

import java.util.function.BiPredicate;
import java.util.function.Predicate;
import javax.annotation.Nullable;
import mekanism.api.annotations.NonNull;
import mekanism.api.chemical.ChemicalStack;
import mekanism.api.recipes.CombinerRecipe;
import mekanism.api.recipes.MekanismRecipe;
import mekanism.api.recipes.inputs.IInputHandler;
import mekanism.common.tile.interfaces.ITileCachedRecipeHolder;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;

public class RecipeLookupUtil {

    @Nullable
    public static <RECIPE extends MekanismRecipe & Predicate<FluidStack>> RECIPE findFluidRecipe(ITileCachedRecipeHolder<RECIPE> holder,
          IInputHandler<@NonNull FluidStack> inputHandler) {
        FluidStack fluid = inputHandler.getInput();
        return fluid.isEmpty() ? null : holder.findFirstRecipe(recipe -> recipe.test(fluid));
    }

    @Nullable
    public static <RECIPE extends MekanismRecipe & Predicate<ItemStack>> RECIPE findItemStackRecipe(ITileCachedRecipeHolder<RECIPE> holder,
          IInputHandler<@NonNull ItemStack> inputHandler) {
        ItemStack stack = inputHandler.getInput();
        return stack.isEmpty() ? null : holder.findFirstRecipe(recipe -> recipe.test(stack));
    }

    @Nullable
    public static <STACK extends ChemicalStack<?>, RECIPE extends MekanismRecipe & Predicate<STACK>> RECIPE findChemicalRecipe(ITileCachedRecipeHolder<RECIPE> holder,
          IInputHandler<@NonNull STACK> chemicalInputHandler) {
        STACK chemicalStack = chemicalInputHandler.getInput();
        return chemicalStack.isEmpty() ? null : holder.findFirstRecipe(recipe -> recipe.test(chemicalStack));
    }

    @Nullable
    public static <STACK_A extends ChemicalStack<?>, STACK_B extends ChemicalStack<?>, RECIPE extends MekanismRecipe & BiPredicate<STACK_A, STACK_B>> RECIPE
    findChemicalChemicalRecipe(ITileCachedRecipeHolder<RECIPE> holder, IInputHandler<@NonNull STACK_A> inputHandlerA, IInputHandler<@NonNull STACK_B> inputHandlerB) {
        STACK_A stackA = inputHandlerA.getInput();
        if (stackA.isEmpty()) {
            return null;
        }
        STACK_B stackB = inputHandlerB.getInput();
        return stackB.isEmpty() ? null : holder.findFirstRecipe(recipe -> recipe.test(stackA, stackB));
    }

    @Nullable
    public static <STACK extends ChemicalStack<?>, RECIPE extends MekanismRecipe & BiPredicate<ItemStack, STACK>> RECIPE findItemStackChemicalRecipe(
          ITileCachedRecipeHolder<RECIPE> holder, IInputHandler<@NonNull ItemStack> inputHandler, IInputHandler<@NonNull STACK> chemicalInputHandler) {
        ItemStack stack = inputHandler.getInput();
        if (stack.isEmpty()) {
            return null;
        }
        STACK chemicalStack = chemicalInputHandler.getInput();
        return chemicalStack.isEmpty() ? null : holder.findFirstRecipe(recipe -> recipe.test(stack, chemicalStack));
    }

    @Nullable
    public static CombinerRecipe findCombinerRecipe(ITileCachedRecipeHolder<CombinerRecipe> holder, IInputHandler<@NonNull ItemStack> inputHandler,
          IInputHandler<@NonNull ItemStack> extraInputHandler) {
        ItemStack stack = inputHandler.getInput();
        if (stack.isEmpty()) {
            return null;
        }
        ItemStack extra = extraInputHandler.getInput();
        return extra.isEmpty() ? null : holder.findFirstRecipe(recipe -> recipe.test(stack, extra));
    }
}