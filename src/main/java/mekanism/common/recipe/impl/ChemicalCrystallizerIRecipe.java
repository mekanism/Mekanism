package mekanism.common.recipe.impl;

import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.chemical.ChemicalType;
import mekanism.api.recipes.basic.BasicChemicalCrystallizerRecipe;
import mekanism.api.recipes.ingredients.ChemicalStackIngredient;
import mekanism.common.registries.MekanismBlocks;
import mekanism.common.registries.MekanismRecipeSerializers;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeSerializer;

@NothingNullByDefault
public class ChemicalCrystallizerIRecipe extends BasicChemicalCrystallizerRecipe {

    public ChemicalCrystallizerIRecipe(ChemicalStackIngredient<?, ?> input, ItemStack output) {
        super(input, output);
    }

    @Override
    public RecipeSerializer<ChemicalCrystallizerIRecipe> getSerializer() {
        return MekanismRecipeSerializers.CRYSTALLIZING.get();
    }

    @Override
    public ItemStack getToastSymbol() {
        return MekanismBlocks.CHEMICAL_CRYSTALLIZER.getItemStack();
    }

    public ItemStack getOutputRaw() {
        return this.output;
    }

    public ChemicalType getChemicalType() {
        return chemicalType;
    }
}