package mekanism.common.recipe.lookup;

import java.util.function.Predicate;
import javax.annotation.Nullable;
import mekanism.api.chemical.Chemical;
import mekanism.api.chemical.ChemicalStack;
import mekanism.api.recipes.MekanismRecipe;
import mekanism.api.recipes.inputs.IInputHandler;
import mekanism.common.recipe.lookup.IRecipeLookupHandler.IRecipeTypedLookupHandler;
import mekanism.common.recipe.lookup.cache.InputRecipeCache.SingleChemical;
import mekanism.common.recipe.lookup.cache.InputRecipeCache.SingleFluid;
import mekanism.common.recipe.lookup.cache.InputRecipeCache.SingleItem;
import mekanism.common.recipe.lookup.cache.SingleInputRecipeCache;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;

/**
 * Helper expansion of {@link IRecipeLookupHandler} for easily implementing contains and find recipe lookups for recipes that take a single input using the input cache.
 */
public interface ISingleRecipeLookupHandler<INPUT, RECIPE extends MekanismRecipe & Predicate<INPUT>, INPUT_CACHE extends SingleInputRecipeCache<INPUT, ?, RECIPE, ?>>
      extends IRecipeTypedLookupHandler<RECIPE, INPUT_CACHE> {

    /**
     * Checks if there is a matching recipe of type {@link #getRecipeType()} that has the given input.
     *
     * @param input Recipe input.
     *
     * @return {@code true} if there is a match, {@code false} if there isn't.
     */
    default boolean containsRecipe(INPUT input) {
        return getRecipeType().getInputCache().containsInput(getHandlerWorld(), input);
    }

    /**
     * Finds the first recipe for the type of recipe we handle ({@link #getRecipeType()}) by looking up the given input against the recipe type's input cache.
     *
     * @param input Recipe input.
     *
     * @return Recipe matching the given input, or {@code null} if no recipe matches.
     */
    @Nullable
    default RECIPE findFirstRecipe(INPUT input) {
        return getRecipeType().getInputCache().findFirstRecipe(getHandlerWorld(), input);
    }

    /**
     * Finds the first recipe for the type of recipe we handle ({@link #getRecipeType()}) by looking up the given input against the recipe type's input cache.
     *
     * @param inputHandler Input handler to grab the recipe input from.
     *
     * @return Recipe matching the given input, or {@code null} if no recipe matches.
     */
    @Nullable
    default RECIPE findFirstRecipe(IInputHandler<INPUT> inputHandler) {
        return findFirstRecipe(inputHandler.getInput());
    }

    /**
     * Helper interface to make the generics that we have to pass to {@link ISingleRecipeLookupHandler} not as messy.
     */
    interface ItemRecipeLookupHandler<RECIPE extends MekanismRecipe & Predicate<ItemStack>> extends ISingleRecipeLookupHandler<ItemStack, RECIPE, SingleItem<RECIPE>> {
    }

    /**
     * Helper interface to make the generics that we have to pass to {@link ISingleRecipeLookupHandler} not as messy.
     */
    interface FluidRecipeLookupHandler<RECIPE extends MekanismRecipe & Predicate<FluidStack>> extends ISingleRecipeLookupHandler<FluidStack, RECIPE, SingleFluid<RECIPE>> {
    }

    /**
     * Helper interface to make the generics that we have to pass to {@link ISingleRecipeLookupHandler} not as messy.
     */
    interface ChemicalRecipeLookupHandler<CHEMICAL extends Chemical<CHEMICAL>, STACK extends ChemicalStack<CHEMICAL>, RECIPE extends MekanismRecipe & Predicate<STACK>>
          extends ISingleRecipeLookupHandler<STACK, RECIPE, SingleChemical<CHEMICAL, STACK, RECIPE>> {

        /**
         * Helper wrapper to convert a chemical to a chemical stack and pass it to {@link #containsRecipe(Object)} to make validity predicates easier and cleaner.
         */
        default boolean containsRecipe(CHEMICAL input) {
            return containsRecipe((STACK) input.getStack(1));
        }
    }
}