package mekanism.common.util;

import java.util.function.BiPredicate;
import java.util.function.Predicate;
import javax.annotation.Nullable;
import mekanism.api.annotations.NonNull;
import mekanism.api.chemical.ChemicalStack;
import mekanism.api.chemical.infuse.InfusionStack;
import mekanism.api.recipes.CombinerRecipe;
import mekanism.api.recipes.MekanismRecipe;
import mekanism.api.recipes.MetallurgicInfuserRecipe;
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
    public static MetallurgicInfuserRecipe findMetallurgicInfuserRecipe(ITileCachedRecipeHolder<MetallurgicInfuserRecipe> holder,
          IInputHandler<@NonNull ItemStack> inputHandler, IInputHandler<@NonNull InfusionStack> infusionInputHandler) {
        //TODO - 1.17: Switch MetallurgicInfuserRecipes from being a BiPredicate<InfusionStack, ItemStack> to being a BiPredicate<ItemStack, InfusionStack>
        // so that we can just use the above findItemStackChemicalRecipe. We can't do this now as it would be a breaking change
        ItemStack stack = inputHandler.getInput();
        if (stack.isEmpty()) {
            return null;
        }
        InfusionStack infusionStack = infusionInputHandler.getInput();
        return infusionStack.isEmpty() ? null : holder.findFirstRecipe(recipe -> recipe.test(infusionStack, stack));
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