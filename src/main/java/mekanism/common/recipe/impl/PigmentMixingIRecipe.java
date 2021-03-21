package mekanism.common.recipe.impl;

import javax.annotation.Nonnull;
import mekanism.api.chemical.pigment.PigmentStack;
import mekanism.api.recipes.PigmentMixingRecipe;
import mekanism.api.recipes.inputs.chemical.PigmentStackIngredient;
import mekanism.common.recipe.MekanismRecipeType;
import mekanism.common.registries.MekanismBlocks;
import mekanism.common.registries.MekanismRecipeSerializers;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.item.crafting.IRecipeType;
import net.minecraft.util.ResourceLocation;

public class PigmentMixingIRecipe extends PigmentMixingRecipe {

    public PigmentMixingIRecipe(ResourceLocation id, PigmentStackIngredient leftInput, PigmentStackIngredient rightInput, PigmentStack output) {
        super(id, leftInput, rightInput, output);
    }

    @Nonnull
    @Override
    public IRecipeType<PigmentMixingRecipe> getType() {
        return MekanismRecipeType.PIGMENT_MIXING;
    }

    @Nonnull
    @Override
    public IRecipeSerializer<PigmentMixingRecipe> getSerializer() {
        return MekanismRecipeSerializers.PIGMENT_MIXING.getRecipeSerializer();
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