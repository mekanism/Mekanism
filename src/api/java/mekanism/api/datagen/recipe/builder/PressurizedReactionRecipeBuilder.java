package mekanism.api.datagen.recipe.builder;

import java.util.Objects;
import mekanism.api.chemical.ChemicalStackTemplate;
import mekanism.api.datagen.recipe.MekanismRecipeBuilder;
import mekanism.api.recipes.PressurizedReactionRecipe;
import mekanism.api.recipes.basic.BasicPressurizedReactionRecipe;
import mekanism.api.recipes.ingredients.ChemicalStackIngredient;
import mekanism.api.recipes.ingredients.FluidStackIngredient;
import mekanism.api.recipes.ingredients.ItemStackIngredient;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.crafting.Recipe;
import org.jspecify.annotations.Nullable;

public class PressurizedReactionRecipeBuilder extends MekanismRecipeBuilder<PressurizedReactionRecipeBuilder> {

    private final ItemStackIngredient inputSolid;
    private final FluidStackIngredient inputFluid;
    private final ChemicalStackIngredient inputChemical;
    private int energyRequired = 0;
    private final int duration;
    @Nullable
    private final ItemStackTemplate outputItem;
    @Nullable
    private final ChemicalStackTemplate outputChemical;

    protected PressurizedReactionRecipeBuilder(ItemStackIngredient inputSolid, FluidStackIngredient inputFluid, ChemicalStackIngredient inputChemical, int duration,
          @Nullable ItemStackTemplate outputItem, @Nullable ChemicalStackTemplate outputChemical) {
        this.inputSolid = inputSolid;
        this.inputFluid = inputFluid;
        this.inputChemical = inputChemical;
        this.duration = duration;
        this.outputItem = outputItem;
        this.outputChemical = outputChemical;
    }

    @Override
    public ResourceKey<Recipe<?>> defaultId() {
        return NO_DEFAULT_ID;
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
          int duration, ItemStackTemplate outputItem) {
        Objects.requireNonNull(outputItem, "This reaction recipe requires a non empty output item.");
        validateDuration(duration);
        return new PressurizedReactionRecipeBuilder(inputSolid, inputFluid, inputChemical, duration, outputItem, null);
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
          int duration, ChemicalStackTemplate outputChemical) {
        Objects.requireNonNull(outputChemical, "This reaction recipe requires a non empty output chemical.");
        validateDuration(duration);
        return new PressurizedReactionRecipeBuilder(inputSolid, inputFluid, inputChemical, duration, null, outputChemical);
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
          int duration, ItemStackTemplate outputItem, ChemicalStackTemplate outputChemical) {
        Objects.requireNonNull(outputItem, "This reaction recipe requires non empty item and chemical outputs.");
        Objects.requireNonNull(outputChemical, "This reaction recipe requires non empty item and chemical outputs.");
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
    public PressurizedReactionRecipeBuilder energyRequired(int energyRequired) {
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