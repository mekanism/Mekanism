package mekanism.common.recipe.impl;

import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.chemical.ChemicalType;
import mekanism.api.recipes.ChemicalCrystallizerRecipe;
import mekanism.api.recipes.ingredients.ChemicalStackIngredient;
import mekanism.common.recipe.MekanismRecipeType;
import mekanism.common.registries.MekanismBlocks;
import mekanism.common.registries.MekanismRecipeSerializers;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;

@NothingNullByDefault
public class ChemicalCrystallizerIRecipe extends ChemicalCrystallizerRecipe {

    public ChemicalCrystallizerIRecipe(ChemicalStackIngredient<?, ?> input, ItemStack output) {
        super(input, output);
    }

    @Override
    public RecipeType<ChemicalCrystallizerRecipe> getType() {
        return MekanismRecipeType.CRYSTALLIZING.get();
    }

    @Override
    public RecipeSerializer<ChemicalCrystallizerIRecipe> getSerializer() {
        return MekanismRecipeSerializers.CRYSTALLIZING.get();
    }

    @Override
    public String getGroup() {
        return MekanismBlocks.CHEMICAL_CRYSTALLIZER.getName();
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