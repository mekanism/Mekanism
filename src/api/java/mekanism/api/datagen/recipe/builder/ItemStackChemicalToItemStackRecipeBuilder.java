package mekanism.api.datagen.recipe.builder;

import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.datagen.recipe.MekanismRecipeBuilder;
import mekanism.api.recipes.basic.BasicCompressingRecipe;
import mekanism.api.recipes.basic.BasicInjectingRecipe;
import mekanism.api.recipes.basic.BasicMetallurgicInfuserRecipe;
import mekanism.api.recipes.basic.BasicPaintingRecipe;
import mekanism.api.recipes.basic.BasicPurifyingRecipe;
import mekanism.api.recipes.ItemStackChemicalToItemStackRecipe;
import mekanism.api.recipes.ingredients.ChemicalStackIngredient;
import mekanism.api.recipes.ingredients.ItemStackIngredient;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.world.item.ItemStack;

@NothingNullByDefault
public class ItemStackChemicalToItemStackRecipeBuilder extends MekanismRecipeBuilder<ItemStackChemicalToItemStackRecipeBuilder> {

    private final ItemStackChemicalToItemStackRecipeBuilder.Factory factory;
    private final ItemStackIngredient itemInput;
    private final ChemicalStackIngredient chemicalInput;
    private final ItemStack output;

    protected ItemStackChemicalToItemStackRecipeBuilder(ItemStackIngredient itemInput, ChemicalStackIngredient chemicalInput, ItemStack output,
          ItemStackChemicalToItemStackRecipeBuilder.Factory factory) {
        this.itemInput = itemInput;
        this.chemicalInput = chemicalInput;
        this.output = output;
        this.factory = factory;
    }

    /**
     * Creates a Compressing recipe builder.
     *
     * @param itemInput     Item Input.
     * @param chemicalInput Chemical Input, used at a constant rate over the duration of the recipe.
     * @param output        Output.
     */
    public static ItemStackChemicalToItemStackRecipeBuilder compressing(ItemStackIngredient itemInput, ChemicalStackIngredient chemicalInput,
          ItemStack output) {
        if (output.isEmpty()) {
            throw new IllegalArgumentException("This compressing recipe requires a non empty item output.");
        }
        return new ItemStackChemicalToItemStackRecipeBuilder(itemInput, chemicalInput, output, BasicCompressingRecipe::new);
    }

    /**
     * Creates a Purifying recipe builder.
     *
     * @param itemInput     Item Input.
     * @param chemicalInput Chemical Input, used at a near constant rate over the duration of the recipe.
     * @param output        Output.
     */
    public static ItemStackChemicalToItemStackRecipeBuilder purifying(ItemStackIngredient itemInput, ChemicalStackIngredient chemicalInput,
          ItemStack output) {
        if (output.isEmpty()) {
            throw new IllegalArgumentException("This purifying recipe requires a non empty item output.");
        }
        return new ItemStackChemicalToItemStackRecipeBuilder(itemInput, chemicalInput, output, BasicPurifyingRecipe::new);
    }

    /**
     * Creates an Injecting recipe builder.
     *
     * @param itemInput     Item Input.
     * @param chemicalInput Chemical Input, used at a near constant rate over the duration of the recipe.
     * @param output        Output.
     */
    public static ItemStackChemicalToItemStackRecipeBuilder injecting(ItemStackIngredient itemInput, ChemicalStackIngredient chemicalInput,
          ItemStack output) {
        if (output.isEmpty()) {
            throw new IllegalArgumentException("This injecting recipe requires a non empty item output.");
        }
        return new ItemStackChemicalToItemStackRecipeBuilder(itemInput, chemicalInput, output, BasicInjectingRecipe::new);
    }

    /**
     * Creates a Metallurgic Infusing recipe builder.
     *
     * @param itemInput     Item Input.
     * @param chemicalInput Infusion Input.
     * @param output        Output.
     */
    public static ItemStackChemicalToItemStackRecipeBuilder metallurgicInfusing(ItemStackIngredient itemInput,
          ChemicalStackIngredient chemicalInput, ItemStack output) {
        if (output.isEmpty()) {
            throw new IllegalArgumentException("This metallurgic infusing recipe requires a non empty output.");
        }
        return new ItemStackChemicalToItemStackRecipeBuilder(itemInput, chemicalInput, output, BasicMetallurgicInfuserRecipe::new);
    }

    /**
     * Creates a Painting recipe builder.
     *
     * @param itemInput     Item Input.
     * @param chemicalInput Chemical Input.
     * @param output        Output.
     */
    public static ItemStackChemicalToItemStackRecipeBuilder painting(ItemStackIngredient itemInput, ChemicalStackIngredient chemicalInput, ItemStack output) {
        if (output.isEmpty()) {
            throw new IllegalArgumentException("This painting recipe requires a non empty item output.");
        }
        return new ItemStackChemicalToItemStackRecipeBuilder(itemInput, chemicalInput, output, BasicPaintingRecipe::new);
    }

    @Override
    protected ItemStackChemicalToItemStackRecipe asRecipe() {
        return factory.create(itemInput, chemicalInput, output);
    }

    /**
     * Builds this recipe using the output item's name as the recipe name.
     *
     * @param recipeOutput Finished Recipe Consumer.
     */
    public void build(RecipeOutput recipeOutput) {
        build(recipeOutput, output.getItem());
    }

    @FunctionalInterface
    public interface Factory {

        ItemStackChemicalToItemStackRecipe create(ItemStackIngredient itemInput, ChemicalStackIngredient chemicalInput, ItemStack output);
    }
}