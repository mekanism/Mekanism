/** Copyright (c) 2011-2015, SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 *
 * The BuildCraft API is distributed under the terms of the MIT License. Please check the contents of the license, which
 * should be located as "LICENSE.API" in the BuildCraft source code distribution. */
package buildcraft.api.recipes;

import java.util.Collection;

public interface IProgrammingRecipeManager {
    void addRecipe(IProgrammingRecipe recipe);

    void removeRecipe(String id);

    void removeRecipe(IProgrammingRecipe recipe);

    IProgrammingRecipe getRecipe(String id);

    Collection<IProgrammingRecipe> getRecipes();
}
