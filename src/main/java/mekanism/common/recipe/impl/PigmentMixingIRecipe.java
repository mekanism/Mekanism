package mekanism.common.recipe.impl;

import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.chemical.pigment.Pigment;
import mekanism.api.chemical.pigment.PigmentStack;
import mekanism.api.recipes.PigmentMixingRecipe;
import mekanism.api.recipes.basic.BasicPigmentMixingRecipe;
import mekanism.api.recipes.ingredients.ChemicalStackIngredient.PigmentStackIngredient;
import mekanism.common.recipe.MekanismRecipeType;
import mekanism.common.registries.MekanismBlocks;
import mekanism.common.registries.MekanismRecipeSerializers;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;

@NothingNullByDefault
public class PigmentMixingIRecipe extends BasicPigmentMixingRecipe implements ChemicalOutputInternal<Pigment, PigmentStack> {

    public PigmentMixingIRecipe(PigmentStackIngredient leftInput, PigmentStackIngredient rightInput, PigmentStack output) {
        super(leftInput, rightInput, output);
    }

    @Override
    public RecipeType<PigmentMixingRecipe> getType() {
        return MekanismRecipeType.PIGMENT_MIXING.get();
    }

    @Override
    public RecipeSerializer<PigmentMixingIRecipe> getSerializer() {
        return MekanismRecipeSerializers.PIGMENT_MIXING.get();
    }

    @Override
    public String getGroup() {
        return MekanismBlocks.PIGMENT_MIXER.getName();
    }

    @Override
    public ItemStack getToastSymbol() {
        return MekanismBlocks.PIGMENT_MIXER.getItemStack();
    }

    @Override
    public PigmentStack getOutputRaw() {
        return output;
    }
}