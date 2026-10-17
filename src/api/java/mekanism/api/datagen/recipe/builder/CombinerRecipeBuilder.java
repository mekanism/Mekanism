package mekanism.api.datagen.recipe.builder;

import mekanism.api.datagen.recipe.MekanismRecipeBuilder;
import mekanism.api.recipes.CombinerRecipe;
import mekanism.api.recipes.basic.BasicCombinerRecipe;
import mekanism.api.recipes.book.CombiningBookCategory;
import mekanism.api.recipes.book.CombiningBookCategory.CombiningBookInfo;
import mekanism.api.recipes.ingredients.ItemStackIngredient;
import net.minecraft.data.recipes.RecipeBuilder;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.crafting.Recipe;

public class CombinerRecipeBuilder extends MekanismRecipeBuilder<CombiningBookCategory, CombiningBookInfo, CombinerRecipeBuilder> {

    private final ItemStackIngredient mainInput;
    private final ItemStackIngredient extraInput;
    private final ItemStackTemplate output;

    protected CombinerRecipeBuilder(ItemStackIngredient mainInput, ItemStackIngredient extraInput, ItemStackTemplate output) {
        this.mainInput = mainInput;
        this.extraInput = extraInput;
        this.output = output;
        super(CombiningBookInfo::new, CombiningBookCategory.MISC);
    }

    @Override
    public ResourceKey<Recipe<?>> defaultId() {
        return RecipeBuilder.getDefaultRecipeId(output);
    }

    /// Creates a Combining recipe builder.
    ///
    /// @param mainInput  Main Input.
    /// @param extraInput Extra/Secondary Input.
    /// @param output     Output.
    public static CombinerRecipeBuilder combining(ItemStackIngredient mainInput, ItemStackIngredient extraInput, ItemStackTemplate output) {
        return new CombinerRecipeBuilder(mainInput, extraInput, output);
    }

    @Override
    protected CombinerRecipe asRecipe() {
        return new BasicCombinerRecipe(commonInfo(), bookInfo(), mainInput, extraInput, output);
    }
}