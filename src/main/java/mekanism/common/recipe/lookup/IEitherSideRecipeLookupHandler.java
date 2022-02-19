package mekanism.common.recipe.lookup;

import java.util.function.BiPredicate;
import javax.annotation.Nullable;
import mekanism.api.chemical.Chemical;
import mekanism.api.chemical.ChemicalStack;
import mekanism.api.recipes.MekanismRecipe;
import mekanism.api.recipes.chemical.ChemicalChemicalToChemicalRecipe;
import mekanism.api.recipes.inputs.IInputHandler;
import mekanism.api.recipes.inputs.chemical.IChemicalStackIngredient;
import mekanism.common.recipe.lookup.IRecipeLookupHandler.IRecipeTypedLookupHandler;
import mekanism.common.recipe.lookup.cache.EitherSideInputRecipeCache;
import mekanism.common.recipe.lookup.cache.InputRecipeCache.EitherSideChemical;
import net.minecraft.world.World;

/**
 * Helper expansion of {@link IRecipeLookupHandler} for easily implementing contains and find recipe lookups for recipes that takes two inputs of the same type that are
 * valid in either slot/tank.
 */
public interface IEitherSideRecipeLookupHandler<INPUT, RECIPE extends MekanismRecipe & BiPredicate<INPUT, INPUT>,
      INPUT_CACHE extends EitherSideInputRecipeCache<INPUT, ?, RECIPE, ?>> extends IRecipeTypedLookupHandler<RECIPE, INPUT_CACHE> {

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
     * Checks if there is a matching recipe of type {@link #getRecipeType()} that has the given inputs.
     *
     * @param inputA Recipe input a.
     * @param inputB Recipe input b.
     *
     * @return {@code true} if there is a match, {@code false} if there isn't.
     *
     * @apiNote See {@link EitherSideInputRecipeCache#containsInput(World, Object, Object)} for more details about what order to pass the inputs.
     */
    default boolean containsRecipe(INPUT inputA, INPUT inputB) {
        return getRecipeType().getInputCache().containsInput(getHandlerWorld(), inputA, inputB);
    }

    /**
     * Finds the first recipe for the type of recipe we handle ({@link #getRecipeType()}) by looking up the given inputs against the recipe type's input cache.
     *
     * @param inputA Recipe input a.
     * @param inputB Recipe input b.
     *
     * @return Recipe matching the given inputs, or {@code null} if no recipe matches.
     */
    @Nullable
    default RECIPE findFirstRecipe(INPUT inputA, INPUT inputB) {
        return getRecipeType().getInputCache().findFirstRecipe(getHandlerWorld(), inputA, inputB);
    }

    /**
     * Finds the first recipe for the type of recipe we handle ({@link #getRecipeType()}) by looking up the given inputs against the recipe type's input cache.
     *
     * @param inputAHandler Input handler to grab the first recipe input from.
     * @param inputBHandler Input handler to grab the second recipe input from.
     *
     * @return Recipe matching the given inputs, or {@code null} if no recipe matches.
     */
    @Nullable
    default RECIPE findFirstRecipe(IInputHandler<INPUT> inputAHandler, IInputHandler<INPUT> inputBHandler) {
        return findFirstRecipe(inputAHandler.getInput(), inputBHandler.getInput());
    }

    /**
     * Helper interface to make the generics that we have to pass to {@link IEitherSideRecipeLookupHandler} not as messy.
     */
    interface EitherSideChemicalRecipeLookupHandler<CHEMICAL extends Chemical<CHEMICAL>, STACK extends ChemicalStack<CHEMICAL>,
          RECIPE extends ChemicalChemicalToChemicalRecipe<CHEMICAL, STACK, ? extends IChemicalStackIngredient<CHEMICAL, STACK>>> extends
          IEitherSideRecipeLookupHandler<STACK, RECIPE, EitherSideChemical<CHEMICAL, STACK, RECIPE>> {

        /**
         * Helper wrapper to convert a chemical to a chemical stack and pass it to {@link #containsRecipe(Object)} to make validity predicates easier and cleaner.
         */
        default boolean containsRecipe(CHEMICAL input) {
            return containsRecipe((STACK) input.getStack(1));
        }

        /**
         * Helper wrapper to convert a chemical to a chemical stack and pass it to {@link #containsRecipe(Object)} to make validity predicates easier and cleaner.
         */
        default boolean containsRecipe(CHEMICAL inputA, STACK inputB) {
            return containsRecipe((STACK) inputA.getStack(1), inputB);
        }
    }
}