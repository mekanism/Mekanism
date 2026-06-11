package mekanism.api.datagen.recipe.builder;

import mekanism.api.datagen.recipe.MekanismRecipeBuilder;
import mekanism.api.recipes.ItemStackChemicalToItemStackRecipe;
import mekanism.api.recipes.basic.BasicCompressingRecipe;
import mekanism.api.recipes.basic.BasicInjectingRecipe;
import mekanism.api.recipes.basic.BasicMetallurgicInfuserRecipe;
import mekanism.api.recipes.basic.BasicPaintingRecipe;
import mekanism.api.recipes.basic.BasicPurifyingRecipe;
import mekanism.api.recipes.ingredients.ChemicalStackIngredient;
import mekanism.api.recipes.ingredients.ItemStackIngredient;
import net.minecraft.data.recipes.RecipeBuilder;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.crafting.Recipe;

public class ItemStackChemicalToItemStackRecipeBuilder extends MekanismRecipeBuilder<ItemStackChemicalToItemStackRecipeBuilder> {

    private final ItemStackChemicalToItemStackRecipeBuilder.Factory factory;
    private final ItemStackIngredient itemInput;
    private final ChemicalStackIngredient chemicalInput;
    private final ItemStackTemplate output;
    private final boolean perTickUsage;

    protected ItemStackChemicalToItemStackRecipeBuilder(ItemStackIngredient itemInput, ChemicalStackIngredient chemicalInput, ItemStackTemplate output, boolean perTickUsage,
          ItemStackChemicalToItemStackRecipeBuilder.Factory factory) {
        this.itemInput = itemInput;
        this.chemicalInput = chemicalInput;
        this.output = output;
        this.perTickUsage = perTickUsage;
        this.factory = factory;
    }

    @Override
    public ResourceKey<Recipe<?>> defaultId() {
        return RecipeBuilder.getDefaultRecipeId(output);
    }

    /**
     * Creates a Compressing recipe builder.
     *
     * @param itemInput     Item Input.
     * @param chemicalInput Chemical Input, used at a constant rate over the duration of the recipe.
     * @param output        Output.
     * @param perTickUsage  Should the recipe consume the chemical input each tick it is processing.
     */
    public static ItemStackChemicalToItemStackRecipeBuilder compressing(ItemStackIngredient itemInput, ChemicalStackIngredient chemicalInput, ItemStackTemplate output,
          boolean perTickUsage) {
        return new ItemStackChemicalToItemStackRecipeBuilder(itemInput, chemicalInput, output, perTickUsage, BasicCompressingRecipe::new);
    }

    /**
     * Creates a Purifying recipe builder.
     *
     * @param itemInput     Item Input.
     * @param chemicalInput Chemical Input, used at a near constant rate over the duration of the recipe.
     * @param output        Output.
     * @param perTickUsage  Should the recipe consume the chemical input each tick it is processing.
     */
    public static ItemStackChemicalToItemStackRecipeBuilder purifying(ItemStackIngredient itemInput, ChemicalStackIngredient chemicalInput, ItemStackTemplate output,
          boolean perTickUsage) {
        return new ItemStackChemicalToItemStackRecipeBuilder(itemInput, chemicalInput, output, perTickUsage, BasicPurifyingRecipe::new);
    }

    /**
     * Creates an Injecting recipe builder.
     *
     * @param itemInput     Item Input.
     * @param chemicalInput Chemical Input, used at a near constant rate over the duration of the recipe.
     * @param output        Output.
     * @param perTickUsage  Should the recipe consume the chemical input each tick it is processing.
     */
    public static ItemStackChemicalToItemStackRecipeBuilder injecting(ItemStackIngredient itemInput, ChemicalStackIngredient chemicalInput, ItemStackTemplate output,
          boolean perTickUsage) {
        return new ItemStackChemicalToItemStackRecipeBuilder(itemInput, chemicalInput, output, perTickUsage, BasicInjectingRecipe::new);
    }

    /**
     * Creates a Metallurgic Infusing recipe builder.
     *
     * @param itemInput     Item Input.
     * @param chemicalInput Infusion Input.
     * @param output        Output.
     * @param perTickUsage  Should the recipe consume the chemical input each tick it is processing.
     */
    public static ItemStackChemicalToItemStackRecipeBuilder metallurgicInfusing(ItemStackIngredient itemInput, ChemicalStackIngredient chemicalInput, ItemStackTemplate output,
          boolean perTickUsage) {
        return new ItemStackChemicalToItemStackRecipeBuilder(itemInput, chemicalInput, output, perTickUsage, BasicMetallurgicInfuserRecipe::new);
    }

    /**
     * Creates a Painting recipe builder.
     *
     * @param itemInput     Item Input.
     * @param chemicalInput Chemical Input.
     * @param output        Output.
     * @param perTickUsage  Should the recipe consume the chemical input each tick it is processing.
     */
    public static ItemStackChemicalToItemStackRecipeBuilder painting(ItemStackIngredient itemInput, ChemicalStackIngredient chemicalInput, ItemStackTemplate output,
          boolean perTickUsage) {
        return new ItemStackChemicalToItemStackRecipeBuilder(itemInput, chemicalInput, output, perTickUsage, BasicPaintingRecipe::new);
    }

    @Override
    protected ItemStackChemicalToItemStackRecipe asRecipe() {
        return factory.create(itemInput, chemicalInput, output, perTickUsage);
    }

    @FunctionalInterface
    public interface Factory {

        ItemStackChemicalToItemStackRecipe create(ItemStackIngredient itemInput, ChemicalStackIngredient chemicalInput, ItemStackTemplate output, boolean perTickUsage);
    }
}