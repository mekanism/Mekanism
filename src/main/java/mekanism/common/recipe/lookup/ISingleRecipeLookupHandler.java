package mekanism.common.recipe.lookup;

import java.util.function.Predicate;
import mekanism.api.chemical.Chemical;
import mekanism.api.chemical.ChemicalStack;
import mekanism.api.recipes.MekanismRecipe;
import mekanism.api.recipes.inputs.IInputHandler;
import mekanism.common.recipe.lookup.IRecipeLookupHandler.IRecipeTypedLookupHandler;
import mekanism.common.recipe.lookup.cache.InputRecipeCache.SingleChemical;
import mekanism.common.recipe.lookup.cache.InputRecipeCache.SingleFluid;
import mekanism.common.recipe.lookup.cache.InputRecipeCache.SingleItem;
import mekanism.common.recipe.lookup.cache.SingleInputRecipeCache;
import net.minecraft.core.TypedInstance;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.fluids.FluidStack;
import org.jspecify.annotations.Nullable;

/// Helper expansion of [IRecipeLookupHandler] for easily implementing contains and find recipe lookups for recipes that take a single input using the input cache.
public interface ISingleRecipeLookupHandler<TYPE, INPUT extends TypedInstance<TYPE>, RECIPE extends MekanismRecipe<?> & Predicate<INPUT>,
      INPUT_CACHE extends SingleInputRecipeCache<TYPE, INPUT, ?, RECIPE, ?>> extends IRecipeTypedLookupHandler<RECIPE, INPUT_CACHE> {

    /// Checks if there is a matching recipe of type [#getRecipeType()] that has the given input.
    ///
    /// @param input Recipe input.
    ///
    /// @return `true` if there is a match, `false` if there isn't.
    default boolean containsRecipe(TypedInstance<TYPE> input) {
        return getRecipeType().getInputCache().containsInput(getLevel(), input);
    }

    /// Finds the first recipe for the type of recipe we handle ([#getRecipeType()]) by looking up the given input against the recipe type's input cache.
    ///
    /// @param input Recipe input.
    ///
    /// @return Recipe matching the given input, or `null` if no recipe matches.
    @Nullable
    default RECIPE findFirstRecipe(INPUT input) {
        return getRecipeType().getInputCache().findFirstRecipe(getLevel(), input);
    }

    /// Finds the first recipe for the type of recipe we handle ([#getRecipeType()]) by looking up the given input against the recipe type's input cache.
    ///
    /// @param inputHandler Input handler to grab the recipe input from.
    ///
    /// @return Recipe matching the given input, or `null` if no recipe matches.
    @Nullable
    default RECIPE findFirstRecipe(IInputHandler<TYPE, INPUT> inputHandler) {
        return findFirstRecipe(inputHandler.getInput());
    }

    /// Helper interface to make the generics that we have to pass to [ISingleRecipeLookupHandler] not as messy.
    interface ItemRecipeLookupHandler<RECIPE extends MekanismRecipe<?> & Predicate<ItemStack>> extends ISingleRecipeLookupHandler<Item, ItemStack, RECIPE, SingleItem<RECIPE>> {
    }

    /// Helper interface to make the generics that we have to pass to [ISingleRecipeLookupHandler] not as messy.
    interface FluidRecipeLookupHandler<RECIPE extends MekanismRecipe<?> & Predicate<FluidStack>> extends ISingleRecipeLookupHandler<Fluid, FluidStack, RECIPE, SingleFluid<RECIPE>> {
    }

    /// Helper interface to make the generics that we have to pass to [ISingleRecipeLookupHandler] not as messy.
    interface ChemicalRecipeLookupHandler<RECIPE extends MekanismRecipe<?> & Predicate<ChemicalStack>> extends ISingleRecipeLookupHandler<Chemical, ChemicalStack, RECIPE, SingleChemical<RECIPE>> {
    }
}