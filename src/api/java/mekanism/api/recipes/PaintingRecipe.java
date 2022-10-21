package mekanism.api.recipes;

import mekanism.api.annotations.ParametersAreNotNullByDefault;
import mekanism.api.chemical.pigment.Pigment;
import mekanism.api.chemical.pigment.PigmentStack;
import mekanism.api.recipes.chemical.ItemStackChemicalToItemStackRecipe;
import mekanism.api.recipes.ingredients.ChemicalStackIngredient.PigmentStackIngredient;
import mekanism.api.recipes.ingredients.ItemStackIngredient;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

/**
 * Input: ItemStack
 * <br>
 * Input: Pigment
 * <br>
 * Output: ItemStack
 *
 * @apiNote Painting Machines can process this recipe type.
 */
@ParametersAreNotNullByDefault
public abstract class PaintingRecipe extends ItemStackChemicalToItemStackRecipe<Pigment, PigmentStack, PigmentStackIngredient> {

    /**
     * @param id           Recipe name.
     * @param itemInput    Item input.
     * @param pigmentInput Pigment input.
     * @param output       Output.
     */
    public PaintingRecipe(ResourceLocation id, ItemStackIngredient itemInput, PigmentStackIngredient pigmentInput, ItemStack output) {
        super(id, itemInput, pigmentInput, output);
    }
}