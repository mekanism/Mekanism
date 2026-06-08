package mekanism.api.datagen.recipe.builder;

import java.util.Objects;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.datagen.recipe.MekanismRecipeBuilder;
import mekanism.api.recipes.SawmillRecipe;
import mekanism.api.recipes.basic.BasicSawmillRecipe;
import mekanism.api.recipes.ingredients.ItemStackIngredient;
import net.minecraft.data.recipes.RecipeBuilder;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.crafting.Recipe;
import org.jetbrains.annotations.Nullable;

@NothingNullByDefault
public class SawmillRecipeBuilder extends MekanismRecipeBuilder<SawmillRecipeBuilder> {

    private final ItemStackIngredient input;
    @Nullable
    private final ItemStackTemplate mainOutput;
    @Nullable
    private final ItemStackTemplate secondaryOutput;
    private final double secondaryChance;

    protected SawmillRecipeBuilder(ItemStackIngredient input, @Nullable ItemStackTemplate mainOutput, @Nullable ItemStackTemplate secondaryOutput, double secondaryChance) {
        this.input = input;
        this.mainOutput = mainOutput;
        this.secondaryOutput = secondaryOutput;
        this.secondaryChance = secondaryChance;
    }

    @Override
    public ResourceKey<Recipe<?>> defaultId() {
        ItemStackTemplate template = Objects.requireNonNull(mainOutput == null ? secondaryOutput : mainOutput, "Illegal config");
        return RecipeBuilder.getDefaultRecipeId(template);
    }

    /**
     * Creates a Sawing recipe builder.
     *
     * @param input      Input.
     * @param mainOutput Main Output.
     */
    public static SawmillRecipeBuilder sawing(ItemStackIngredient input, ItemStackTemplate mainOutput) {
        Objects.requireNonNull(mainOutput, "This sawing recipe requires a non empty output.");
        return new SawmillRecipeBuilder(input, mainOutput, null, 0);
    }

    /**
     * Creates a Sawing recipe builder.
     *
     * @param input           Input.
     * @param secondaryOutput Secondary Output.
     * @param secondaryChance Chance of the secondary output being produced. This must be a number greater than zero and less than one.
     */
    public static SawmillRecipeBuilder sawing(ItemStackIngredient input, ItemStackTemplate secondaryOutput, double secondaryChance) {
        Objects.requireNonNull(secondaryOutput, "This sawing recipe requires a non empty secondary output.");
        if (secondaryChance <= 0 || secondaryChance > 1) {
            throw new IllegalArgumentException("This sawing recipe requires a secondary output chance greater than zero and at most one.");
        } else if (secondaryChance == 1) {
            throw new IllegalArgumentException("Sawing recipes with a single 100% change output should specify their output as the main output.");
        }
        return new SawmillRecipeBuilder(input, null, secondaryOutput, secondaryChance);
    }

    /**
     * Creates a Sawing recipe builder.
     *
     * @param input           Input.
     * @param mainOutput      Main Output.
     * @param secondaryOutput Secondary Output.
     * @param secondaryChance Chance of the secondary output being produced. This must be a number greater than zero and at most one.
     */
    public static SawmillRecipeBuilder sawing(ItemStackIngredient input, ItemStackTemplate mainOutput, ItemStackTemplate secondaryOutput, double secondaryChance) {
        Objects.requireNonNull(mainOutput, "This sawing recipe requires a non empty output.");
        Objects.requireNonNull(secondaryOutput, "This sawing recipe requires a non empty secondary output.");
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