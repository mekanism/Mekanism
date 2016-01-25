/** Copyright (c) 2011-2015, SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 *
 * The BuildCraft API is distributed under the terms of the MIT License. Please check the contents of the license, which
 * should be located as "LICENSE.API" in the BuildCraft source code distribution. */
package buildcraft.api.recipes;

import java.util.List;

import net.minecraft.item.ItemStack;

public interface IIntegrationRecipe {
    /** Get the energy cost (in RF) of the recipe.
     * 
     * @return The energy cost of the recipe. */
    int getEnergyCost();

    /** Get a list of example inputs. This is used for recipe preview.
     * 
     * @return A list of example inputs. */
    List<ItemStack> getExampleInput();

    /** Get a list of example expansions. This is used for recipe preview. If the amount of lists of expansions is
     * smaller than the maximum expansion count, the lists will be repeated.
     * 
     * @return A list of every slot's list of example expansions. */
    List<List<ItemStack>> getExampleExpansions();

    /** Get a list of example outputs. This is used for recipe preview.
     * 
     * @return A list of example outputs. */
    List<ItemStack> getExampleOutput();

    /** Check if an input is valid.
     * 
     * @param input The input.
     * @return Whether the input is valid. */
    boolean isValidInput(ItemStack input);

    /** Check if an expansion is valid.
     * 
     * @param input The input currently in.
     * @param expansion The expansion.
     * @return Whether the expansion can be fitted to a given input. */
    boolean isValidExpansion(ItemStack input, ItemStack expansion);

    /** Craft the recipe. Keep in mind that you need to decrease the amount of expansions yourself - the amount of
     * inputs is decreased for you.
     * 
     * @param input The input.
     * @param expansions All inserted expansions.
     * @param preview If true, do not decrease the amount of expansions.
     * @return The output stack. */
    ItemStack craft(ItemStack input, List<ItemStack> expansions, boolean preview);

    /** Returns the maximum count of expansions this recipe can have.
     * 
     * @return -1 for no limit, a different number otherwise */
    int getMaximumExpansionCount(ItemStack input);
}
