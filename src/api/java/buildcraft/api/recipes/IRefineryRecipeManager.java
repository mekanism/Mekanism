/** Copyright (c) 2011-2015, SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 *
 * The BuildCraft API is distributed under the terms of the MIT License. Please check the contents of the license, which
 * should be located as "LICENSE.API" in the BuildCraft source code distribution. */
package buildcraft.api.recipes;

import java.util.Collection;

import net.minecraftforge.fluids.FluidStack;

public interface IRefineryRecipeManager {

    void addRecipe(String id, FluidStack ingredient, FluidStack result, int energy, int delay);

    void addRecipe(String id, FluidStack ingredient1, FluidStack ingredient2, FluidStack result, int energy, int delay);

    void removeRecipe(String id);

    void removeRecipe(IFlexibleRecipe<FluidStack> recipe);

    Collection<IFlexibleRecipe<FluidStack>> getRecipes();

    IFlexibleRecipe<FluidStack> getRecipe(String currentRecipeId);

}
