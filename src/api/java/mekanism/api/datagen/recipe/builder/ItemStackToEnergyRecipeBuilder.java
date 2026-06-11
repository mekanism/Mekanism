package mekanism.api.datagen.recipe.builder;

import mekanism.api.datagen.recipe.MekanismRecipeBuilder;
import mekanism.api.recipes.ItemStackToEnergyRecipe;
import mekanism.api.recipes.basic.BasicItemStackToEnergyRecipe;
import mekanism.api.recipes.ingredients.ItemStackIngredient;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.crafting.Recipe;

public class ItemStackToEnergyRecipeBuilder extends MekanismRecipeBuilder<ItemStackToEnergyRecipeBuilder> {

    private final ItemStackIngredient input;
    private final int output;

    protected ItemStackToEnergyRecipeBuilder(ItemStackIngredient input, int output) {
        this.input = input;
        this.output = output;
    }

    @Override
    public ResourceKey<Recipe<?>> defaultId() {
        return NO_DEFAULT_ID;
    }

    /// Creates an Energy Conversion recipe builder.
    ///
    /// @param input  Input.
    /// @param output Output.
    public static ItemStackToEnergyRecipeBuilder energyConversion(ItemStackIngredient input, int output) {
        if (output <= 0) {
            throw new IllegalArgumentException("This energy conversion recipe requires an energy output greater than zero");
        }
        return new ItemStackToEnergyRecipeBuilder(input, output);
    }

    @Override
    protected ItemStackToEnergyRecipe asRecipe() {
        return new BasicItemStackToEnergyRecipe(input, output);
    }
}