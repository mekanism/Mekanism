package mekanism.common.recipe.impl;

import java.util.function.Consumer;
import mekanism.api.datagen.recipe.builder.ItemStackToEnergyRecipeBuilder;
import mekanism.api.math.FloatingLong;
import mekanism.api.recipes.ingredients.creator.IngredientCreatorAccess;
import mekanism.common.Mekanism;
import mekanism.common.recipe.ISubRecipeProvider;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraftforge.common.Tags;

class EnergyConversionRecipeProvider implements ISubRecipeProvider {

    @Override
    public void addRecipes(Consumer<FinishedRecipe> consumer) {
        String basePath = "energy_conversion/";
        FloatingLong redstoneEnergy = FloatingLong.createConst(10_000);
        addEnergyConversionRecipe(consumer, basePath, "redstone", Tags.Items.DUSTS_REDSTONE, redstoneEnergy);
        addEnergyConversionRecipe(consumer, basePath, "redstone_block", Tags.Items.STORAGE_BLOCKS_REDSTONE, redstoneEnergy.multiply(9));
    }

    private void addEnergyConversionRecipe(Consumer<FinishedRecipe> consumer, String basePath, String name, TagKey<Item> inputTag, FloatingLong output) {
        ItemStackToEnergyRecipeBuilder.energyConversion(
              IngredientCreatorAccess.item().from(inputTag),
              output
        ).build(consumer, Mekanism.rl(basePath + name));
    }
}