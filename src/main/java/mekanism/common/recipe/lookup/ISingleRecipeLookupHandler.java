package mekanism.common.recipe.lookup;

import java.util.function.Predicate;
import mekanism.api.chemical.Chemical;
import mekanism.api.chemical.ChemicalResource;
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
import net.neoforged.neoforge.transfer.fluid.FluidResource;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.resource.RegisteredResource;
import org.jetbrains.annotations.Nullable;

/**
 * Helper expansion of {@link IRecipeLookupHandler} for easily implementing contains and find recipe lookups for recipes that take a single input using the input cache.
 */
public interface ISingleRecipeLookupHandler<I_TYPE, I_RESOURCE extends RegisteredResource<I_TYPE>, INPUT extends TypedInstance<I_TYPE>,
      RECIPE extends MekanismRecipe<?> & Predicate<INPUT>, INPUT_CACHE extends SingleInputRecipeCache<I_TYPE, I_RESOURCE, INPUT, ?, RECIPE, ?>>
      extends IRecipeTypedLookupHandler<RECIPE, INPUT_CACHE> {

    /**
     * Checks if there is a matching recipe of type {@link #getRecipeType()} that has the given input.
     *
     * @param input Recipe input.
     *
     * @return {@code true} if there is a match, {@code false} if there isn't.
     */
    default boolean containsRecipe(INPUT input) {
        return getRecipeType().getInputCache().containsInput(getLevel(), input);
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
        return getRecipeType().getInputCache().findFirstRecipe(getLevel(), input);
    }

    /**
     * Finds the first recipe for the type of recipe we handle ({@link #getRecipeType()}) by looking up the given input against the recipe type's input cache.
     *
     * @param inputHandler Input handler to grab the recipe input from.
     *
     * @return Recipe matching the given input, or {@code null} if no recipe matches.
     */
    @Nullable
    default RECIPE findFirstRecipe(IInputHandler<I_TYPE, I_RESOURCE, INPUT> inputHandler) {
        return findFirstRecipe(inputHandler.getInput());
    }

    /**
     * Helper interface to make the generics that we have to pass to {@link ISingleRecipeLookupHandler} not as messy.
     */
    interface ItemRecipeLookupHandler<RECIPE extends MekanismRecipe<?> & Predicate<ItemStack>> extends ISingleRecipeLookupHandler<Item, ItemResource, ItemStack, RECIPE, SingleItem<RECIPE>> {
    }

    /**
     * Helper interface to make the generics that we have to pass to {@link ISingleRecipeLookupHandler} not as messy.
     */
    interface FluidRecipeLookupHandler<RECIPE extends MekanismRecipe<?> & Predicate<FluidStack>> extends ISingleRecipeLookupHandler<Fluid, FluidResource, FluidStack, RECIPE, SingleFluid<RECIPE>> {
    }

    /**
     * Helper interface to make the generics that we have to pass to {@link ISingleRecipeLookupHandler} not as messy.
     */
    interface ChemicalRecipeLookupHandler<RECIPE extends MekanismRecipe<?> & Predicate<ChemicalStack>> extends ISingleRecipeLookupHandler<Chemical, ChemicalResource, ChemicalStack, RECIPE, SingleChemical<RECIPE>> {
    }
}