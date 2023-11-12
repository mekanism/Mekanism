package mekanism.common.recipe.impl;

import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.chemical.pigment.PigmentStack;
import mekanism.api.recipes.basic.BasicItemStackToPigmentRecipe;
import mekanism.api.recipes.ingredients.ItemStackIngredient;
import mekanism.common.registries.MekanismRecipeSerializers;
import net.minecraft.world.item.crafting.RecipeSerializer;

@NothingNullByDefault
public class PigmentExtractingIRecipe extends BasicItemStackToPigmentRecipe {

    public PigmentExtractingIRecipe(ItemStackIngredient input, PigmentStack output) {
        super(input, output);
    }

    @Override
    public RecipeSerializer<BasicItemStackToPigmentRecipe> getSerializer() {
        return MekanismRecipeSerializers.PIGMENT_EXTRACTING.get();
    }

}