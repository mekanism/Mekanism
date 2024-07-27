package mekanism.common.recipe.impl;

import mekanism.api.datagen.recipe.builder.ItemStackToEnergyRecipeBuilder;
import mekanism.api.recipes.ingredients.creator.IngredientCreatorAccess;
import mekanism.common.Mekanism;
import mekanism.common.recipe.ISubRecipeProvider;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.common.Tags;

class EnergyConversionRecipeProvider implements ISubRecipeProvider {

    @Override
    public void addRecipes(RecipeOutput consumer, HolderLookup.Provider registries) {
        String basePath = "energy_conversion/";
        long redstoneEnergy = 10_000L;
        addEnergyConversionRecipe(consumer, basePath, "redstone", Tags.Items.DUSTS_REDSTONE, redstoneEnergy);
        addEnergyConversionRecipe(consumer, basePath, "redstone_block", Tags.Items.STORAGE_BLOCKS_REDSTONE, 9 * redstoneEnergy);
    }

    private void addEnergyConversionRecipe(RecipeOutput consumer, String basePath, String name, TagKey<Item> inputTag, long output) {
        ItemStackToEnergyRecipeBuilder.energyConversion(
              IngredientCreatorAccess.item().from(inputTag),
              output
        ).build(consumer, Mekanism.rl(basePath + name));
    }
}