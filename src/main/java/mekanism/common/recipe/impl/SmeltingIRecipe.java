package mekanism.common.recipe.impl;

import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.recipes.MekanismRecipeTypes;
import mekanism.api.recipes.basic.BasicItemStackToItemStackRecipe;
import mekanism.api.recipes.ingredients.ItemStackIngredient;
import mekanism.common.registries.MekanismBlocks;
import mekanism.common.registries.MekanismRecipeSerializers;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeSerializer;

@NothingNullByDefault
public class SmeltingIRecipe extends BasicItemStackToItemStackRecipe implements ItemStackOutputInternal {

    public SmeltingIRecipe(ItemStackIngredient input, ItemStack output) {
        super(input, output, MekanismRecipeTypes.TYPE_SMELTING.get());
    }

    @Override
    public RecipeSerializer<SmeltingIRecipe> getSerializer() {
        return MekanismRecipeSerializers.SMELTING.get();
    }

    @Override
    public String getGroup() {
        return MekanismBlocks.ENERGIZED_SMELTER.getName();
    }

    @Override
    public ItemStack getToastSymbol() {
        return MekanismBlocks.ENERGIZED_SMELTER.getItemStack();
    }

    @Override
    public ItemStack getOutputRaw() {
        return output;
    }
}