package mekanism.api.recipes;

import mekanism.api.annotations.ParametersAreNotNullByDefault;
import mekanism.api.chemical.infuse.InfuseType;
import mekanism.api.chemical.infuse.InfusionStack;
import mekanism.api.recipes.chemical.ItemStackToChemicalRecipe;
import mekanism.api.recipes.ingredients.ItemStackIngredient;

/**
 * Input: ItemStack
 * <br>
 * Output: InfusionStack
 *
 * @apiNote Infusion conversion recipes can be used in any slots in Mekanism machines that are able to convert items to infuse types, for example in Metallurgic Infusers
 * and Infusing Factories.
 */
@ParametersAreNotNullByDefault
public abstract class ItemStackToInfuseTypeRecipe extends ItemStackToChemicalRecipe<InfuseType, InfusionStack> {

    /**
     * @param input  Input.
     * @param output Output.
     */
    public ItemStackToInfuseTypeRecipe(ItemStackIngredient input, InfusionStack output) {
        super(input, output);
    }
}