package mekanism.common.recipe.impl;

import mekanism.api.chemical.Chemical;
import mekanism.api.datagen.recipe.builder.ItemStackToEnergyRecipeBuilder;
import mekanism.api.recipes.ingredients.creator.IngredientCreatorAccess;
import mekanism.common.Mekanism;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.common.Tags;

class EnergyConversionRecipeProvider extends BaseSubRecipeProvider {

    EnergyConversionRecipeProvider(HolderGetter<Item> items, HolderGetter<Fluid> fluids, HolderGetter<Chemical> chemicals) {
        super(items, fluids, chemicals);
    }

    @Override
    public void addRecipes(RecipeOutput consumer, HolderLookup.Provider registries) {
        String basePath = "energy_conversion/";
        int redstoneEnergy = 10_000;
        addEnergyConversionRecipe(consumer, basePath, "redstone", Tags.Items.DUSTS_REDSTONE, redstoneEnergy);
        addEnergyConversionRecipe(consumer, basePath, "redstone_block", Tags.Items.STORAGE_BLOCKS_REDSTONE, 9 * redstoneEnergy);
    }

    private void addEnergyConversionRecipe(RecipeOutput consumer, String basePath, String name, TagKey<Item> inputTag, int output) {
        ItemStackToEnergyRecipeBuilder.energyConversion(
              IngredientCreatorAccess.item().from(this.items, inputTag),
              output
        ).save(consumer, Mekanism.rl(basePath + name));
    }
}