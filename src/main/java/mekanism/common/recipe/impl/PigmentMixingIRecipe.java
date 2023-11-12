package mekanism.common.recipe.impl;

import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.chemical.pigment.PigmentStack;
import mekanism.api.recipes.basic.BasicPigmentMixingRecipe;
import mekanism.api.recipes.ingredients.ChemicalStackIngredient.PigmentStackIngredient;
import mekanism.common.registries.MekanismRecipeSerializers;
import net.minecraft.world.item.crafting.RecipeSerializer;

@NothingNullByDefault
public class PigmentMixingIRecipe extends BasicPigmentMixingRecipe {

    public PigmentMixingIRecipe(PigmentStackIngredient leftInput, PigmentStackIngredient rightInput, PigmentStack output) {
        super(leftInput, rightInput, output);
    }

    @Override
    public RecipeSerializer<BasicPigmentMixingRecipe> getSerializer() {
        return MekanismRecipeSerializers.PIGMENT_MIXING.get();
    }

}