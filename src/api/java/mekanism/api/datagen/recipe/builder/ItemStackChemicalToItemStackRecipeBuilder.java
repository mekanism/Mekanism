package mekanism.api.datagen.recipe.builder;

import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.chemical.Chemical;
import mekanism.api.chemical.ChemicalStack;
import mekanism.api.chemical.gas.Gas;
import mekanism.api.chemical.gas.GasStack;
import mekanism.api.chemical.infuse.InfuseType;
import mekanism.api.chemical.infuse.InfusionStack;
import mekanism.api.chemical.pigment.Pigment;
import mekanism.api.chemical.pigment.PigmentStack;
import mekanism.api.datagen.recipe.MekanismRecipeBuilder;
import mekanism.api.recipes.basic.BasicCompressingRecipe;
import mekanism.api.recipes.basic.BasicInjectingRecipe;
import mekanism.api.recipes.basic.BasicMetallurgicInfuserRecipe;
import mekanism.api.recipes.basic.BasicPaintingRecipe;
import mekanism.api.recipes.basic.BasicPurifyingRecipe;
import mekanism.api.recipes.chemical.ItemStackChemicalToItemStackRecipe;
import mekanism.api.recipes.ingredients.ChemicalStackIngredient;
import mekanism.api.recipes.ingredients.GasStackIngredient;
import mekanism.api.recipes.ingredients.InfusionStackIngredient;
import mekanism.api.recipes.ingredients.PigmentStackIngredient;
import mekanism.api.recipes.ingredients.ItemStackIngredient;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.world.item.ItemStack;

@NothingNullByDefault
public class ItemStackChemicalToItemStackRecipeBuilder<CHEMICAL extends Chemical<CHEMICAL>, STACK extends ChemicalStack<CHEMICAL>,
      INGREDIENT extends ChemicalStackIngredient<CHEMICAL, STACK, ?>> extends MekanismRecipeBuilder<ItemStackChemicalToItemStackRecipeBuilder<CHEMICAL, STACK, INGREDIENT>> {

    private final ItemStackChemicalToItemStackRecipeBuilder.Factory<CHEMICAL, STACK, INGREDIENT> factory;
    private final ItemStackIngredient itemInput;
    private final INGREDIENT chemicalInput;
    private final ItemStack output;

    protected ItemStackChemicalToItemStackRecipeBuilder(ItemStackIngredient itemInput, INGREDIENT chemicalInput, ItemStack output,
          ItemStackChemicalToItemStackRecipeBuilder.Factory<CHEMICAL, STACK, INGREDIENT> factory) {
        this.itemInput = itemInput;
        this.chemicalInput = chemicalInput;
        this.output = output;
        this.factory = factory;
    }

    /**
     * Creates a Compressing recipe builder.
     *
     * @param itemInput Item Input.
     * @param gasInput  Gas Input, used at a constant rate over the duration of the recipe.
     * @param output    Output.
     */
    public static ItemStackChemicalToItemStackRecipeBuilder<Gas, GasStack, GasStackIngredient> compressing(ItemStackIngredient itemInput, GasStackIngredient gasInput,
          ItemStack output) {
        if (output.isEmpty()) {
            throw new IllegalArgumentException("This compressing recipe requires a non empty item output.");
        }
        return new ItemStackChemicalToItemStackRecipeBuilder<>(itemInput, gasInput, output, BasicCompressingRecipe::new);
    }

    /**
     * Creates a Purifying recipe builder.
     *
     * @param itemInput Item Input.
     * @param gasInput  Gas Input, used at a near constant rate over the duration of the recipe.
     * @param output    Output.
     */
    public static ItemStackChemicalToItemStackRecipeBuilder<Gas, GasStack, GasStackIngredient> purifying(ItemStackIngredient itemInput, GasStackIngredient gasInput,
          ItemStack output) {
        if (output.isEmpty()) {
            throw new IllegalArgumentException("This purifying recipe requires a non empty item output.");
        }
        return new ItemStackChemicalToItemStackRecipeBuilder<>(itemInput, gasInput, output, BasicPurifyingRecipe::new);
    }

    /**
     * Creates an Injecting recipe builder.
     *
     * @param itemInput Item Input.
     * @param gasInput  Gas Input, used at a near constant rate over the duration of the recipe.
     * @param output    Output.
     */
    public static ItemStackChemicalToItemStackRecipeBuilder<Gas, GasStack, GasStackIngredient> injecting(ItemStackIngredient itemInput, GasStackIngredient gasInput,
          ItemStack output) {
        if (output.isEmpty()) {
            throw new IllegalArgumentException("This injecting recipe requires a non empty item output.");
        }
        return new ItemStackChemicalToItemStackRecipeBuilder<>(itemInput, gasInput, output, BasicInjectingRecipe::new);
    }

    /**
     * Creates a Metallurgic Infusing recipe builder.
     *
     * @param itemInput     Item Input.
     * @param infusionInput Infusion Input.
     * @param output        Output.
     */
    public static ItemStackChemicalToItemStackRecipeBuilder<InfuseType, InfusionStack, InfusionStackIngredient> metallurgicInfusing(ItemStackIngredient itemInput,
          InfusionStackIngredient infusionInput, ItemStack output) {
        if (output.isEmpty()) {
            throw new IllegalArgumentException("This metallurgic infusing recipe requires a non empty output.");
        }
        return new ItemStackChemicalToItemStackRecipeBuilder<>(itemInput, infusionInput, output, BasicMetallurgicInfuserRecipe::new);
    }

    /**
     * Creates a Painting recipe builder.
     *
     * @param itemInput    Item Input.
     * @param pigmentInput Pigment Input.
     * @param output       Output.
     */
    public static ItemStackChemicalToItemStackRecipeBuilder<Pigment, PigmentStack, PigmentStackIngredient> painting(ItemStackIngredient itemInput,
          PigmentStackIngredient pigmentInput, ItemStack output) {
        if (output.isEmpty()) {
            throw new IllegalArgumentException("This painting recipe requires a non empty item output.");
        }
        return new ItemStackChemicalToItemStackRecipeBuilder<>(itemInput, pigmentInput, output, BasicPaintingRecipe::new);
    }

    @Override
    protected ItemStackChemicalToItemStackRecipe<CHEMICAL, STACK, INGREDIENT> asRecipe() {
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
    public interface Factory<CHEMICAL extends Chemical<CHEMICAL>, STACK extends ChemicalStack<CHEMICAL>, INGREDIENT extends ChemicalStackIngredient<CHEMICAL, STACK, ?>> {

        ItemStackChemicalToItemStackRecipe<CHEMICAL, STACK, INGREDIENT> create(ItemStackIngredient itemInput, INGREDIENT chemicalInput, ItemStack output);
    }
}