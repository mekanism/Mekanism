package mekanism.common.recipe.lookup;

import java.util.ArrayList;
import java.util.List;
import mekanism.api.IContentsListener;
import mekanism.api.inventory.IInventorySlot;
import mekanism.api.recipes.MekanismRecipe;
import mekanism.api.recipes.cache.CachedRecipe;
import mekanism.api.resource.IResourceContainer;
import mekanism.client.recipe_viewer.type.IRecipeViewerRecipeType;
import mekanism.common.inventory.slot.BasicInventorySlot;
import mekanism.common.recipe.IMekanismRecipeTypeProvider;
import mekanism.common.recipe.lookup.cache.IInputRecipeCache;
import mekanism.common.registration.impl.RecipeBookCategoryDeferredRegister;
import net.minecraft.core.HolderLookup;
import net.minecraft.world.entity.player.StackedItemContents;
import net.minecraft.world.inventory.RecipeBookType;
import net.minecraft.world.inventory.StackedContentsCompatible;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.Level;
import org.jspecify.annotations.Nullable;

public interface IRecipeLookupHandler<RECIPE extends MekanismRecipe<?, ?, ?>> extends IContentsListener, StackedContentsCompatible {

    /// @return The world for this [IRecipeLookupHandler].
    @Nullable
    Level getLevel();

    /// @return The recipe type this [IRecipeLookupHandler] handles.
    IMekanismRecipeTypeProvider<?, ?, ?, RECIPE, ?> getRecipeType();

    @Nullable
    default IRecipeViewerRecipeType<RECIPE> recipeViewerType() {
        return null;
    }

    void markForSave();

    /// Tries to lookup/get the recipe that a given cacheIndex would represent the slots/tanks/etc. for.
    ///
    /// @param cacheIndex The "recipe index" for which cache to interact with.
    ///
    /// @return Recipe that a given cacheIndex corresponds to, or null if there is no matching recipe.
    @Nullable
    RecipeHolder<RECIPE> getRecipe(int cacheIndex);

    /// Creates a new cached recipe representing a given recipe.
    ///
    /// @param recipe     The backing recipe to create a cached version of.
    /// @param cacheIndex The "recipe index" for which cache to interact with.
    ///
    /// @return A new cached recipe representing the given recipe.
    CachedRecipe<RECIPE> createNewCachedRecipe(RECIPE recipe, int cacheIndex);

    /// Called when the cached recipe changes at a given index before processing the new cached recipe.
    ///
    /// @param registries   Registry lookup.
    /// @param cachedRecipe New cached recipe, or null if there is none due to the caches being invalidated.
    /// @param cacheIndex   The "recipe index" for which cache to interact with.
    default void onCachedRecipeChanged(HolderLookup.Provider registries, @Nullable CachedRecipe<RECIPE> cachedRecipe, int cacheIndex) {
        clearRecipeErrors(cacheIndex);
    }

    /// Called by [#onCachedRecipeChanged(HolderLookup.Provider, CachedRecipe, int)] when the list of cached errors should be reset due to the recipe not being valid
    /// anymore.
    ///
    /// @param cacheIndex The "recipe index" for which cache to interact with.
    default void clearRecipeErrors(int cacheIndex) {
    }

    @Override
    default void fillStackedContents(StackedItemContents contents) {
        RecipeContainers recipeContainers = getRecipeContainers(0);
        for (IResourceContainer<?> input : recipeContainers.inputs()) {
            if (input instanceof IInventorySlot slot) {
                contents.accountStack(slot.asItemStack());
            }
        }
        for (IResourceContainer<?> input : recipeContainers.outputs()) {
            if (input instanceof IInventorySlot slot) {
                contents.accountStack(slot.asItemStack());
            }
        }
    }

    /// {@return the resource containers that pertain to recipe processing}
    RecipeContainers getRecipeContainers(int cacheIndex);

    /// {@return the corresponding recipe book type}
    default RecipeBookType getRecipeBookType() {
        return getRecipeType().getRecipeType().getRecipeBookType();
    }

    /// {@return the corresponding recipe book categories}
    default RecipeBookCategoryDeferredRegister.MekanismRecipeBookTypes getBookCategories() {
        return getRecipeType().getRecipeType().getBookCategories();
    }

    /// Helper class that specifies the input cache's type for the recipe type. The reason it isn't defined in the main [IRecipeLookupHandler] is it isn't needed and
    /// would just make the class definitions a lot messier with very long generics that can be folded away into the helper interfaces we use anyway ofr actual lookup
    /// purposes.
    interface IRecipeTypedLookupHandler<RECIPE extends MekanismRecipe<?, ?, ?>, INPUT_CACHE extends IInputRecipeCache> extends IRecipeLookupHandler<RECIPE> {

        @Override
        IMekanismRecipeTypeProvider<?, ?, ?, RECIPE, INPUT_CACHE> getRecipeType();
    }

    interface ConstantUsageRecipeLookupHandler {

        /// Returns how much of the constant secondary input had been used for purposes of persisting through saves how far a cached recipe is through processing.
        ///
        /// @param cacheIndex The "recipe index" for which cache to interact with.
        ///
        /// @return Constant amount of secondary input that had been used before saving.
        default int getSavedUsedSoFar(int cacheIndex) {
            return 0;
        }
    }

    record RecipeContainers(List<? extends IResourceContainer<?>> inputs, List<? extends IResourceContainer<?>> outputs) {

        public RecipeContainers(List<? extends IResourceContainer<?>> inputs, IResourceContainer<?> output) {
            this(inputs, List.of(output));
        }

        public RecipeContainers(IResourceContainer<?> input, List<? extends IResourceContainer<?>> outputs) {
            this(List.of(input), outputs);
        }

        public RecipeContainers(IResourceContainer<?> input, IResourceContainer<?> output) {
            this(List.of(input), output);
        }

        public RecipeContainers {
            for (IResourceContainer<?> input : inputs) {
                if (input instanceof BasicInventorySlot slot) {
                    slot.setRecipeCraftingSlot(true);
                }
            }
            for (IResourceContainer<?> output : outputs) {
                if (output instanceof BasicInventorySlot slot) {
                    slot.setRecipeCraftingSlot(true);
                }
            }
        }

        public RecipeContainers withExtraInput(IResourceContainer<?> input) {
            List<IResourceContainer<?>> newInputs = new ArrayList<>(inputs);
            newInputs.add(input);
            return new RecipeContainers(List.copyOf(newInputs), outputs);
        }
    }
}