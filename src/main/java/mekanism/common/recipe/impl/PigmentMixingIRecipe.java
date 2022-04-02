package mekanism.common.recipe.impl;

import javax.annotation.Nonnull;
import mekanism.api.chemical.pigment.PigmentStack;
import mekanism.api.recipes.PigmentMixingRecipe;
import mekanism.api.recipes.ingredients.ChemicalStackIngredient.PigmentStackIngredient;
import mekanism.common.recipe.MekanismRecipeType;
import mekanism.common.registries.MekanismBlocks;
import mekanism.common.registries.MekanismRecipeSerializers;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;

public class PigmentMixingIRecipe extends PigmentMixingRecipe {

    public PigmentMixingIRecipe(ResourceLocation id, PigmentStackIngredient leftInput, PigmentStackIngredient rightInput, PigmentStack output) {
        super(id, leftInput, rightInput, output);
    }

    @Nonnull
    @Override
    public RecipeType<PigmentMixingRecipe> getType() {
        return MekanismRecipeType.PIGMENT_MIXING.get();
    }

    @Nonnull
    @Override
    public RecipeSerializer<PigmentMixingRecipe> getSerializer() {
        return MekanismRecipeSerializers.PIGMENT_MIXING.get();
    }

    @Nonnull
    @Override
    public String getGroup() {
        return MekanismBlocks.PIGMENT_MIXER.getName();
    }

    @Nonnull
    @Override
    public ItemStack getToastSymbol() {
        return MekanismBlocks.PIGMENT_MIXER.getItemStack();
    }
}