/**
 * Team CoFH
 * 
 * Thermal Expansion
 */

package thermalexpansion.api.crafting;

import net.minecraft.item.ItemStack;

/**
 * Provides an interface to the recipe manager of the Induction Smelter. Accessible via
 * {@link CraftingManagers.smelterManager}
 */
public interface ISmelterManager {

    /**
     * Add a recipe to the Induction Smelter
     * 
     * @param energy
     *            Energy needed to process the item.
     * @param primaryInput
     *            ItemStack representing the primary input item.
     * @param secondaryInput
     *            ItemStack representing the secondary input item.
     * @param primaryOutput
     *            ItemStack representing the primary (only) output product.
     * @param overwrite
     *            Flag to enable recipe overwriting. This will only be allowed if enabled in the
     *            Thermal Expansion Configuration file and will be logged for information purposes.
     */
    public boolean addRecipe(int energy, ItemStack primaryInput, ItemStack secondaryInput, ItemStack primaryOutput, boolean overwrite);

    public boolean addRecipe(int energy, ItemStack primaryInput, ItemStack secondaryInput, ItemStack primaryOutput);

    /**
     * Add a recipe to the Induction Smelter
     * 
     * @param energy
     *            Energy needed to process the item.
     * @param primaryInput
     *            ItemStack representing the primary input item.
     * @param secondaryInput
     *            ItemStack representing the secondary input item.
     * @param primaryOutput
     *            ItemStack representing the primary output product.
     * @param secondaryOutput
     *            ItemStack representing the secondary output product. Product % is taken to be 100.
     * @param overwrite
     *            Flag to enable recipe overwriting. This will only be allowed if enabled in the
     *            Thermal Expansion Configuration file and will be logged for information purposes.
     */
    public boolean addRecipe(int energy, ItemStack primaryInput, ItemStack secondaryInput, ItemStack primaryOutput, ItemStack secondaryOutput, boolean overwrite);

    public boolean addRecipe(int energy, ItemStack primaryInput, ItemStack secondaryInput, ItemStack primaryOutput, ItemStack secondaryOutput);

    /**
     * Add a recipe to the Induction Smelter
     * 
     * @param energy
     *            Energy needed to process the item.
     * @param primaryInput
     *            ItemStack representing the primary input item.
     * @param secondaryInput
     *            ItemStack representing the secondary input item.
     * @param primaryOutput
     *            ItemStack representing the primary output product.
     * @param secondaryOutput
     *            ItemStack representing the secondary output product.
     * @param secondaryChance
     *            Integer representing % chance (out of 100) of the secondary product being created.
     * @param overwrite
     *            Flag to enable recipe overwriting. This will only be allowed if enabled in the
     *            Thermal Expansion Configuration file and will be logged for information purposes.
     */
    public boolean addRecipe(int energy, ItemStack primaryInput, ItemStack secondaryInput, ItemStack primaryOutput, ItemStack secondaryOutput, int secondaryChance,
            boolean overwrite);

    public boolean addRecipe(int energy, ItemStack primaryInput, ItemStack secondaryInput, ItemStack primaryOutput, ItemStack secondaryOutput, int secondaryChance);

    /**
     * Access to the list of recipes.
     */
    ISmelterRecipe[] getRecipeList();
}
