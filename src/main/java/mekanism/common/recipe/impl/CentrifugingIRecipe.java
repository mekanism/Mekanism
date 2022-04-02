package mekanism.common.recipe.impl;

import javax.annotation.Nonnull;
import mekanism.api.chemical.gas.GasStack;
import mekanism.api.recipes.GasToGasRecipe;
import mekanism.api.recipes.ingredients.ChemicalStackIngredient.GasStackIngredient;
import mekanism.common.recipe.MekanismRecipeType;
import mekanism.common.registries.MekanismBlocks;
import mekanism.common.registries.MekanismRecipeSerializers;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;

public class CentrifugingIRecipe extends GasToGasRecipe {

    public CentrifugingIRecipe(ResourceLocation id, GasStackIngredient input, GasStack output) {
        super(id, input, output);
    }

    @Nonnull
    @Override
    public RecipeType<GasToGasRecipe> getType() {
        return MekanismRecipeType.CENTRIFUGING.get();
    }

    @Nonnull
    @Override
    public RecipeSerializer<GasToGasRecipe> getSerializer() {
        return MekanismRecipeSerializers.CENTRIFUGING.get();
    }

    @Nonnull
    @Override
    public String getGroup() {
        return MekanismBlocks.ISOTOPIC_CENTRIFUGE.getName();
    }

    @Nonnull
    @Override
    public ItemStack getToastSymbol() {
        return MekanismBlocks.ISOTOPIC_CENTRIFUGE.getItemStack();
    }
}
