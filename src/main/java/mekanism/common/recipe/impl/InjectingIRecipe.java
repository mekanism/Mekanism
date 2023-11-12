package mekanism.common.recipe.impl;

import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.recipes.MekanismRecipeTypes;
import mekanism.api.recipes.basic.BasicItemStackGasToItemStackRecipe;
import mekanism.api.recipes.ingredients.ChemicalStackIngredient.GasStackIngredient;
import mekanism.api.recipes.ingredients.ItemStackIngredient;
import mekanism.common.registries.MekanismBlocks;
import mekanism.common.registries.MekanismRecipeSerializers;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeSerializer;

@NothingNullByDefault
public class InjectingIRecipe extends BasicItemStackGasToItemStackRecipe implements ItemStackOutputInternal {

    public InjectingIRecipe(ItemStackIngredient itemInput, GasStackIngredient gasInput, ItemStack output) {
        super(itemInput, gasInput, output, MekanismRecipeTypes.TYPE_INJECTING.get());
    }

    @Override
    public RecipeSerializer<InjectingIRecipe> getSerializer() {
        return MekanismRecipeSerializers.INJECTING.get();
    }

    @Override
    public String getGroup() {
        return MekanismBlocks.CHEMICAL_INJECTION_CHAMBER.getName();
    }

    @Override
    public ItemStack getToastSymbol() {
        return MekanismBlocks.CHEMICAL_INJECTION_CHAMBER.getItemStack();
    }

    @Override
    public ItemStack getOutputRaw() {
        return output;
    }
}