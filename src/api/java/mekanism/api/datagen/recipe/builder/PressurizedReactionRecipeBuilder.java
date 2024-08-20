package mekanism.api.datagen.recipe.builder;

import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.chemical.ChemicalStack;
import mekanism.api.datagen.recipe.MekanismRecipeBuilder;
import mekanism.api.recipes.PressurizedReactionRecipe;
import mekanism.api.recipes.basic.BasicPressurizedReactionRecipe;
import mekanism.api.recipes.ingredients.ChemicalStackIngredient;
import mekanism.api.recipes.ingredients.FluidStackIngredient;
import mekanism.api.recipes.ingredients.ItemStackIngredient;
import net.minecraft.world.item.ItemStack;

@NothingNullByDefault
public class PressurizedReactionRecipeBuilder extends MekanismRecipeBuilder<PressurizedReactionRecipeBuilder> {

    private final ItemStackIngredient inputSolid;
    private final FluidStackIngredient inputFluid;
    private final ChemicalStackIngredient inputChemical;
    private long energyRequired = 0;
    private final int duration;
    private final ItemStack outputItem;
    private final ChemicalStack outputChemical;

    protected PressurizedReactionRecipeBuilder(ItemStackIngredient inputSolid, FluidStackIngredient inputFluid, ChemicalStackIngredient inputChemical, int duration,
          ItemStack outputItem, ChemicalStack outputChemical) {
        this.inputSolid = inputSolid;
        this.inputFluid = inputFluid;
        this.inputChemical = inputChemical;
        this.duration = duration;
        this.outputItem = outputItem;
        this.outputChemical = outputChemical;
    }

    /**
     * Creates a Pressurized Reaction recipe builder.
     *
     * @param inputSolid    Item Input.
     * @param inputFluid    Fluid Input.
     * @param inputChemical Chemical Input.
     * @param duration      Base duration in ticks that this recipe takes to complete. Must be greater than zero.
     * @param outputItem    Item Output.
     */
    public static PressurizedReactionRecipeBuilder reaction(ItemStackIngredient inputSolid, FluidStackIngredient inputFluid, ChemicalStackIngredient inputChemical,
          int duration, ItemStack outputItem) {
        if (outputItem.isEmpty()) {
            throw new IllegalArgumentException("This reaction recipe requires a non empty output item.");
        }
        validateDuration(duration);
        return new PressurizedReactionRecipeBuilder(inputSolid, inputFluid, inputChemical, duration, outputItem, ChemicalStack.EMPTY);
    }

    /**
     * Creates a Pressurized Reaction recipe builder.
     *
     * @param inputSolid     Item Input.
     * @param inputFluid     Fluid Input.
     * @param inputChemical  Chemical Input.
     * @param duration       Base duration in ticks that this recipe takes to complete. Must be greater than zero.
     * @param outputChemical Chemical Output.
     */
    public static PressurizedReactionRecipeBuilder reaction(ItemStackIngredient inputSolid, FluidStackIngredient inputFluid, ChemicalStackIngredient inputChemical,
          int duration, ChemicalStack outputChemical) {
        if (outputChemical.isEmpty()) {
            throw new IllegalArgumentException("This reaction recipe requires a non empty output chemical.");
        }
        validateDuration(duration);
        return new PressurizedReactionRecipeBuilder(inputSolid, inputFluid, inputChemical, duration, ItemStack.EMPTY, outputChemical);
    }

    /**
     * Creates a Pressurized Reaction recipe builder.
     *
     * @param inputSolid     Item Input.
     * @param inputFluid     Fluid Input.
     * @param inputChemical  Chemical Input.
     * @param duration       Base duration in ticks that this recipe takes to complete. Must be greater than zero.
     * @param outputItem     Item Output.
     * @param outputChemical Chemical Output.
     */
    public static PressurizedReactionRecipeBuilder reaction(ItemStackIngredient inputSolid, FluidStackIngredient inputFluid, ChemicalStackIngredient inputChemical,
          int duration, ItemStack outputItem, ChemicalStack outputChemical) {
        if (outputItem.isEmpty() || outputChemical.isEmpty()) {
            throw new IllegalArgumentException("This reaction recipe requires non empty item and chemical outputs.");
        }
        validateDuration(duration);
        return new PressurizedReactionRecipeBuilder(inputSolid, inputFluid, inputChemical, duration, outputItem, outputChemical);
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
        if (energyRequired < 0) {
            throw new IllegalArgumentException("This reaction recipe must have a positive or zero energy requirement.");
        }
        this.energyRequired = energyRequired;
        return this;
    }

    @Override
    protected PressurizedReactionRecipe asRecipe() {
        return new BasicPressurizedReactionRecipe(inputSolid, inputFluid, inputChemical, energyRequired, duration, outputItem, outputChemical);
    }
}