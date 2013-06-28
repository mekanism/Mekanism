/**
 * Team CoFH
 * 
 * Thermal Expansion
 */

package thermalexpansion.api.crafting;

import net.minecraft.item.ItemStack;

/**
 * Provides an interface to the recipe manager of the Powered Furnace. Accessible via
 * {@link CraftingManagers.FurnaceManager}
 */
public interface IFurnaceManager {

    /**
     * Add a recipe to the Powered Furnace.
     * 
     * @param energy
     *            Energy needed to process the item.
     * @param input
     *            ItemStack representing the input item.
     * @param output
     *            ItemStack representing the output item.
     * @param overwrite
     *            Flag to enable recipe overwriting. This will only be allowed if enabled in the
     *            Thermal Expansion Configuration file and will be logged for information purposes.
     */
    public boolean addRecipe(int energy, ItemStack input, ItemStack output, boolean overwrite);

    public boolean addRecipe(int energy, ItemStack input, ItemStack output);

    /**
     * Access to the full list of recipes.
     */
    IFurnaceRecipe[] getRecipeList();
}
