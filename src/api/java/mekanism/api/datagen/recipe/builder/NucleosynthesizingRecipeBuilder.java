package mekanism.api.datagen.recipe.builder;

import mekanism.api.datagen.recipe.MekanismRecipeBuilder;
import mekanism.api.recipes.NucleosynthesizingRecipe;
import mekanism.api.recipes.basic.BasicNucleosynthesizingRecipe;
import mekanism.api.recipes.book.NucleosynthesizingBookCategory;
import mekanism.api.recipes.book.NucleosynthesizingBookCategory.NucleosynthesizingBookInfo;
import mekanism.api.recipes.ingredients.ChemicalStackIngredient;
import mekanism.api.recipes.ingredients.ItemStackIngredient;
import net.minecraft.data.recipes.RecipeBuilder;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.crafting.Recipe;

public class NucleosynthesizingRecipeBuilder extends MekanismRecipeBuilder<NucleosynthesizingBookCategory, NucleosynthesizingBookInfo, NucleosynthesizingRecipeBuilder> {

    private final ItemStackIngredient itemInput;
    private final ChemicalStackIngredient chemicalInput;
    private final ItemStackTemplate output;
    private final int duration;
    private final boolean perTickUsage;

    protected NucleosynthesizingRecipeBuilder(ItemStackIngredient itemInput, ChemicalStackIngredient chemicalInput, boolean perTickUsage, ItemStackTemplate output, int duration) {
        this.itemInput = itemInput;
        this.chemicalInput = chemicalInput;
        this.perTickUsage = perTickUsage;
        this.output = output;
        this.duration = duration;
        super(NucleosynthesizingBookInfo::new, NucleosynthesizingBookCategory.MISC);
    }

    @Override
    public ResourceKey<Recipe<?>> defaultId() {
        return RecipeBuilder.getDefaultRecipeId(output);
    }

    /// Creates a Nucleosynthesizing recipe builder.
    ///
    /// @param itemInput     Item Input.
    /// @param chemicalInput Chemical Input.
    /// @param perTickUsage  Should the recipe consume the chemical input each tick it is processing.
    /// @param output        Output.
    /// @param duration      Duration in ticks that it takes the recipe to complete. Must be greater than zero.
    public static NucleosynthesizingRecipeBuilder nucleosynthesizing(ItemStackIngredient itemInput, ChemicalStackIngredient chemicalInput, boolean perTickUsage,
          ItemStackTemplate output, int duration) {
        if (duration <= 0) {
            throw new IllegalArgumentException("This nucleosynthesizing recipe must have a positive duration.");
        }
        return new NucleosynthesizingRecipeBuilder(itemInput, chemicalInput, perTickUsage, output, duration);
    }

    @Override
    protected NucleosynthesizingRecipe asRecipe() {
        return new BasicNucleosynthesizingRecipe(commonInfo(), bookInfo(), itemInput, chemicalInput, perTickUsage, output, duration);
    }
}