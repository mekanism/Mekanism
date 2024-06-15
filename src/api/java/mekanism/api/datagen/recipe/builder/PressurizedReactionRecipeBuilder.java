package mekanism.api.datagen.recipe.builder;

import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.chemical.gas.GasStack;
import mekanism.api.datagen.recipe.MekanismRecipeBuilder;
import mekanism.api.math.FloatingLong;
import mekanism.api.recipes.PressurizedReactionRecipe;
import mekanism.api.recipes.basic.BasicPressurizedReactionRecipe;
import mekanism.api.recipes.ingredients.GasStackIngredient;
import mekanism.api.recipes.ingredients.FluidStackIngredient;
import mekanism.api.recipes.ingredients.ItemStackIngredient;
import net.minecraft.world.item.ItemStack;

@NothingNullByDefault
public class PressurizedReactionRecipeBuilder extends MekanismRecipeBuilder<PressurizedReactionRecipeBuilder> {

    private final ItemStackIngredient inputSolid;
    private final FluidStackIngredient inputFluid;
    private final GasStackIngredient inputGas;
    private long energyRequired = 0;
    private final int duration;
    private final ItemStack outputItem;
    private final GasStack outputGas;

    protected PressurizedReactionRecipeBuilder(ItemStackIngredient inputSolid, FluidStackIngredient inputFluid, GasStackIngredient inputGas, int duration,
          ItemStack outputItem, GasStack outputGas) {
        this.inputSolid = inputSolid;
        this.inputFluid = inputFluid;
        this.inputGas = inputGas;
        this.duration = duration;
        this.outputItem = outputItem;
        this.outputGas = outputGas;
    }

    /**
     * Creates a Pressurized Reaction recipe builder.
     *
     * @param inputSolid Item Input.
     * @param inputFluid Fluid Input.
     * @param inputGas   Gas Input.
     * @param duration   Base duration in ticks that this recipe takes to complete. Must be greater than zero.
     * @param outputItem Item Output.
     */
    public static PressurizedReactionRecipeBuilder reaction(ItemStackIngredient inputSolid, FluidStackIngredient inputFluid, GasStackIngredient inputGas,
          int duration, ItemStack outputItem) {
        if (outputItem.isEmpty()) {
            throw new IllegalArgumentException("This reaction recipe requires a non empty output item.");
        }
        validateDuration(duration);
        return new PressurizedReactionRecipeBuilder(inputSolid, inputFluid, inputGas, duration, outputItem, GasStack.EMPTY);
    }

    /**
     * Creates a Pressurized Reaction recipe builder.
     *
     * @param inputSolid Item Input.
     * @param inputFluid Fluid Input.
     * @param inputGas   Gas Input.
     * @param duration   Base duration in ticks that this recipe takes to complete. Must be greater than zero.
     * @param outputGas  Gas Output.
     */
    public static PressurizedReactionRecipeBuilder reaction(ItemStackIngredient inputSolid, FluidStackIngredient inputFluid, GasStackIngredient inputGas, int duration,
          GasStack outputGas) {
        if (outputGas.isEmpty()) {
            throw new IllegalArgumentException("This reaction recipe requires a non empty output gas.");
        }
        validateDuration(duration);
        return new PressurizedReactionRecipeBuilder(inputSolid, inputFluid, inputGas, duration, ItemStack.EMPTY, outputGas);
    }

    /**
     * Creates a Pressurized Reaction recipe builder.
     *
     * @param inputSolid Item Input.
     * @param inputFluid Fluid Input.
     * @param inputGas   Gas Input.
     * @param duration   Base duration in ticks that this recipe takes to complete. Must be greater than zero.
     * @param outputItem Item Output.
     * @param outputGas  Gas Output.
     */
    public static PressurizedReactionRecipeBuilder reaction(ItemStackIngredient inputSolid, FluidStackIngredient inputFluid, GasStackIngredient inputGas, int duration,
          ItemStack outputItem, GasStack outputGas) {
        if (outputItem.isEmpty() || outputGas.isEmpty()) {
            throw new IllegalArgumentException("This reaction recipe requires non empty item and gas outputs.");
        }
        validateDuration(duration);
        return new PressurizedReactionRecipeBuilder(inputSolid, inputFluid, inputGas, duration, outputItem, outputGas);
    }

    private static void validateDuration(int duration) {
        if (duration <= 0) {
            throw new IllegalArgumentException("This reaction recipe must have a positive duration.");
        }
    }

    /**
     * Sets the "extra" energy required for this recipe.
     *
     * @param energyRequired How much "extra" energy this recipe requires, compared to the base energy requirements of the machine performing the recipe.
     */
    public PressurizedReactionRecipeBuilder energyRequired(long energyRequired) {
        this.energyRequired = energyRequired;
        return this;
    }

    @Override
    protected PressurizedReactionRecipe asRecipe() {
        return new BasicPressurizedReactionRecipe(inputSolid, inputFluid, inputGas, energyRequired, duration, outputItem, outputGas);
    }
}