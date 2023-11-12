package mekanism.common.recipe.impl;

import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.chemical.infuse.InfusionStack;
import mekanism.api.recipes.basic.BasicItemStackToInfuseTypeRecipe;
import mekanism.api.recipes.ingredients.ItemStackIngredient;
import mekanism.common.registries.MekanismRecipeSerializers;
import net.minecraft.world.item.crafting.RecipeSerializer;

@NothingNullByDefault
public class InfusionConversionIRecipe extends BasicItemStackToInfuseTypeRecipe {

    public InfusionConversionIRecipe(ItemStackIngredient input, InfusionStack output) {
        super(input, output);
    }

    @Override
    public RecipeSerializer<InfusionConversionIRecipe> getSerializer() {
        return MekanismRecipeSerializers.INFUSION_CONVERSION.get();
    }

}