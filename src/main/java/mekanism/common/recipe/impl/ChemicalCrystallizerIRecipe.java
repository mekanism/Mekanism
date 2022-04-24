package mekanism.common.recipe.impl;

import javax.annotation.Nonnull;
import mekanism.api.recipes.ChemicalCrystallizerRecipe;
import mekanism.api.recipes.ingredients.ChemicalStackIngredient;
import mekanism.common.recipe.MekanismRecipeType;
import mekanism.common.registries.MekanismBlocks;
import mekanism.common.registries.MekanismRecipeSerializers;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;

public class ChemicalCrystallizerIRecipe extends ChemicalCrystallizerRecipe {

    public ChemicalCrystallizerIRecipe(ResourceLocation id, ChemicalStackIngredient<?, ?> input, ItemStack output) {
        super(id, input, output);
    }

    @Nonnull
    @Override
    public RecipeType<ChemicalCrystallizerRecipe> getType() {
        return MekanismRecipeType.CRYSTALLIZING.get();
    }

    @Nonnull
    @Override
    public RecipeSerializer<ChemicalCrystallizerRecipe> getSerializer() {
        return MekanismRecipeSerializers.CRYSTALLIZING.get();
    }

    @Nonnull
    @Override
    public String getGroup() {
        return MekanismBlocks.CHEMICAL_CRYSTALLIZER.getName();
    }

    @Nonnull
    @Override
    public ItemStack getToastSymbol() {
        return MekanismBlocks.CHEMICAL_CRYSTALLIZER.getItemStack();
    }
}