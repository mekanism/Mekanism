package mekanism.api.datagen.recipe.builder;

import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.datagen.recipe.MekanismRecipeBuilder;
import mekanism.api.recipes.ItemStackToEnergyRecipe;
import mekanism.api.recipes.basic.BasicItemStackToEnergyRecipe;
import mekanism.api.recipes.ingredients.ItemStackIngredient;

@NothingNullByDefault
public class ItemStackToEnergyRecipeBuilder extends MekanismRecipeBuilder<ItemStackToEnergyRecipeBuilder> {

    private final ItemStackIngredient input;
    private final long output;

    protected ItemStackToEnergyRecipeBuilder(ItemStackIngredient input, long output) {
        this.input = input;
        this.output = output;
    }

    /**
     * Creates an Energy Conversion recipe builder.
     *
     * @param input  Input.
     * @param output Output.
     */
    public static ItemStackToEnergyRecipeBuilder energyConversion(ItemStackIngredient input, long output) {
        if (output <= 0L) {
            throw new IllegalArgumentException("This energy conversion recipe requires an energy output greater than zero");
        }
        return new ItemStackToEnergyRecipeBuilder(input, output);
    }

    @Override
    protected ItemStackToEnergyRecipe asRecipe() {
        return new BasicItemStackToEnergyRecipe(input, output);
    }
}