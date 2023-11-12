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
public class CrushingIRecipe extends BasicItemStackToItemStackRecipe implements ItemStackOutputInternal {

    public CrushingIRecipe(ItemStackIngredient input, ItemStack output) {
        super(input, output, MekanismRecipeTypes.TYPE_CRUSHING.get());
    }

    @Override
    public RecipeSerializer<CrushingIRecipe> getSerializer() {
        return MekanismRecipeSerializers.CRUSHING.get();
    }

    @Override
    public String getGroup() {
        return "crusher";
    }

    @Override
    public ItemStack getToastSymbol() {
        return MekanismBlocks.CRUSHER.getItemStack();
    }

    @Override
    public ItemStack getOutputRaw() {
        return output;
    }
}