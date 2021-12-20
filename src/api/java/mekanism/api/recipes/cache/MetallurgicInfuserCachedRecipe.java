package mekanism.api.recipes.cache;

import javax.annotation.ParametersAreNonnullByDefault;
import mekanism.api.annotations.NonNull;
import mekanism.api.chemical.infuse.InfuseType;
import mekanism.api.chemical.infuse.InfusionStack;
import mekanism.api.recipes.MetallurgicInfuserRecipe;
import mekanism.api.recipes.cache.chemical.ItemStackChemicalToItemStackCachedRecipe;
import mekanism.api.recipes.inputs.IInputHandler;
import mekanism.api.recipes.inputs.chemical.InfusionStackIngredient;
import mekanism.api.recipes.outputs.IOutputHandler;
import net.minecraft.item.ItemStack;

/**
 * Base class to help implement handling of item infuse type to item recipes.
 */
@Deprecated//TODO - 1.18: Remove this
@ParametersAreNonnullByDefault
public class MetallurgicInfuserCachedRecipe extends ItemStackChemicalToItemStackCachedRecipe<InfuseType, InfusionStack, InfusionStackIngredient, MetallurgicInfuserRecipe> {

    /**
     * @param recipe               Recipe.
     * @param itemInputHandler     Item input handler.
     * @param infusionInputHandler Infusion input handler.
     * @param outputHandler        Output handler.
     */
    public MetallurgicInfuserCachedRecipe(MetallurgicInfuserRecipe recipe, IInputHandler<@NonNull InfusionStack> infusionInputHandler,
          IInputHandler<@NonNull ItemStack> itemInputHandler, IOutputHandler<@NonNull ItemStack> outputHandler) {
        super(recipe, itemInputHandler, infusionInputHandler, outputHandler);
    }
}