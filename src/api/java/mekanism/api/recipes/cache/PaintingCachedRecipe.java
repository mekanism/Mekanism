package mekanism.api.recipes.cache;

import javax.annotation.ParametersAreNonnullByDefault;
import mekanism.api.annotations.NonNull;
import mekanism.api.chemical.pigment.Pigment;
import mekanism.api.chemical.pigment.PigmentStack;
import mekanism.api.recipes.PaintingRecipe;
import mekanism.api.recipes.cache.chemical.ItemStackChemicalToItemStackCachedRecipe;
import mekanism.api.recipes.inputs.IInputHandler;
import mekanism.api.recipes.inputs.chemical.PigmentStackIngredient;
import mekanism.api.recipes.outputs.IOutputHandler;
import net.minecraft.item.ItemStack;

/**
 * Base class to help implement handling of painting recipes.
 */
@ParametersAreNonnullByDefault
public class PaintingCachedRecipe extends ItemStackChemicalToItemStackCachedRecipe<Pigment, PigmentStack, PigmentStackIngredient, PaintingRecipe> {

    /**
     * @param recipe              Recipe.
     * @param itemInputHandler    Item input handler.
     * @param pigmentInputHandler Pigment input handler.
     * @param outputHandler       Output handler.
     */
    public PaintingCachedRecipe(PaintingRecipe recipe, IInputHandler<@NonNull ItemStack> itemInputHandler, IInputHandler<@NonNull PigmentStack> pigmentInputHandler,
          IOutputHandler<@NonNull ItemStack> outputHandler) {
        super(recipe, itemInputHandler, pigmentInputHandler, outputHandler);
    }
}