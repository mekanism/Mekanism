package mekanism.common.recipe.impl;

import java.util.function.Consumer;
import mekanism.api.datagen.recipe.builder.ItemStackToEnergyRecipeBuilder;
import mekanism.api.math.FloatingLong;
import mekanism.api.recipes.inputs.ItemStackIngredient;
import mekanism.common.Mekanism;
import mekanism.common.recipe.ISubRecipeProvider;
import net.minecraft.data.IFinishedRecipe;
import net.minecraft.item.Item;
import net.minecraft.tags.ITag;
import net.minecraftforge.common.Tags;

class EnergyConversionRecipeProvider implements ISubRecipeProvider {

    @Override
    public void addRecipes(Consumer<IFinishedRecipe> consumer) {
        String basePath = "energy_conversion/";
        FloatingLong redstoneEnergy = FloatingLong.createConst(10_000);
        addEnergyConversionRecipe(consumer, basePath, "redstone", Tags.Items.DUSTS_REDSTONE, redstoneEnergy);
        addEnergyConversionRecipe(consumer, basePath, "redstone_block", Tags.Items.STORAGE_BLOCKS_REDSTONE, redstoneEnergy.multiply(9));
    }

    private void addEnergyConversionRecipe(Consumer<IFinishedRecipe> consumer, String basePath, String name, ITag<Item> inputTag, FloatingLong output) {
        ItemStackToEnergyRecipeBuilder.energyConversion(
              ItemStackIngredient.from(inputTag),
              output
        ).build(consumer, Mekanism.rl(basePath + name));
    }
}