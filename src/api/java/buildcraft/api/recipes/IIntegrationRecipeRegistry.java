package buildcraft.api.recipes;

import java.util.Iterator;

public interface IIntegrationRecipeRegistry extends IIntegrationRecipeProvider {
    void addRecipe(IntegrationRecipe recipe);

    void addRecipeProvider(IIntegrationRecipeProvider provider);

    /** Gets all of the simple recipes that are registered. Note that you *can* use the returned iterator's
     * {@link Iterator#remove()} method to remove recipes from this registry. */
    Iterable<IntegrationRecipe> getAllRecipes();

    /** Gets all of the complex recipe providers that are registered. Note that you *can* use the returned iterator's
     * {@link Iterator#remove()} method to remove providers from this registry. */
    Iterable<IIntegrationRecipeProvider> getAllRecipeProviders();
}
