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
public class EnrichingIRecipe extends BasicItemStackToItemStackRecipe implements ItemStackOutputInternal {

    public EnrichingIRecipe(ItemStackIngredient input, ItemStack output) {
        super(input, output, MekanismRecipeTypes.TYPE_ENRICHING.get());
    }

    @Override
    public RecipeSerializer<EnrichingIRecipe> getSerializer() {
        return MekanismRecipeSerializers.ENRICHING.get();
    }

    @Override
    public String getGroup() {
        return "enrichment_chamber";
    }

    @Override
    public ItemStack getToastSymbol() {
        return MekanismBlocks.ENRICHMENT_CHAMBER.getItemStack();
    }

    @Override
    public ItemStack getOutputRaw() {
        return output;
    }
}