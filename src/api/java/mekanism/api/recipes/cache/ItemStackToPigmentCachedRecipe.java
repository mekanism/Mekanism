package mekanism.api.recipes.cache;

import javax.annotation.ParametersAreNonnullByDefault;
import mekanism.api.annotations.FieldsAreNonnullByDefault;
import mekanism.api.annotations.NonNull;
import mekanism.api.chemical.pigment.Pigment;
import mekanism.api.chemical.pigment.PigmentStack;
import mekanism.api.recipes.ItemStackToPigmentRecipe;
import mekanism.api.recipes.cache.chemical.ItemStackToChemicalCachedRecipe;
import mekanism.api.recipes.inputs.IInputHandler;
import mekanism.api.recipes.outputs.IOutputHandler;
import net.minecraft.item.ItemStack;

/**
 * Base class to help implement handling of item to pigment recipes.
 */
@FieldsAreNonnullByDefault
@ParametersAreNonnullByDefault
public class ItemStackToPigmentCachedRecipe extends ItemStackToChemicalCachedRecipe<Pigment, PigmentStack, ItemStackToPigmentRecipe> {

    /**
     * @param recipe        Recipe.
     * @param inputHandler  Input handler.
     * @param outputHandler Output handler.
     */
    public ItemStackToPigmentCachedRecipe(ItemStackToPigmentRecipe recipe, IInputHandler<@NonNull ItemStack> inputHandler,
          IOutputHandler<@NonNull PigmentStack> outputHandler) {
        super(recipe, inputHandler, outputHandler);
    }
}