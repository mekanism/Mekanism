package mekanism.api.recipes;

import mekanism.api.annotations.ParametersAreNotNullByDefault;
import mekanism.api.chemical.infuse.InfuseType;
import mekanism.api.chemical.infuse.InfusionStack;
import mekanism.api.recipes.chemical.ItemStackChemicalToItemStackRecipe;
import mekanism.api.recipes.ingredients.ChemicalStackIngredient.InfusionStackIngredient;
import mekanism.api.recipes.ingredients.ItemStackIngredient;
import net.minecraft.world.item.ItemStack;

/**
 * Input: ItemStack
 * <br>
 * Input: Infuse Type
 * <br>
 * Output: ItemStack
 *
 * @apiNote Metallurgic Infusers and Infusing Factories can process this recipe type.
 */
@ParametersAreNotNullByDefault
public abstract class MetallurgicInfuserRecipe extends ItemStackChemicalToItemStackRecipe<InfuseType, InfusionStack, InfusionStackIngredient> {

    /**
     * @param itemInput     Item input.
     * @param infusionInput Infusion input.
     * @param output        Output.
     */
    public MetallurgicInfuserRecipe(ItemStackIngredient itemInput, InfusionStackIngredient infusionInput, ItemStack output) {
        super(itemInput, infusionInput, output);
    }
}