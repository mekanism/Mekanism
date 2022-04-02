package mekanism.common.recipe.impl;

import javax.annotation.Nonnull;
import mekanism.api.chemical.gas.GasStack;
import mekanism.api.recipes.ChemicalInfuserRecipe;
import mekanism.api.recipes.ingredients.ChemicalStackIngredient.GasStackIngredient;
import mekanism.common.recipe.MekanismRecipeType;
import mekanism.common.registries.MekanismBlocks;
import mekanism.common.registries.MekanismRecipeSerializers;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;

public class ChemicalInfuserIRecipe extends ChemicalInfuserRecipe {

    public ChemicalInfuserIRecipe(ResourceLocation id, GasStackIngredient leftInput, GasStackIngredient rightInput, GasStack output) {
        super(id, leftInput, rightInput, output);
    }

    @Nonnull
    @Override
    public RecipeType<ChemicalInfuserRecipe> getType() {
        return MekanismRecipeType.CHEMICAL_INFUSING.get();
    }

    @Nonnull
    @Override
    public RecipeSerializer<ChemicalInfuserRecipe> getSerializer() {
        return MekanismRecipeSerializers.CHEMICAL_INFUSING.get();
    }

    @Nonnull
    @Override
    public String getGroup() {
        return MekanismBlocks.CHEMICAL_INFUSER.getName();
    }

    @Nonnull
    @Override
    public ItemStack getToastSymbol() {
        return MekanismBlocks.CHEMICAL_INFUSER.getItemStack();
    }
}