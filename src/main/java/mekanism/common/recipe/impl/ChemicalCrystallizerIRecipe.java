package mekanism.common.recipe.impl;

import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.recipes.basic.BasicChemicalCrystallizerRecipe;
import mekanism.api.recipes.ingredients.ChemicalStackIngredient;
import mekanism.common.registries.MekanismRecipeSerializers;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeSerializer;

@NothingNullByDefault
public class ChemicalCrystallizerIRecipe extends BasicChemicalCrystallizerRecipe {

    public ChemicalCrystallizerIRecipe(ChemicalStackIngredient<?, ?> input, ItemStack output) {
        super(input, output);
    }

    @Override
    public RecipeSerializer<BasicChemicalCrystallizerRecipe> getSerializer() {
        return MekanismRecipeSerializers.CRYSTALLIZING.get();
    }

}