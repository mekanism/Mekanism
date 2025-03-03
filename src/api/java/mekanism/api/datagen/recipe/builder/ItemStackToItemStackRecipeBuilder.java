package mekanism.api.datagen.recipe.builder;

import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.datagen.recipe.MekanismRecipeBuilder;
import mekanism.api.recipes.ItemStackToItemStackRecipe;
import mekanism.api.recipes.basic.BasicCrushingRecipe;
import mekanism.api.recipes.basic.BasicEnrichingRecipe;
import mekanism.api.recipes.basic.BasicSmeltingRecipe;
import mekanism.api.recipes.ingredients.ItemStackIngredient;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.world.item.ItemStack;

@NothingNullByDefault
public class ItemStackToItemStackRecipeBuilder extends MekanismRecipeBuilder<ItemStackToItemStackRecipeBuilder> {

    private final ItemStackToItemStackRecipeBuilder.Factory factory;
    private final ItemStackIngredient input;
    private final ItemStack output;

    protected ItemStackToItemStackRecipeBuilder(ItemStackIngredient input, ItemStack output, ItemStackToItemStackRecipeBuilder.Factory factory) {
        this.input = input;
        this.output = output;
        this.factory = factory;
    }

    /**
     * Creates a Crushing recipe builder.
     *
     * @param input  Input.
     * @param output Output.
     */
    public static ItemStackToItemStackRecipeBuilder crushing(ItemStackIngredient input, ItemStack output) {
        if (output.isEmpty()) {
            throw new IllegalArgumentException("This crushing recipe requires a non empty item output.");
        }
        return new ItemStackToItemStackRecipeBuilder(input, output, BasicCrushingRecipe::new);
    }

    /**
     * Creates an Enriching recipe builder.
     *
     * @param input  Input.
     * @param output Output.
     */
    public static ItemStackToItemStackRecipeBuilder enriching(ItemStackIngredient input, ItemStack output) {
        if (output.isEmpty()) {
            throw new IllegalArgumentException("This enriching recipe requires a non empty item output.");
        }
        return new ItemStackToItemStackRecipeBuilder(input, output, BasicEnrichingRecipe::new);
    }

    /**
     * Creates a Smelting recipe builder.
     *
     * @param input  Input.
     * @param output Output.
     */
    public static ItemStackToItemStackRecipeBuilder smelting(ItemStackIngredient input, ItemStack output) {
        if (output.isEmpty()) {
            throw new IllegalArgumentException("This smelting recipe requires a non empty item output.");
        }
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
        build(recipeOutput, output.getItemHolder());
    }

    @FunctionalInterface
    public interface Factory {

        ItemStackToItemStackRecipe create(ItemStackIngredient input, ItemStack output);
    }
}