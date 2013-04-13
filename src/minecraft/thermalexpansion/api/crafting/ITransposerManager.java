/**
 * Team CoFH
 * 
 * Thermal Expansion
 */

package thermalexpansion.api.crafting;

import net.minecraft.item.ItemStack;
import net.minecraftforge.liquids.LiquidStack;

/**
 * Provides an interface to the recipe manager of the Liquid Transposer. Accessible via
 * {@link CraftingManagers.transposerManager}
 */
public interface ITransposerManager {

    /**
     * Add a recipe to the Liquid Transposer
     * 
     * @param energy
     *            Energy needed to process the item.
     * @param input
     *            ItemStack representing the input item.
     * @param output
     *            ItemStack representing the output item.
     * @param liquid
     *            LiquidStack representing the required liquid.
     * @param reversible
     *            Flag the recipe as reversible (container can be emptied).
     * @param overwrite
     *            Flag to enable recipe overwriting. This will only be allowed if enabled in the
     *            Thermal Expansion Configuration file and will be logged for information purposes.
     */
    public boolean addFillRecipe(int energy, ItemStack input, ItemStack output, LiquidStack liquid, boolean reversible, boolean overwrite);

    public boolean addFillRecipe(int energy, ItemStack input, ItemStack output, LiquidStack liquid, boolean reversible);

    /**
     * Add a recipe to the Liquid Transposer
     * 
     * @param energy
     *            Energy needed to process the item.
     * @param input
     *            ItemStack representing the input item.
     * @param output
     *            ItemStack representing the output item - this can be NULL if necessary, if the
     *            recipe is NOT reversible.
     * @param liquid
     *            LiquidStack representing the required liquid.
     * @param chance
     *            Integer representing % chance (out of 100) of receiving the item - liquid will
     *            always be extracted. If output is NULL, this MUST be set to 0. The recipe will not
     *            be added otherwise.
     * @param reversible
     *            Flag the recipe as reversible (container can be filled).
     * @param overwrite
     *            Flag to enable recipe overwriting. This will only be allowed if enabled in the
     *            Thermal Expansion Configuration file and will be logged for information purposes.
     */
    public boolean addExtractionRecipe(int energy, ItemStack input, ItemStack output, LiquidStack liquid, int chance, boolean reversible, boolean overwrite);

    public boolean addExtractionRecipe(int energy, ItemStack input, ItemStack output, LiquidStack liquid, int chance, boolean reversible);

    /**
     * Access to the list of recipes.
     */
    ITransposerRecipe[] getFillRecipeList();

    ITransposerRecipe[] getExtractionRecipeList();
}
