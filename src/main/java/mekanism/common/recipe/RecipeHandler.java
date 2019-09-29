package mekanism.common.recipe;

import javax.annotation.ParametersAreNonnullByDefault;
import mekanism.api.recipes.inputs.InfusionIngredient;
import mekanism.api.recipes.inputs.ItemStackIngredient;
import net.minecraft.item.ItemStack;

/**
 * Class used to handle machine recipes. This is used for both adding and fetching recipes.
 *
 * @author AidanBrady, unpairedbracket
 */
@ParametersAreNonnullByDefault
public final class RecipeHandler {

    /**
     * Add a Metallurgic Infuser recipe.
     *
     * @param infusionIngredient - which Infuse to use
     * @param input              - input ItemStack
     * @param output             - output ItemStack
     */
    @Deprecated
    public static void addMetallurgicInfuserRecipe(InfusionIngredient infusionIngredient, ItemStackIngredient input, ItemStack output) {
        //TODO: API way of adding recipes
        //Recipe.METALLURGIC_INFUSER.put(new MetallurgicInfuserRecipe(input, infusionIngredient, output));
    }

    /**
     * Add a Precision Sawmill recipe.
     *
     * @param input           - input ItemStack
     * @param primaryOutput   - guaranteed output
     * @param secondaryOutput - possible extra output
     * @param chance          - probability of obtaining extra output
     */
    @Deprecated
    public static void addPrecisionSawmillRecipe(ItemStackIngredient input, ItemStack primaryOutput, ItemStack secondaryOutput, double chance) {
        //TODO: API way of adding recipes
        //Recipe.PRECISION_SAWMILL.put(new SawmillRecipe(input, primaryOutput, secondaryOutput, chance));
    }

    /**
     * Add a Precision Sawmill recipe with no chance output
     *
     * @param input         - input ItemStack
     * @param primaryOutput - guaranteed output
     */
    @Deprecated
    public static void addPrecisionSawmillRecipe(ItemStackIngredient input, ItemStack primaryOutput) {
        addPrecisionSawmillRecipe(input, primaryOutput, ItemStack.EMPTY, 0);
    }
}