package buildcraft.api.recipes;

import java.util.Iterator;

import javax.annotation.Nonnull;

public interface IAssemblyRecipeRegistry extends IAssemblyRecipeProvider {
    void addRecipe(@Nonnull AssemblyRecipe recipe);

    void addRecipeProvider(@Nonnull IAssemblyRecipeProvider provider);

    /** Gets all of the simple recipes that are registered. Note that you *can* use the returned iterator's
     * {@link Iterator#remove()} method to remove recipes from this registry. */
    Iterable<AssemblyRecipe> getAllRecipes();

    /** Gets all of the complex recipe providers that are registered. Note that you *can* use the returned iterator's
     * {@link Iterator#remove()} method to remove providers from this registry. */
    Iterable<IAssemblyRecipeProvider> getAllRecipeProviders();
}
