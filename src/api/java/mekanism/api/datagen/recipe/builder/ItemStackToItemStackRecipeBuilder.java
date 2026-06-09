package mekanism.api.datagen.recipe.builder;

import mekanism.api.datagen.recipe.MekanismRecipeBuilder;
import mekanism.api.recipes.ItemStackToItemStackRecipe;
import mekanism.api.recipes.basic.BasicCrushingRecipe;
import mekanism.api.recipes.basic.BasicEnrichingRecipe;
import mekanism.api.recipes.basic.BasicSmeltingRecipe;
import mekanism.api.recipes.ingredients.ItemStackIngredient;
import net.minecraft.data.recipes.RecipeBuilder;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.crafting.Recipe;

public class ItemStackToItemStackRecipeBuilder extends MekanismRecipeBuilder<ItemStackToItemStackRecipeBuilder> {

    private final ItemStackToItemStackRecipeBuilder.Factory factory;
    private final ItemStackIngredient input;
    private final ItemStackTemplate output;

    protected ItemStackToItemStackRecipeBuilder(ItemStackIngredient input, ItemStackTemplate output, ItemStackToItemStackRecipeBuilder.Factory factory) {
        this.input = input;
        this.output = output;
        this.factory = factory;
    }

    @Override
    public ResourceKey<Recipe<?>> defaultId() {
        return RecipeBuilder.getDefaultRecipeId(output);
    }

    /**
     * Creates a Crushing recipe builder.
     *
     * @param input  Input.
     * @param output Output.
     */
    public static ItemStackToItemStackRecipeBuilder crushing(ItemStackIngredient input, ItemStackTemplate output) {
        return new ItemStackToItemStackRecipeBuilder(input, output, BasicCrushingRecipe::new);
    }

    /**
     * Creates an Enriching recipe builder.
     *
     * @param input  Input.
     * @param output Output.
     */
    public static ItemStackToItemStackRecipeBuilder enriching(ItemStackIngredient input, ItemStackTemplate output) {
        return new ItemStackToItemStackRecipeBuilder(input, output, BasicEnrichingRecipe::new);
    }

    /**
     * Creates a Smelting recipe builder.
     *
     * @param input  Input.
     * @param output Output.
     */
    public static ItemStackToItemStackRecipeBuilder smelting(ItemStackIngredient input, ItemStackTemplate output) {
        return new ItemStackToItemStackRecipeBuilder(input, output, BasicSmeltingRecipe::new);
    }

    @Override
    protected ItemStackToItemStackRecipe asRecipe() {
        return factory.create(input, output);
    }

    /**
     * Builds this recipe using the output item's name as the recipe name.
     *
     * @param recipeOutput Finished Recipe Consumer.
     */
    public void build(RecipeOutput recipeOutput) {
        save(recipeOutput, output.typeHolder());
    }

    @FunctionalInterface
    public interface Factory {

        ItemStackToItemStackRecipe create(ItemStackIngredient input, ItemStackTemplate output);
    }
}