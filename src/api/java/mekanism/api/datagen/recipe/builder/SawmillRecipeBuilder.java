package mekanism.api.datagen.recipe.builder;

import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.datagen.recipe.MekanismRecipeBuilder;
import mekanism.api.recipes.SawmillRecipe;
import mekanism.api.recipes.basic.BasicSawmillRecipe;
import mekanism.api.recipes.ingredients.ItemStackIngredient;
import net.minecraft.world.item.ItemStack;

@NothingNullByDefault
public class SawmillRecipeBuilder extends MekanismRecipeBuilder<SawmillRecipeBuilder> {

    private final ItemStackIngredient input;
    private final ItemStack mainOutput;
    private final ItemStack secondaryOutput;
    private final double secondaryChance;

    protected SawmillRecipeBuilder(ItemStackIngredient input, ItemStack mainOutput, ItemStack secondaryOutput, double secondaryChance) {
        this.input = input;
        this.mainOutput = mainOutput;
        this.secondaryOutput = secondaryOutput;
        this.secondaryChance = secondaryChance;
    }

    /**
     * Creates a Sawing recipe builder.
     *
     * @param input      Input.
     * @param mainOutput Main Output.
     */
    public static SawmillRecipeBuilder sawing(ItemStackIngredient input, ItemStack mainOutput) {
        if (mainOutput.isEmpty()) {
            throw new IllegalArgumentException("This sawing recipe requires a non empty output.");
        }
        return new SawmillRecipeBuilder(input, mainOutput, ItemStack.EMPTY, 0);
    }

    /**
     * Creates a Sawing recipe builder.
     *
     * @param input           Input.
     * @param secondaryOutput Secondary Output.
     * @param secondaryChance Chance of the secondary output being produced. This must be a number greater than zero and less than one.
     */
    public static SawmillRecipeBuilder sawing(ItemStackIngredient input, ItemStack secondaryOutput, double secondaryChance) {
        if (secondaryOutput.isEmpty()) {
            throw new IllegalArgumentException("This sawing recipe requires a non empty secondary output.");
        }
        if (secondaryChance <= 0 || secondaryChance > 1) {
            throw new IllegalArgumentException("This sawing recipe requires a secondary output chance greater than zero and at most one.");
        } else if (secondaryChance == 1) {
            throw new IllegalArgumentException("Sawing recipes with a single 100% change output should specify their output as the main output.");
        }
        return new SawmillRecipeBuilder(input, ItemStack.EMPTY, secondaryOutput, secondaryChance);
    }

    /**
     * Creates a Sawing recipe builder.
     *
     * @param input           Input.
     * @param mainOutput      Main Output.
     * @param secondaryOutput Secondary Output.
     * @param secondaryChance Chance of the secondary output being produced. This must be a number greater than zero and at most one.
     */
    public static SawmillRecipeBuilder sawing(ItemStackIngredient input, ItemStack mainOutput, ItemStack secondaryOutput, double secondaryChance) {
        if (mainOutput.isEmpty() || secondaryOutput.isEmpty()) {
            throw new IllegalArgumentException("This sawing recipe requires a non empty primary, and secondary output.");
        }
        if (secondaryChance <= 0 || secondaryChance > 1) {
            throw new IllegalArgumentException("This sawing recipe requires a secondary output chance greater than zero and at most one.");
        }
        return new SawmillRecipeBuilder(input, mainOutput, secondaryOutput, secondaryChance);
    }

    @Override
    protected SawmillRecipe asRecipe() {
        return new BasicSawmillRecipe(input, mainOutput, secondaryOutput, secondaryChance);
    }
}